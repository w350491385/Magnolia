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

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.User;
import info.magnolia.repository.RepositoryManager;

import javax.inject.Inject;
import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

/**
 * Uses a user based access manager.
 *
 * @version $Id$
 */
public class DefaultRepositoryStrategy extends AbstractRepositoryStrategy {

    protected UserContext context;

    @Inject
    public DefaultRepositoryStrategy(RepositoryManager repositoryManager, UserContext context) {
        super(repositoryManager);
        this.context = context;
    }

    @Override
    protected Session internalGetSession(String workspaceName) throws RepositoryException {
        return repositoryManager.getSession(workspaceName, getCredentials());
    }

    /**
     * @return credentials of current user - anonymous if unknown.
     */
    protected Credentials getCredentials() {
        User user = MgnlContext.getUser();
        if (user == null) {
            // there is no user logged in, so this is just a system call. Returned credentials are used only to access repository, but do not allow any access over Magnolia.
            // FIXME: stop using SystemProperty, but IoC is not ready yet when this is called (config loader calls repo.init() which results in authentication calls being made and this method being invoked
            // TODO: can also read it from the Login Module properties ... but WAU has no access to that
            String user1 = SystemProperty.getProperty("magnolia.connection.jcr.anonymous.userId", "anonymous");
            String pwd = SystemProperty.getProperty("magnolia.connection.jcr.anonymous.password", "anonymous");
            return new SimpleCredentials(user1, pwd.toCharArray());
        }
        final String password = user.getPassword();
        if (password == null) {
            return new SimpleCredentials(user.getName(), "".toCharArray()); //user is already logged into instance, we can pass empty string
        } else {
            return new SimpleCredentials(user.getName(), password.toCharArray());
        }
    }

    @Override
    public void release() {
        release(false);
    }

}

