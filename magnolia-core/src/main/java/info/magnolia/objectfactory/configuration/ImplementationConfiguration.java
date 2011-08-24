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
package info.magnolia.objectfactory.configuration;

/**
 * A simple type mapping.
 *
 * @param <T> the type
 * @version $Id$
 */
public class ImplementationConfiguration<T> extends ComponentConfiguration {

    private Class<? extends T> implementation;

    public ImplementationConfiguration() {
    }

    public ImplementationConfiguration(Class<T> type, Class<? extends T> implementation) {
        super(type);
        this.implementation = implementation;
    }

    public Class<? extends T> getImplementation() {
        return implementation;
    }

    public void setImplementation(Class<? extends T> implementation) {
        this.implementation = implementation;
    }

    public static <T> ImplementationConfiguration<T> valueOf(Class<T> type, Class<? extends T> implementation) {
        return new ImplementationConfiguration<T>(type, implementation);
    }
}
