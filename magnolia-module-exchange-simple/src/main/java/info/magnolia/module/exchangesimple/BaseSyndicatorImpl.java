/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.exchangesimple;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.core.version.ContentVersion;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.exchange.Subscription;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.SecurityUtil;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.cms.util.RuleBasedContentFilter;
import info.magnolia.context.MgnlContext;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.logging.AuditLoggingUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import EDU.oswego.cs.dl.util.concurrent.Sync;

import com.google.inject.Inject;

/**
 * Default implementation of {@link Syndicator}. Activates all the content to a subscriber configured on the server.
 * @author Sameer Charles
 * $Id: $
 */
public abstract class BaseSyndicatorImpl implements Syndicator {
    private static final Logger log = LoggerFactory.getLogger(BaseSyndicatorImpl.class);

    /**
     * URI used for activation.
     */
    public static final String DEFAULT_HANDLER = ".magnolia/activation";

    public static final String PARENT_PATH = "mgnlExchangeParentPath";

    public static final String MAPPED_PARENT_PATH = "mgnlExchangeMappedParent";

    /**
     * Path to be activated or deactivated.
     */
    public static final String PATH = "mgnlExchangePath";

    public static final String NODE_UUID = "mgnlExchangeNodeUUID";

    /**
     * @deprecated since 4.5 - use logical workspace instead.
     */
    @Deprecated
    public static final String REPOSITORY_NAME = "mgnlExchangeRepositoryName";

    public static final String WORKSPACE_NAME = "mgnlExchangeWorkspaceName";

    public static final String VERSION_NAME = "mgnlExchangeVersionName";

    /**
     * Name of the resource containing reading sequence for importing the data in activation target.
     */
    public static final String RESOURCE_MAPPING_FILE = "mgnlExchangeResourceMappingFile";

    public static final String UTF8_STATUS = "mgnlUTF8Status";

    /**
     * Name of the element in the resource file describing siblings of activated node.
     * Siblings element will contain all siblings of the same node type which are "before"
     * this node.
     */
    public static final String SIBLINGS_ROOT_ELEMENT = "NodeSiblings";

    public static final String SIBLINGS_ELEMENT = "sibling";

    public static final String SIBLING_UUID = "siblingUUID";

    public static final String RESOURCE_MAPPING_FILE_ELEMENT = "File";

    public static final String RESOURCE_MAPPING_NAME_ATTRIBUTE = "name";

    public static final String RESOURCE_MAPPING_UUID_ATTRIBUTE = "contentUUID";

    public static final String RESOURCE_MAPPING_ID_ATTRIBUTE = "resourceId";

    public static final String RESOURCE_MAPPING_MD_ATTRIBUTE = "resourceMD";

    public static final String RESOURCE_MAPPING_ROOT_ELEMENT = "Resources";

    public static final String ACTION = "mgnlExchangeAction";

    public static final String ACTIVATE = "activate";

    public static final String DEACTIVATE = "deactivate";

    public static final String COMMIT = "commit";

    public static final String ROLLBACK = "rollback";

    public static final String CONTENT_FILTER_RULE = "mgnlExchangeFilterRule";

    public static final String ACTIVATION_SUCCESSFUL = "sa_success";

    public static final String ACTIVATION_HANDSHAKE = "sa_handshake";

    public static final String ACTIVATION_FAILED = "sa_failed";

    public static final String ACTIVATION_ATTRIBUTE_STATUS = "sa_attribute_status";

    public static final String ACTIVATION_ATTRIBUTE_MESSAGE = "sa_attribute_message";

    public static final String ACTIVATION_ATTRIBUTE_VERSION = "sa_attribute_version";

    public static final String ACTIVATION_AUTH = "X-magnolia-act-auth";
    public static final String ACTIVATION_AUTH_KEY = "X-magnolia-act-auth-init";

    public BaseSyndicatorImpl() {
    }
    /**
     * Runs a given job in the thread pool.
     *
     * @param job the job to run
     * @throws ExchangeException if the job could not be put in the pool
     */
    protected static void executeInPool(Runnable job) throws ExchangeException {
        try {
            ThreadPool.getInstance().execute(job);
        } catch (InterruptedException e) {
            // this is kind of a problem, we could not add the job to the pool
            // retrying might or might not work now that the interruption
            // status is cleared but there is not much we can do so throwing
            // an ExchangeException seems like the least bad choice
            String message = "could not execute job in pool";
            log.error(message, e);
            throw new ExchangeException(message, e);
        }
    }

