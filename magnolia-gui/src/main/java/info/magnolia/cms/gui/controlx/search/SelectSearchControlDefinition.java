/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.cms.gui.controlx.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class SelectSearchControlDefinition extends SearchControlDefinition {

    /**
     * @param name
     * @param label
     */
    public SelectSearchControlDefinition(String name, String label) {
        super(name, label, "select");
    }

    // array
    public OrderedMap options = new ListOrderedMap();

    @Override
    public String getJsField() {

        List pairs = new ArrayList();
        for (MapIterator iter = this.getOptions().orderedMapIterator(); iter.hasNext();) {
            iter.next();
            String key = (String) iter.getKey();
            String value = (String) iter.getValue();
            pairs.add("'" + key + "': '" + value + "'");
        }

        String str = super.getJsField();
        str = StringUtils.removeEnd(str, "}");
        str += ",options: {";
        str += StringUtils.join(pairs.iterator(), ",");
        str += "}}";
        return str;
    }

    /**
     * @return Returns the options.
     */
    public OrderedMap getOptions() {
        return this.options;
    }

    public void addOption(String value, String label) {
        this.getOptions().put(value, label);
    }
}
