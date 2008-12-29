/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.cms.core.search;

import info.magnolia.cms.core.HierarchyManager;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;


/**
 * Date: Mar 29, 2005 Time: 2:54:21 PM
 * @author Sameer Charles
 */

public class QueryManagerImpl implements QueryManager {

    private javax.jcr.query.QueryManager queryManager;

    private HierarchyManager hm;

    protected QueryManagerImpl(javax.jcr.query.QueryManager queryManager, HierarchyManager hm) {
        this.queryManager = queryManager;
        this.hm = hm;
    }

    public Query createQuery(String s, String s1) throws InvalidQueryException, RepositoryException {
        javax.jcr.query.Query query = this.queryManager.createQuery(s, s1);
        return (new QueryImpl(query, this.hm));
    }

    public Query getQuery(Node node) throws InvalidQueryException, RepositoryException {
        javax.jcr.query.Query query = this.queryManager.getQuery(node);
        return (new QueryImpl(query, this.hm));
    }

    public String[] getSupportedQueryLanguages() throws RepositoryException {
        return this.queryManager.getSupportedQueryLanguages();
    }

 }
