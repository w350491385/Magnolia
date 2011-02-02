/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.module.admincentral.tree;

import info.magnolia.module.admincentral.jcr.JCRMetadataUtil;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author daniellipp
 * @version $Id$
 */
public class MetaDataColumnTest {

    @Test
    public void testGetValue() throws RepositoryException {
        Node node = new MockNode();
        Node metaData = node.addNode(JCRMetadataUtil.META_DATA_NODE_NAME);
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        metaData.setProperty(MetaDataColumn.PROPERTY_NAME, cal);
        MetaDataColumn column = new MetaDataColumn();
        Object result = column.getValue(node);
        assertEquals(now, result);
    }

    @Test
    public void testSetValue() throws RepositoryException {
        Node node = new MockNode();
        Node metaData = node.addNode(JCRMetadataUtil.META_DATA_NODE_NAME);
        Calendar cal = Calendar.getInstance();
        metaData.setProperty(MetaDataColumn.PROPERTY_NAME, cal);
        TreeColumn< ? > column = new MetaDataColumn();
        column.setValue(node, cal.getTime());
        assertEquals(metaData.getProperty(MetaDataColumn.PROPERTY_NAME).getDate(), cal);
    }
}
