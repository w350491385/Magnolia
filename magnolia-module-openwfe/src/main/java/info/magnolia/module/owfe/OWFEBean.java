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
package info.magnolia.module.owfe;

import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.util.MgnlCoreConstants;
import info.magnolia.module.owfe.jcr.JCRFlowDefinition;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;
import info.magnolia.module.owfe.jcr.JCRWorkItemAPI;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import openwfe.org.engine.expressions.FlowExpressionId;
import openwfe.org.engine.workitem.AttributeUtils;
import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the class implements all the interface of work flow API
 * @author jackie
 */
public class OWFEBean implements WorkflowAPI {

	private static final String ATT_COMMENT = "comment";

	private final static Logger log = LoggerFactory.getLogger(OWFEBean.class
			.getName());

	JCRWorkItemAPI storage = null;

	public OWFEBean() throws Exception {
		this.storage = new JCRWorkItemAPI();
	}

	/**
	 * get user name from request
	 * 
	 * @param request
	 * @return get the user name from the request
	 */
	public String getUsername(HttpServletRequest request) {

		return MgnlContext.getUser().getName();
	}

	/**
	 * get all work items for the user
	 * 
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public List getWorkItems(String userName) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("enter getWorkItems");
			log.debug("user name = " + userName);
		}

		long start = System.currentTimeMillis();
		MgnlUser user = (MgnlUser) MgnlContext.getUser();
		Collection groups = user.getGroups();
		Collection roles = user.getRoles();
		StringBuffer queryString = new StringBuffer();
		queryString.append("//*[(@assignTo=\"");
		queryString.append(userName);
		queryString.append("\") or (@participant=\"user-");
		queryString.append(userName);
		queryString.append("\" and not(@assignTo))");
		for (Iterator iter = groups.iterator(); iter.hasNext();) {
			Object group = iter.next();
			queryString.append(" or (@participant=\"group-");
			queryString.append(group);
			queryString.append("\" and @assignTo!=\"");
			queryString.append(userName);
			queryString.append("\") ");
		}
		for (Iterator iter = roles.iterator(); iter.hasNext();) {
			Object role = iter.next();
			queryString.append(" or (@participant=\"role-");
			queryString.append(role);
			queryString.append("\" and @assignTo!=\"");
			queryString.append(userName);
			queryString.append("\") ");
		}
		queryString.append("]");

		if (log.isDebugEnabled())
			log.info("xpath query string = " + queryString);
		
		final List doQuery = this.storage.doQuery(queryString.toString());
		long end = System.currentTimeMillis();
		log.info("Retrieving workitems done. (Took " + (end - start) + " ms)");
		return doQuery;
	}

	/**
	 * get number of work items for current user
	 */
	public int getWorkItemsNumber(HttpServletRequest request) throws Exception {
		return getWorkItems(getUsername(request)).size();
	}

	/**
	 * return a list of work item for current user
	 */
	public List getWorkItems(HttpServletRequest request) throws Exception {
		return getWorkItems(getUsername(request));
	}

	/**
	 * remove one work item by id
	 */
	public void removeWorkItem(InFlowWorkItem wi) throws Exception {
		this.storage.removeWorkItem(wi.getId());
	}

	/**
	 * approve activiation
	 */
	public void approveActivation(String expressionId) throws Exception {
		// get workitem
		InFlowWorkItem wi = this.storage.retrieveWorkItem("", FlowExpressionId
				.fromParseableString(expressionId));
		if (wi == null) {
			throw new Exception(
					"can't get the work iem by this expression id ("
							+ expressionId + ")");
		}

		wi.touch();

		try {
			wi.getAttributes().puts(MgnlConstants.ATT_OK,
					MgnlCoreConstants.TRUE);
			OWFEEngine.getEngine().reply(wi);
		} catch (Exception e) {
			log.error("reply to engine failed", e);

		} finally {
			removeWorkItem(wi);
		}

		log.info("approve ok");
	}

	/**
	 * reject the activation request, the work item will be removed
	 */
	public void rejectActivation(String expressionId, String comment)
			throws Exception {

		InFlowWorkItem wi = this.storage.retrieveWorkItem("", FlowExpressionId
				.fromParseableString(expressionId));
		if (wi == null) {
			throw new Exception(
					"cant not get the work iem by this expression id ("
							+ expressionId + ")");
		}
		wi.touch();

		try {
			wi.getAttributes().puts(MgnlConstants.ATT_OK,
					MgnlCoreConstants.FALSE);
			wi.getAttributes().puts(ATT_COMMENT, comment);
			OWFEEngine.getEngine().reply(wi);
		} catch (Exception e) {
			log.error("Error while accessing the workflow engine", e);
		} finally {
			removeWorkItem(wi);
		}

		if (log.isDebugEnabled()) {
			log.debug("work item removed.");
		}

		log.info("reject ok");
	}

	public void cancel(String expressionId) {
		try {
			this.storage.removeWorkItem(FlowExpressionId
					.fromParseableString(expressionId));
		} catch (Exception e) {
			log.info("can't cancel", e);
		}
	}

