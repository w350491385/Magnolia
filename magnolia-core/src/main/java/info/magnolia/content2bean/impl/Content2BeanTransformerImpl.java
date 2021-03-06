/**
 * This file Copyright (c) 2003-2012 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.content2bean.impl;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.SystemContentWrapper;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.PropertyTypeDescriptor;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;
import info.magnolia.content2bean.TypeMapping;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.ComponentProvider;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.RepositoryException;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation using reflection and adder methods.
 * 
 * @author philipp
 * @version $Id$
 */
@Singleton
public class Content2BeanTransformerImpl implements Content2BeanTransformer, Content.ContentFilter {

    private static final Logger log = LoggerFactory.getLogger(Content2BeanTransformerImpl.class);

    private final BeanUtilsBean beanUtilsBean;

    /**
     * @deprecated should not be needed since we pass it around now... or will we ? ... TODO MAGNOLIA-3525
     */
    @Inject
    private TypeMapping typeMapping;

    public Content2BeanTransformerImpl() {
        super();

        // We use non-static BeanUtils conversion, so we can
        // * use our custom ConvertUtilsBean
        // * control converters (convertUtilsBean.register()) - we can register them here, locally, as opposed to a
        // global ConvertUtils.register()
        final EnumAwareConvertUtilsBean convertUtilsBean = new EnumAwareConvertUtilsBean();

        // de-register the converter for Class, we do our own conversion in convertPropertyValue()
        convertUtilsBean.deregister(Class.class);

        this.beanUtilsBean = new BeanUtilsBean(convertUtilsBean, new PropertyUtilsBean());
    }

    @Override
    @Deprecated
    public TypeDescriptor resolveType(TransformationState state) throws ClassNotFoundException {
        throw new UnsupportedOperationException();
    }

    /**
     * Resolves the <code>TypeDescriptor</code> from current transformation state. Resolving happens in the following
     * order:
     * <ul>
     * <li>checks the class property of the current node
     * <li>calls onResolve subclasses should override
     * <li>reflection on the parent bean
     * <li>in case of a collection/map type call getClassForCollectionProperty
     * <li>otherwise use a Map
     * </ul>
     */
    @Override
    public TypeDescriptor resolveType(TypeMapping typeMapping, TransformationState state, ComponentProvider componentProvider) throws ClassNotFoundException {
        TypeDescriptor typeDscr = null;
        Content node = state.getCurrentContent();

        try {
            if (node.hasNodeData("class")) {
                String className = node.getNodeData("class").getString();
                if (StringUtils.isBlank(className)) {
                    throw new ClassNotFoundException("(no value for class property)");
                }
                Class<?> clazz = Classes.getClassFactory().forName(className);
                typeDscr = typeMapping.getTypeDescriptor(clazz);
            }
        } catch (RepositoryException e) {
            // ignore
            log.warn("can't read class property", e);
        }

        if (typeDscr == null && state.getLevel() > 1) {
            TypeDescriptor parentTypeDscr = state.getCurrentType();
            PropertyTypeDescriptor propDscr;

            if (parentTypeDscr.isMap() || parentTypeDscr.isCollection()) {
                if (state.getLevel() > 2) {
                    // this is not necessarily the parent node of the current
                    String mapProperyName = state.peekContent(1).getName();
                    propDscr = state.peekType(1).getPropertyTypeDescriptor(mapProperyName, typeMapping);
                    if (propDscr != null) {
                        typeDscr = propDscr.getCollectionEntryType();
                    }
                }
            } else {
                propDscr = state.getCurrentType().getPropertyTypeDescriptor(node.getName(), typeMapping);
                if (propDscr != null) {
                    typeDscr = propDscr.getType();
                }
            }
        }

        typeDscr = onResolveType(typeMapping, state, typeDscr, componentProvider);

        if (typeDscr != null) {
            // might be that the factory util defines a default implementation for interfaces
            final Class<?> type = typeDscr.getType();
            typeDscr = typeMapping.getTypeDescriptor(componentProvider.getImplementation(type));

            // now that we know the property type we should delegate to the custom transformer if any defined
            Content2BeanTransformer customTransformer = typeDscr.getTransformer();
            if (customTransformer != null && customTransformer != this) {
                TypeDescriptor typeFoundByCustomTransformer = customTransformer.resolveType(typeMapping, state, componentProvider);
                // if no specific type has been provided by the
                // TODO - is this comparison working ?
                if (typeFoundByCustomTransformer != TypeMapping.MAP_TYPE) {
                    // might be that the factory util defines a default implementation for interfaces
                    Class<?> implementation = componentProvider.getImplementation(typeFoundByCustomTransformer.getType());
                    typeDscr = typeMapping.getTypeDescriptor(implementation);
                }
            }
        }

        if (typeDscr == null || typeDscr.needsDefaultMapping()) {
            if (typeDscr == null) {
                log.debug("was not able to resolve type for node [{}] will use a map", node);
            }
            typeDscr = TypeMapping.MAP_TYPE;
        }

        log.debug("{} --> {}", node.getHandle(), typeDscr.getType());

        return typeDscr;
    }

