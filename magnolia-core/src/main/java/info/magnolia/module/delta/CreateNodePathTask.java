/**
 * This file Copyright (c) 2007-2012 Magnolia International
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
package info.magnolia.module.delta;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;


/**
 * A Task to create a full path, i.e the parent doesn't need to exist.
 *
 * @author vsteller
 * @version $Id$
 */
public class CreateNodePathTask extends AbstractRepositoryTask {

    private final String workspace;
    private final String path;
    private final ItemType type;

    public CreateNodePathTask(String name, String description, String workspace, String path) {
        this(name, description, workspace, path, ItemType.CONTENT);
    }

    public CreateNodePathTask(String name, String description, String workspace, String path, ItemType type) {
        super(name, description);
        this.workspace = workspace;
        this.path = path;
        this.type = type;
    }

    @Override
    protected void doExecute(InstallContext installContext) throws TaskExecutionException, RepositoryException {
        final HierarchyManager hm = installContext.getHierarchyManager(workspace);
        ContentUtil.createPath(hm, path, type, false);
    }
}
