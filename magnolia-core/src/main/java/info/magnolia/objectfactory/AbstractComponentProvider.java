/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.objectfactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.util.DeprecationUtil;
import info.magnolia.objectfactory.configuration.ComponentFactoryConfiguration;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ConfiguredComponentConfiguration;
import info.magnolia.objectfactory.configuration.ImplementationConfiguration;
import info.magnolia.objectfactory.configuration.InstanceConfiguration;

/**
 * Abstract ComponentProvider that supports component factories. Subclasses are responsible for registering components
 * and factories. Both components and factories are created on-demand when registered as classes.
 *
 * @author tmattsson
 */
public abstract class AbstractComponentProvider implements MutableComponentProvider, HierarchicalComponentProvider {

    private static class ComponentDefinition<T> {

        private Class<T> type;
        private T instance;
        private ComponentFactory<T> factory;
        private Class<? extends ComponentFactory<T>> factoryClass;
        private Class<? extends T> implementationType;

        public Class<T> getType() {
            return type;
        }

        public void setType(Class<T> type) {
            this.type = type;
        }

        public T getInstance() {
            return instance;
        }

        public void setInstance(T instance) {
            this.instance = instance;
        }

        public ComponentFactory<T> getFactory() {
            return factory;
        }

        public void setFactory(ComponentFactory<T> factory) {
            this.factory = factory;
        }

        /**
         * The implementation type is unknown for components created by ComponentFactoryUtil.
         */
        public Class<? extends T> getImplementationType() {
            return implementationType;
        }

        public void setImplementationType(Class<? extends T> implementationType) {
            this.implementationType = implementationType;
        }

        public Class<? extends ComponentFactory<T>> getFactoryClass() {
            return factoryClass;
        }

        public void setFactoryClass(Class<? extends ComponentFactory<T>> factoryClass) {
            this.factoryClass = factoryClass;
        }

        public boolean isFactory() {
            return factoryClass != null || factory != null;
        }
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private HierarchicalComponentProvider parent;
    private final Map<Class<?>, ComponentDefinition<?>> definitions = new ConcurrentHashMap<Class<?>, ComponentDefinition<?>>();

    protected AbstractComponentProvider() {
    }

    protected AbstractComponentProvider(HierarchicalComponentProvider parent) {
        this();
        this.parent = parent;
    }

    @Override
    public HierarchicalComponentProvider getParent() {
        return parent;
    }

    @Override
    public <T> T getSingleton(Class<T> type) {
        DeprecationUtil.isDeprecated();
        return getComponent(type);
    }

    @Override
    public synchronized <T> T getComponent(Class<T> type) {
        ComponentDefinition<T> definition = getComponentDefinition(type);
        if (definition == null) {
            if (parent != null && parent.isConfiguredFor(type))
                return parent.getComponent(type);
            // Register the component on-demand
            if (!Classes.isConcrete(type))
                throw new MgnlInstantiationException("No concrete implementation defined for " + type);
            registerImplementation(type, type);
            definition = getComponentDefinition(type);
        }
        T instance = definition.getInstance();
        if (instance == null) {
            log.debug("No instance for {} yet, creating new one.", type);
            instance = newInstance(type);
            definition.setInstance(instance);
            log.debug("New instance for {} created: {}", type, instance);
        }
        return instance;
    }

    @Override
    public synchronized <T> T newInstance(Class<T> type, Object... parameters) {
        if (type == null) {
            log.error("type can't be null", new Throwable());
            return null;
        }
        try {
            ComponentDefinition<T> definition = getComponentDefinition(type);
            if (definition == null) {
                if (parent != null && parent.isConfiguredFor(type))
                    return parent.newInstance(type);
                if (!Classes.isConcrete(type)) {
                    throw new MgnlInstantiationException("No concrete implementation defined for " + type);
                }
                return createInstance(type, parameters);
            }
            if (definition.isFactory()) {
                return this.<T>instantiateFactoryIfNecessary(definition).newInstance();
            }
            return createInstance(definition.getImplementationType(), parameters);
        } catch (Exception e) {
            if (e instanceof MgnlInstantiationException) {
                throw (MgnlInstantiationException) e;
            }
            throw new MgnlInstantiationException("Can't instantiate an implementation of this class [" + type.getName() + "]: " + ExceptionUtils.getMessage(e), e);
        }
    }

