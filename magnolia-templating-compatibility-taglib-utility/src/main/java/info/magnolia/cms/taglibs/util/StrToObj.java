/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.cms.taglibs.util;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.lang.StringUtils;


/**
 * converts a string to an object using split.
 * @jsp.tag name="strToObj" body-content="JSP"
 *
 * @author Vinzenz Wyser
 * @author Fabrizio Giustina
 * @version $Revision $ ($Author $)
 */
public class StrToObj extends BodyTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private String var;

    private String delims;

    /**
     * name of the pageContext variable where the obj is put to.
     * @jsp.attribute required="true" rtexprvalue="false"
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * delimiters characters; default: <code>"\n"</Code>.
     * @jsp.attribute required="false" rtexprvalue="false"
     */
    public void setDelims(String delims) {
        this.delims = delims;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
    public int doEndTag() {
        String str = getBodyContent().getString();
        if (StringUtils.isNotEmpty(str)) {
            String[] obj = str.split(StringUtils.defaultString(this.delims, "\n")); //$NON-NLS-1$
            pageContext.setAttribute(this.var, obj, PageContext.PAGE_SCOPE);

        }
        else {
            pageContext.setAttribute(this.var, StringUtils.EMPTY, PageContext.PAGE_SCOPE);
        }
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#release()
     */
    @Override
    public void release() {
        this.var = null;
        this.delims = null;
        super.release();
    }
}