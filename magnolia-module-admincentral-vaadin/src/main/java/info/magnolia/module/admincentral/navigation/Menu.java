/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.navigation;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.security.Permission;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.admincentral.AdminCentralVaadinModule;
import info.magnolia.module.admincentral.dialog.DialogSandboxPage;
import info.magnolia.module.admincentral.views.IFrameView;
import info.magnolia.objectfactory.Classes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.navigator.Navigator;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.themes.BaseTheme;


/**
 * The Application accordion Menu.
 * @author fgrilli
 *
 */
public class Menu extends Accordion {

    private static final long serialVersionUID = 1L;
    private static final String SELECTED_ACTION_IS_NOT_AVAILABLE_IN_MILESTONE = "Selected action is not available in Milestone 1";

    private static final Logger log = LoggerFactory.getLogger(Menu.class);
    private Navigator navigator;
    private final Map<Tab, MenuItemConfiguration> menuItems = new HashMap<Tab, MenuItemConfiguration>();
    private final Map<Tab, String> menuItemKeys = new HashMap<Tab, String>();

    public Menu(Navigator navigator) throws RepositoryException {
        this.navigator = navigator;
    }
    /**
     * See {@link com.vaadin.ui.AbstractComponent#getApplication()} javadoc as to why we need to do most of the initialization here and not in the constructor.
     */
    @Override
    public void attach() {
        super.attach();

        final Map<String, MenuItemConfiguration> menuConfig = ((AdminCentralVaadinModule) ModuleRegistry.Factory.getInstance().getModuleInstance("admin-central")).getMenuItems();

        for (Entry<String, MenuItemConfiguration> menuItemEntry : menuConfig.entrySet()) {
            MenuItemConfiguration menuItem = menuItemEntry.getValue();
            // check permission
            if (!isMenuItemRenderable(menuItem)) {
                continue;
            }

            // register new top level menu
            addTab(menuItemEntry.getKey(), menuItem);

        }
        //TODO for testing only. To be removed.
        MenuItemConfiguration testDialogsMenu = new MenuItemConfiguration();
        testDialogsMenu.setLabel("Dialogs");
        AdminCentralAction testDialogMenuAction= new AdminCentralAction("DialogsMA") {

            @Override
            public void handleAction(Object sender, Object target) {
                log.error("Supposed to handle something from {} to {}, but have been told to do nothing :(", sender, target);
            }
        };
        testDialogsMenu.setView(DialogSandboxPage.class.getName());
        testDialogsMenu.setAction(testDialogMenuAction);
        addTab("testDialogs", testDialogsMenu);

        // register trigger for menu actions ... sucks but TabSheet doesn't support actions for tabs only for sub menu items
        addListener(new SelectedMenuItemTabChangeListener());
    }


    /**
     * The only way to add tabs to Magnolia menu - ensure he have references to all items.
     * @param menuItemKey unique menu item key. The key is used for bookmarking and IS visible to the end users ... take care
     * @param menuItem menu item configuration entry
     */
    public void addTab(String menuItemKey, MenuItemConfiguration menuItem) {
        // layout for sub menu entries
        Component subMenu = addSubMenuItemsIntoLayout(menuItem);
        Tab tab = super.addTab(subMenu == null ? new Label() : subMenu, getLabel(menuItem), getIcon(menuItem));
        // store tab reference
        this.menuItems.put(tab, menuItem);
        this.menuItemKeys.put(tab, menuItemKey);

        // navigator needs to register views.
        setupAndRegisterView(navigator, menuItemKey, menuItem);

    }

    @Override
    public Tab addTab(Component c) {
        throw new UnsupportedOperationException("Use addTab(String, MenuItemConfiguration) instead.");
    };

    @Override
    public Tab addTab(Component c, String caption, Resource icon) {
        throw new UnsupportedOperationException("Use addTab(String, MenuItemConfiguration) instead.");
    }

