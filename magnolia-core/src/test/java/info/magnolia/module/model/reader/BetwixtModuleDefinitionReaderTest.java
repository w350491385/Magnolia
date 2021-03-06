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
package info.magnolia.module.model.reader;

import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.model.ComponentDefinition;
import info.magnolia.module.model.ComponentsDefinition;
import info.magnolia.module.model.ConfigurerDefinition;
import info.magnolia.module.model.DependencyDefinition;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.RepositoryDefinition;
import info.magnolia.module.model.ServletDefinition;
import info.magnolia.module.model.TypeMappingDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.VersionRange;
import info.magnolia.module.model.VersionTest;
import info.magnolia.test.ComponentsTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class BetwixtModuleDefinitionReaderTest {

    @Before
    public void setUp() throws Exception {
        // shunt log4j
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
        // shunt the chatty Digester log explicitely - possibly because of the commons-logging wrapping, shunting the root logger isn't enough
        org.apache.log4j.Logger.getLogger(org.apache.commons.digester.Digester.class).setLevel(org.apache.log4j.Level.OFF);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testDisplayNameCanBeWrittenWithDashEventhoughThisIsDeprecated() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<module>\n" +
        "  <name>the name</name>\n" +
        "  <display-name>The Display Name</display-name>" +
        "  <class>foo</class>\n" +
        "  <versionHandler>java.lang.String</versionHandler>\n" +
        "  <version>1.0</version>\n" +
        "</module>";
        ModuleDefinition mod = new BetwixtModuleDefinitionReader().read(new StringReader(xml));
        assertEquals("The Display Name", mod.getDisplayName());
    }

    @Test
    public void testDisplayNameShouldBeWrittenWithCapitalN() throws Exception {
        String xml = "<module>\n" +
        "  <name>the name</name>\n" +
        "  <displayName>The Display Name</displayName>\n" +
        "  <class>foo</class>\n" +
        "  <versionHandler>java.lang.String</versionHandler>\n" +
        "  <version>1.0</version>\n" +
        "</module>";
        ModuleDefinition mod = new BetwixtModuleDefinitionReader().read(new StringReader(xml));
        assertEquals("The Display Name", mod.getDisplayName());
    }

    @Test
    public void testClassIsResolvedToClassNameAsAString() throws Exception {
        String xml = "<module>\n" +
        "  <name>the name</name>\n" +
        "  <class>java.lang.Integer</class>\n" +
        "  <versionHandler>java.lang.String</versionHandler>\n" +
        "  <version>1.0</version>\n" +
        "</module>";
        ModuleDefinition mod = new BetwixtModuleDefinitionReader().read(new StringReader(xml));
        assertEquals("java.lang.Integer", mod.getClassName());
    }

    @Test
    public void testVersionHandlerIsResolvedToAClass() throws Exception {
        String xml = "<module>\n" +
        "  <name>the name</name>\n" +
        "  <class>java.lang.Integer</class>\n" +
        "  <versionHandler>java.lang.String</versionHandler>" +
        "  <version>1.0</version>\n" +
        "</module>";
        ModuleDefinition mod = new BetwixtModuleDefinitionReader().read(new StringReader(xml));
        assertEquals(String.class, mod.getVersionHandler());
    }

    @Test
    public void testModuleVersionIsProperlyRead() throws ModuleManagementException {
        String xml = "<module>\n" +
        "  <name>the name</name>\n" +
        "  <class>foo</class>\n" +
        "  <versionHandler>java.lang.String</versionHandler>\n" +
        "  <version>1.2.3</version>\n" +
        "</module>";
        final ModuleDefinition def = new BetwixtModuleDefinitionReader().read(new StringReader(xml));

        final Version version = def.getVersion();
        assertNotNull(version);
        assertEquals("1.2.3", version.toString());
        VersionTest.assertVersion(1, 2, 3, null, version);
    }

    @Test
    public void testDependenciesVersionAreProperlyRead() throws ModuleManagementException {
        //org.apache.log4j.Logger.getLogger("org.apache").setLevel(org.apache.log4j.Level.DEBUG);
        String xml = "<module>\n" +
        "  <name>myName</name>\n" +
        "  <class>foo</class>\n" +
        "  <versionHandler>java.lang.String</versionHandler>\n" +
        "  <version>1.2.3</version>\n" +
        "  <dependencies>\n" +
        "    <dependency>\n" +
        "      <name>foo</name>\n" +
        "      <version>2.3.4/*</version>\n" +
        "      <optional>true</optional>\n" +
        "    </dependency>\n" +
        "    <dependency>\n" +
        "      <name>bar</name>\n" +
        "      <version>5.6.7/8.9.0</version>\n" +
        "    </dependency>\n" +
        "  </dependencies>\n" +
        "</module>";
        final ModuleDefinition def = new BetwixtModuleDefinitionReader().read(new StringReader(xml));
        final Collection deps = def.getDependencies();
        assertEquals(2, deps.size());
        final Iterator it = deps.iterator();

        final DependencyDefinition dep1 = (DependencyDefinition) it.next();
        final DependencyDefinition dep2 = (DependencyDefinition) it.next();
        assertEquals("foo", dep1.getName());
        assertEquals(true, dep1.isOptional());
        assertEquals("2.3.4/*", dep1.getVersion());
        final VersionRange dep1versionRange = dep1.getVersionRange();
        assertNotNull(dep1versionRange);
        assertEquals("2.3.4/*", dep1versionRange.toString());
        VersionTest.assertVersion(2, 3, 4, null, dep1versionRange.getFrom());
        assertEquals(Version.UNDEFINED_TO, dep1versionRange.getTo());

        assertEquals("bar", dep2.getName());
        assertEquals(false, dep2.isOptional());
        assertEquals("5.6.7/8.9.0", dep2.getVersion());
        final VersionRange dep2versionRange = dep2.getVersionRange();
        assertNotNull(dep2versionRange);
        assertEquals("5.6.7/8.9.0", dep2versionRange.toString());
        VersionTest.assertVersion(5, 6, 7, null, dep2versionRange.getFrom());
        VersionTest.assertVersion(8, 9, 0, null, dep2versionRange.getTo());
    }

    @Test
    public void testDependenciesCanBeSpecifiedWithoutVersion() throws ModuleManagementException {
        String xml = "<module>\n" +
        "  <name>myName</name>\n" +
        "  <version>1.2.3</version>\n" +
        "  <dependencies>\n" +
        "    <dependency>\n" +
        "      <name>foo</name>\n" +
        "      <optional>true</optional>\n" +
        "    </dependency>\n" +
        "    <dependency>\n" +
        "      <name>bar</name>\n" +
        "    </dependency>\n" +
        "  </dependencies>\n" +
        "</module>";

        final ModuleDefinition def = new BetwixtModuleDefinitionReader().read(new StringReader(xml));
        final List<DependencyDefinition> deps = new ArrayList<DependencyDefinition>(def.getDependencies());
        assertEquals(2, deps.size());
        final DependencyDefinition dep1 = deps.get(0);
        final DependencyDefinition dep2 = deps.get(1);
        assertEquals("foo", dep1.getName());
        assertEquals(true, dep1.isOptional());
        assertEquals(null, dep1.getVersion());
        final VersionRange dep1versionRange = dep1.getVersionRange();
        assertEquals(Version.UNDEFINED_FROM, dep1versionRange.getFrom());
        assertEquals(Version.UNDEFINED_TO, dep1versionRange.getTo());

        assertEquals("bar", dep2.getName());
        assertEquals(false, dep2.isOptional());
        assertEquals(null, dep2.getVersion());
        final VersionRange dep2versionRange = dep2.getVersionRange();
        assertEquals(Version.UNDEFINED_FROM, dep2versionRange.getFrom());
        assertEquals(Version.UNDEFINED_TO, dep2versionRange.getTo());
    }

    @Test
    public void testInvalidXmlIsCheckedAgainstDTD() {
        String xmlWithVersionElementMisplaced = "<module>\n" +
        "  <version>2.3.4</version>\n" +
        "  <name>the name</name>\n" +
        "</module>";
        try {
            new BetwixtModuleDefinitionReader().read(new StringReader(xmlWithVersionElementMisplaced));
            fail("should have failed");
        } catch (ModuleManagementException e) {
            assertEquals("Invalid module definition file, error at line 6 column 10: The content of element type \"module\" must match \"(name,(displayName|display-name)?,description?,class?,versionHandler?,version,properties?,components*,dependencies?,servlets?,repositories?)\".", e.getMessage());
        }
    }

    @Test
    public void testGivenDtdIsIgnoredAndCheckedAgainstOurs() {
        String xmlWithWrongDtd = "<!DOCTYPE log4j:configuration SYSTEM \"log4j.dtd\">\n" +
        "<module>\n" +
        "  <version>2.3.4</version>\n" +
        "  <name>the name</name>\n" +
        "</module>";
        try {
            new BetwixtModuleDefinitionReader().read(new StringReader(xmlWithWrongDtd));
            fail("should have failed");
        } catch (ModuleManagementException e) {
            assertEquals("Invalid module definition file, error at line 6 column 10: The content of element type \"module\" must match \"(name,(displayName|display-name)?,description?,class?,versionHandler?,version,properties?,components*,dependencies?,servlets?,repositories?)\".", e.getMessage());
        }
    }

    @Test
    public void testReadCompleteDescriptorAndCheckAllPropertiesDamnYouBetwixt() throws Exception {
        final BetwixtModuleDefinitionReader reader = new BetwixtModuleDefinitionReader();
        final ModuleDefinition m = reader.readFromResource("/info/magnolia/module/model/reader/dummy-module.xml");
        assertNotNull(m);
        assertEquals("dummy", m.getName());
        assertEquals("dummy module", m.getDisplayName());
        assertEquals("a dummy module descriptor for tests", m.getDescription());
        // this dummy module descriptor uses random classes that we know are there when running the test
        assertEquals(BetwixtModuleDefinitionReaderTest.class.getName(), m.getClassName());
        assertEquals(DependencyCheckerImplTest.class, m.getVersionHandler());
        assertEquals("7.8.9", m.getVersion().toString());
        assertNotNull(m.getProperties());
        assertEquals(2, m.getProperties().size());
        assertEquals("bar", m.getProperty("foo"));
        assertEquals("lolo", m.getProperty("lala"));

        assertNotNull(m.getDependencies());
        assertEquals(2, m.getDependencies().size());

        assertNotNull(m.getServlets());
        assertEquals(2, m.getServlets().size());
        final ServletDefinition servlet1 = (ServletDefinition) fromList(m.getServlets(), 0);
        assertNotNull(servlet1);
        assertEquals("AServlet", servlet1.getName());
        assertEquals(DependencyLevelComparatorTest.class.getName(), servlet1.getClassName());
        assertEquals("lalala", servlet1.getComment());
        assertEquals(2, servlet1.getMappings().size());
        assertEquals("/foo/*", fromList(servlet1.getMappings(), 0));
        assertEquals("/bar", fromList(servlet1.getMappings(), 1));
        final ServletDefinition servlet2 = (ServletDefinition) fromList(m.getServlets(), 1);
        assertNotNull(servlet2);
        assertEquals("OtherServlet", servlet2.getName());
        assertEquals(info.magnolia.module.model.VersionComparatorTest.class.getName(), servlet2.getClassName());
        assertEquals("blahblah", servlet2.getComment());
        assertEquals(1, servlet2.getMappings().size());
        assertEquals("/blah/*", fromList(servlet2.getMappings(), 0));

        assertNotNull(m.getRepositories());
        assertEquals(2, m.getRepositories().size());
        final RepositoryDefinition repo1 = (RepositoryDefinition) fromList(m.getRepositories(), 0);
        assertEquals("some-repo", repo1.getName());
        assertEquals(2, repo1.getWorkspaces().size());
        assertEquals("workspace-a", fromList(repo1.getWorkspaces(), 0));
        assertEquals("workspace-b", fromList(repo1.getWorkspaces(), 1));
        assertEquals(null, repo1.getNodeTypeFile());
        final RepositoryDefinition repo2 = (RepositoryDefinition) fromList(m.getRepositories(), 1);
        assertEquals("other-repo", repo2.getName());
        assertEquals(1, repo2.getWorkspaces().size());
        assertEquals("bleh", fromList(repo2.getWorkspaces(), 0));
        assertEquals("/chalala/testNodeTypes.xml", repo2.getNodeTypeFile());
    }

    @Test
    public void testSelf() {
        // make sure these resources are available - ide might not have compiled them
        assertNotNull(BetwixtModuleDefinitionReaderTest.class.getResourceAsStream("/info/magnolia/module/model/ModuleDefinition.betwixt"));
        assertNotNull(BetwixtModuleDefinitionReaderTest.class.getResourceAsStream("/info/magnolia/module/model/ServletDefinition.betwixt"));
    }

    @Test
    public void testReadModuleDescriptorWithComponents() throws ModuleManagementException {
        final BetwixtModuleDefinitionReader reader = new BetwixtModuleDefinitionReader();
        final ModuleDefinition m = reader.readFromResource("/info/magnolia/module/model/reader/dummy-module-with-components.xml");
        assertNotNull(m);
        assertEquals(1, m.getComponents().size());
        ComponentsDefinition components = m.getComponents().iterator().next();
        assertEquals("platform", components.getId());

        assertEquals(2, components.getConfigurers().size());
        Iterator<ConfigurerDefinition> iterator = components.getConfigurers().iterator();
        ConfigurerDefinition configurer = iterator.next();
        assertEquals("com.acme.FirstTestConfigurer", configurer.getClassName());
        configurer = iterator.next();
        assertEquals("com.acme.SecondTestConfigurer", configurer.getClassName());

        assertEquals(1, components.getComponents().size());
        ComponentDefinition componentDefinition = components.getComponents().iterator().next();
        assertEquals("com.acme.ComponentKey", componentDefinition.getType());
        assertEquals("session", componentDefinition.getScope());
        assertEquals("impl", componentDefinition.getImplementation());
        assertEquals("provider", componentDefinition.getProvider());
        assertEquals("true", componentDefinition.getObserved());
        assertEquals("config", componentDefinition.getWorkspace());
        assertEquals("/modules/acme/foo", componentDefinition.getPath());

        assertEquals(1, components.getTypeMappings().size());
        TypeMappingDefinition typeMapping = components.getTypeMappings().iterator().next();
        assertEquals("t-m-key", typeMapping.getType());
        assertEquals("t-m-impl", typeMapping.getImplementation());
    }

    private Object fromList(Collection coll, int index) {
        return ((List)coll).get(index);
    }

}
