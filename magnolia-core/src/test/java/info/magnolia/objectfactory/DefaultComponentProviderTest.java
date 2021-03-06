/**
 * This file Copyright (c) 2010-2012 Magnolia International
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.TransformationState;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;

import java.io.IOException;
import java.util.Properties;

import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class DefaultComponentProviderTest {
    @Before
    public void setUp() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        SystemProperty.clear();
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        SystemProperty.clear();
    }

    @Test
    public void testReturnsGivenConcreteClassIfNoneConfigured() {
        final DefaultComponentProvider componentProvider = new DefaultComponentProvider(new Properties());
        Object obj = componentProvider.getSingleton(TestImplementation.class);
        assertTrue(obj instanceof TestImplementation);
    }

    @Test
    public void testBlowsIfGivenInterfaceAndNoImplementationIsConfigured() {
        final DefaultComponentProvider componentProvider = new DefaultComponentProvider(new Properties());
        try {
            componentProvider.getSingleton(TestInterface.class);
            fail("should have thrown a MgnlInstantiationException");
        } catch (MgnlInstantiationException e) {
            assertEquals("No concrete implementation defined for interface info.magnolia.objectfactory.DefaultComponentProviderTest$TestInterface", e.getMessage());
        }
    }

    @Test
    public void testReturnsConfiguredImplementation() {
        final Properties p = new Properties();
        p.setProperty("info.magnolia.objectfactory.DefaultComponentProviderTest$TestInterface", "info.magnolia.objectfactory.DefaultComponentProviderTest$TestImplementation");
        final DefaultComponentProvider componentProvider = new DefaultComponentProvider(p);
        Object obj = componentProvider.getSingleton(TestInterface.class);
        assertTrue(obj instanceof TestImplementation);
    }

    @Test
    public void testGetSingletonReturnsSameInstance() {
        final ComponentProvider cp = new DefaultComponentProvider(new Properties());

        assertEquals(cp.getSingleton(TestImplementation.class), cp.getSingleton(TestImplementation.class));
        assertSame(cp.getSingleton(TestImplementation.class), cp.getSingleton(TestImplementation.class));
    }

    @Test
    public void testNewInstanceReallyReturnsNewInstance() {
        final ComponentProvider cp = new DefaultComponentProvider(new Properties());
        assertNotSame(cp.newInstance(TestImplementation.class), cp.newInstance(TestImplementation.class));
    }

    @Test
    public void testUsesComponentFactoryIfSuchFactoryIsConfigured() {
        final Properties p = new Properties();
        p.setProperty("info.magnolia.objectfactory.DefaultComponentProviderTest$TestInterface", "info.magnolia.objectfactory.DefaultComponentProviderTest$TestInstanceFactory");
        final DefaultComponentProvider componentProvider = new DefaultComponentProvider(p);

        final TestInterface obj = componentProvider.getSingleton(TestInterface.class);
        // DefaultComponentProviderTest$TestInstanceFactory will instantiate a TestOtherImplementation
        assertTrue(obj instanceof TestOtherImplementation);
        // double-check we still get the same instance, since we're calling getSingleton()
        assertSame(obj, componentProvider.getSingleton(TestInterface.class));
    }


    /**
     * TODO - these tests uses ComponentsTestUtil and {@link Components#getSingleton(Class)}: since
     * C2B and ObserverComponentFactory both use {@link Components} to retrieve their ... components.
     * (sort of a cyclic-dependency there)
     */
    @Test
    public void testSingletonDefinedInRepositoryDefaultToConfigWorkspace() throws RepositoryException, IOException {
        setDefaultImplementationsAndInitMockRepository("/test", RepositoryConstants.CONFIG,
                "test.class=" + TestImplementation.class.getName()
        );

        Object obj = Components.getSingleton(TestInterface.class);
        assertNotNull(obj);
        assertTrue(obj instanceof TestImplementation);
    }


    /**
     * TODO - these tests uses ComponentsTestUtil and {@link Components#getSingleton(Class)}: since
     * C2B and ObserverComponentFactory both use {@link Components} to retrieve their ... components.
     * (sort of a cyclic-dependency there)
     */
    @Test
    public void testSingletonDefinedInRepositoryUsesGivenRepoName() throws RepositoryException, IOException {
        setDefaultImplementationsAndInitMockRepository("dummy:/test", "dummy",
                "test.class=" + TestImplementation.class.getName()
        );
        Object obj = Components.getSingleton(TestInterface.class);
        assertNotNull(obj);
        assertTrue(obj instanceof TestImplementation);
    }

    /**
     * TODO - these tests uses ComponentsTestUtil and {@link Components#getSingleton(Class)}: since
     * C2B and ObserverComponentFactory both use {@link Components} to retrieve their ... components.
     * (sort of a cyclic-dependency there)
     */
    @Test
    public void testProxiesReturnedByObserverComponentFactoryCanBeCastToTheirSubclass() throws Exception {
        setDefaultImplementationsAndInitMockRepository("config:/test", "config",
                "test.class=" + TestOtherImplementation.class.getName()
        );
        TestInterface obj = Components.getSingleton(TestInterface.class);
        assertNotNull(obj);
        // so, I know my project is configured to use a subclass of TestInterface, I can cast away if i want (typically, a module replacing a default implementation)
        assertTrue(obj instanceof TestOtherImplementation);
        assertEquals("bar", ((TestOtherImplementation) obj).getFoo());
    }

    private void setDefaultImplementationsAndInitMockRepository(String componentPropertyValue, String expectedRepoName, String repoContent) throws RepositoryException, IOException {
        // configuration value for the interface, i.e the value set in magnolia.properties, for instance
        ComponentsTestUtil.setImplementation(TestInterface.class, componentPropertyValue);

        // default impl's for content2bean TODO - refactor PropertiesInitializer
        final TypeMappingImpl typeMapping = new TypeMappingImpl();
        ComponentsTestUtil.setInstance(TypeMapping.class, typeMapping);
        ComponentsTestUtil.setImplementation(Node2BeanProcessor.class, Node2BeanProcessorImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanTransformer.class, "info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl");
        ComponentsTestUtil.setImplementation(TransformationState.class, "info.magnolia.jcr.node2bean.impl.TransformationStateImpl");

        MockUtil.initMockContext();
        MockUtil.createAndSetHierarchyManager(expectedRepoName, repoContent);
    }

    public static interface TestInterface {

    }

    public static class TestImplementation implements TestInterface {

    }

    public static class TestOtherImplementation extends TestImplementation {
        public String getFoo() {
            return "bar";
        }
    }

    public static final class TestInstanceFactory implements ComponentFactory<TestInterface> {
        @Override
        public TestInterface newInstance() {
            return new TestOtherImplementation();
        }
    }
}