    /**
     * Binds the given key with the view devised from menu item configuration.
     * @param navigator Bindings manager.
     * @param menuKey Unique menu item key.
     * @param menuItem Menu item configuration.
     */
    private void setupAndRegisterView(Navigator navigator, String menuKey, MenuItemConfiguration menuItem){
        final String view = menuItem.getView();
        Class viewClass = null;
        // check if view is not a simple html redirect only
        if (!StringUtils.isBlank(menuItem.getViewTarget())) {
           viewClass = IFrameView.class;
        } else {
            try {
                // custom view class
                viewClass = Classes.getClassFactory().forName(view);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            log.info("Registering navigator view ['{}', {}]",menuKey, viewClass);
            try {
                navigator.addView(menuKey, viewClass);
            } catch (IllegalArgumentException e) {
                log.error("Failed to register view for " + menuKey + ". View class " + viewClass + " is already registered.");
            }
        }
    }

    /**
     * Iterates over sub menu entries and adds them to the layout.
     * @return Component with all relevant sub menu entries or null when none exists.
     */
    private Component addSubMenuItemsIntoLayout(MenuItemConfiguration menuItem) {
        if (menuItem.getMenuItems().isEmpty()) {
            return null;
        }
        final GridLayout layout = new GridLayout(1,1);
        layout.setSpacing(true);
        layout.setMargin(true);
        // sub menu items (2 levels only)
        for (MenuItemConfiguration sub :  menuItem.getMenuItems().values()) {
            if (isMenuItemRenderable(sub)) {
                layout.addComponent(new MenuItem(sub));
            }
        }

        return layout;
    }

    /**
     * Converts label key into i18n-ized string.
     */
    protected String getLabel(MenuItemConfiguration menuItem) {
        return menuItem.getMessages().getWithDefault(menuItem.getLabel(), menuItem.getLabel());
    }

    protected Resource getIcon(MenuItemConfiguration menuItem){
//        // TODO: why isn't external resource working? Urls?
//        return menuItem.getAction().getIcon();

        // TODO: why do we have to replace ==> because we do not go via filter chain here
        if (menuItem.getIcon() == null) {
            return null;
        }
        return new ExternalResource(MgnlContext.getContextPath() + menuItem.getIcon());
    }

    /**
     * @param menuItem
     * @return <code>true</code> if the the current user is granted access to this menu item, <code>false</code> otherwise
     */
    protected boolean isMenuItemRenderable(MenuItemConfiguration menuItem) {
        return MgnlContext.getAccessManager(ContentRepository.CONFIG).isGranted(menuItem.getLocation(), Permission.READ);
    }

    /**
     * Menu item button implementation.
     * @author fgrilli
     *
     */
    //TODO extract this as a top level class?
    public class MenuItem extends Button{
        private static final long serialVersionUID = 1L;
        private MenuItemConfiguration item;

        public MenuItem(final MenuItemConfiguration item) {
            this.item = item;
        }

        /**
         * See {@link com.vaadin.ui.AbstractComponent#getApplication()} javadoc as to why we need to do most of the initialization here and not in the constructor.
         */
        @Override
        public void attach() {
            Resource icon = Menu.this.getIcon(item);
            if (icon != null) {
                setIcon(icon);
            }
            setCaption(Menu.this.getLabel(item));
            super.attach();
            setStyleName(BaseTheme.BUTTON_LINK);
            setHeight(20f, Button.UNITS_PIXELS);

            final AdminCentralAction action = item.getAction();
            log.info("Attaching action {} to menu {}", action != null ? action.getCaption() : "null", item.getLabel());
            this.addListener(new ClickListener() {

                public void buttonClick(ClickEvent event) {
                    if (action == null ) {
                        return;
                    }
                    action.handleAction(SELECTED_ACTION_IS_NOT_AVAILABLE_IN_MILESTONE, getApplication());
                }
            });
            // this ain't working
            //super.getActionManager().addAction(action);

       }
    }

    /**
     * Trigger for all menu actions.
     * @author fgrilli
     *
     */
    public class SelectedMenuItemTabChangeListener implements SelectedTabChangeListener {

        private static final long serialVersionUID = 1L;

        public SelectedMenuItemTabChangeListener() {
        }

        public void selectedTabChange(SelectedTabChangeEvent event) {
            TabSheet tabsheet = event.getTabSheet();
            Tab tab = tabsheet.getTab(tabsheet.getSelectedTab());
            if (tab != null) {
                //TODO this is possibly how we will wire up navigator into our menu. Just need to know how to retrieve the correct view based on the clicked item.
                MenuItemConfiguration item = menuItems.get(tab);
                //Just give the navigator the uri fragment and it will know where to go and which View to instantiate
                String uriFragment = menuItemKeys.get(tab);

                Object message;
                Class view = navigator.getViewClass(uriFragment);
                if (view == null || IFrameView.class.equals(view)) {
                    message = SELECTED_ACTION_IS_NOT_AVAILABLE_IN_MILESTONE;
                } else {
                    message = null;
                }
                item.getAction().handleAction(message, getApplication());

                navigator.navigateTo(uriFragment);

            }
        }
    }
}

