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
package info.magnolia.templating.editor.client.model.focus;

import info.magnolia.templating.editor.client.dom.MgnlElement;
import info.magnolia.templating.editor.client.model.ModelStorage;

import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;

/**
 * Helper class to keep tack on selected items.
 */
public class FocusModelImpl2 implements FocusModel {

    private ModelStorage storage;

    public FocusModelImpl2(ModelStorage storage) {
        super();
        this.storage = storage;
    }

    @Override
    public void onMouseUp(Element element) {

    }

    @Override
    public void onMouseUp(MgnlElement mgnlElement) {

        hideRoot();
        if (storage.getSelectedMgnlElement() != null) {
            deSelect(storage.getSelectedMgnlElement());
        }
        select(mgnlElement);

        storage.setSelectedMgnlElement(mgnlElement);
    }


    @Override
    public void onMouseDown(Element element) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reset() {
        deSelect();
        showRoot();
        computeOverlay();
    }

    protected void select(MgnlElement mgnlElement) {

        if (mgnlElement != null) {

                if (storage.getOverlay(mgnlElement) != null) {
                    storage.getOverlay(mgnlElement).getElement().getStyle().setProperty("pointerEvents", "none");
                }
                    if (storage.getEditBar(mgnlElement) != null) {
                        storage.getEditBar(mgnlElement).setVisible(true);
                    }

                for (MgnlElement component : mgnlElement.getComponents()) {
                    if (storage.getOverlay(component) != null) {
                        storage.getOverlay(component).getElement().getStyle().setProperty("pointerEvents", "none");
                    }
                    if (component != null && storage.getEditBar(component) != null) {
                        storage.getEditBar(component).setVisible(true);
                    }

                }
                for (MgnlElement parentArea = mgnlElement.getParentArea(); parentArea != null; parentArea= parentArea.getParentArea()) {
                    if (storage.getOverlay(parentArea) != null) {
                        storage.getOverlay(parentArea).getElement().getStyle().setProperty("pointerEvents", "none");

                    }
                }
                computeOverlay();
           }
    }

    public void deSelect() {
        if (storage.getSelectedMgnlElement() != null) {
            deSelect(storage.getSelectedMgnlElement());
        }
    }

    public void deSelect(MgnlElement mgnlElement) {
        mgnlElement = mgnlElement.getRoot();
        if (mgnlElement != null) {

            if (storage.getOverlay(mgnlElement) != null) {
                storage.getOverlay(mgnlElement).getElement().getStyle().clearProperty("pointerEvents");

            }
            for (MgnlElement descendant : mgnlElement.getDescendants()) {
                if (storage.getOverlay(descendant) != null) {
                    storage.getOverlay(descendant).getElement().getStyle().clearProperty("pointerEvents");

                }
                if (storage.getEditBar(descendant) != null) {
                    if (!descendant.getParent().equals(mgnlElement)) {
                        storage.getEditBar(descendant).setVisible(false);
                    }

                }

            }
            computeOverlay();

        }
    }

    public void showRoot() {
        for (MgnlElement root : storage.getRootElements()) {
            if (root != null && storage.getEditBar(root) != null) {
                storage.getEditBar(root).setVisible(true);

            }
        }
    }
    public void hideRoot() {
        for (MgnlElement root : storage.getRootElements()) {
            if (root != null && storage.getEditBar(root) != null) {
                storage.getEditBar(root).setVisible(false);

            }
        }
    }

    public void computeOverlay () {
        for (MgnlElement root : storage.getRootElements()) {
            List<MgnlElement> mgnlElements = root.getDescendants();
            mgnlElements.add(root);
            for (MgnlElement mgnlElement : mgnlElements) {

                if (storage.getOverlay(mgnlElement) == null) {
                    continue;
                }

                Element firstElement = mgnlElement.getFirstElement();
                if (firstElement == null) {
                    continue;
                }
                storage.getOverlay(mgnlElement).getElement().getStyle().setTop(firstElement.getAbsoluteTop(), Unit.PX);
                storage.getOverlay(mgnlElement).getElement().getStyle().setLeft(firstElement.getAbsoluteLeft(), Unit.PX);
                storage.getOverlay(mgnlElement).getElement().getStyle().setWidth(firstElement.getAbsoluteRight() - firstElement.getAbsoluteLeft(), Unit.PX);

                Element lastElement = mgnlElement.getLastElement();
                if (lastElement != null) {
                    storage.getOverlay(mgnlElement).getElement().getStyle().setHeight(lastElement.getAbsoluteBottom() - storage.getOverlay(mgnlElement).getElement().getAbsoluteTop(), Unit.PX);
                }
            }

/*            for (MgnlElement mgnlElement : mgnlElements) {

                if (storage.getOverlay(mgnlElement) == null) {
                    continue;
                }

                double top = Double.MAX_VALUE;
                double bottom = 0;
                double left = Double.MAX_VALUE;
                double right = 0;

                for (Element element : storage.getElements(mgnlElement)) {
                    if (top > element.getAbsoluteTop()) top = element.getAbsoluteTop();
                    if (bottom < element.getAbsoluteBottom()) bottom = element.getAbsoluteBottom();
                    if (left > element.getAbsoluteLeft()) left = element.getAbsoluteLeft();
                    if (right < element.getAbsoluteRight()) right = element.getAbsoluteRight();
                }

                storage.getOverlay(mgnlElement).getElement().getStyle().setTop(top, Unit.PX);
                storage.getOverlay(mgnlElement).getElement().getStyle().setLeft(left, Unit.PX);
                storage.getOverlay(mgnlElement).getElement().getStyle().setWidth(right - left, Unit.PX);
                storage.getOverlay(mgnlElement).getElement().getStyle().setHeight(bottom - top, Unit.PX);

            }*/
        }
    }

    @Override
    public void toggleRootSelection() {
        // TODO Auto-generated method stub

    }

    @Override
    public void toggleSelection(MgnlElement mgnlElement, boolean visible) {
    }

}
