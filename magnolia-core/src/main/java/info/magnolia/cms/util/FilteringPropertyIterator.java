/**
 * This file Copyright (c) 2011 Magnolia International
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

import javax.jcr.Property;
import javax.jcr.PropertyIterator;

import org.apache.jackrabbit.commons.predicate.Predicate;

/**
 * Property iterator hiding all properties that do not pass the predicate.
 * @author had
 * @version $Id: $
 */
public class FilteringPropertyIterator implements PropertyIterator {

    private final PropertyIterator iterator;
    private Property nextItem;
    private final Predicate predicate;

    public FilteringPropertyIterator(PropertyIterator iterator, Predicate predicate) {
        this.iterator = iterator;
        this.predicate = predicate;
    }
    public Property nextProperty() {
        if (nextItem != null) {
            Property temp = nextItem;
            nextItem = null;
            return temp;
        }
        return iterator.nextProperty();
    }

    public long getPosition() {
        return iterator.getPosition();
    }

    public long getSize() {
        return iterator.getSize();
    }

    public void skip(long skipNum) {
        iterator.skip(skipNum);
    }

    public boolean hasNext() {
        while (nextItem == null) {
            if (!iterator.hasNext()) {
                return false;
            }
            nextItem = iterator.nextProperty();
            if (!predicate.evaluate(nextItem)) {
                nextItem = null;
            }
        }
        return true;
    }

    public Object next() {
        return nextProperty();
    }

    public void remove() {
        iterator.remove();
    }

}