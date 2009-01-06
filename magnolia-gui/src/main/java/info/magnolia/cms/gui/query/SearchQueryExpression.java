/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.gui.query;

/**
 * @author Sameer Charles $Id$
 */
public interface SearchQueryExpression {

    /**
     * get left expression
     * @return object on left
     */
    public SearchQueryExpression getLeft();

    /**
     * set left expression
     * @param left object on left
     */
    public void setleft(SearchQueryExpression left);

    /**
     * checks if has left
     * @return true if left object exist
     */
    public boolean hasLeft();

    /**
     * get right expression
     * @return object on right
     */
    public SearchQueryExpression getRight();

    /**
     * set right expression
     * @param right object on right
     */
    public void setRight(SearchQueryExpression right);

    /**
     * checks if has right
     * @return true if right object exist
     */
    public boolean hasRight();

    /**
     * get string representation
     * @return string representation if this expression
     */
    public String toString();

}
