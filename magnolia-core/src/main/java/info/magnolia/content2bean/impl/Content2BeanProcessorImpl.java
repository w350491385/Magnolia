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
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ExtendingContentWrapper;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanProcessor;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.PropertyTypeDescriptor;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.RepositoryException;

import info.magnolia.content2bean.TypeMapping;
import info.magnolia.objectfactory.ComponentProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the logic for traversing the hierarchy and do the calls to the transformer.
 * @author philipp
 * @version $Id$
 *
 */
@Singleton
public class Content2BeanProcessorImpl implements Content2BeanProcessor {
    private static final Logger log = LoggerFactory.getLogger(Content2BeanProcessorImpl.class);

    private final TypeMapping typeMapping;

    private boolean forceCreation = true;

    @Inject
    public Content2BeanProcessorImpl(TypeMapping typeMapping) {
        this.typeMapping = typeMapping;
    }

    @Override
    public Object toBean(Content node, boolean recursive, final Content2BeanTransformer transformer, ComponentProvider componentProvider) throws Content2BeanException{
        return toBean(new ExtendingContentWrapper(node), recursive, transformer, transformer.newState(), componentProvider);
    }

    protected Object toBean(Content node, boolean recursive, Content2BeanTransformer transformer, TransformationState state, ComponentProvider componentProvider) throws Content2BeanException{

        state.pushContent(node);

        TypeDescriptor type = null;
        try {
            type = transformer.resolveType(typeMapping, state, componentProvider);
        }
        catch (Throwable e) {
            if(isForceCreation()){
                log.warn("can't resolve class for node " +  node.getHandle(), e);
            }
            else{
                throw new Content2BeanException("can't resolve class for node " +  node.getHandle(), e);
            }
        }

        Object bean = null;
        if(type != null){
            state.pushType(type);

            transformer = resolveTransformer(type, transformer);

            Map<String, Object> values = toMap(node, recursive, transformer, state, componentProvider);

            try {
                bean = transformer.newBeanInstance(state, values, componentProvider);
            }
            catch (Throwable e) {
                if(isForceCreation()){
                    log.warn("Can't instantiate bean for " +  node.getHandle(), e);
                }
                else{
                    throw new Content2BeanException("Can't instantiate bean for " +  node.getHandle(), e);
                }
            }

            if(bean != null){
                state.pushBean(bean);

                setProperties(values, transformer, state);

                transformer.initBean(state, values);

                bean = state.getCurrentBean();

                state.popBean();
            }
            else{
                if(forceCreation){
                    log.warn("can't instantiate bean of type " + type.getType().getName() + " for node " + node.getHandle());
                }
                else{
                    throw new Content2BeanException("can't instantiate bean of type " + type.getType().getName());
                }
            }

            state.popType();
        }
        state.popContent();

        return bean;
    }

    @Override
    public Object setProperties(final Object bean, Content node, boolean recursive, Content2BeanTransformer transformer, ComponentProvider componentProvider) throws Content2BeanException {
        // enable extending feature
        node = new ExtendingContentWrapper(node);

        TransformationState state = transformer.newState();
        state.pushBean(bean);
        state.pushContent(node);

        // TODO -  MAGNOLIA-3525 TypeDescriptor type = transformer.getTypeMapping().getTypeDescriptor(bean.getClass());
        TypeDescriptor type = typeMapping.getTypeDescriptor(bean.getClass());

        state.pushType(type);

        transformer = resolveTransformer(type, transformer);

        Map<String, Object> values = toMap(node, recursive, transformer, state, componentProvider);

        setProperties(values, transformer, state);

        transformer.initBean(state, values);

        state.popBean();
        state.popType();
        state.popContent();

        return bean;
    }

    /**
     * Transforms the children of provided content into a map.
     */
    protected Map<String, Object> toMap(Content node, boolean recursive, Content2BeanTransformer transformer, TransformationState state, ComponentProvider componentProvider) throws Content2BeanException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (NodeData nd : node.getNodeDataCollection()) {
            Object val = NodeDataUtil.getValueObject(nd);
            if (val != null) {
                map.put(nd.getName(), val);
            }
        }

        if(recursive){
            final Collection<Content> children = transformer.getChildren(node);
            for (Content childNode : children) {
                // in case the the class can not get resolved we can use now
                // the parent bean to resolve the class

                Object childBean = toBean(childNode, true, transformer, state, componentProvider);
                // can be null if forceCreation is true
                if(childBean != null){
                    String name = childNode.getName();
                    try {
                        if(childNode.getIndex() > 1){
                            name += childNode.getIndex();
                        }
                    }
                    catch (RepositoryException e) {
                        log.error("can't read index of the node [" + childNode + "]", e);
                    }
                    map.put(name, childBean);
                }
            }
        }

        return map;
    }

    /**
     * Populates the properties of the bean with values from the map.
     * TODO in case the bean is a map / collection the transfomer.setProperty() method should be called too
     * TODO if the bean has not a certain property but a value is present, transformer.setProperty() should be called with a fake property descriptor
     */
    protected void setProperties(Map<String, Object> values, final Content2BeanTransformer transformer, TransformationState state) throws Content2BeanException {
        Object bean = state.getCurrentBean();
        log.debug("will populate bean {} with the values {}", bean.getClass().getName(), values);

        if(bean instanceof Map){
            ((Map<String, Object>)bean).putAll(values);
        }

        if(bean instanceof Collection){
            ((Collection<Object>)bean).addAll(values.values());
        }

        else{
            // TypeDescriptor beanTypeDescriptor = transformer.getTypeMapping().getTypeDescriptor(bean.getClass());
            TypeDescriptor beanTypeDescriptor = typeMapping.getTypeDescriptor(bean.getClass());
            final Collection<PropertyTypeDescriptor> dscrs = beanTypeDescriptor.getPropertyDescriptors(typeMapping).values();

            for (PropertyTypeDescriptor descriptor : dscrs) {
                transformer.setProperty(typeMapping, state, descriptor, values);
            }
        }
    }

    protected Content2BeanTransformer resolveTransformer(TypeDescriptor type, Content2BeanTransformer transformer) {
        Content2BeanTransformer customTransformer = type.getTransformer();
        if(customTransformer != null){
            transformer = customTransformer;
        }
        return transformer;
    }

    public boolean isForceCreation() {
        return this.forceCreation;
    }

    /**
     * @deprecated only used in tests
     */
    public void setForceCreation(boolean handleExceptions) {
        this.forceCreation = handleExceptions;
    }

}