	/**
	 * update the attributes of the work item
	 */
	public void updateWorkItem(String expressionId, String[] names,
			String values[]) throws Exception {

		InFlowWorkItem ifwi = this.storage.retrieveWorkItem("",
				FlowExpressionId.fromParseableString(expressionId));
		if (ifwi == null) {
			throw new Exception(
					"cant not get the work iem by this expression id ("
							+ expressionId + ")");
		}
		for (int i = 0; i < names.length; i++) {
			ifwi.setAttribute(names[i], new StringAttribute(values[i]));
		}
		this.storage.storeWorkItem("", ifwi);
	}

	/**
	 * assign work item to a user, if userName = "", then assignment for the
	 * workItem will be deleted
	 */
	public void assignWorkItemToUser(String expressionId, String userName) {
		if (expressionId == null || expressionId.length() == 0) {
			log.error("can not assign work item, invalid express id "
					+ expressionId);
			return;
		}

		if (userName == null) {
			log.info("User name was null");
			return;
		}

		FlowExpressionId eid = FlowExpressionId
				.fromParseableString(expressionId);
		if (eid == null) {
			log
					.error("can not assign work item, can not parse invalid express id "
							+ expressionId);
			return;
		}

		InFlowWorkItem if_wi = null;
		try {
			if_wi = this.storage.retrieveWorkItem("", eid);
		} catch (Exception e) {
			log.error("retrieve work item failed", e);
		}
		if (if_wi == null) {

			log
					.error("can not assign work item, can not retrieve work tiem by  express id "
							+ expressionId);
			return;
		}
		assignWorkItemToUser(if_wi, userName);

	}

	/**
	 * assign work item to a user, if userName = "", then assignment for the
	 * workItem will be deleted
	 */
	public void assignWorkItemToUser(InFlowWorkItem wi, String userName) {
		if (userName == null) {
			log.info("User name was null");
			return;
		}

		try {
			wi.addAttribute(MgnlConstants.ATT_ASSIGN_TO, new StringAttribute(
					userName));
			this.storage.storeWorkItem(StringUtils.EMPTY, wi);
		} catch (Exception e) {
			log.error("assign work item to user " + userName + " failed.)", e);
		}

	}

	/**
	 * return a list of workItem for one group
	 */
	public List getGroupInbox(String GroupName) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("enter getGroupInbox");
			log.debug("GroupName = " + GroupName);
		}

		StringBuffer queryString = new StringBuffer();
		queryString.append("//*[@participant=\"group-");
		queryString.append(GroupName);
		queryString.append("\"]");

		if (log.isDebugEnabled())
			log.debug("xpath query string = " + queryString);
		return this.storage.doQuery(queryString.toString());

	}

	/**
	 * return a list of workItem for one role
	 */
	public List getRoleInbox(String roleName) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("enter getGroupInbox");
			log.debug("roleName = " + roleName);
		}

		StringBuffer queryString = new StringBuffer();
		queryString.append("//*[@participant=\"group-");
		queryString.append(roleName);
		queryString.append("\"]");

		if (log.isDebugEnabled())
			log.debug("xpath query string = " + queryString);
		return this.storage.doQuery(queryString.toString());
	}

	/**
	 * return a list of workItem for one usre
	 */
	public List getUserInbox(String userName) throws Exception {
		return this.getWorkItems(userName);
	}

	/**
	 * Simply launch a flow
	 */
	public void LaunchFlow(HierarchyManager hm, String path, String flowName)
			throws Exception {
		if (log.isDebugEnabled())
			log
					.debug("- Lauch flow -" + this.getClass().toString()
							+ "- Start");
		if (flowName == null || flowName.length() == 0) {
			throw new IllegalArgumentException(
					"flowName is null or empty string");
		}
		try {
			// Get the references
			LaunchItem li = new LaunchItem();
			li.addAttribute(MgnlConstants.P_ACTION, new StringAttribute(this
					.getClass().getName()));
			li
					.setWorkflowDefinitionUrl(MgnlConstants.P_WORKFLOW_DEFINITION_URL);

			// Retrieve and add the flow definition to the LaunchItem
			String flowDef = new JCRFlowDefinition()
					.getflowDefAsString(flowName);
			li.getAttributes().puts(MgnlConstants.P_DEFINITION, flowDef);
			JCRPersistedEngine engine = OWFEEngine.getEngine();

			// start activation
			if (hm != null) {
				li.addAttribute(MgnlConstants.P_HM, AttributeUtils
						.java2owfe(hm));
			}
			if (path != null) {
				li
						.addAttribute(MgnlConstants.P_PATH,
								new StringAttribute(path));
			}

			// Launch the item
			engine.launch(li, true);

		} catch (Exception e) {
			log.error("Launching flow " + flowName + " failed", e);
		}

		if (log.isDebugEnabled())
			log.debug("- Lauch flow -{}- End", this.getClass().getName());

	}

}