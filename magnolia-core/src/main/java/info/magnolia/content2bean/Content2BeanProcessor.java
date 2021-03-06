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
package info.magnolia.content2bean;

import info.magnolia.cms.core.Content;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;


/**
 * Transforms nodes to beans or maps. The transformer is use to resolve classes or to instantiate beans.
 *
 * @deprecated since 4.5, use {@link Content2Bean}.
 *
 * @author philipp
 * @version $Id$
 */
public interface Content2BeanProcessor {

    /**
     * Transforms the node to a bean using the passed transformer and component provider.
     */
    public Object toBean(Content node, boolean recursive, final Content2BeanTransformer transformer, ComponentProvider componentProvider)
        throws Content2BeanException;

    /**
     * Similar to <code>toBean()</code> but uses a passed bean as the root bean.
     */
    public Object setProperties(final Object bean, Content node, boolean recursive, final Content2BeanTransformer transformer, ComponentProvider componentProvider) throws Content2BeanException;

    /**
     * Creates a singleton instance of the content2bean processor.
     * @deprecated since 4.5, we should use IoC. However, we'll leave the processor as is for now.
     */
    class Factory {
        public static Content2BeanProcessor getProcessor() {
            return Components.getSingleton(Content2BeanProcessor.class);
        }
    }

}
