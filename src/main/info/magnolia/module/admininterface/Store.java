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
package info.magnolia.module.admininterface;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import info.magnolia.cms.core.Content;


/**
 * Date: Oct 14, 2004 Time: 3:23:06 PM
 * @author Sameer Charles
 * @version 2.0
 */
public class Store {

    private static Logger log = Logger.getLogger(Store.class);

    /**
     * Map with repository name/handler class for admin tree. When this servlet will receive a call with a parameter
     * <code>repository</code>, the corresponding handler will be used top display the admin tree.
     */
    private final Map treeHandlers = new HashMap();
    private final Map dialogHandlers = new HashMap();
    
    private static Store store = new Store();

    private Content localStore;

    protected Store() {
    }

    public static Store getInstance() {
        return Store.store;
    }

    public void setStore(Content localStore) {
        this.localStore = localStore;
    }

    public Content getStore() {
        return this.localStore;
    }

    public void registerTreeHandler(String name, Class treeHandler) {
        treeHandlers.put(name, treeHandler);
    }
    
    /**
     * Register only if not yet an other handler is present
     * 
     * @param name
     * @param class
     */
    public void registerDefaultTreeHandler(String name, Class dialogHandler) {
        if(!this.treeHandlers.containsKey(name)){
            registerTreeHandler(name, dialogHandler);
        }
    }
    public AdminTreeMVCHandler getTreeHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        try {
            Class treeHandlerClass = (Class) treeHandlers.get(name);
            Constructor constructor = treeHandlerClass.getConstructor(new Class[]{String.class, HttpServletRequest.class, HttpServletResponse.class});
            return (AdminTreeMVCHandler) constructor.newInstance(new Object[]{name, request, response});
        }
        catch (Exception e) {
            log.error("can't instanciate the treehandler: " + name, e);
        }
        return null;
    }

    public void registerDialogHandler(String name, Class dialogHandler) {
        registerDialogHandler(name, dialogHandler, null);
    }
    
    public void registerDialogHandler(String name, Class dialogHandler, Content configNode) {
        dialogHandlers.put(name, new Object[]{dialogHandler, configNode});
    }

    public DialogMVCHandler getDialogHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        DialogMVCHandler handler = null;
        try {
            Class dialogHandlerClass = (Class) ((Object[])dialogHandlers.get(name))[0];
            Content configNode = (Content) ((Object[])dialogHandlers.get(name))[1];
            if(configNode != null){
                Constructor constructor = dialogHandlerClass.getConstructor(new Class[]{String.class, HttpServletRequest.class, HttpServletResponse.class, Content.class});
                return (DialogMVCHandler) constructor.newInstance(new Object[]{name, request, response, configNode});
                
            }
            else{
                Constructor constructor = dialogHandlerClass.getConstructor(new Class[]{String.class, HttpServletRequest.class, HttpServletResponse.class});
                return (DialogMVCHandler) constructor.newInstance(new Object[]{name, request, response});                
            }
        }
        catch (Exception e) {
            log.error("can't instanciate the dialoghandler: " + name, e);
        }
        return handler;
    }



}