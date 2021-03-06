/**
 * This file Copyright (c) 2003-2013 Magnolia International
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
package info.magnolia.context;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.repository.DefaultRepositoryManager;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.SimpleCredentials;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for DefaultRepositoryStrategy.
 */
public class DefaultRepositoryStrategyTest extends RepositoryTestCase {

    private DefaultRepositoryStrategy strategy;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        UserContext ctx = mock(UserContext.class);
        RepositoryManager repositoryManager = new DefaultRepositoryManager();
        strategy = new DefaultRepositoryStrategy(repositoryManager, ctx);
    }

    @Test
    public void testGetUserCredentialsReturnsCredentialsFromContextUserIfSet() {
        // GIVEN
        Context context = mock(Context.class);
        MgnlContext.setInstance(context);
        User user = mock(User.class);
        when(context.getUser()).thenReturn(user);
        when(user.getName()).thenReturn("user");
        when(user.getPassword()).thenReturn("password");

        // WHEN
        SimpleCredentials credentials = (SimpleCredentials) strategy.getCredentials();

        // THEN
        assertEquals("user", credentials.getUserID());
        assertEquals("password", new String(credentials.getPassword()));
    }

    @Test
    public void testPasswordIsNull() {
        // GIVEN
        Context context = mock(Context.class);
        MgnlContext.setInstance(context);
        User user = mock(User.class);
        when(context.getUser()).thenReturn(user);
        when(user.getName()).thenReturn("user");
        when(user.getPassword()).thenReturn(null);

        // WHEN
        SimpleCredentials credentials = (SimpleCredentials) strategy.getCredentials();

        // THEN
        assertEquals("user", credentials.getUserID());
        assertEquals("", new String(credentials.getPassword()));
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        MgnlContext.setInstance(null);
    }

}
