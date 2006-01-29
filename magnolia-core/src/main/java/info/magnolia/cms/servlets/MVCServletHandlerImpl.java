/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.servlets;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of a MVCHandler. Calls the command through reflection.
 * @author Philipp Bracher
 * @version $Id$
 */
public abstract class MVCServletHandlerImpl implements MVCServletHandler {

    protected static final String VIEW_ERROR = "error"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MVCServletHandlerImpl.class);

    protected HttpServletRequest request;

    protected HttpServletResponse response;

    private String name;

    protected MVCServletHandlerImpl(String name, HttpServletRequest request, HttpServletResponse response) {
        super();
        this.name = name;
        this.request = request;
        this.response = response;
    }

    /**
     * @see info.magnolia.cms.servlets.MVCServletHandler#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * Call the method through reflection
     * @param command
     * @return the name of the view to show (used in renderHtml)
     */
    public String execute(String command) {
        String view = VIEW_ERROR;
        Method method;

        try {
            method = this.getClass().getMethod(command, new Class[]{});
            // method.setAccessible(true);
            view = (String) method.invoke(this, new Object[]{});
        }
        catch (InvocationTargetException e) {
            log.error("can't call command: " + command, e.getTargetException()); //$NON-NLS-1$
        }
        catch (Exception e) {
            log.error("can't call command: " + command, e); //$NON-NLS-1$
        }

        return view;
    }

}