    /**
     * Acquires a {@link Sync} ignoring any interruptions. Should any
     * interruption occur the interruption status will be set. Might
     * potentially block/wait forever.
     *
     * @see Sync#acquire()
     *
     * @param latch the latch on which to wait
     */
    protected static void acquireIgnoringInterruption(Sync latch) {
        try {
            latch.acquire();
        } catch (InterruptedException e) {
            // waken up externally - ignore try again
            acquireIgnoringInterruption(latch);
            // be a good citizen and set back the interruption status
            Thread.currentThread().interrupt();
        }
    }

    /**
     * @deprecated since 4.5 - should no longer be needed when operating with logical workspace names
     */
    @Deprecated
    protected String repositoryName;

    protected String workspaceName;

    protected String parent;

    protected Rule contentFilterRule;

    protected User user;

    private Calendar contentVersionDate;

    private MagnoliaConfigurationProperties properties;

    private ResourceCollector resourceCollector;

    /**
     * @param user
     * @param repositoryName repository ID
     * @param workspaceName workspace ID
     * @param rule content filter rule
     * @see info.magnolia.cms.exchange.Syndicator#init(info.magnolia.cms.security.User, String, String,
     * info.magnolia.cms.util.Rule)
     */
    @Override
    public void init(User user, String repositoryName, String workspaceName, Rule rule) {
        this.user = user;
        this.contentFilterRule = rule;
        this.repositoryName = repositoryName;
        this.workspaceName = workspaceName;
    }

    /**
     * This will activate specifies page (sub pages) to all configured subscribers.
     *
     * @param parent parent under which this page will be activated
     * @param content to be activated
     * @throws javax.jcr.RepositoryException
     * @throws info.magnolia.cms.exchange.ExchangeException
     */
    @Override
    public void activate(String parent, Content content) throws ExchangeException, RepositoryException {
        this.activate(parent, content, null);
    }

    /**
     * This will activate specified node to all configured subscribers.
     *
     * @param parent parent under which this page will be activated
     * @param content to be activated
     * @param orderBefore List of UUID to be used by the implementation to order this node after activation
     * @throws javax.jcr.RepositoryException
     * @throws info.magnolia.cms.exchange.ExchangeException
     *
     */
    @Override
    public void activate(String parent, Content content, List<String> orderBefore) throws ExchangeException, RepositoryException {
        this.activate(null, parent, content, orderBefore);
    }

    /**
     * This will activate specifies page (sub pages) to the specified subscriber.
     *
     * @param subscriber
     * @param parent parent under which this page will be activated
     * @param content to be activated
     * @throws javax.jcr.RepositoryException
     * @throws info.magnolia.cms.exchange.ExchangeException
     */
    @Override
    public void activate(Subscriber subscriber, String parent, Content content) throws ExchangeException, RepositoryException {
        this.activate(subscriber, parent, content, null);
    }

