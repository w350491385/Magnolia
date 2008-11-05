/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.filters;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import junit.framework.TestCase;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import static org.easymock.EasyMock.*;

import org.easymock.IAnswer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class MultipartRequestFilterTest extends TestCase {
    private static final File TESTFILE = new File("pom.xml");
    private HttpServletRequest req;
    private HttpServletResponse res;
    private FilterChain filterChain;

    protected void setUp() throws Exception {
        super.setUp();

        SystemProperty.setProperty(SystemProperty.MAGNOLIA_UPLOAD_TMPDIR, System.getProperty("java.io.tmpdir"));

        req = createMock(HttpServletRequest.class);
        res = createNiceMock(HttpServletResponse.class);
        filterChain = createNiceMock(FilterChain.class);
    }

    public void testFilterCOS() throws Throwable {
        doTest(new CosMultipartRequestFilter(), "text/xml");
    }

    public void testFilterCommonsFileUpload() throws Throwable {
        doTest(new MultipartRequestFilter(), "text/xml; charset=UTF-8");
    }

    public void doTest(Filter filter, final String expectedDocumentType) throws Throwable {
        final MultipartRequestEntity multipart = newMultipartRequestEntity();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        multipart.writeRequest(output);
        final byte[] bytes = output.toByteArray();
        final ByteArrayInputStream delegateStream = new ByteArrayInputStream(bytes);
        final ServletInputStream servletInputStream = new ServletInputStream() {
            public int read() throws IOException {
                return delegateStream.read();
            }
        };

        WebContext webCtx = createMock(WebContext.class);
        MgnlContext.setInstance(webCtx);

        webCtx.push(req, res);
        expect(req.getContentType()).andReturn(multipart.getContentType()).anyTimes();
        expect(req.getHeader("Content-Type")).andReturn(multipart.getContentType()).anyTimes();
        expect(req.getCharacterEncoding()).andReturn("UTF-8").anyTimes();
        expect(req.getQueryString()).andReturn("").anyTimes();
        expect(req.getContentLength()).andReturn(new Integer((int) multipart.getContentLength())).anyTimes();
        expect(req.getInputStream()).andReturn(servletInputStream);
        webCtx.pop();
        req.setAttribute(eq(MultipartForm.REQUEST_ATTRIBUTE_NAME), isA(MultipartForm.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                final Object[] args = getCurrentArguments();
                checkMultipartForm((MultipartForm) args[1], expectedDocumentType);
                return null;
            }
        });

        replay(req, res, filterChain, webCtx);
        filter.doFilter(req, res, filterChain);
        verify(req, res, filterChain, webCtx);
    }

    private void checkMultipartForm(MultipartForm form, String expectedDocumentType) throws IOException {
        assertNotNull("MultipartForm request attribute expected", form);
        assertEquals(3, form.getParameters().size());
        assertEquals("value1", form.getParameter("param1"));
        assertEquals("àèìòù", form.getParameter("param2"));

        String[] value3 = form.getParameterValues("param3");
        assertNotNull("multi-value parameter has not been parsed", value3);
        assertEquals(2, value3.length);

        assertEquals(1, form.getDocuments().size());

        Document document = form.getDocument("document");
        assertNotNull("expected non-null Document", document);
        assertEquals("document", document.getAtomName());
        assertEquals("xml", document.getExtension());
        assertEquals("pom", document.getFileName());
        assertEquals("pom.xml", document.getFileNameWithExtension());
        assertEquals(TESTFILE.length(), document.getLength());

        assertEquals(expectedDocumentType, document.getType());

        assertTrue(document.getType().startsWith("text/xml"));

        File file1 = document.getFile();
        assertTrue(file1.exists());
        assertTrue(file1.canRead());
        InputStream stream1 = document.getStream();
        assertEquals(TESTFILE.length(), stream1.available());
        assertEquals(TESTFILE.length(), stream1.skip(TESTFILE.length()));
        assertEquals(0, stream1.available());
    }


    private MultipartRequestEntity newMultipartRequestEntity() throws Exception {
        PostMethod method = new PostMethod();
        Part[] parts = {
                new StringPart("param1", "value1", "UTF-8"),
                new StringPart("param2", "àèìòù", "UTF-8"),
                new StringPart("param3", "value3a", "UTF-8"),
                new StringPart("param3", "value3b", "UTF-8"),
                new FilePart("document", TESTFILE, "text/xml", "UTF-8")};

        return new MultipartRequestEntity(parts, method.getParams());
    }
}
