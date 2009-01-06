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
package info.magnolia.freemarker;

import freemarker.cache.TemplateLoader;

import java.io.IOException;
import java.io.Reader;

/**
 * An abstract TemplateLoader that will attempt to initialize its delegate TemplateLoader
 * and use a nullobject implementation until it succeeds.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractDelegatingTemplateLoader implements TemplateLoader {
    private static final TemplateLoader NULL = new NullTemplateLoader();

    private TemplateLoader delegate;

    public Object findTemplateSource(String name) throws IOException {
        return getDelegate().findTemplateSource(name);
    }

    public long getLastModified(Object templateSource) {
        return getDelegate().getLastModified(templateSource);
    }

    public Reader getReader(Object templateSource, String encoding) throws IOException {
        return getDelegate().getReader(templateSource, encoding);
    }

    public void closeTemplateSource(Object templateSource) throws IOException {
        getDelegate().closeTemplateSource(templateSource);
    }

    /**
     * First attempts to initialize a new delegate if needed. If it doesn't succeed, use the NullTemplateLoader.
     */
    protected TemplateLoader getDelegate() {
        if (delegate == null) {
            delegate = newDelegate();
        }
        if (delegate == null) {
            return NULL;
        }
        return delegate;
    }

    /**
     * Implementations should return null if it is to early to instanciate the delegate.
     */
    protected abstract TemplateLoader newDelegate();

    private static class NullTemplateLoader implements TemplateLoader {
        public Object findTemplateSource(String name) throws IOException {
            return null;
        }

        public long getLastModified(Object templateSource) {
            return -1;
        }

        public Reader getReader(Object templateSource, String encoding) throws IOException {
            return null;
        }

        public void closeTemplateSource(Object templateSource) throws IOException {
        }
    }
}
