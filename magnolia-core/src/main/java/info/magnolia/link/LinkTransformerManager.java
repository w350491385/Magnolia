/**
 * This file Copyright (c) 2009 Magnolia International
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
package info.magnolia.link;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.FactoryUtil;

/**
 * Single point of access for all Link Transformers.
 * @author had
 *
 */
public class LinkTransformerManager {

    /**
     * Gets the current singleton instance.
     */
    public static LinkTransformerManager getInstance() {
        return (LinkTransformerManager) FactoryUtil.getSingleton(LinkTransformerManager.class);
    }
    
    /**
     * @see AbsolutePathTransformer
     */
    public AbsolutePathTransformer getAbsolute(boolean addContextPath, boolean useURI2RepositoryMapping, boolean useI18N) {
        return new AbsolutePathTransformer(addContextPath, useURI2RepositoryMapping, useI18N);
    }
    
    /**
     * @see RelativePathTransformer
     */
    public RelativePathTransformer getRelative(Content page, boolean useURI2RepositoryMapping, boolean useI18N) {
        return new RelativePathTransformer(page, useURI2RepositoryMapping, useI18N);
    }

    /**
     * @see RelativePathTransformer
     */
    public RelativePathTransformer getRelative(String absolutePath, boolean useURI2RepositoryMapping, boolean useI18N) {
        return new RelativePathTransformer(absolutePath, useURI2RepositoryMapping, useI18N);
    }
    
    /**
     * @see CompleteUrlPathTransformer
     */
    public CompleteUrlPathTransformer getCompleteUrl(boolean useURI2RepositoryMapping, boolean useI18N) {
        return new CompleteUrlPathTransformer(useURI2RepositoryMapping, useI18N);
    }
    
    /**
     * @see EditorLinkTransformer
     */
    public EditorLinkTransformer getEditorLink() {
        return new EditorLinkTransformer();
    }
    
    public BrowserLinkTransformer getBrowserLink(String currentPath) {
        // need to instantiate using factory to use settings from /server/renderer/linkManagement
        BrowserLinkTransformer tfmr = (BrowserLinkTransformer) FactoryUtil.newInstance(BrowserLinkTransformer.class);
        tfmr.setCurrentPath(currentPath);
        return tfmr;
    }
}
