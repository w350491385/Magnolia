/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.freemarker;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.freemarker.models.MagnoliaModelFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Observed bean holding Freemarker configuration. Not to be confused with
 * Freemarker's own freemarker.template.Configuration. This only exposes the few
 * properties that Magnolia allows to configure and is able to handle properly.
 * It also provides a few additional methods used internally.
 * 
 * @see info.magnolia.freemarker.FreemarkerHelper
 * @see info.magnolia.freemarker.models.MagnoliaObjectWrapper
 * @see info.magnolia.freemarker.models.MagnoliaModelFactory
 */
public class FreemarkerConfig {
    public static FreemarkerConfig getInstance() {
        return (FreemarkerConfig) FactoryUtil.getSingleton(FreemarkerConfig.class);
    }

    /**
     * The MagnoliaModelFactory implementations explicitely registered by modules.
     */
    private  List registeredModelFactories;

    private List templateLoaders;
    private MultiTemplateLoader multiTL;

    public FreemarkerConfig() {
        this.registeredModelFactories = new ArrayList();
        this.templateLoaders = new ArrayList();

        // There is a bit of a messy dependency here: 
        // Ultimately, FreemarkerHelper and FreemarkerConfig could be merged,
        // but since FreemarkerHelper could be kept around, we'll first need
        // to implement FactoryUtil so that the observed components are wrapped
        // by a proxy.
        FreemarkerHelper.getInstance().resetObjectWrapper();
    }

    // public init() { would be called by content2bean if needed. }

    protected TemplateLoader getMultiTemplateLoader() {
        if (multiTL == null) {
            // add a ClassTemplateLoader as our last loader
            final int s = templateLoaders.size();
            final TemplateLoader[] tl = (TemplateLoader[]) templateLoaders.toArray(new TemplateLoader[s + 1]);
            tl[s] = new ClassTemplateLoader(getClass(), "/");
            multiTL = new MultiTemplateLoader(tl);
        }
        return multiTL;
    }

    public void setModelFactories(List registeredModelFactories) {
        this.registeredModelFactories = registeredModelFactories;
    }

    public List getModelFactories() {
        return registeredModelFactories;
    }

    public void addModelFactory(MagnoliaModelFactory modelFactory) {
        this.registeredModelFactories.add(modelFactory);
    }

    public List getTemplateLoaders() {
        return templateLoaders;
    }

    public void setTemplateLoaders(List templateLoaders) {
        this.templateLoaders = templateLoaders;
    }

    public void addTemplateLoader(TemplateLoader templateLoader) {
        this.templateLoaders.add(templateLoader);
    }

}
