/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.admininterface.trees;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.gui.control.TreeMenuItem;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Handles the tree rendering for the "users" repository.
 * @author Fabrizio Giustina
 * @version $Id: AdminTreeUsers.java 685 2005-05-04 19:23:59Z philipp $
 */
public class AdminTreeUsers extends AdminTreeMVCHandler {

    /**
     * @param name
     * @param request
     * @param response
     */
    public AdminTreeUsers(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.AdminTree#prepareTree(info.magnolia.cms.gui.control.Tree,
     * javax.servlet.http.HttpServletRequest)
     */
    protected void prepareTree(Tree tree, HttpServletRequest request) {
        Messages msgs = MessagesManager.getMessages(request);

        tree.setDrawShifter(false);
        // context path is already added by Tree
        tree.setIconPage(Tree.ICONDOCROOT + "pawn_glass_yellow.gif");
        if (Server.isAdmin()) {
            tree.setIconOndblclick("mgnlTreeMenuOpenDialog("
                + tree.getJavascriptTree()
                //+ ",'.magnolia/adminCentral/users/dialog.html');");
                + ",'.magnolia/dialogx/useredit.html');");
        }
        tree.addItemType(ItemType.CONTENT);

        TreeColumn column0 = new TreeColumn(tree.getJavascriptTree(), request);
        column0.setIsLabel(true);
        if (Server.isAdmin()) {
            column0.setHtmlEdit();
        }
        column0.setTitle(msgs.get("tree.users.name"));
        column0.setWidth(2);
        TreeColumn column1 = new TreeColumn(tree.getJavascriptTree(), request);
        column1.setName("title");
        if (Server.isAdmin()) {
            column1.setHtmlEdit();
        }
        column1.setTitle(msgs.get("tree.users.fullname"));
        column1.setWidth(2);
        TreeColumn columnIcons = new TreeColumn(tree.getJavascriptTree(), request);
        columnIcons.setCssClass("");
        columnIcons.setWidth(1);
        columnIcons.setIsIcons(true);
        columnIcons.setIconsActivation(true);
        TreeColumn column2 = new TreeColumn(tree.getJavascriptTree(), request);
        column2.setName(MetaData.LAST_MODIFIED);
        column2.setIsMeta(true);
        column2.setDateFormat("yyyy-MM-dd, HH:mm");
        column2.setTitle(msgs.get("tree.users.date"));
        column2.setWidth(2);
        tree.addColumn(column0);
        tree.addColumn(column1);
        if (Server.isAdmin()) {
            tree.addColumn(columnIcons);
        }
        tree.addColumn(column2);
        TreeMenuItem menuOpen = new TreeMenuItem();
        menuOpen.setLabel(msgs.get("tree.users.menu.edit"));
        menuOpen.setOnclick("mgnlTreeMenuOpenDialog("
            + tree.getJavascriptTree()
            + ",'.magnolia/dialogx/user.html');");
        menuOpen.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        TreeMenuItem menuNewPage = new TreeMenuItem();
        menuNewPage.setLabel(msgs.get("tree.users.menu.new"));
        menuNewPage.setOnclick(tree.getJavascriptTree()
            + ".createRootNode('"
            + ItemType.CONTENT.getSystemName()
            + "');");
        TreeMenuItem menuDelete = new TreeMenuItem();
        menuDelete.setLabel(msgs.get("tree.users.menu.delete"));
        menuDelete.setOnclick(tree.getJavascriptTree() + ".deleteNode();");
        menuDelete.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        TreeMenuItem menuMove = new TreeMenuItem();
        menuMove.setLabel(msgs.get("tree.users.menu.move"));
        menuMove.setOnclick(tree.getJavascriptTree() + ".cutNode();");
        menuMove.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        TreeMenuItem menuCopy = new TreeMenuItem();
        menuCopy.setLabel(msgs.get("tree.users.menu.copy"));
        menuCopy.setOnclick(tree.getJavascriptTree() + ".copyNode();");
        menuCopy.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        TreeMenuItem menuActivateExcl = new TreeMenuItem();
        menuActivateExcl.setLabel(msgs.get("tree.users.menu.activate"));
        menuActivateExcl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",false);");
        menuActivateExcl.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        TreeMenuItem menuDeActivate = new TreeMenuItem();
        menuDeActivate.setLabel(msgs.get("tree.users.menu.deactivate"));
        menuDeActivate.setOnclick(tree.getJavascriptTree() + ".deActivateNode(" + Tree.ACTION_DEACTIVATE + ");");
        menuDeActivate.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        TreeMenuItem menuRefresh = new TreeMenuItem();
        menuRefresh.setLabel(msgs.get("tree.menu.refresh"));
        menuRefresh.setOnclick(tree.getJavascriptTree() + ".refresh();");
        if (Server.isAdmin()) {
            tree.addMenuItem(menuOpen);
            tree.addMenuItem(null); // line
            tree.addMenuItem(menuNewPage);
        }
        tree.addMenuItem(menuDelete);
        if (Server.isAdmin()) {
            tree.addMenuItem(null); // line
            tree.addMenuItem(menuActivateExcl);
            tree.addMenuItem(menuDeActivate);
        }
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuRefresh);

    }

}
