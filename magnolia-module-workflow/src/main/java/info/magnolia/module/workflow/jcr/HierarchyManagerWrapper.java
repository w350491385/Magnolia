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
package info.magnolia.module.workflow.jcr;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;

import javax.jcr.RepositoryException;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface HierarchyManagerWrapper {

    void save() throws RepositoryException;

    boolean isExist(String path);

    Content getContent(String path) throws RepositoryException;

    Content createPath(String path, ItemType itemType) throws RepositoryException;
}
