/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.cms.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;

import javax.jcr.AccessDeniedException;
import javax.jcr.Credentials;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.NamespaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.retention.RetentionManager;
import javax.jcr.security.AccessControlManager;
import javax.jcr.version.VersionException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Wrapper executing all calls on the delegated instance of Session.
 * @author had
 * @version $Id: $
 */
public abstract class DelegateSessionWrapper implements Session {

    public abstract Session getDelegate();

    @Override
    public void addLockToken(String lt) {
        getDelegate().addLockToken(lt);
    }

    @Override
    public void checkPermission(String absPath, String actions) throws AccessControlException, RepositoryException {
        getDelegate().checkPermission(absPath, actions);
    }

    @Override
    public void exportDocumentView(String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse) throws PathNotFoundException,
    SAXException, RepositoryException {
        getDelegate().exportDocumentView(absPath, contentHandler, skipBinary, noRecurse);
    }

    @Override
    public void exportDocumentView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse) throws IOException, PathNotFoundException,
    RepositoryException {
        getDelegate().exportDocumentView(absPath, out, skipBinary, noRecurse);
    }

    @Override
    public void exportSystemView(String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse) throws PathNotFoundException,
    SAXException, RepositoryException {
        getDelegate().exportSystemView(absPath, contentHandler, skipBinary, noRecurse);
    }

    @Override
    public void exportSystemView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse) throws IOException, PathNotFoundException,
    RepositoryException {
        getDelegate().exportSystemView(absPath, out, skipBinary, noRecurse);
    }

    @Override
    public AccessControlManager getAccessControlManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        return getDelegate().getAccessControlManager();
    }

    @Override
    public Object getAttribute(String name) {
        return getDelegate().getAttribute(name);
    }

    @Override
    public String[] getAttributeNames() {
        return getDelegate().getAttributeNames();
    }

    @Override
    public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehavior) throws PathNotFoundException, ConstraintViolationException,
    VersionException, LockException, RepositoryException {
        return getDelegate().getImportContentHandler(parentAbsPath, uuidBehavior);
    }

    @Override
    public Item getItem(String absPath) throws PathNotFoundException, RepositoryException {
        return getDelegate().getItem(absPath);
    }

    @Override
    public String[] getLockTokens() {
        return getDelegate().getLockTokens();
    }

    @Override
    public String getNamespacePrefix(String uri) throws NamespaceException, RepositoryException {
        return getDelegate().getNamespacePrefix(uri);
    }

    @Override
    public String[] getNamespacePrefixes() throws RepositoryException {
        return getDelegate().getNamespacePrefixes();
    }

    @Override
    public String getNamespaceURI(String prefix) throws NamespaceException, RepositoryException {
        return getDelegate().getNamespaceURI(prefix);
    }

    @Override
    public Node getNode(String absPath) throws PathNotFoundException, RepositoryException {
        return getDelegate().getNode(absPath);
    }

    @Override
    public Node getNodeByIdentifier(String id) throws ItemNotFoundException, RepositoryException {
        return getDelegate().getNodeByIdentifier(id);
    }

    @Override
    public Node getNodeByUUID(String uuid) throws ItemNotFoundException, RepositoryException {
        return getDelegate().getNodeByUUID(uuid);
    }

    @Override
    public Property getProperty(String absPath) throws PathNotFoundException, RepositoryException {
        return getDelegate().getProperty(absPath);
    }

    @Override
    public Repository getRepository() {
        return getDelegate().getRepository();
    }

    @Override
    public RetentionManager getRetentionManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        return getDelegate().getRetentionManager();
    }

    @Override
    public Node getRootNode() throws RepositoryException {
        return getDelegate().getRootNode();
    }

    @Override
    public String getUserID() {
        return getDelegate().getUserID();
    }

    @Override
    public ValueFactory getValueFactory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return getDelegate().getValueFactory();
    }

    @Override
    public Workspace getWorkspace() {
        return getDelegate().getWorkspace();
    }

    @Override
    public boolean hasCapability(String methodName, Object target, Object[] arguments) throws RepositoryException {
        return getDelegate().hasCapability(methodName, target, arguments);
    }

    @Override
    public boolean hasPendingChanges() throws RepositoryException {
        return getDelegate().hasPendingChanges();
    }

    @Override
    public boolean hasPermission(String absPath, String actions) throws RepositoryException {
        return getDelegate().hasPermission(absPath, actions);
    }

    @Override
    public Session impersonate(Credentials credentials) throws LoginException, RepositoryException {
        return getDelegate().impersonate(credentials);
    }

    @Override
    public void importXML(String parentAbsPath, InputStream in, int uuidBehavior) throws IOException, PathNotFoundException, ItemExistsException,
    ConstraintViolationException, VersionException, InvalidSerializedDataException, LockException, RepositoryException {
        getDelegate().importXML(parentAbsPath, in, uuidBehavior);
    }

    @Override
    public boolean isLive() {
        return getDelegate().isLive();
    }

    @Override
    public boolean itemExists(String absPath) throws RepositoryException {
        return getDelegate().itemExists(absPath);
    }

    @Override
    public void logout() {
        getDelegate().logout();
    }

    @Override
    public void move(String srcAbsPath, String destAbsPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException,
    LockException, RepositoryException {
        getDelegate().move(srcAbsPath, destAbsPath);
    }

    @Override
    public boolean nodeExists(String absPath) throws RepositoryException {
        return getDelegate().nodeExists(absPath);
    }

    @Override
    public boolean propertyExists(String absPath) throws RepositoryException {
        return getDelegate().propertyExists(absPath);
    }

    @Override
    public void refresh(boolean keepChanges) throws RepositoryException {
        getDelegate().refresh(keepChanges);
    }

    @Override
    public void removeItem(String absPath) throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException {
        getDelegate().removeItem(absPath);
    }

    @Override
    public void removeLockToken(String lt) {
        getDelegate().removeLockToken(lt);
    }

    @Override
    public void save() throws AccessDeniedException, ItemExistsException, ReferentialIntegrityException, ConstraintViolationException,
    InvalidItemStateException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        getDelegate().save();
    }

    @Override
    public void setNamespacePrefix(String prefix, String uri) throws NamespaceException, RepositoryException {
        getDelegate().setNamespacePrefix(prefix, uri);
    }

}
