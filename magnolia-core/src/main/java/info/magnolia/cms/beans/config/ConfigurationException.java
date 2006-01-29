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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.BaseRuntimeException;


/**
 *
 */
public class ConfigurationException extends BaseRuntimeException {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Exception cause) {
        super(message, (cause instanceof ConfigurationException) ? ((ConfigurationException) cause).getCause() : cause);
    }

    public ConfigurationException(Exception root) {
        super(root);
    }
}
