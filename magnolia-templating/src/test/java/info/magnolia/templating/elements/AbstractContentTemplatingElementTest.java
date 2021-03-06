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
package info.magnolia.templating.elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.junit.Test;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.test.mock.MockWebContext;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @version $Id$
 */
public class AbstractContentTemplatingElementTest extends AbstractElementTestCase {
    @Test
    public void testGetTargetContent() throws Exception {
        final RenderingContext aggregationState = mock(RenderingContext.class);
        when(aggregationState.getMainContent()).thenReturn(getSession().getNode("/foo/bar"));

        final AbstractContentTemplatingElement compo = new DummyComponent(null, aggregationState);
        final Node expectedNode = getSession().getNode("/foo/bar/paragraphs/1");

        when(aggregationState.getCurrentContent()).thenReturn(expectedNode);

        Node node = compo.getPassedContent();
        assertNull(node);

        compo.setWorkspace("workspace");

        try {
            compo.getPassedContent();
            fail("Expceted IllegalArguementException as workspace is set but not uuid or path");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testSetAttributesInWebContext() throws Exception {
        //GIVEN
        final RenderingContext aggregationState = mock(RenderingContext.class);

        final WebContext ctx = new MockWebContext();
        ctx.setAttribute("foo", "foo value", WebContext.LOCAL_SCOPE);
        ctx.setAttribute("bar", 1, WebContext.LOCAL_SCOPE);
        ctx.setAttribute("baz", true, WebContext.LOCAL_SCOPE);
        MgnlContext.setInstance(ctx);

        assertEquals(3, ctx.getAttributes().size());

        final AbstractContentTemplatingElement compo = new DummyComponent(null, aggregationState);
        Map<String,Object> attributes = new HashMap<String, Object>();
        attributes.put("foo", "new foo");
        attributes.put("qux", "blah");

        //WHEN
        compo.setAttributesInWebContext(attributes, WebContext.LOCAL_SCOPE);

        //THEN
        assertEquals(4, ctx.getAttributes().size());
        assertEquals("new foo", ctx.getAttribute("foo"));
        assertEquals("blah", ctx.getAttribute("qux"));

    }

    @Test
    public void testRestoreAttributesInWebContext() throws Exception {
        //GIVEN
        final RenderingContext aggregationState = mock(RenderingContext.class);

        final WebContext ctx = new MockWebContext();
        ctx.setAttribute("foo", "foo value", WebContext.LOCAL_SCOPE);
        ctx.setAttribute("bar", 1, WebContext.LOCAL_SCOPE);
        ctx.setAttribute("baz", true, WebContext.LOCAL_SCOPE);
        MgnlContext.setInstance(ctx);

        assertEquals(3, ctx.getAttributes().size());

        final AbstractContentTemplatingElement compo = new DummyComponent(null, aggregationState);
        Map<String,Object> attributes = new HashMap<String, Object>();
        attributes.put("foo", "new foo");
        attributes.put("qux", "blah");

        compo.setAttributesInWebContext(attributes, WebContext.LOCAL_SCOPE);

        assertEquals(4, ctx.getAttributes().size());
        assertEquals("new foo", ctx.getAttribute("foo"));
        assertEquals("blah", ctx.getAttribute("qux"));

        //WHEN
        compo.restoreAttributesInWebContext(attributes, WebContext.LOCAL_SCOPE);

        //THEN
        assertEquals(3, ctx.getAttributes().size());
        assertEquals("foo value", ctx.getAttribute("foo"));
        assertNull(ctx.getAttribute("qux"));

    }

    private static class DummyComponent extends AbstractContentTemplatingElement {
        public DummyComponent(ServerConfiguration serverConfiguration, RenderingContext renderingContext) {
            super(serverConfiguration, renderingContext);
        }

        @Override
        public void begin(Appendable out) throws IOException {
            out.append("hello world");
        }
    }
}
