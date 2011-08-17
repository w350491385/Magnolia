/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.nodebuilder;

import java.util.ArrayList;
import java.util.List;

import info.magnolia.cms.core.Content;

import javax.jcr.RepositoryException;

import org.apache.commons.collections.CollectionUtils;

/**
 * Abstract implementation of NodeOperation. Mainly implementing {@link #then(NodeOperation...)}.
 */
public abstract class AbstractNodeOperation implements NodeOperation {
    private List<NodeOperation> childrenOps = new ArrayList<NodeOperation>();

    @Override
    public void exec(Content context, ErrorHandler errorHandler) {
        try {
            context = doExec(context, errorHandler);
        } catch (RepositoryException e) {
            errorHandler.handle(e, context);
        }

        for (NodeOperation childrenOp : childrenOps) {
            childrenOp.exec(context, errorHandler);
        }

    }

    /**
     * @return the node that should now be used as the context for subsequent operations
     */
    protected abstract Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException;

    @Override
    public NodeOperation then(NodeOperation... childrenOps) {
        // add the operations to allow multiple calls on the method.
        CollectionUtils.addAll(this.childrenOps, childrenOps);
        return this;
    }
}
