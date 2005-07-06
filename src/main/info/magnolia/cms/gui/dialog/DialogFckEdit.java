/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.ControlSuper;
import info.magnolia.cms.util.LinkUtil;

import java.io.IOException;
import java.io.Writer;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * An Magnolia dialog for the universal usage and configuration of the fckeditor. Credits for FCKEditor:
 * http://www.fckeditor.net/
 * @author bert schulzki
 * @author Fabrizio Giustina
 * @version 1.0 11.10.2004
 */
public class DialogFckEdit extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogFckEdit.class);

    /**
     * The new .BasePath of the editor
     */
    public static final String FCKEDIT_PATH = "/admindocroot/fckeditor/";

    /**
     * This parameter defines the startup script. This parameter is searched in the dialog configuration.
     */
    public static final String PARAM_JS_INIT_FILE = "jsInitFile";

    /**
     * This parameter defines the configuration script
     */
    public static final String PARAM_CUSTOM_CONFIGURATION_PATH = "customConfigurationPath";

    /**
     * If jsInitFile is not defined
     */
    public static final String PARAM_JS_INIT_FILE_DEFAULT = "/admindocroot/fckeditor/custom/init/magnoliaStandard.js";

    /**
     * If customConfigurationPath is not defined
     */
    public static final String PARAM_CUSTOM_CONFIGURATION_PATH_DEFAULT = "/admindocroot/fckeditor/custom/config/magnoliaStandard.js";

    /**
     * the configuration script name
     */
    private String customConfigurationsPath = PARAM_CUSTOM_CONFIGURATION_PATH_DEFAULT;

    /**
     * the initialization script name
     */
    private String jsInitFile = PARAM_JS_INIT_FILE_DEFAULT;

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogFckEdit() {
    }

    /**
     * @return The name of the variable for the editor object
     */
    public String getVarName() {
        String id = getId();
        if (id == null) {
            id = getName();
        }
        return "fck_" + id.replace('-', '_');
    }

    /**
     * @param name script name
     */
    public void setCustomConfigurationPath(String name) {
        if (name != null) {
            customConfigurationsPath = name;
        }
    }

    /**
     * @param name init file
     */
    public void setJSInitFile(String name) {
        if (name != null) {
            jsInitFile = name;
        }
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#init(HttpServletRequest, HttpServletResponse, Content, Content)
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
        String jsInitFile = this.getConfigValue(PARAM_JS_INIT_FILE, PARAM_JS_INIT_FILE_DEFAULT);
        String customConfigurationPath = this.getConfigValue(
            PARAM_CUSTOM_CONFIGURATION_PATH,
            PARAM_CUSTOM_CONFIGURATION_PATH_DEFAULT);
        this.setJSInitFile(jsInitFile);
        this.setCustomConfigurationPath(customConfigurationPath);
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        this.drawHtmlPre(out);

        // load the script onece: if there are multiple instances
        if (getRequest().getAttribute("__fcked_loaded") == null) {
            out.write("<script type=\"text/javascript\" src=\""
                + this.getRequest().getContextPath()
                + "/admindocroot/fckeditor/fckeditor.js\"></script>");
            getRequest().setAttribute("__fcked_loaded", "true");
        }

        String id = getName();

        if (id == null) {
            log.error("Missing id for fckEditor instance");
        }

        String var = getVarName();
        String value = convertToView(getValue());
        value = LinkUtil.convertUUIDsToAbsoluteLinks(value);
        out.write("<script type=\"text/javascript\">");
        out.write("var " + var + " = null;");
        out.write("fckInstance = new FCKeditor( '" + id + "' );");
        out.write("fckInstance.Value = '" + escapeJsValue(value) + "';");
        out.write("fckInstance.BasePath = '" + this.getRequest().getContextPath() + FCKEDIT_PATH + "';");
        if (customConfigurationsPath.length() > 0) {
            out.write("fckInstance.Config['CustomConfigurationsPath'] = '"
                + this.getRequest().getContextPath()
                + customConfigurationsPath
                + "';");
        }
        if (jsInitFile.length() > 0) {
            out.write("</script>");
            out.write("<script type=\"text/javascript\" src=\""
                + this.getRequest().getContextPath()
                + jsInitFile
                + "\"></script>\n");
            out.write("<script type=\"text/javascript\">");
        }
        out.write("fckInstance.Create();");
        out.write(var + " = fckInstance;");
        out.write("</script>");

        // write the saveInfo for the writting back to the repository
        out.write("<input type='hidden' name='mgnlSaveInfo' value='"
            + id
            + ",String,"
            + ControlSuper.VALUETYPE_SINGLE
            + ","
            + ControlSuper.RICHEDIT_FCK
            + ","
            + ControlSuper.ENCODING_NO
            + "' />");

        this.drawHtmlPost(out);

        // out.write("</td>");
        // out.write("</tr>");
    }

    /**
     * @param value
     * @return
     */
    private static String convertToView(String value) {
        String tmp = value;
        if (tmp != null) {
            tmp = tmp.replaceAll("\r\n", "<br />");
            tmp = tmp.replaceAll("\n", "<br />");
            return tmp;
        }
        return StringUtils.EMPTY;
    }

    /**
     * Replacements:
     * 
     * <pre>
     * ' -> \'
     * " -> \"
     * \r\n -> \\r\\n
     * \n -> \\n
     * \ -> \\
     * </pre>
     * 
     * @param src
     * @return escaped js String
     */
    public static String escapeJsValue(String src) {
        if (src == null) {
            return null;
        }
        String escapedSrc = src.replaceAll("'", "\\\\'");
        escapedSrc = escapedSrc.replaceAll("\"", "\\\"");
        escapedSrc = escapedSrc.replaceAll("\\r\\n", "\\\\r\\\\n");
        escapedSrc = escapedSrc.replaceAll("\\n", "\\\\n");

        return escapedSrc;
    }
}