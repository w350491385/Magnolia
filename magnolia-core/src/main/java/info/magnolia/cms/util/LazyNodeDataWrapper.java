/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.context.MgnlContext;

import java.io.Serializable;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores a path and will re-fetch the node data in {@link #getWrappedNodeData()} if the session is closed.
 *
 * @author ochytil
 * @version $Id$
 */
public class LazyNodeDataWrapper extends NodeDataWrapper implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(LazyNodeDataWrapper.class);

    private String repository;

    private String path;

    private transient NodeData nodeData;

    public LazyNodeDataWrapper(NodeData nodeData) {
        try {
            this.setRepository(nodeData.getParent().getWorkspace().getName());
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        this.setPath(nodeData.getHandle());
        this.nodeData = nodeData;
    }

    @Override
    public NodeData getWrappedNodeData() {
        try {
            // DHM.getNodeData() can still return null and the var itself is transient and won't survive serialization
            if (nodeData == null || (nodeData.isExist() && !nodeData.getJCRProperty().getSession().isLive())) {
                nodeData = getHierarchyManager().getNodeData(getPath());
            }
        }
        catch (RepositoryException e) {
            log.error("can't reinitialize node " + getPath(), e);
        }
        return nodeData;
    }

    @Override
    public HierarchyManager getHierarchyManager() {
        return MgnlContext.getSystemContext().getHierarchyManager(getRepository());
    }

    protected void setPath(String uuid) {
        this.path = uuid;
    }

    protected String getPath() {
        return path;
    }

    protected void setRepository(String repository) {
        this.repository = repository;
    }

    protected String getRepository() {
        return repository;
    }

    @Override
    protected Content wrap(Content content) {
        return new LazyContentWrapper(content);
    }
}
