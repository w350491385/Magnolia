/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.ui.admincentral.navigation;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.model.navigation.definition.NavigationDefinition;
import info.magnolia.ui.model.navigation.definition.NavigationItemDefinition;
import info.magnolia.ui.model.navigation.definition.NavigationWorkareaDefinition;
import info.magnolia.ui.model.navigation.registry.NavigationPermissionSchema;
import info.magnolia.ui.model.navigation.registry.NavigationRegistry;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * The Application accordion Menu.
 * TODO Add simple animation to make it look nicer.
 * @author fgrilli
 *
 */
// FIXME don't extend CustomComponent, make it composite.
public class NavigationViewImpl extends CustomComponent implements NavigationView, IsVaadinComponent{

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(NavigationViewImpl.class);

    private VerticalLayout outerNavigationContainer = new VerticalLayout();
    private Presenter presenter;
    private Map<WorkbenchChooser, NavigationWorkArea> registeredNavigationAreas = new HashMap<WorkbenchChooser, NavigationWorkArea>();

    //TODO don't pass the registry but the navigation itself
    public NavigationViewImpl(NavigationRegistry navigationRegistry, NavigationPermissionSchema permissions) {
        setCompositionRoot(outerNavigationContainer);
        setSizeFull();

        final HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false, false, true, false);

        final VerticalLayout navigationWorkareaContainer = new VerticalLayout();

        final NavigationDefinition navigation = navigationRegistry.getNavigation();

        for(NavigationWorkareaDefinition definition : navigation.getWorkareas()){
            List<NavigationGroup> groups = new ArrayList<NavigationGroup>();

            for (NavigationItemDefinition navigationItem : definition.getItems()) {
                groups.add(new NavigationGroup(navigationItem, permissions));
            }

            final WorkbenchChooser button = new WorkbenchChooser(definition);
            buttons.addComponent(button);

            final NavigationWorkArea wa = new NavigationWorkArea(groups);

            if(definition.isVisible()){
                wa.setVisible(true);
            }
            registeredNavigationAreas.put(button, wa);
            navigationWorkareaContainer.addComponent(wa);
        }
        outerNavigationContainer.addComponent(buttons);
        outerNavigationContainer.addComponent(navigationWorkareaContainer);
    }


    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;

    }

    public Component asVaadinComponent() {
        return this;
    }

    public class WorkbenchChooser extends Button {

        private static final long serialVersionUID = 1L;

        public WorkbenchChooser(final NavigationWorkareaDefinition definition) {
            addListener(new ClickListener() {
                public void buttonClick(ClickEvent event) {
                    for(NavigationWorkArea wa : registeredNavigationAreas.values()){
                        wa.setVisible(false);
                    }
                    NavigationWorkArea current = registeredNavigationAreas.get(event.getButton());
                    current.setVisible(true);
                    presenter.onMenuSelection(definition);
                }
            });
            String icon = definition.getIcon();
            if(StringUtils.isNotBlank(icon)) {
                setIcon(new ExternalResource(MgnlContext.getContextPath() + icon));
            }
        }
    }
    public class WorkbenchChooserClickListener implements ClickListener {

        private static final long serialVersionUID = 1L;
        private NavigationWorkareaDefinition definition;
        public WorkbenchChooserClickListener(final NavigationWorkareaDefinition definition) {
            this.definition = definition;
        }

        public void buttonClick(ClickEvent event) {
            for(NavigationWorkArea wa : registeredNavigationAreas.values()){
                wa.setVisible(false);
            }
            NavigationWorkArea current = registeredNavigationAreas.get(event.getButton());
            current.setVisible(true);
            presenter.onMenuSelection(definition);
        }
    }
}
