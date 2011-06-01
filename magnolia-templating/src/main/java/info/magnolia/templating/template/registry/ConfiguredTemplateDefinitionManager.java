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
package info.magnolia.templating.template.registry;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeTypeFilter;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ObservedManager for {@link TemplateDefinition} configured in repository.
 */
public class ConfiguredTemplateDefinitionManager extends ObservedManager {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final Set<String> registeredIds = new HashSet<String>();
    private TemplateDefinitionRegistry templateDefinitionRegistry;

    public ConfiguredTemplateDefinitionManager(TemplateDefinitionRegistry registry) {
        this.templateDefinitionRegistry = registry;
    }

    @Override
    protected void onRegister(Content node) {
        // TODO use the jcr api

        try {
            ContentUtil.visit(node, new ContentUtil.Visitor() {

                @Override
                public void visit(Content node) throws Exception {
                    for (Content templateDefinitionNode : node.getChildren(ItemType.CONTENTNODE)) {
                        registerTemplateDefinition(templateDefinitionNode);
                    }
                }
            }, new NodeTypeFilter(ItemType.CONTENT));
        }
        catch (Exception e) {
            throw new RuntimeException("Can't register template definitions defined at " + node, e);
        }
    }

    protected void registerTemplateDefinition(Content templateDefinitionNode) {
        final String path = templateDefinitionNode.getHandle();
        final String[] pathElements = path.split("/");
        final String moduleName = pathElements[2];
        final String id = moduleName + ":" + StringUtils.removeStart(path, "/modules/" + moduleName + "/templates/");

        synchronized (registeredIds) {
            try {
                ConfiguredTemplateDefinitionProvider templateDefinitionProvider = new ConfiguredTemplateDefinitionProvider(templateDefinitionNode);
                templateDefinitionRegistry.registerTemplateDefinition(id, templateDefinitionProvider);
                this.registeredIds.add(id);
            } catch (IllegalStateException e) {
                log.error("Unable to register template definition [" + id + "]", e);
            }
        }
    }

    @Override
    protected void onClear() {
        synchronized (registeredIds) {
            for (String id : registeredIds) {
                templateDefinitionRegistry.unregister(id);
            }
            this.registeredIds.clear();
        }
    }
}
