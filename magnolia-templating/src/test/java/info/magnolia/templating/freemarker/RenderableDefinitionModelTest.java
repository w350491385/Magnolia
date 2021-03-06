/**
 * This file Copyright (c) 2009-2012 Magnolia International
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
package info.magnolia.templating.freemarker;

import static org.junit.Assert.assertEquals;

import info.magnolia.freemarker.FreemarkerConfig;
import info.magnolia.freemarker.models.MagnoliaObjectWrapper;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import freemarker.ext.util.ModelFactory;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * These tests are a little larger than unit-level, since they actually test this model in
 * the context of Freemarker rendering.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RenderableDefinitionModelTest {

    @Test
    public void testRenderableDefinitionParametersAreAvailableAsTopLevelProperties() throws IOException, TemplateException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("foo", "bar");

        final ConfiguredTemplateDefinition def = new ConfiguredTemplateDefinition();
        def.setName("myname");
        def.setParameters(parameters);

        Map<String, ConfiguredTemplateDefinition> root = new HashMap<String, ConfiguredTemplateDefinition>();
        root.put("def", def);

        doTestFreemarkerRendering(":myname:bar:", ":${def.name}:${def.foo}:", root);
    }

    @Test
    public void testRenderableDefinitionPropertiesHaveHigherPriorityThanParameters() throws IOException, TemplateException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("foo", "bar");
        parameters.put("name", "should not appear");

        final ConfiguredTemplateDefinition def = new ConfiguredTemplateDefinition();
        def.setName("real name");
        def.setParameters(parameters);

        final Map<String, ConfiguredTemplateDefinition> root = new HashMap<String, ConfiguredTemplateDefinition>();
        root.put("def", def);

        doTestFreemarkerRendering(":real name:bar:", ":${def.name}:${def.foo}:", root);
    }

    @Test
    public void testRenderableDefinitionPropertiesAreStillAvailableIfReallyNeeded() throws IOException, TemplateException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("foo", "bar");
        parameters.put("name", "other name");

        final ConfiguredTemplateDefinition def = new ConfiguredTemplateDefinition();
        def.setName("real name");
        def.setParameters(parameters);

        final Map<String, ConfiguredTemplateDefinition> root = new HashMap<String, ConfiguredTemplateDefinition>();
        root.put("def", def);

        doTestFreemarkerRendering(":real name:other name:", ":${def.name}:${def.parameters.name}:", root);
    }

    // TODO -- this could be moved elsewhere for reuse, if we let the model factory be a parameter of the test method for instance
    public static void doTestFreemarkerRendering(String expectedResult, String testTemplate, Map<String, ConfiguredTemplateDefinition> root) throws TemplateException, IOException {
        final MagnoliaObjectWrapper objectWrapper = new MagnoliaObjectWrapper(new FreemarkerConfig()) {
            @Override
            protected ModelFactory getModelFactory(Class clazz) {
                if (TemplateDefinition.class.isAssignableFrom(clazz)) {
                    return new RenderableDefinitionModel.Factory();
                }
                return super.getModelFactory(clazz);
            }
        };

        final Template template1 = new Template("test-template", new StringReader(testTemplate), new Configuration());
        final StringWriter out1 = new StringWriter();
        template1.process(root, out1, objectWrapper);
        assertEquals(expectedResult, out1.toString());

    }

}
