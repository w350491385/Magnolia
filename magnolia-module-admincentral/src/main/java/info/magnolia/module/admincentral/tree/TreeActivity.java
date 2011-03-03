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
package info.magnolia.module.admincentral.tree;

import info.magnolia.module.admincentral.RuntimeRepositoryException;
import info.magnolia.module.admincentral.event.ContentChangedEvent;
import info.magnolia.module.admincentral.event.ContentChangedEvent.Handler;
import info.magnolia.module.admincentral.model.UIModel;
import info.magnolia.module.admincentral.place.ItemSelectedPlace;
import info.magnolia.ui.activity.AbstractActivity;
import info.magnolia.ui.component.HasComponent;
import info.magnolia.ui.event.EventBus;
import info.magnolia.ui.place.PlaceController;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

/**
 * TODO: write javadoc.
 *
 * @author tmattsson
 *
 */
public class TreeActivity extends AbstractActivity implements TreeView.Presenter, Handler {

    private final String treeName;
    private EventBus eventBus;
    private PlaceController placeController;
    // FIXME use the interface not the implementation
    private TreeView treeView;
    private UIModel uiModel;
    private String path;

    public TreeActivity(String treeName, String path, PlaceController placeController, UIModel uiModel) {
        this.uiModel = uiModel;
        this.treeName = treeName;
        this.path = path;
        this.placeController = placeController;
    }

    // TODO is this good practice?
    public void update(String path){
        if(!this.path.equals(path)){
            this.path = path;
            treeView.select(path);
        }
    }

    public void start(HasComponent display, EventBus eventBus) {
        this.eventBus = eventBus;
        try {
            this.treeView = new TreeViewImpl(treeName, this, uiModel);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        treeView.select(path);
        eventBus.addHandler(ContentChangedEvent.class, this);
        display.setComponent(treeView.asComponent());
    }

    public UIModel getUIModel() {
        return uiModel;
    }

    public void onItemSelection(Item jcrItem) {
        try {
            String path = uiModel.getPathInTree(treeName, jcrItem);
            placeController.goTo(new ItemSelectedPlace(treeName, path));
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public void onContentChanged(ContentChangedEvent event) {
        // FIXME only if we are not the source!
        treeView.refresh();
    }

}