    /**
     * Called once the type should have been resolved. The resolvedType might be null if no type has been resolved.
     * After the call the FactoryUtil and custom transformers are used to get the final type. TODO - check javadoc
     */
    protected TypeDescriptor onResolveType(TypeMapping typeMapping, TransformationState state, TypeDescriptor resolvedType, ComponentProvider componentProvider) {
        return resolvedType;
    }

    /**
     * @deprecated since 4.5, use {@link #onResolveType(info.magnolia.content2bean.TypeMapping, info.magnolia.content2bean.TransformationState, info.magnolia.content2bean.TypeDescriptor, info.magnolia.objectfactory.ComponentProvider)}
     */
    protected TypeDescriptor onResolveType(TransformationState state, TypeDescriptor resolvedType, ComponentProvider componentProvider) {
        return onResolveType(getTypeMapping(), state, resolvedType, componentProvider);
    }

    @Override
    public Collection<Content> getChildren(Content node) {
        return node.getChildren(this);
    }

    /**
     * Process all nodes except MetaData and nodes with names prefixed by "jcr:".
     */
    @Override
    public boolean accept(Content content) {
        return ContentUtil.EXCLUDE_META_DATA_CONTENT_FILTER.accept(content);
    }

    @Override
    public void setProperty(TransformationState state, PropertyTypeDescriptor descriptor, Map<String, Object> values) {
        throw new UnsupportedOperationException();
    }

    /**
     * Do not set class property. In case of a map/collection try to use adder method.
     */
    @Override
    public void setProperty(TypeMapping mapping, TransformationState state, PropertyTypeDescriptor descriptor, Map<String, Object> values) {
        String propertyName = descriptor.getName();
        if (propertyName.equals("class")) {
            return;
        }
        Object value = values.get(propertyName);
        Object bean = state.getCurrentBean();

        if (propertyName.equals("content") && value == null) {
            value = new SystemContentWrapper(state.getCurrentContent());
        } else if (propertyName.equals("name") && value == null) {
            value = state.getCurrentContent().getName();
        } else if (propertyName.equals("className") && value == null) {
            value = values.get("class");
        }

        // do no try to set a bean-property that has no corresponding node-property
        // else if (!values.containsKey(propertyName)) {
        if (value == null) {
            return;
        }

        log.debug("try to set {}.{} with value {}", new Object[] { bean, propertyName, value });

        // if the parent bean is a map, we can't guess the types.
        if (!(bean instanceof Map)) {
            try {
                PropertyTypeDescriptor dscr = mapping.getPropertyTypeDescriptor(bean.getClass(), propertyName);
                if (dscr.getType() != null) {

                    // try to use an adder method for a Collection property of the bean
                    if (dscr.isCollection() || dscr.isMap()) {
                        log.debug("{} is of type collection, map or /array", propertyName);
                        Method method = dscr.getAddMethod();

                        if (method != null) {
                            log.debug("clearing the current content of the collection/map");
                            try {
                                Object col = PropertyUtils.getProperty(bean, propertyName);
                                if (col != null) {
                                    MethodUtils.invokeExactMethod(col, "clear", new Object[] {});
                                }
                            } catch (Exception e) {
                                log.debug("no clear method found on collection {}", propertyName);
                            }

                            Class<?> entryClass = dscr.getCollectionEntryType().getType();

                            log.debug("will add values by using adder method {}", method.getName());
                            for (Iterator<Object> iter = ((Map<Object, Object>) value).keySet().iterator(); iter
                                    .hasNext();) {
                                Object key = iter.next();
                                Object entryValue = ((Map<Object, Object>) value).get(key);
                                entryValue = convertPropertyValue(entryClass, entryValue);
                                if (entryClass.isAssignableFrom(entryValue.getClass())) {
                                    if (dscr.isCollection()) {
                                        log.debug("will add value {}", entryValue);
                                        method.invoke(bean, new Object[] { entryValue });
                                    }
                                    // is a map
                                    else {
                                        log.debug("will add key {} with value {}", key, entryValue);
                                        method.invoke(bean, new Object[] { key, entryValue });
                                    }
                                }
                            }

                            return;
                        }
                        log.debug("no add method found for property {}", propertyName);
                        if (dscr.isCollection()) {
                            log.debug("transform the values to a collection", propertyName);
                            value = ((Map<Object, Object>) value).values();
                        }
                    } else {
                        value = convertPropertyValue(dscr.getType().getType(), value);
                    }
                }
            } catch (Exception e) {
                // do it better
                log.error("Can't set property [{}] to value [{}] in bean [{}] for node {} due to {}",
                        new Object[] { propertyName, value, bean.getClass().getName(),
                                state.getCurrentContent().getHandle(), e.toString() });
                log.debug("stacktrace", e);
            }
        }

        try {
            // This uses the converters registered in beanUtilsBean.convertUtilsBean (see constructor of this class)
            // If a converter is registered, beanutils will convert value.toString(), not the value object as-is.
            // If no converter is registered, then the value Object is set as-is.
            // If convertPropertyValue() already converted this value, you'll probably want to unregister the beanutils
            // converter.
            // some conversions like string to class. Performance of PropertyUtils.setProperty() would be better
            beanUtilsBean.setProperty(bean, propertyName, value);

            // TODO this also does things we probably don't want/need, i.e nested and indexed properties

        } catch (Exception e) {
            // do it better
            log.error("Can't set property [{}] to value [{}] in bean [{}] for node {} due to {}",
                    new Object[] { propertyName, value, bean.getClass().getName(),
                            state.getCurrentContent().getHandle(), e.toString() });
            log.debug("stacktrace", e);
        }

    }