    @Override
    public <T> Class<? extends T> getImplementation(Class<T> type) throws ClassNotFoundException {
        ComponentDefinition<T> definition = getComponentDefinition(type);
        if (definition == null)
            if (parent != null && parent.isConfiguredFor(type))
                return parent.getImplementation(type);
            else
                return type;
        if (definition.isFactory())
            return type;
        return definition.getImplementationType();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void configure(ComponentProviderConfiguration configuration) {
        for (ImplementationConfiguration config : configuration.getImplementations()) {
            if (config.getImplementation() == null && config.getType() == null) {
                // do not fail when configuration is bogged, try to get server running anyway
                log.error("Can't instantiate factory, implementation and type are not set: " + config);
                continue;
            }
            registerImplementation(config.getType(), config.getImplementation());
        }
        for (InstanceConfiguration config : configuration.getInstances()) {
            registerInstance(config.getType(), config.getInstance());
        }
        for (ComponentFactoryConfiguration config : configuration.getFactories()) {
            ComponentFactory< ? > factory;
            try {
                factory = ComponentFactoryUtil.createFactory(config.getFactoryClass(), this);
                registerComponentFactory(config.getType(), factory);
            }
            catch (Exception e) {
                throw new RuntimeException("Can't instantiate factory: " + config.getFactoryClass().getName(), e);
            }
        }
        for (ConfiguredComponentConfiguration config : configuration.getConfigured()) {
            if (config.getType() == null) {
                log.error("Type definitions contain invalid configuration. " + config.getWorkspace() + ":" + config.getPath() + (config.isObserved() ? " [observed]": ""));
                continue;
            }
            registerConfiguredComponent(config.getType(), config.getWorkspace(), config.getPath(), config.isObserved());
        }

    };

    @Override
    public synchronized <T> void registerConfiguredComponent(Class<T> type, final String workspace, final String path, boolean observed) {
        final ComponentFactory<T> factory;
        if(observed){
            factory = new LazyObservedComponentFactory<T>(workspace, path, type, this);
        }
        else{
            factory = new ConfiguredComponentFactory<T>(path, workspace, this);
        }
        registerComponentFactory(type, factory);

    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T> void registerImplementation(Class<T> type, Class<? extends T> implementationType) {

//      Allow changing implementations TODO this should work for all register- methods.
//        if (definitions.containsKey(type))
//            throw new MgnlInstantiationException("Component already registered for type " + type.getName());
        if (implementationType == null) {
            throw new MgnlInstantiationException("ImplementationConfiguration type is not set a for type " + type);
        }
        if (!Classes.isConcrete(implementationType)) {
            throw new MgnlInstantiationException("ImplementationConfiguration type is not a concrete class for type " + type);
        }
        if (ComponentFactory.class.isAssignableFrom(implementationType)) {
            ComponentDefinition<T> definition = new ComponentDefinition<T>();
            definition.setType(type);
            definition.setFactoryClass((Class<? extends ComponentFactory<T>>)implementationType);
            definitions.put(type, definition);
        } else {
            ComponentDefinition<T> definition = new ComponentDefinition<T>();
            definition.setType(type);
            definition.setImplementationType(implementationType);
            definitions.put(type, definition);
        }
    }

    @Override
    public synchronized <T> void registerComponentFactory(Class<T> type, ComponentFactory<T> componentFactory) {
        if (type == null) {
            throw new NullPointerException("Null type is not allowed. Please check your type definition and classes on the classpath.");
        }
        if (definitions.containsKey(type))
            throw new MgnlInstantiationException("Component already registered for type " + type.getName());
        ComponentDefinition<T> definition = new ComponentDefinition<T>();
        definition.setType(type);
        definition.setFactory(componentFactory);
        definitions.put(type, definition);
    }

    @Override
    public synchronized <T> void registerInstance(Class<T> type, T instance) {
        if (definitions.containsKey(type))
            throw new MgnlInstantiationException("Component already registered for type " + type.getName());
        ComponentDefinition<T> definition = new ComponentDefinition<T>();
        definition.setType(type);
        definition.setInstance(instance);
        definitions.put(type, definition);
    }

    @SuppressWarnings("unchecked")
    private <T> ComponentDefinition<T> getComponentDefinition(Class<T> type) {
        return (ComponentDefinition<T>)definitions.get(type);
    }

    private <T> ComponentFactory<T> instantiateFactoryIfNecessary(ComponentDefinition<T> definition) {
        if (definition.getFactoryClass() != null && definition.getFactory() == null) {
            definition.setFactory(createInstance(definition.getFactoryClass()));
        }
        return definition.getFactory();
    }

    protected <T> T createInstance(Class<T> implementationType, Object... parameters) {
        return Classes.getClassFactory().newInstance(implementationType, parameters);
    }

    protected synchronized void removeComponent(Class<?> type) {
        definitions.remove(type);
    }

    protected synchronized void clear() {
        definitions.clear();
    }

    @Override
    public boolean isConfiguredFor(Class<?> type) {
        ComponentDefinition<?> definition = getComponentDefinition(type);
        if (definition != null)
            return true;
        if (parent != null)
            return parent.isConfiguredFor(type);
        return false;
    }
}
