/**
 * This file Copyright (c) 2010-2010 Magnolia International
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

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jcr.RepositoryException;

import info.magnolia.cms.core.AbstractContent;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;


/**
 * This wrapper allows extending other nodes (mainly useful to extend configurations). A content node can define
 * a nodedata with the name 'extends'. Its value is either an absolute or relative path. The merge is then performed as follows:
 *
 * <ul>
 * <li>node datas are merged and values are overwritten
 * <li>sub content nodes are merged, the original order is guaranteed, new nodes are added at the
 * end of the list
 * </ul>
 *
 * The mechanism supports multiple inheritances as such:
 * <ul>
 * <li>the node the current node inherits from can again extend a node
 * <li>nodes laying deeper in the hierarchy can extend an other node
 * </ul>
 *
 * @author pbaerfuss
 * @version $Id$
 * @see InheritanceContentWrapper a class supporting content inheritance.
 */
public class ExtendingContentWrapper extends ContentWrapper {

    private static final String EXTENDING_NODE_DATA = "extends";

    private boolean extending;

    private Content extendedContent;

    public ExtendingContentWrapper(Content wrappedContent) {
        super(wrappedContent);
        try {
            extending = getWrappedContent().hasNodeData(EXTENDING_NODE_DATA);
            if (extending) {
                // support multiple inheritance
                extendedContent = wrapIfNeeded(getWrappedContent().getNodeData(EXTENDING_NODE_DATA).getReferencedContent());
            }
        }
        catch (RepositoryException e) {
            throw new RuntimeException("Can't wrap node [" + wrappedContent + "]", e);
        }
    }

    /**
     * Does not support the extends nodedata but chains the two nodes directly. Each node is
     * wrapped internally to ensure that each of them support the extends nodedata for themselves.
     */
    protected ExtendingContentWrapper(Content wrappedContent, Content extendedContent) {
        super(wrapIfNeeded(wrappedContent));
        this.extending = true;
        // might extend further more
        this.extendedContent = wrapIfNeeded(extendedContent);
    }

    private static Content wrapIfNeeded(Content content) {
        if (content instanceof ExtendingContentWrapper) {
            return content;
        }
        return new ExtendingContentWrapper(content);
    }

    public boolean isExtending() {
        return this.extending;
    }

    @Override
    public boolean hasContent(String name) throws RepositoryException {
        if (getWrappedContent().hasContent(name)) {
            return true;
        }
        else if (extending && extendedContent.hasContent(name)) {
            return true;
        }
        return false;
    }

    @Override
    public Content getContent(String name) throws RepositoryException {
        Content content;
        if (getWrappedContent().hasContent(name)) {
            content = getWrappedContent().getContent(name);
        }
        else if (extending && extendedContent.hasContent(name)) {
            content = extendedContent.getContent(name);
        }
        else {
            // this will throw a PathNotFoundException
            content = getWrappedContent().getContent(name);
        }
        return wrap(content);
    }

    @Override
    public Collection<Content> getChildren(ContentFilter filter, String namePattern, Comparator<Content> orderCriteria) {
        Collection<Content> directChildren = ((AbstractContent)getWrappedContent()).getChildren(filter, namePattern, orderCriteria);
        if (extending) {
            Collection<Content> inheritedChildren = ((AbstractContent)extendedContent).getChildren(filter, namePattern, orderCriteria);
            // keep order, add new elements at the end of the collection
            LinkedHashMap<String, Content> merged = new LinkedHashMap<String, Content>();
            for (Content content : inheritedChildren) {
                merged.put(content.getName(), content);
            }
            for (Content content : directChildren) {
                merged.put(content.getName(), content);
            }
            return wrapContentNodes(merged.values());
        }
        else {
            return wrapContentNodes(directChildren);
        }
    }

    @Override
    public NodeData getNodeData(String name) {
        try {
            if (getWrappedContent().hasNodeData(name)) {
                return wrap(getWrappedContent().getNodeData(name));
            }
            else if (extending && extendedContent.hasNodeData(name)) {
                return wrap(extendedContent.getNodeData(name));
            }
            else {
                return wrap(getWrappedContent().getNodeData(name));
            }
        }
        catch (RepositoryException e) {
            throw new RuntimeException("Can't read nodedata from extended node [" + extendedContent + "]", e);
        }
    }

    @Override
    public Collection<NodeData> getNodeDataCollection() {
        Collection<NodeData> directChildren = getWrappedContent().getNodeDataCollection();
        if (extending) {
            Collection<NodeData> inheritedChildren = extendedContent.getNodeDataCollection();
            // sort by name
            SortedMap<String, NodeData> merged = new TreeMap<String, NodeData>();
            for (NodeData nodeData : inheritedChildren) {
                merged.put(nodeData.getName(), nodeData);
            }
            for (NodeData nodeData : directChildren) {
                merged.put(nodeData.getName(), nodeData);
            }
            return wrapNodeDatas(merged.values());
        }
        else {
            return wrapNodeDatas(directChildren);
        }
    }

    protected Content wrap(Content node) {
        // get the same subnode of the extended content
        try {
            if (extending && extendedContent.hasContent(node.getName())) {
                // FIXME we have to calculate the relative path
                Content extendedSubContent = extendedContent.getContent(node.getName());
                return new ExtendingContentWrapper(node, extendedSubContent);
            }
        }
        catch (RepositoryException e) {
            throw new RuntimeException("Can't wrap " + node, e);
        }
        return new ExtendingContentWrapper(node);
    }

}
