package info.magnolia.cms.core.ie;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ie.filters.ImportXmlRootFilter;
import info.magnolia.cms.core.ie.filters.VersionFilter;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Iterator;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.log4j.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class DataTransporter {
    
    static Logger log = Logger.getLogger(DataTransporter.class.getSimpleName());
    
    /**
     * Perform import.
     * 
     * @param repository selected repository
     * @param basepath base path in repository
     * @param xmlFile uploaded file
     * @param keepVersionHistory if <code>false</code> version info will be stripped before importing the document
     * @param importMode a valid value for ImportUUIDBehavior
     * @see ImportUUIDBehavior
     */
    public static synchronized void executeImport(String basepath,
            String repository, Document xmlFile, boolean keepVersionHistory,
            int importMode,boolean saveAfterImport) throws IOException {
        HierarchyManager hr = MgnlContext.getHierarchyManager(repository);
        Workspace ws = hr.getWorkspace();

        if (log.isInfoEnabled()) {
            String message = "Importing content into repository: ["+repository+"] from File: ["+xmlFile.getFileName()+ "] into path:"+basepath;
            log.info(message); //$NON-NLS-1$
        }

        InputStream stream = xmlFile.getStream();
        Session session = ws.getSession();

        try {
            if (keepVersionHistory) {
                // do not manipulate
                session.importXML(basepath, stream, importMode);
            }

            else {
                ContentHandler handler = session.getImportContentHandler(
                        basepath, importMode);

                XMLReader filteredReader = new ImportXmlRootFilter(
                        new VersionFilter(XMLReaderFactory.createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName())));
                filteredReader.setContentHandler(handler);

                // import it
                try {
                    filteredReader.parse(new InputSource(stream));
                } finally {
                    IOUtils.closeQuietly(stream);
                }

                if (((ImportXmlRootFilter) filteredReader).rootNodeFound) {
                    String path = basepath;
                    if (!path.endsWith("/")) {
                        path += "/";
                    }

                    Node dummyRoot = (Node) session.getItem(path + "jcr:root");
                    for (Iterator iter = dummyRoot.getNodes(); iter.hasNext();) {
                        Node child = (Node) iter.next();
                        // move childs to real root

                        if (session.itemExists(path + child.getName())) {
                            session.getItem(path + child.getName()).remove();
                        }

                        session.move(child.getPath(), path + child.getName());
                    }
                    // delete the dummy node
                    dummyRoot.remove();
                }
            }
        } catch (Exception e) {
            throw new NestableRuntimeException(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        
        try {
            if (saveAfterImport)
                session.save();
        } catch (RepositoryException e) {
            log.error(MessageFormat.format("Unable to save changes to the [{0}] repository due to a {1} Exception: {2}.", //$NON-NLS-1$
                                            new Object[] { repository,
                                                    e.getClass().getName(),
                                                    e.getMessage() }), e);
            throw new IOException(e.getMessage());
        }
    }

}
