/**
 * This file Copyright (c) 2010 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
package info.magnolia.module.admincentral.tree.container;

import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.NodeDataWrapper;
import info.magnolia.module.admincentral.tree.TreeDefinition;

import java.util.Collection;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;


/**
 * Vaadin Item wrapping a NodeData.
 *
 * @author daniellipp
 * @version $Id$
 */
public class NodeDataItem extends NodeDataWrapper implements Item {

    private static final long serialVersionUID = 2758187921120400527L;

    private static Logger log = LoggerFactory.getLogger(NodeDataItem.class);

    private TreeDefinition definition;

    NodeData node;

    String handle;

    public NodeDataItem(NodeData data, TreeDefinition definition)
            throws RepositoryException {
        super(data);
        this.handle = data.getHandle();
        this.node = data;
        this.definition = definition;
    }

    protected void assertIdIsString(Object id) {
        if (!(id instanceof String)) {
            throw new UnsupportedOperationException(
                    "JCR requires all property id's to be String");
        }
    }

    /**
     *
     * @return absolute path as vaadin item id
     * @throws RepositoryException
     */
    public String getItemId() throws RepositoryException {
        return getHandle();
    }

    public Property getItemProperty(Object id) {
        assertIdIsString(id);
        return new ObjectProperty(id);
    }

    public Collection<String> getItemPropertyIds() {
        try {
            return getAttributeNames();
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean removeItemProperty(Object id)
            throws UnsupportedOperationException {
            return false;
    }

    public synchronized NodeData getWrappedNodeData() {
        return node;
    }

    public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
        return false;
    }

}
