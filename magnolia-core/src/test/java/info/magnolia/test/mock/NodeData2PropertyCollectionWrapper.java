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
package info.magnolia.test.mock;

import info.magnolia.cms.core.NodeData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.jcr.PathNotFoundException;
import javax.jcr.Property;

/**
 * Wrapper for collections of NodeData where collections of Properties are expected.
 * @author had
 * @version $Id: $
 */
public class NodeData2PropertyCollectionWrapper implements Collection<Property> {

    private final Collection<NodeData> col;

    public NodeData2PropertyCollectionWrapper(Collection<NodeData> c) {
        this.col = c;
    }

    @Override
    public boolean add(Property arg0) {
        throw new UnsupportedOperationException("This collection is read only");
    }

    @Override
    public boolean addAll(Collection<? extends Property> arg0) {
        throw new UnsupportedOperationException("This collection is read only");
    }

    @Override
    public void clear() {
        col.clear();
    }

    @Override
    public boolean contains(Object arg0) {
        for (NodeData c : col) {
            if (getPropertyOrThrowException(c).equals(arg0)) {
                return true;
            }
        }
        return false;
    }

    protected Property getPropertyOrThrowException(NodeData c) {
        try {
            return c.getJCRProperty();
        }
        catch (PathNotFoundException e) {
            // should not happen because we read the NodeData collection first
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean containsAll(Collection<?> arg0) {
        if (arg0 == null) {
            return false;
        }
        Collection<Object> test = new ArrayList(arg0);
        for (NodeData c : col) {
            test.remove(getPropertyOrThrowException(c));
        }
        return test.isEmpty();
    }

    @Override
    public boolean isEmpty() {
        return col.isEmpty();
    }

    @Override
    public Iterator<Property> iterator() {
        Collection<Property> test = new ArrayList<Property>();
        for (NodeData c : col) {
            test.add(getPropertyOrThrowException(c));
        }

        return test.iterator();
    }

    @Override
    public boolean remove(Object arg0) {
        for (NodeData c : col) {
            if (getPropertyOrThrowException(c).equals(arg0)) {
                col.remove(c);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> propertiesToRemove) {
        Collection<NodeData> nodeDataToRemove = createCollectionOfContainedNodeData(propertiesToRemove);
        return col.removeAll(nodeDataToRemove);
    }

    @Override
    public boolean retainAll(Collection<?> propertiesToRetain) {
        Collection<NodeData> nodeDataToRetain = createCollectionOfContainedNodeData(propertiesToRetain);
        return col.retainAll(nodeDataToRetain);
    }

    private Collection<NodeData> createCollectionOfContainedNodeData(Collection<?> propertiesToRemove) {
        Collection<NodeData> test = new ArrayList<NodeData>();
        for (Object o : propertiesToRemove) {
            for (NodeData c : col) {
                if (getPropertyOrThrowException(c).equals(o)) {
                    test.add(c);
                }
            }
        }
        return test;
    }


    @Override
    public int size() {
        return col.size();
    }

    @Override
    public Object[] toArray() {
        Collection<Property> test = new ArrayList<Property>();
        for (NodeData c : col) {
            test.add(getPropertyOrThrowException(c));
        }
        return test.toArray();
    }

    @Override
    public <T> T[] toArray(T[] arg0) {
        Collection<Property> test = new ArrayList<Property>();
        for (NodeData c : col) {
            test.add(getPropertyOrThrowException(c));
        }
        return test.toArray(arg0);
    }
}
