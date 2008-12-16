/**
 * This file Copyright (c) 2008 Magnolia International
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
package info.magnolia.module.samples.model;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.beans.config.RenderableDefinition;
import info.magnolia.cms.beans.config.RenderingModel;
import info.magnolia.cms.beans.config.RenderingModelImpl;
import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

/**
 *
 * @author tmiyar
 *
 */
public class MainSampleParagraphModel extends RenderingModelImpl {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MainSampleParagraphModel.class);

    public MainSampleParagraphModel(Content content, RenderableDefinition definition, RenderingModel parent) {
        super(content, definition, parent);
        log.info("Running sample paragraph model");

    }

    public String getFilterAttribute() {
        return (String) MgnlContext.getAttribute("sampleFilter");

    }

    public String execute() {
        String url = "";
        String query = MgnlContext.getParameter("query");

        if(!StringUtils.isEmpty(query) ) {
            url = MgnlContext.getContextPath() +"/" + MgnlContext.getParameter("resultPage")
               + ".html?query=" + query;
            try {
                ((WebContext)MgnlContext.getInstance()).getResponse().sendRedirect(url);
            } catch (IOException e) {
                log.error("error running query");
            }
        }
        return "success";
    }


}