    /**
     * Most of the conversion is done by the BeanUtils. TODO don't use bean utils conversion since it can't be used for
     * the adder methods
     */
    @Override
    public Object convertPropertyValue(Class<?> propertyType, Object value) throws Content2BeanException {
        if (Class.class.equals(propertyType)) {
            try {
                return Classes.getClassFactory().forName(value.toString());
            } catch (ClassNotFoundException e) {
                log.error(e.getMessage());
                throw new Content2BeanException(e);
            }
        }

        if (Locale.class.equals(propertyType)) {
            if (value instanceof String) {
                String localeStr = (String) value;
                if (StringUtils.isNotEmpty(localeStr)) {
                    return LocaleUtils.toLocale(localeStr);
                }
            }
        }

        if (Collection.class.equals(propertyType) && value instanceof Map) {
            // TODO never used ?
            return ((Map) value).values();
        }

        // this is mainly the case when we are flattening node hierarchies
        if (String.class.equals(propertyType) && value instanceof Map && ((Map) value).size() == 1) {
            return ((Map) value).values().iterator().next();
        }

        return value;
    }

    /**
     * Use the factory util to instantiate. This is useful to get default implementation of interfaces
     */
    @Override
    public Object newBeanInstance(TransformationState state, Map properties, ComponentProvider componentProvider) throws Content2BeanException {
        // we try first to use conversion (Map --> primitive type)
        // this is the case when we flattening the hierarchy?
        final Object bean = convertPropertyValue(state.getCurrentType().getType(), properties);
        // were the properties transformed?
        if (bean == properties) {
            try {
                // TODO MAGNOLIA-2569 MAGNOLIA-3525 what is going on here ? (added the following if to avoid permanently
                // requesting LinkedHashMaps to ComponentFactory)
                final Class<?> type = state.getCurrentType().getType();
                if (LinkedHashMap.class.equals(type)) {
                    // TODO - as far as I can tell, "bean" and "properties" are already the same instance of a
                    // LinkedHashMap, so what are we doing in here ?
                    return new LinkedHashMap();
                } else if (Map.class.isAssignableFrom(type)) {
                    // TODO ?
                    log.warn("someone wants another type of map ? " + type);
                }
                return componentProvider.newInstance(type);
            } catch (Throwable e) {
                throw new Content2BeanException(e);
            }
        }
        return bean;
    }

    /**
     * Initializes bean by calling its init method if present.
     */
    @Override
    public void initBean(TransformationState state, Map properties) throws Content2BeanException {
        Object bean = state.getCurrentBean();

        Method init;
        try {
            init = bean.getClass().getMethod("init", new Class[] {});
            try {
                init.invoke(bean); // no parameters
            } catch (Exception e) {
                throw new Content2BeanException("can't call init method", e);
            }
        } catch (SecurityException e) {
            return;
        } catch (NoSuchMethodException e) {
            return;
        }
        log.debug("{} is initialized", bean);
    }

    @Override
    public TransformationState newState() {
        return new TransformationStateImpl();
        // TODO - do we really need different impls for TransformationState ?
        // if so, this was defined in mgnl-beans.properties
        // Components.getComponentProvider().newInstance(TransformationState.class);
    }

    /**
     * Returns the default mapping.
     * 
     * @deprecated since 4.5, do not use.
     */
    @Override
    public TypeMapping getTypeMapping() {
        return typeMapping;// TypeMapping.Factory.getDefaultMapping();
    }

}
