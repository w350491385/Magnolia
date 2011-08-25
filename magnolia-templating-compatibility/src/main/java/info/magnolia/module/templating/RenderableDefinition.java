/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.module.templating;

import info.magnolia.cms.core.Content;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Abstract rendering definition used for templates and paragraphs.
 *
 * @version $Id$
 * @deprecated since 4.5, replaced by {@link info.magnolia.rendering.template.configured.RenderableDefinition}
 */
@Deprecated
public interface RenderableDefinition extends info.magnolia.rendering.template.RenderableDefinition {
    @Override
    public String getName();

    public String getType();

    @Override
    public String getTitle();

    @Override
    public String getDescription();

    @Override
    public String getI18nBasename();

    public String getTemplatePath();

    public String getDialog();

    /**
     * An arbitrary list of parameters. Used to omit subclass with getters and setters for each extra parameter.
     */
    @Override
    public Map getParameters();

    /**
     * Create the model based on the current content.
     */
    public RenderingModel newModel(Content content, RenderableDefinition definition, RenderingModel parentModel) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException;

    /**
     * The modules execute() method can return a string which is passed to this method to determine the template to use.
     */
    public String determineTemplatePath(String actionResult, RenderingModel model);
}
