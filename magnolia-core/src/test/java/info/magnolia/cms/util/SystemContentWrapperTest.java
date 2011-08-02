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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.util;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.ContextFactory;
import info.magnolia.context.LifeTimeJCRSessionUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.UserContextImpl;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;

import org.junit.Before;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpSession;

/**
 * @author pbaerfuss
 * @version $Id$
 *
 */
public class SystemContentWrapperTest extends RepositoryTestCase {

    private Object[] mocks;

    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        final UserManager man = createNiceMock(UserManager.class);
        ComponentsTestUtil.setInstance(SecuritySupport.class, new SecuritySupportImpl() {
            @Override
            public UserManager getUserManager(String realmName) {
                return man;
            }
        });
        User anonymous = createNiceMock(User.class);
        User dummy = createNiceMock(User.class);
        expect(man.getAnonymousUser()).andReturn(anonymous);
        expect(anonymous.getName()).andReturn("anonymous");
        expect(dummy.getName()).andReturn("admin");
        expect(dummy.getPassword()).andReturn("admin");

        mocks = new Object[] { man, anonymous, dummy };
        replay(mocks);
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setSession(session);
        MgnlContext.setInstance(ContextFactory.getInstance().createWebContext(req, null, null));
        ((UserContextImpl) MgnlContext.getInstance()).login(dummy);
    }

    @Override
    protected void tearDown() throws Exception {
        verify(mocks);
        super.tearDown();
    }

    public void testClosedSessionResistance() throws Exception {
        HierarchyManager hm = MgnlContext.getInstance().getHierarchyManager("config");

        // preconditions
        assertNotSame(hm, LifeTimeJCRSessionUtil.getHierarchyManager("config"));

        // create test data
        hm.getRoot().createContent("bla", ItemType.CONTENT.getSystemName()).setNodeData("nodeData2", "boo");
        hm.save();
        Content plainContent = hm.getContent("/bla");
        Content systemContent = new SystemContentWrapper(plainContent);
        NodeData nd = systemContent.getNodeData("nodeData2");


        // close the session SCW has to be able to recover
        hm.getWorkspace().getSession().logout();

        assertFalse(plainContent.getHierarchyManager().getWorkspace().getSession().isLive());
        assertTrue(systemContent.getHierarchyManager().getWorkspace().getSession().isLive());

        assertEquals(systemContent.getHierarchyManager(), LifeTimeJCRSessionUtil.getHierarchyManager("config"));
        assertEquals(systemContent.getHierarchyManager(), nd.getHierarchyManager());

    }

    public void testWrappingNDParent() throws Exception {
        HierarchyManager hm = MgnlContext.getInstance().getHierarchyManager("config");

        // preconditions
        assertNotSame(hm, LifeTimeJCRSessionUtil.getHierarchyManager("config"));

        // create test data
        hm.getRoot().createContent("bla", ItemType.CONTENT.getSystemName()).setNodeData("nodeData2", "boo");
        hm.save();
        Content plainContent = hm.getContent("/bla");
        Content systemContent = new SystemContentWrapper(plainContent);
        NodeData nd = systemContent.getNodeData("nodeData2");

        assertTrue(nd.getParent() instanceof SystemContentWrapper);
    }

    public void testWrappingReferencedContent() throws Exception {
        HierarchyManager hm = MgnlContext.getInstance().getHierarchyManager("config");

        // preconditions
        assertNotSame(hm, LifeTimeJCRSessionUtil.getHierarchyManager("config"));

        // create test data
        hm.getRoot().createContent("bla", ItemType.CONTENT.getSystemName()).setNodeData("nodeData2", "/boo");
        hm.getRoot().createContent("boo", ItemType.CONTENT.getSystemName());
        hm.save();
        Content plainContent = hm.getContent("/bla");
        Content systemContent = new SystemContentWrapper(plainContent);
        Content referencedContent = systemContent.getNodeData("nodeData2").getReferencedContent();
        assertTrue(referencedContent instanceof SystemContentWrapper);

        referencedContent = systemContent.getNodeData("nodeData2").getReferencedContent("config");
        assertTrue(referencedContent instanceof SystemContentWrapper);
    }
}