    /**
     * This will activate specifies node to the specified subscriber.
     *
     * @param subscriber
     * @param parent      parent under which this page will be activated
     * @param content     to be activated
     * @param orderBefore List of UUID to be used by the subscriber to order this node after activation
     * @throws javax.jcr.RepositoryException
     * @throws info.magnolia.cms.exchange.ExchangeException
     */
    @Override
    public void activate(Subscriber subscriber, String parent, Content content, List<String> orderBefore) throws ExchangeException, RepositoryException {
        this.parent = parent;
        String path = content.getHandle();

        if (content instanceof ContentVersion) {
            contentVersionDate = ((ContentVersion)content).getCreated();
        }

        ActivationContent activationContent = null;
        try {
            activationContent = resourceCollector.collect(content, orderBefore, parent, workspaceName, repositoryName, contentFilterRule);
            if (null == subscriber) {
                this.activate(activationContent, path);
            } else {
                this.activate(subscriber, activationContent, path);
            }
            if (Boolean.parseBoolean(activationContent.getproperty(ItemType.DELETED_NODE_MIXIN))) {
                final HierarchyManager hm = content.getHierarchyManager();
                final Session session = content.getJCRNode().getSession();
                String uuid = content.getUUID();
                if (StringUtils.isNotBlank(uuid)) {
                    if (content instanceof ContentVersion) {
                        // replace versioned content with the real node
                        content = hm.getContentByUUID(uuid);
                    }
                    Content parentContent = content.getParent();
                    content.delete();
                    parentContent.save();
                } else {
                    log.warn("Content {}:{} was already removed.", new String[] {content.getWorkspace().getName(), path});
                }
            } else {
                this.updateActivationDetails(path);
            }
            log.info("Exchange: activation succeeded [{}]", path);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.error("Exchange: activation failed for path:" + ((path != null) ? path : "[null]"), e);
                long timestamp = System.currentTimeMillis();
                log.warn("moving files from failed activation to *.failed" + timestamp );
                Iterator<File> keys = activationContent.getFiles().values().iterator();
                while (keys.hasNext()) {
                    File f = keys.next();
                    f.renameTo(new File(f.getAbsolutePath()+".failed" + timestamp));
                }
                activationContent.getFiles().clear();

            }
            throw new ExchangeException(e);
        } finally {
            log.debug("Cleaning temporary files");
            cleanTemporaryStore(activationContent);
        }
    }

    /**
     * @throws ExchangeException
     */
    public abstract void activate(ActivationContent activationContent, String nodePath) throws ExchangeException;


    /**
     * Send request of activation of activationContent to the subscriber. Subscriber might choose not to react if it is not subscribed to the URI under which activationContent exists.
     */
    public String activate(Subscriber subscriber, ActivationContent activationContent, String nodePath) throws ExchangeException {
        // FYI: this method is invoked from multiple threads at a same time (one for each subscriber, activationContent is assumed to be NOT shared between threads (cloned or by other means replicated) )
        log.debug("activate");
        if (null == subscriber) {
            throw new ExchangeException("Null Subscriber");
        }

        String parentPath = null;

        // concurrency: from path and repo name are same for all subscribers
        Subscription subscription = subscriber.getMatchedSubscription(nodePath, this.repositoryName);
        if (null != subscription) {
            // its subscribed since we found the matching subscription
            parentPath = this.getMappedPath(this.parent, subscription);
            activationContent.setProperty(PARENT_PATH, parentPath);
        } else {
            log.debug("Exchange : subscriber [{}] is not subscribed to {}", subscriber.getName(), nodePath);
            return "not subscribed";
        }
        log.debug("Exchange : sending activation request to {} with user {}", subscriber.getName(), this.user.getName());

        URLConnection urlConnection = null;
        String versionName = null;
        try {
            urlConnection = prepareConnection(subscriber, getActivationURL(subscriber));
            versionName = transportActivatedData(activationContent, urlConnection, null);

            String status = urlConnection.getHeaderField(ACTIVATION_ATTRIBUTE_STATUS);

            if (StringUtils.equals(status, ACTIVATION_HANDSHAKE)) {
                String handshakeKey = urlConnection.getHeaderField(ACTIVATION_AUTH);
                // receive all pending data
                urlConnection.getContent();

                // transport the data again
                urlConnection = prepareConnection(subscriber, getActivationURL(subscriber));
                // and get the version & status again
                versionName = transportActivatedData(activationContent, urlConnection, handshakeKey);
                status = urlConnection.getHeaderField(ACTIVATION_ATTRIBUTE_STATUS);
            }

            // check if the activation failed
            if (StringUtils.equals(status, ACTIVATION_FAILED)) {
                String message = urlConnection.getHeaderField(ACTIVATION_ATTRIBUTE_MESSAGE);
                throw new ExchangeException("Message received from subscriber: " + message);
            }
            urlConnection.getContent();
            log.debug("Exchange : activation request sent to {}", subscriber.getName());
        }
        catch (ExchangeException e) {
            throw e;
        }
        catch (IOException e) {
            log.debug("Failed to transport following activated content {" + StringUtils.join(activationContent.getProperties().keySet().iterator(), ',') + "} due to " + e.getMessage(), e);
            String url = (urlConnection == null ? null : urlConnection.getURL().toString());
            url = SecurityUtil.stripPasswordFromUrl(url);
            // hide pwd if present
            throw new ExchangeException("Not able to send the activation request [" + url + "]: " + e.getMessage(), e);
        }
        catch (Exception e) {
            throw new ExchangeException(e);
        }
        return versionName;
    }

    private String transportActivatedData(ActivationContent activationContent, URLConnection urlConnection, String handshakeKey) throws ExchangeException {
        String versionName;
        this.addActivationHeaders(urlConnection, activationContent, handshakeKey);

        Transporter.transport((HttpURLConnection) urlConnection, activationContent);

        versionName = urlConnection.getHeaderField(ACTIVATION_ATTRIBUTE_VERSION);
        return versionName;
    }

    /**
     * Cleans up temporary file store after activation.
     */
    protected void cleanTemporaryStore(ActivationContent activationContent) {
        if (activationContent == null) {
            log.debug("Clean temporary store - nothing to do");
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Debugging is enabled. Keeping temporary files in store for debugging purposes. Clean the store manually once done with debugging.");
            return;
        }

        Iterator<String> keys = activationContent.getFiles().keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            log.debug("Removing temporary file {}", key);
            activationContent.getFile(key).delete();
        }
    }

    public synchronized void deactivate(String path) throws ExchangeException, RepositoryException {
        final Content node = getHierarchyManager().getContent(path);
        deactivate(node);
    }

    /**
     * @param node to deactivate
     * @throws RepositoryException
     * @throws ExchangeException
     */
    @Override
    public synchronized void deactivate(Content node) throws ExchangeException, RepositoryException {
        String nodeUUID = node.getUUID();
        String path = node.getHandle();
        this.doDeactivate(nodeUUID, path);
        updateDeactivationDetails(nodeUUID);
    }

    /**
     * @param node , to deactivate
     * @param subscriber
     * @throws RepositoryException
     * @throws ExchangeException
     */
    @Override
    public synchronized void deactivate(Subscriber subscriber, Content node) throws ExchangeException, RepositoryException {
        String nodeUUID = node.getUUID();
        String path = node.getHandle();
        this.doDeactivate(subscriber, nodeUUID, path);
        updateDeactivationDetails(nodeUUID);
    }

    /**
     * @throws ExchangeException
     */
    public abstract void doDeactivate(String nodeUUID, String nodePath) throws ExchangeException;

    /**
     * Deactivate content from specified subscriber.
     * @param subscriber
     * @throws ExchangeException
     */
    public abstract String doDeactivate(Subscriber subscriber, String nodeUUID, String nodePath) throws ExchangeException;

    /**
     * Return URI set for deactivation.
     * @param subscriberInfo
     */
    protected String getDeactivationURL(Subscriber subscriberInfo) {
        return getActivationURL(subscriberInfo);
    }

    /**
     * Adds header fields describing deactivation request.
     * 
     * @param connection
     * @param handshakeKey
     *            optional key to encrypt public key before sending it over
     */
    protected void addDeactivationHeaders(URLConnection connection, String nodeUUID, String handshakeKey) {
        connection.addRequestProperty(REPOSITORY_NAME, this.repositoryName);
        connection.addRequestProperty(WORKSPACE_NAME, this.workspaceName);
        // TODO: how can this ever be null?? We don't send path along anywhere, so there's no way to delete anything w/o uuid, which means we pbly do not support deactivation of content w/o UUID!!!
        String md5 = "";
        if (nodeUUID != null) {
            connection.addRequestProperty(NODE_UUID, nodeUUID);
            // send md5 of uuid ... it would be silly to send clear text along the encrypted message
            md5 = SecurityUtil.getMD5Hex(nodeUUID);
        }
        // send md5 of uuid ... it would be silly to send clear text along the encrypted message
        String pass = System.currentTimeMillis() + ";" + this.user.getName() + ";" + md5;

        // optional
        addHandshakeInfo(connection, handshakeKey);

        connection.setRequestProperty(ACTIVATION_AUTH, SecurityUtil.encrypt(pass));
        connection.addRequestProperty(ACTION, DEACTIVATE);
    }

    protected void addHandshakeInfo(URLConnection connection, String handshakeKey) {
        if (handshakeKey != null) {
            connection.setRequestProperty(ACTIVATION_AUTH_KEY, SecurityUtil.encrypt(SecurityUtil.getPublicKey(), handshakeKey));
        }
    }
    
    /**
     * Retrieves URL subscriber is listening on for (de)activation requests.
     */
    protected String getActivationURL(Subscriber subscriberInfo) {
        final String url = subscriberInfo.getURL();
        if (!url.endsWith("/")) {
            return url + "/" + DEFAULT_HANDLER;
        }
        return url + DEFAULT_HANDLER;
    }

    /**
     * Adds headers fields describing activation request.
     * 
     * @param handshakeKey
     *            Optional key previously received from subscriber, used to encrypt activation public key for delivery to said subscriber. Or null when such key is not known or present.
     */
    protected void addActivationHeaders(URLConnection connection, ActivationContent activationContent, String handshakeKey) {

        String md5 = activationContent.getproperty(RESOURCE_MAPPING_MD_ATTRIBUTE);
        String pass = System.currentTimeMillis() + ";" + this.user.getName() + ";" + md5;
        activationContent.setProperty(ACTIVATION_AUTH, SecurityUtil.encrypt(pass));
        Iterator<String> headerKeys = activationContent.getProperties().keySet().iterator();
        while (headerKeys.hasNext()) {
            String key = headerKeys.next();
            if (RESOURCE_MAPPING_MD_ATTRIBUTE.equals(key)) {
                // do not send md5 in plain string
                continue;
            }
            String value = activationContent.getproperty(key);
            if(SystemProperty.getBooleanProperty(SystemProperty.MAGNOLIA_UTF8_ENABLED)) {
                try {
                    value = URLEncoder.encode(value, "UTF-8");
                }
                catch (UnsupportedEncodingException e) {
                    // do nothing
                }
            }
            connection.setRequestProperty(key, value);
        }
        addHandshakeInfo(connection, handshakeKey);
    }

    /**
     * Updates current content activation meta data with the time stamp and user details of the activation.
     */
    protected void updateActivationDetails(String path) throws RepositoryException {
        // page activated already use system context to ensure meta data is activated even if activating user has no rights to the activated page children
        Content page = getSystemHierarchyManager().getContent(path);
        updateMetaData(page, ACTIVATE);
        page.save();
        AuditLoggingUtil.log(AuditLoggingUtil.ACTION_ACTIVATE, this.workspaceName, page.getItemType(), path );
    }

    /**
     * Updates current content activation meta data with the timestamp and user details of the deactivation.
     */
    protected void updateDeactivationDetails(String nodeUUID) throws RepositoryException {
        // page deactivated already use system context to ensure meta data is activated even if activating user has no rights to the activated page children
        Content page = getSystemHierarchyManager().getContentByUUID(nodeUUID);
        updateMetaData(page, DEACTIVATE);
        page.save();
        AuditLoggingUtil.log(AuditLoggingUtil.ACTION_DEACTIVATE, this.workspaceName, page.getItemType(), page.getHandle() );
    }


    private HierarchyManager getHierarchyManager() {
        return MgnlContext.getHierarchyManager(this.workspaceName);
    }

    private HierarchyManager getSystemHierarchyManager() {
        return MgnlContext.getSystemContext().getHierarchyManager(this.workspaceName);
    }

    /**
     * @param node
     * @param type (activate / deactivate)
     */
    protected void updateMetaData(Content node, String type) throws AccessDeniedException {
        // update the passed node
        MetaData md = node.getMetaData();
        if (type.equals(ACTIVATE)) {
            md.setActivated();
        }
        else {
            md.setUnActivated();
        }
        md.setActivatorId(this.user.getName());
        md.setLastActivationActionDate();

        if(type.equals(ACTIVATE)){
            if(md.getModificationDate() != null && md.getModificationDate().after(contentVersionDate)){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                md.setModificationDate();
            }
        }

        Iterator<Content> children;
        if (type.equals(ACTIVATE)) {
            // use syndicator rule based filter
            children = node.getChildren(new RuleBasedContentFilter(contentFilterRule)).iterator();
        }
        else {
            // all children
            children = node.getChildren(ContentUtil.EXCLUDE_META_DATA_CONTENT_FILTER).iterator();
        }

        while (children.hasNext()) {
            Content child = children.next();
            this.updateMetaData(child, type);
        }


    }
    /**
     * Gets target path to which the current path is mapped in given subscription. Provided path should be without trailing slash.
     */
    protected String getMappedPath(String path, Subscription subscription) {
        String toURI = subscription.getToURI();
        if (null != toURI) {
            String fromURI = subscription.getFromURI();
            // remove trailing slash if any
            fromURI = StringUtils.removeEnd(fromURI, "/");
            toURI = StringUtils.removeEnd(toURI, "/");
            // apply path transformation if any
            path = path.replaceFirst(fromURI, toURI);
            if (path.equals("")) {
                path = "/";
            }
        }
        return path;
    }

    protected URLConnection prepareConnection(Subscriber subscriber, String urlString) throws ExchangeException {

        //String handle = getActivationURL(subscriber);

        try {
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(subscriber.getConnectTimeout());
            urlConnection.setReadTimeout(subscriber.getReadTimeout());

            return urlConnection;
        } catch (MalformedURLException e) {
            throw new ExchangeException("Incorrect URL for subscriber " + subscriber + "[" + SecurityUtil.stripPasswordFromUrl(urlString) + "]");
        } catch (IOException e) {
            throw new ExchangeException("Not able to send the activation request [" + SecurityUtil.stripPasswordFromUrl(urlString) + "]: " + e.getMessage());
        } catch (Exception e) {
            throw new ExchangeException(e);
        }
    }

    @Inject
    public void setResouceCollector(ResourceCollector resourceCollector) {
        this.resourceCollector = resourceCollector;
    }
}
