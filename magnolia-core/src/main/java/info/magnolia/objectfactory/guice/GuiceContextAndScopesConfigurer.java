/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.objectfactory.guice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.inject.Provider;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Channel;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.objectfactory.annotation.LazySingleton;
import info.magnolia.objectfactory.annotation.LocalScoped;
import info.magnolia.objectfactory.annotation.SessionScoped;

/**
 * Registers request and session scopes and providers for accessing MgnlContext.
 *
 * @version $Id$
 */
public class GuiceContextAndScopesConfigurer extends AbstractGuiceComponentConfigurer {

    @Override
    protected void configure() {

        // We don't need to register these providers at every level, would be enough to do it in the top parent
        bind(Context.class).toProvider(new Provider<Context>() {
            @Override
            public Context get() {
                return MgnlContext.getInstance();
            }
        });
        bind(WebContext.class).toProvider(new Provider<WebContext>() {
            @Override
            public WebContext get() {
                return MgnlContext.getWebContext();
            }
        }).in(LocalScoped.class);
        bind(AggregationState.class).toProvider(new Provider<AggregationState>() {
            @Override
            public AggregationState get() {
                return MgnlContext.getAggregationState();
            }
        }).in(LocalScoped.class);
        bind(Channel.class).toProvider(new Provider<Channel>() {
            @Override
            public Channel get() {
                return MgnlContext.getAggregationState().getChannel();
            }
        }).in(LocalScoped.class);
        bind(HttpSession.class).toProvider(new Provider<HttpSession>() {
            @Override
            public HttpSession get() {
                return MgnlContext.getWebContext().getRequest().getSession();
            }
        }).in(SessionScoped.class);
        bind(HttpServletRequest.class).toProvider(new Provider<HttpServletRequest>() {
            @Override
            public HttpServletRequest get() {
                return MgnlContext.getWebContext().getRequest();
            }
        }).in(LocalScoped.class);
        bind(HttpServletResponse.class).toProvider(new Provider<HttpServletResponse>() {
            @Override
            public HttpServletResponse get() {
                return MgnlContext.getWebContext().getResponse();
            }
        }).in(LocalScoped.class);

        // But the scopes need to be registered at every level
        bindScope(LocalScoped.class, MagnoliaScopes.LOCAL);
        bindScope(SessionScoped.class, MagnoliaScopes.SESSION);
        bindScope(LazySingleton.class, MagnoliaScopes.LAZY_SINGLETON);
    }
}
