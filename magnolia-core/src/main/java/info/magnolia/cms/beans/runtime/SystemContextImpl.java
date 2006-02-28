/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.beans.runtime;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

/**
 * This is the system context using the not secured HierarchyManagers.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class SystemContextImpl extends ContextImpl {

    /**
     * To get and set the attributes for this context
     */
    protected Map scopes = new HashMap();

    /**
     * this instance
     * */
    private static SystemContextImpl thisInstance = new SystemContextImpl();

    /**
     * package private
     * Init the scopes
     */
    SystemContextImpl() {
        scopes.put(new Integer(Context.REQUEST_SCOPE), new HashMap());
        scopes.put(new Integer(Context.SESSION_SCOPE), new HashMap());
        scopes.put(new Integer(Context.APPLICATION_SCOPE), new HashMap());
    }

    /**
     * get this instance
     * @return SystemContextImpl instance
     * */
    protected static SystemContextImpl getInstance() {
        return thisInstance;
    }

    public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
        return ContentRepository.getHierarchyManager(repositoryId, workspaceId);
    }

    public AccessManager getAccessManager(String repositoryId, String workspaceId) {
        return ContentRepository.getAccessManager();
    }

    public QueryManager getQueryManager(String repositoryId, String workspaceId) {
        return this.getHierarchyManager(repositoryId, workspaceId).getQueryManager();
    }

    public void setAttribute(String name, Object value) {
        this.setAttribute(name, value, Context.REQUEST_SCOPE);
    }

    public void setAttribute(String name, Object value, int scope) {
        getScope(scope).put(name, value);
    }

    protected Map getScope(int scope) {
        return (Map) scopes.get(new Integer(scope));
    }

    public Object getAttribute(String name) {
        Object val = null;
        val = getScope(Context.REQUEST_SCOPE).get(name);
        if(val == null){
            val = getScope(Context.SESSION_SCOPE).get(name);
            if(val == null){
                val = getScope(Context.SESSION_SCOPE).get(name);
            }
        }
        return val;
    }
}
