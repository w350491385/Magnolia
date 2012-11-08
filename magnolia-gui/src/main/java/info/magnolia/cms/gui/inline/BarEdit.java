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
package info.magnolia.cms.gui.inline;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.gui.control.Bar;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionUtil;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.io.Writer;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class BarEdit extends Bar {

    private ButtonEdit buttonEdit = new ButtonEdit();

    private Button buttonMove = new Button();

    private Button buttonDelete = new Button();

    private String paragraph;

    /**
     * @deprecated since 4.0 - use the empty constructor.
     */
    @Deprecated
    public BarEdit(HttpServletRequest request) {
    }

    public BarEdit() {

    }

    /**
     * Sets the default buttons.
     */
    public void setDefaultButtons() {
        this.setButtonEdit();
        this.setButtonMove();
        this.setButtonDelete();
    }

    /**
     * Places the default buttons to the very right/left position.
     */
    public void placeDefaultButtons() {
        if (this.getButtonMove() != null) {
            this.getButtonsLeft().add(0, this.getButtonMove());
        }
        if (this.getButtonEdit() != null) {
            this.getButtonsLeft().add(0, this.getButtonEdit());
        }
        if (this.getButtonDelete() != null) {
            this.getButtonsRight().add(this.getButtonsRight().size(), this.getButtonDelete());
        }
    }

    public ButtonEdit getButtonEdit() {
        return this.buttonEdit;
    }

    public void setButtonEdit(ButtonEdit b) {
        this.buttonEdit = b;
    }

    public void setButtonEdit() {
        this.setButtonEdit(this.getPath(), this.getNodeCollectionName(StringUtils.EMPTY), this.getNodeName(StringUtils.EMPTY), this.getParagraph());
    }

    /**
     * Sets the default edit button.
     *
     * @param path , path of the current page
     * @param nodeCollectionName , i.e. 'MainParagarphs'
     * @param nodeName , i.e. '01'
     * @param paragraph , paragraph type
     */
    public void setButtonEdit(String path, String nodeCollectionName, String nodeName, String paragraph) {
        ButtonEdit b = new ButtonEdit(path, nodeCollectionName, nodeName, paragraph);
        b.setDefaultOnclick();
        this.setButtonEdit(b);
    }

    public Button getButtonMove() {
        return this.buttonMove;
    }

    public void setButtonMove(Button b) {
        this.buttonMove = b;
    }

    public void setButtonMove() {
        this.setButtonMove(this.getNodeCollectionName(StringUtils.EMPTY), this.getNodeName(StringUtils.EMPTY));
    }

    /**
     * Sets the default move button.
     *
     * @param nodeCollectionName , i.e. 'MainParagarphs'
     * @param nodeName , i.e. '01'
     */
    public void setButtonMove(String nodeCollectionName, String nodeName) {
        Button b = new Button();
        b.setLabel(MessagesManager.get("buttons.move")); //$NON-NLS-1$
        // sets the id of the bar
        this.setId(nodeCollectionName + "__" + nodeName); //$NON-NLS-1$
        b.setOnclick("mgnlMoveNodeStart('" + nodeCollectionName + "','" + nodeName + "','" + this.getId() + "');"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        this.setButtonMove(b);
    }

    public Button getButtonDelete() {
        return this.buttonDelete;
    }

    public void setButtonDelete(Button b) {
        this.buttonDelete = b;
    }

    /**
     * @deprecated use {@link #setButtonDelete(String)}
     */
    @Deprecated
    public void setButtonDelete() {
        this.setButtonDelete(this.getPath(), this.getNodeCollectionName(), this.getNodeName());
    }

    /**
     * Sets the default delete button.
     *
     * @param path , path of the current page
     * @param nodeCollectionName , i.e. 'MainColumnParagraphs'
     * @param nodeName , i.e. '01'
     *
     * @deprecated use {@link #setButtonDelete(String)}
     */
    @Deprecated
    public void setButtonDelete(String path, String nodeCollectionName, String nodeName) {
        Button b = new Button();
        b.setLabel(MessagesManager.get("buttons.delete")); //$NON-NLS-1$
        b.setOnclick("mgnlDeleteNode('" + path + "','" + nodeCollectionName + "','" + nodeName + "');"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        this.setButtonDelete(b);
    }

    public void setButtonDelete(String pathToDelete) {
        Button b = new Button();
        b.setLabel(MessagesManager.get("buttons.delete"));
        b.setOnclick("mgnlDeleteNode('" + pathToDelete + "');");
        this.setButtonDelete(b);
    }

    /**
     * @deprecated use drawHtml(Writer out) instead.
     */
    @Deprecated
    public void drawHtml(JspWriter out) throws IOException {
        drawHtml((Writer) out);
    }

    /**
     * Draws the main bar (incl. all magnolia specific js and css sources).
     */
    public void drawHtml(Writer out) throws IOException {
        final AggregationState aggregationState = MgnlContext.getAggregationState();
        final Node mainContent = aggregationState.getMainContent().getJCRNode();
        boolean isGranted;
        try {
            isGranted = PermissionUtil.isGranted(mainContent.getSession(), mainContent.getPath(), Permission.SET);
        } catch (RepositoryException e) {
            // TODO dlipp - apply consistent ExceptionHandling
            throw new RuntimeException(e);
        }
        if (!aggregationState.isPreviewMode() && isGranted && ServerConfiguration.getInstance().isAdmin()) {
            this.setEvent("onmousedown", "mgnlMoveNodeEnd(this,'" + this.getPath() + "');");
            this.setEvent("onmouseover", "mgnlMoveNodeHigh(this);");
            this.setEvent("onmouseout", "mgnlMoveNodeReset(this);");
            println(out, getHtml());
        }
    }

    public String getParagraph() {
        return this.paragraph;
    }

    public void setParagraph(String paragraph) {
        this.paragraph = paragraph;
    }
}
