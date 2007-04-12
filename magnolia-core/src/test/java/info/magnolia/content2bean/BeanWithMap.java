/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.content2bean;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class BeanWithMap extends SimpleBean {

    private Map beans;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(BeanWithMap.class);


    /**
     * @return the beans
     */
    public Map getBeans() {
        return this.beans;
    }


    /**
     * @param beans the beans to set
     */
    public void setBeans(Map map) {
        this.beans = map;
    }
}
