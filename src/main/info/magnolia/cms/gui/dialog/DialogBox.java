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

import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.i18n.TemplateMessagesUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogBox extends DialogSuper {

    public static final int BOXTYPE_2COLS = 0;

    public static final int BOXTYPE_1COL = 1;

    public static final String ICONS_GENERAL = "general.gif";

    public static final String ICONS_FOLDER = "folder.gif";

    public static final String ICONS_PATH = "/admindocroot/fileIcons/";

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogBox.class);

    private static Map iconExtensions = new Hashtable();

    private int boxType = BOXTYPE_2COLS;

    static {
        DialogBox.initIconExtensions();
    }

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogBox() {
    }

    public void setBoxType(int i) {
        this.boxType = i;
    }

    public int getBoxType() {
        String configBoxType = this.getConfigValue("boxType");
        if (configBoxType.equals("1Col")) {
            return BOXTYPE_1COL;
        }
        return this.boxType;

    }

    public void drawHtmlPre(Writer out) throws IOException {
        if (this.getConfigValue("lineSemi", "false").equals("true")) {
            out.write(new DialogLine().getHtml(1, 1));
        }
        else if (this.getConfigValue("line", "true").equals("true")) {
            out.write(new DialogLine().getHtml());
        }
        out.write("<tr>");
        if (this.getBoxType() == BOXTYPE_2COLS) {
            out.write("<td width=\"1%\" class=\"" + CssConstants.CSSCLASS_BOXLABEL + "\">");
            // write the label
            out.write(TemplateMessagesUtil.get(this.getRequest(), this.getLabel()));
            if (StringUtils.isNotEmpty(this.getConfigValue("labelDescription"))) {
                String desc = this.getConfigValue("labelDescription");
                desc = TemplateMessagesUtil.get(this.getRequest(), desc);
                out.write("<div class=\"" + CssConstants.CSSCLASS_DESCRIPTION + "\">" + desc + "</div>");
            }
            out.write("</td>");
            String cssClass = CssConstants.CSSCLASS_BOXINPUT;
            if (this.getClass().getName().indexOf("DialogStatic") != -1
                || this.getClass().getName().indexOf("DialogButton") != -1) {
                cssClass = CssConstants.CSSCLASS_BOXLABEL;
            }
            out.write("<td width=\"100%\" class=\"" + cssClass + "\">");
        }
        else {
            out.write("<td width=\"100%\" colspan=\"2\" class=\"" + CssConstants.CSSCLASS_BOXLABEL + "\">");
            if (StringUtils.isNotEmpty(this.getLabel())) {
                out.write("<div class=\""
                    + CssConstants.CSSCLASS_BOXLABEL
                    + "\">"
                    + TemplateMessagesUtil.get(this.getRequest(), this.getLabel())
                    + "</div>");
            }
            if (StringUtils.isNotEmpty(this.getConfigValue("labelDescription"))) {
                String desc = this.getConfigValue("labelDescription");
                out.write("<div class=\""
                    + CssConstants.CSSCLASS_DESCRIPTION
                    + "\">"
                    + TemplateMessagesUtil.get(this.getRequest(), desc)
                    + "</div>");
            }
        }
    }

    public void drawHtmlPost(Writer out) throws IOException {
        out.write(this.getHtmlDescription());
        out.write("</td></tr>");
    }

    public String getHtmlDescription() {

        // use div to force a new line
        if (StringUtils.isNotEmpty(this.getDescription())) {
            String desc = this.getDescription();
            desc = TemplateMessagesUtil.get(this.getRequest(), desc);
            return "<div class=\"" + CssConstants.CSSCLASS_DESCRIPTION + "\">" + desc + "</div>";
        }

        return StringUtils.EMPTY;
    }

    public static void initIconExtensions() {
        getIconExtensions().put("doc", "");
        getIconExtensions().put("eps", "");
        getIconExtensions().put("gif", "");
        getIconExtensions().put("jpg", "");
        getIconExtensions().put("jpeg", ICONS_PATH + "jpg.gif");
        getIconExtensions().put("pdf", "");
        getIconExtensions().put("ppt", "");
        getIconExtensions().put("tif", "");
        getIconExtensions().put("tiff", ICONS_PATH + "tif.gif");
        getIconExtensions().put("xls", "");
        getIconExtensions().put("zip", "");
    }

    public String getIconPath(String name) {
        // name might be name (e.g. "bla.gif") or extension (e.g. "gif")
        String iconPath = ICONS_PATH + ICONS_GENERAL;
        String ext;
        if (name.indexOf(".") != -1) {
            ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        }
        else {
            ext = name;
        }
        if (this.getIconExtensions().containsKey(ext)) {
            iconPath = (String) this.getIconExtensions().get(ext);
            if (StringUtils.isEmpty(iconPath)) {
                iconPath = ICONS_PATH + ext + ".gif";
            }
        }
        return iconPath;
    }

    public static Map getIconExtensions() {
        return iconExtensions;
    }

    public static void setIconExtensions(Map t) {
        iconExtensions = t;
    }

    public void setIconExtensions(String extension, String iconPath) {
        this.iconExtensions.put(extension, iconPath);
    }
}