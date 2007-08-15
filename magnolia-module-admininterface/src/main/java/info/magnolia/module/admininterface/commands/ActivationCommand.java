/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.admininterface.commands;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.context.Context;

import java.util.*;

import javax.jcr.RepositoryException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * the activation command which do real activation
 * @author jackie
 * $Id$
 */
public class ActivationCommand extends BaseActivationCommand {

    /**
     * Log
     */
    private static Logger log = LoggerFactory.getLogger(ActivationCommand.class);

    private boolean recursive;

    private String versionNumber;

    private List versionMap;

    /**
     * Execute activation
     */
    public boolean execute(Context ctx) {
        synchronized(ExclusiveWrite.getInstance()) {
            try {
                Content thisState = getNode(ctx);
                String parentPath = StringUtils.substringBeforeLast(thisState.getHandle(), "/");
                if (StringUtils.isEmpty(parentPath)) {
                    parentPath = "/";
                }
                // make multiple activations instead of a big bulp
                if (recursive) {
                    List versionMap = getVersionMap();
                    if (versionMap == null) {
                        activateRecursive(parentPath, thisState, ctx);
                    } else {
                        activateRecursive(ctx, versionMap);
                    }
                }
                else {
                    List orderInfo = getOrderingInfo(thisState);
                    if (StringUtils.isNotEmpty(getVersion())) {
                        try {
                            thisState = thisState.getVersionedContent(getVersion());
                        } catch (RepositoryException re) {
                            log.error("Failed to get version "+getVersion()+" for "+thisState.getHandle(), re);
                        }
                    }
                    getSyndicator().activate(parentPath, thisState, orderInfo);
                }
            }
            catch (Exception e) {
                log.error("can't activate", e);
                AlertUtil.setException(MessagesManager.get("tree.error.activate"), e, ctx);
                return false;
            }
            log.info("exec successfully.");
            return true;
        }
    }

    /**
     * Activate recursively. This is done one by one to send only small peaces (memory friendly).
     * @param parentPath
     * @param node
     * @throws ExchangeException
     * @throws RepositoryException
     */
    protected void activateRecursive(String parentPath, Content node, Context ctx)
            throws ExchangeException, RepositoryException {

        getSyndicator().activate(parentPath, node, getOrderingInfo(node));

        Iterator children = node.getChildren(new Content.ContentFilter() {
            public boolean accept(Content content) {
                try {
                    return !getRule().isAllowed(content.getNodeTypeName());
                }
                catch (RepositoryException e) {
                    log.error("can't get nodetype", e);
                    return false;
                }
            }
        }).iterator();

        while (children.hasNext()) {
            this.activateRecursive(node.getHandle(), ((Content) children.next()), ctx);
        }
    }

    /**
     * @param ctx
     * @param versionMap
     * */
    protected void activateRecursive(Context ctx, List versionMap)
            throws ExchangeException, RepositoryException {
        // activate all uuid's present in versionMap
        Iterator entries = versionMap.iterator();
        while (entries.hasNext()) {
            Map entry = (Map) entries.next();
            String uuid = (String) entry.get("uuid");
            String versionNumber = (String) entry.get("version");
            if (StringUtils.equalsIgnoreCase("class", uuid)) {
                // todo , this should not happen in between the serialized list, somewhere a bug
                // for the moment simply ignore it
                continue;
            }
            try {
                Content content = ctx.getHierarchyManager(getRepository()).getContentByUUID(uuid);
                // NOTE : on activation of the version it uses current hierarchy to order
                // since admin interface does not protect the state of the hierarchy if its in workflow
                // we have to use the current state
                List orderedList = getOrderingInfo(content);
                String parentPath = content.getParent().getHandle();
                content = content.getVersionedContent(versionNumber);
                // add order info for the first node as it represents the parent in a tree
                getSyndicator().activate(parentPath, content, orderedList);
            } catch (RepositoryException re) {
                log.error("Failed to activate node with UUID : "+uuid);
                log.error(re.getMessage());
            }
        }
    }

    /**
     * collect node UUID of the siblings in the exact order as it should be written on
     * subscribers
     * @param node
     * */
    private List getOrderingInfo(Content node) {
        //do not use magnolia Content class since these objects are only meant for a single use to read UUID
        List siblings = new ArrayList();
        Node thisNode = node.getJCRNode();
        try {
            String thisNodeType = node.getNodeTypeName();
            String thisNodeUUID = node.getUUID();
            NodeIterator nodeIterator = thisNode.getParent().getNodes();
            while (nodeIterator.hasNext()) { // only collect elements after this node
                Node sibling = nodeIterator.nextNode();
                // skip till the actual position
                if (sibling.isNodeType(thisNodeType)) {
                    if (thisNodeUUID.equalsIgnoreCase(sibling.getUUID())) break;
                }
            }
            while (nodeIterator.hasNext()) {
                Node sibling = nodeIterator.nextNode();
                if (sibling.isNodeType(thisNodeType)) {
                    siblings.add(sibling.getUUID());
                }
            }
        } catch (RepositoryException re) {
            // do not throw this exception, if it fails simply do not add any ordering info
            log.error("Failed to get Ordering info", re);
        }
        return siblings;
    }

    /**
     * @return the recursive
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * @param recursive the recursive to set
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * @param number version number to be set for activation
     * */
    public void setVersion(String number) {
        this.versionNumber = number;
    }

    /**
     * @return version number
     * */
    public String getVersion() {
        return this.versionNumber;
    }

    /**
     * @param versionMap version map to be set for activation
     * */
    public void setVersionMap(List versionMap) {
        this.versionMap = versionMap;
    }

    /**
     * @return version map
     * */
    public List getVersionMap() {
        return this.versionMap;
    }


}
