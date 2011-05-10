/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.model.navigation.definition;

import info.magnolia.ui.model.menu.definition.MenuItemDefinitionImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Simple implementation of {@link NavigationItemDefinition}.
 */
public class NavigationItemDefinitionImpl extends MenuItemDefinitionImpl implements NavigationItemDefinition {

    private List<NavigationItemDefinition> items = new ArrayList<NavigationItemDefinition>();

    private NavigationItemDefinition parent;

    // interface
    @Override
    public List<NavigationItemDefinition> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public NavigationItemDefinition getParent() {
        return parent;
    }

    @Override
    public void setParent(NavigationItemDefinition parent) {
        this.parent = parent;
    }

    // content2bean
    public void addItem(NavigationItemDefinition item) {
        item.setParent(this);
        items.add(item);
    }

}
