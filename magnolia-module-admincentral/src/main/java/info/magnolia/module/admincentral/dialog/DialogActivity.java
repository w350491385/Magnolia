/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.module.admincentral.dialog;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.module.admincentral.RuntimeRepositoryException;
import info.magnolia.module.admincentral.editor.ContentDriver;
import info.magnolia.module.admincentral.editor.vaadin.VaadinDialog;
import info.magnolia.module.admincentral.editor.vaadin.VaadinDialogBuilder;
import info.magnolia.module.admincentral.jcr.JCRUtil;
import info.magnolia.module.admincentral.model.UIModel;
import info.magnolia.module.vaadin.activity.AbstractActivity;
import info.magnolia.module.vaadin.component.HasComponent;
import info.magnolia.module.vaadin.event.EventBus;

/**
 * Activity for dialogs.
 *
 * @author tmattsson
 */
public class DialogActivity extends AbstractActivity implements VaadinDialog.Presenter {

    private DialogPlace place;
    private UIModel uiModel;
    private ContentDriver driver;

    public DialogActivity(DialogPlace place, UIModel uiModel) {
        this.place = place;
        this.uiModel = uiModel;
    }

    public void start(HasComponent display, EventBus eventBus) {
        try {

            Node node = getNode();

            String dialogName = place.getDialogName();
            DialogDefinition dialogDefinition = uiModel.getDialogDefinition(dialogName);

            VaadinDialogBuilder builder = new VaadinDialogBuilder();
            VaadinDialog dialog = builder.getDialog();

            driver = new ContentDriver();
            driver.initialize(builder, dialogDefinition);
            driver.edit(node);

            dialog.setPresenter(this);

            display.setComponent(dialog);

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    private Node getNode() throws RepositoryException {
        return (Node) JCRUtil.getSession("users").getItem(this.place.getPath());
    }

    public String mayStop() {
        return "You might have unsaved changes, do you really want to leave this page?";
    }

    public void onSave() {
        try {
            driver.flush(getNode());

            // TODO validation errors that occurred should be displayed here

            // TODO if there was no errors then the dialog should close and we return to the previous place

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public void onCancel() {
    }
}
