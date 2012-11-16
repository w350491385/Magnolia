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
package info.magnolia.jcr.util;

import java.util.Calendar;

import info.magnolia.cms.core.MetaData;
import info.magnolia.logging.AuditLoggingUtil;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

/**
 * Collection of utilities to simplify working with the JCR API. In contrast to info.magnolia.cms.core.Content it is -
 * from a caller perspective - independent from Content API. Internally content API is still used for now, but this will
 * most probably change quite soon.
 *
 * @deprecated since 5.0 - use {@link NodeUtil} instead
 */
public class MetaDataUtil {

    @Deprecated
    public static MetaData getMetaData(Node node) {
        return new MetaData(node);
    }

    public static void updateMetaData(Node node) throws RepositoryException {
        NodeTypes.LastModifiedMixin.updateModification(node);
        AuditLoggingUtil.log(AuditLoggingUtil.ACTION_MODIFY, node.getSession().getWorkspace().getName(), node
                .getPrimaryNodeType().getName(), node.getName());
    }

    /**
     * @return the lastModification or null it it was not set in JCR.
     * @deprecated since 5.0 - use {@link NodeTypes.LastModifiedMixin#getLastModified(javax.jcr.Node)}
     */
    @Deprecated
    public static Calendar getLastModification(Node node) throws PathNotFoundException, RepositoryException, ValueFormatException {
        return NodeTypes.LastModifiedMixin.getLastModified(node);
    }

    /**
     * @deprecated since 5.0 - use {@link NodeTypes.RenderableMixin#getTemplate(javax.jcr.Node)}
     */
    @Deprecated
    public static String getTemplate(Node node) throws RepositoryException {
        return NodeTypes.RenderableMixin.getTemplate(node);
    }

}
