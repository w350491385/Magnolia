/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.link;

import info.magnolia.cms.util.FactoryUtil;

/**
 * Use to transform links
 * <ul>
 * <li> uuid pattern --> absolute links
 * <li> uuid pattern --> relative links
 * <li> internal links --> uuid pattern
 * </ul>
 * <p>
 * The internal links are for example used in the fck editor.<br/>
 * website:/home.html
 * </p>
 * <p>
 * The uuid pattern stores all the needed information like repository, uuid, path, filename (for binaries), ...
 * </p>
 * <p>
 * The absolute links are the links used for the request including all transformations like adding context, i18n content support, repository to uri mapping, ...
 * </p>
 *
 * @author philipp
 * @version $Id$
 * @deprecated since 4.0 use {@link LinkHelper} instead
 *
 */
public interface LinkResolver {

    public static class Factory{
        public static LinkResolver getInstance(){
            return (LinkResolver) FactoryUtil.getSingleton(LinkResolver.class);
        }
    }

    public String convertToRelativeLinks(String str, String currentPath);

    public String convertToEditorLinks(String str);

    /**
     * This method is used to create the public links. So it is used by the cms:out tag for example.
     */
    public String convertToBrowserLinks(String str, String currentPath);

    public String convertToAbsoluteLinks(String str, boolean addContextPath);

    /**
     * Used to render links usable from external (like sending an email, ...)
     */
    public String convertToExternalLinks(String str);

    /**
     * Parses the internal links (used mainly by the fckeditor) and creates the uuid pattern instead.
     */
    public String parseLinks(String str);

}
