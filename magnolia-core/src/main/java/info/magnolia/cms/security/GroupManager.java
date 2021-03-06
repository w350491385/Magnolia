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
package info.magnolia.cms.security;

import info.magnolia.cms.security.auth.ACL;

import java.util.Collection;
import java.util.Map;

/**
 * Manages the groups.
 * @author Sameer Charles $Id$
 */
public interface GroupManager {

    /**
     * @throws UnsupportedOperationException if the implementation does not support writing
     */
    public Group createGroup(String name) throws UnsupportedOperationException, AccessDeniedException;

    /**
     * @throws UnsupportedOperationException if the implementation does not support writing
     */
    public Group getGroup(String name) throws UnsupportedOperationException, AccessDeniedException;

    /**
     * Get all groups defined in the system.
     */
    public Collection<Group> getAllGroups() throws UnsupportedOperationException;

    /**
     * Get all groups related to one concrete group.
     */
    public Collection<String> getAllGroups(String groupName) throws UnsupportedOperationException;

    public Map<String, ACL> getACLs(String group);

    /**
    * Grants to the group a role.
    * @return Group object with the role already granted.
    */
    public Group addRole(Group group, String roleName) throws AccessDeniedException;

    /**
     * Adds to the group to a group.
     * @return group object with the group already assigned.
     */
    public Group addGroup(Group group, String groupName) throws AccessDeniedException;

}
