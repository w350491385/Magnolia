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
package info.magnolia.cms.filters;

import info.magnolia.cms.beans.config.VirtualURIManager;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handle redirects configured using VirtualURIMappings.
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class MgnlVirtualUriFilter implements Filter {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MgnlVirtualUriFilter.class);

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        // unused
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // unused
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     * javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException,
        ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        if (redirect(request, response)) {
            return;
        }

        filterChain.doFilter(req, resp);
    }

    /**
     * Redirect based on the mapping in config/server/.node.xml
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return <code>true</code> if request has been redirected, <code>false</code> otherwise
     */
    private boolean redirect(HttpServletRequest request, HttpServletResponse response) {
        String uri = this.getURIMap(request);
        if (StringUtils.isNotEmpty(uri)) {
            if (!response.isCommitted()) {

                if (uri.startsWith("redirect:")) {
                    try {
                        response.sendRedirect(request.getContextPath() + StringUtils.substringAfter(uri, "redirect:"));
                    }
                    catch (IOException e) {
                        log.error("Failed to redirect to {}:{}", //$NON-NLS-1$
                            new Object[]{uri, e.getMessage()});
                    }
                }
                else {

                    try {
                        request.getRequestDispatcher(uri).forward(request, response);
                    }
                    catch (Exception e) {
                        log.error("Failed to forward to {} - {}:{}", //$NON-NLS-1$
                            new Object[]{uri, ClassUtils.getShortClassName(e.getClass()), e.getMessage()});
                    }
                }
            }
            else {
                log.warn("Response is already committed, cannot forward to {} (original URI was {})",//$NON-NLS-1$
                    uri,
                    request.getRequestURI());
            }

            return true;
        }
        return false;
    }

    /**
     * @return URI mapping as in ServerInfo
     * @param request HttpServletRequest
     */
    private String getURIMap(HttpServletRequest request) {
        return VirtualURIManager.getInstance().getURIMapping(
            StringUtils.substringAfter(request.getRequestURI(), request.getContextPath()));
    }

}