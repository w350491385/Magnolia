/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.workflow.setup;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.admininterface.setup.AddMainMenuItemTask;
import info.magnolia.module.admininterface.setup.AddSubMenuItemTask;
import info.magnolia.module.admininterface.trees.WebsiteTreeConfiguration;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BackupTask;
import info.magnolia.module.delta.BootstrapResourcesTask;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.IsModuleInstalledOrRegistered;
import info.magnolia.module.delta.ModuleDependencyBootstrapTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.WarnTask;
import info.magnolia.module.workflow.setup.for3_5.AddNewDefaultConfig;
import info.magnolia.module.workflow.setup.for3_5.AddUserToGroupTask;
import info.magnolia.module.workflow.setup.for3_5.CheckAndUpdateDefaultWorkflowDefinition;
import info.magnolia.module.workflow.setup.for3_5.RemoveMetadataFromExpressionsWorkspace;
import info.magnolia.module.workflow.setup.for3_5.SetDefaultWorkflowForActivationFlowCommands;
import info.magnolia.module.workflow.setup.for4_0.AddSystemUserToGroupTask;
import info.magnolia.module.workflow.trees.WorkflowWebsiteTreeConfiguration;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class WorkflowModuleVersionHandler extends DefaultModuleVersionHandler {
    private static final String BACKUP_PATH = "/server/install/backup";
    private final Task inboxMenu = new AddMainMenuItemTask("inbox", "menu.inbox", "info.magnolia.module.workflow.messages",
            "MgnlAdminCentral.showContent('/.magnolia/pages/inbox.html', false, false)", "/.resources/icons/24/mail.gif",
            "security");
    private final Task flowsPageMenu = new AddSubMenuItemTask("config", "workflows", "menu.config.workflows",
            "MgnlAdminCentral.showContent('/.magnolia/pages/flows.html');", "/.resources/icons/16/dot.gif");

    private final Task bootstrapSecurityConfig = new BootstrapResourcesTask("New ACL configuration", "Bootstraps the new configuration for the ACL dialogs") {
        protected String[] getResourcesToBootstrap(final InstallContext installContext) {
            return new String[]{
                    "/mgnl-bootstrap/workflow/config.modules.adminInterface.config.securityConfiguration.repositories.Expressions.xml",
                    "/mgnl-bootstrap/workflow/config.modules.adminInterface.config.securityConfiguration.repositories.Store.xml"
            };
        }
    };

    private Task changeWebsiteTreeConfigurationTask = new CheckAndModifyPropertyValueTask("Website tree configuration", "Modifies the website tree configuration so that a message window pops up for activation.",
        ContentRepository.CONFIG,
        "/modules/adminInterface/trees/website",
        "configurationClass",
        WebsiteTreeConfiguration.class.getName(),
        WorkflowWebsiteTreeConfiguration.class.getName());

    private Task changeDMSTreeConfigurationTask = new IsModuleInstalledOrRegistered(
        "DMS tree configuration", "Modifies the dms tree configuration so that a comment window pops up for activation.",
        "dms",
        new CheckAndModifyPropertyValueTask("DMS tree configuration", "Modifies the dms tree configuration so that a comment window pops up for activation.",
            ContentRepository.CONFIG,
            "/modules/dms/trees/dms",
            "configurationClass",
            // can't add a dependency to the dms so must use the class names
            "info.magnolia.module.dms.DMSAdminTreeConfig",
            "info.magnolia.module.dms.WorkflowDMSAdminTreeConfig"
    ));


    public WorkflowModuleVersionHandler() {
        final Delta delta35 = DeltaBuilder.update("3.5", "")
                .addTask(inboxMenu)
                .addTask(flowsPageMenu)
                .addTask(new BootstrapSingleResource("Bootstrap", "Bootstraps the Workflow Utils page.", "/mgnl-bootstrap/workflow/config.modules.workflow.pages.flows.xml"))
                .addTask(new BootstrapSingleResource("Bootstrap", "Bootstraps the Inbox Subpages.", "/mgnl-bootstrap/workflow/config.modules.workflow.pages.inboxSubPages.xml"))
                // TODO refactor the following ArrayDelegateTask into a single one: BackupAndBootstrap("resource.xml", "/backup/location", "Message", MessageType.WARNING), will be fixed with MAGNOLIA-1945
                .addTask(new ArrayDelegateTask("Backup and Bootstrap", "Makes a backup of the current 'EditWorkItem' dialog and re-installs it.", new Task[] {
                        new BackupTask(ContentRepository.CONFIG, "/modules/workflow/dialogs/editWorkItem", true),
                        new BootstrapSingleResource("Bootstrap", "Bootstraps the Inbox Subpages.", "/mgnl-bootstrap/workflow/config.modules.workflow.dialogs.editWorkItem.xml", ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW),
                        new WarnTask("Note: ", "Note that the 'EditWorkItem' dialog was re-installed. Magnolia put a backup of original dialog into '" + BACKUP_PATH + "/editWorkItem'. Please review the changes manually.")
                    }))
                .addTask(new BootstrapSingleResource("Bootstrap", "Bootstraps the 'EditActivationWorkItem' dialog", "/mgnl-bootstrap/workflow/config.modules.workflow.dialogs.editActivationWorkItem.xml"))
                .addTask(new AddNewDefaultConfig())
                .addTask(new CheckAndUpdateDefaultWorkflowDefinition())
                .addTask(new RemoveMetadataFromExpressionsWorkspace())
                .addTask(new SetDefaultWorkflowForActivationFlowCommands())
                .addTask(bootstrapSecurityConfig)
                // TODO we might want to backup the following configurations before overwriting them, as well.
                .addTask(new BootstrapSingleResource("Bootstrap", "Bootstraps the 'editors' group.", "/mgnl-bootstrap/workflow/usergroups.editors.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING))
                .addTask(new BootstrapSingleResource("Bootstrap", "Bootstraps the 'publishers' group.", "/mgnl-bootstrap/workflow/usergroups.publishers.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING))
                .addTask(new BootstrapSingleResource("Bootstrap", "Bootstraps the 'workflow-base' role.", "/mgnl-bootstrap/workflow/userroles.workflow-base.xml"));
        register(delta35);

        register(DeltaBuilder.update("3.6", "")
                .addTask(changeWebsiteTreeConfigurationTask)
                .addTask(changeDMSTreeConfigurationTask)
                .addTask(new BootstrapSingleResource("Comment on activation dialog", "Registers the dialog for activation comments.", "/mgnl-bootstrap/workflow/config.modules.workflow.dialogs.startActivationWorkflow.xml"))
                .addTask(new IsModuleInstalledOrRegistered("Sample users and groups", "Adds sample users to sample editors and publishers group, if the sample module is installed or registered.",
                        "samples", new ArrayDelegateTask("",
                        new AddUserToGroupTask("Sample user", "eve", "editors"),
                        new AddUserToGroupTask("Sample user", "patrick", "publishers"))
                )));

        register(DeltaBuilder.update("4.0", "")
                .addTask(new AddSystemUserToGroupTask("Superuser", "publishers")
                ));
    }

    protected List getExtraInstallTasks(InstallContext ctx) {
        final List tasks = new ArrayList();
        tasks.add(new ModuleDependencyBootstrapTask("dms"));
        tasks.add(inboxMenu);
        tasks.add(flowsPageMenu);
        tasks.add(new InstallWorkflowDefinitionTask("Setup default activation workflow definition", "Adds the default activation workflow definition under the /modules/workflow/config/flows/activation config node.",
                "activation", "info/magnolia/module/workflow/default-activation-workflow.xml"));
        tasks.add(changeWebsiteTreeConfigurationTask);
        tasks.add(changeDMSTreeConfigurationTask);

        if (ctx.isModuleRegistered("samples")) {
            tasks.add(new AddUserToGroupTask("Sample user", "eve", "editors"));
            tasks.add(new AddUserToGroupTask("Sample user", "patrick", "publishers"));
        }
        tasks.add(new AddSystemUserToGroupTask("Superuser", "publishers"));

        return tasks;
    }

}
