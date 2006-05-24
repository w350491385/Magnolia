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
package info.magnolia.jaas.sp.jcr;

import info.magnolia.jaas.sp.AbstractLoginModule;
import info.magnolia.jaas.principal.EntityImpl;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.security.auth.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.codec.binary.Base64;

import javax.security.auth.login.LoginException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 * @author Sameer Charles
 * $Id$
 */
public class JCRAuthenticationModule extends AbstractLoginModule {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(JCRAuthenticationModule.class);

    protected String name;

    protected char[] pswd;

    protected boolean success;

    protected Content user;

    /**
     * Authenticate against magnolia/jcr user repository
     */
    public boolean login() throws LoginException {
        if (this.callbackHandler == null)
            throw new LoginException(
                    "Error: no CallbackHandler available for JCRModule");

        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("name");
        callbacks[1] = new PasswordCallback("pswd", false);

        this.success = false;
        try {
            this.callbackHandler.handle(callbacks);
            this.name = ((NameCallback) callbacks[0]).getName();
            this.pswd = ((PasswordCallback) callbacks[1]).getPassword();
            this.success = this.isValidUser();
        } catch (IOException ioe) {
            if (log.isDebugEnabled())
                log.debug("Exception caught", ioe);
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException ce) {
            if (log.isDebugEnabled())
                log.debug(ce.getMessage(), ce);
            throw new LoginException(ce.getCallback().toString()
                    + " not available");
        }
        if (!this.success)
            throw new LoginException("failed to authenticate "+this.name);

        return this.success;
    }

    /**
     * Update subject with ACL and other properties
     */
    public boolean commit() throws LoginException {
        if (!this.success) {
            throw new LoginException("failed to authenticate "+this.name);
        }
        this.setEntity();
        return true;
    }

    /**
     * Releases all associated memory
     */
    public boolean release() {
        return true;
    }

    /**
     * checks is the credentials exist in the repository
     *
     * @return boolean
     */
    public boolean isValidUser() {
        HierarchyManager hm = ContentRepository
                .getHierarchyManager(ContentRepository.USERS);
        try {
            this.user = hm.getContent(this.name);
            String fromRepository = this.user.getNodeData("pswd").getString()
                    .trim();

            // if callback handler instance of MD5
            // - encode repository password to MD5
            // if callback handler instance of Base64
            // - encode given password to Base64
            // todo


            String encodedPassword = new String(Base64
                    .encodeBase64((new String(this.pswd)).getBytes()));
            return fromRepository.equalsIgnoreCase(encodedPassword);
        } catch (PathNotFoundException pe) {
            log.info("Unable to locate user [" + this.name
                    + "], authentication failed");
        } catch (RepositoryException re) {
            log.error("Unable to locate user [" + this.name
                    + "], authentication failed due to a "
                    + re.getClass().getName(), re);
        }
        return false;
    }

    /**
     * set user details
     */
    public void setEntity() {
        EntityImpl user = new EntityImpl();
        String language = this.user.getNodeData("language").getString();
        user.addProperty(Entity.LANGUAGE, language);
        String name = this.user.getTitle();
        user.addProperty(Entity.NAME, name);
        user.addProperty(Entity.PASSWORD, new String(this.pswd));
        this.subject.getPrincipals().add(user);
    }

    /**
     * set access control list from the user, roles and groups
     */
    public void setACL() {
    }

}
