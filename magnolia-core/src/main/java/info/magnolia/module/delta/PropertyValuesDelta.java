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
package info.magnolia.module.delta;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;
import java.text.MessageFormat;

/**
 * A delta that offers helper methods to check on certain properties.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class PropertyValuesDelta implements Delta {

    /**
     * Checks that the given String property has the expected value. Changes it if so, logs otherwise.
     */
    protected void checkPropertyValue(InstallContext ctx, Content node, String propertyName, String expectedCurrentValue, String newValue) throws RepositoryException {
        final NodeData prop = node.getNodeData(propertyName);
        final String currentvalue = prop.getString();
        if (prop.isExist() && currentvalue.equals(expectedCurrentValue)) {
            prop.setValue(newValue);
        } else {
            final String msg = format("{0} was expected to exist with value {1} but {2,choice,0#does not exist|1#has the value {3} instead}.",
                    prop.getHandle(), expectedCurrentValue, Integer.valueOf(prop.isExist() ? 1 : 0), currentvalue);
            ctx.log(msg);
        }
    }

    /**
     * Checks that the given property does not exist and creates it with the given value, logs otherwise.
     */
    protected void newProperty(InstallContext ctx, Content node, String propertyName, String value) throws RepositoryException {
        final NodeData prop = node.getNodeData(propertyName);
        if (!prop.isExist()) {
            node.createNodeData(propertyName, value);
        } else {
            final String msg = format("{0} was not expected to exist, but exists with value {1} and was going to be created with value {2}.",
                    prop.getHandle(), prop.getString(), value, null);
            ctx.log(msg);
        }
    }

    private String format(String pattern, Object arg0, Object arg1, Object arg2, Object arg3) {
        return MessageFormat.format(pattern, new Object[]{arg0, arg1, arg2, arg3});
    }
}
