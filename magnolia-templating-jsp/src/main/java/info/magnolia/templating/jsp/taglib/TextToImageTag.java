/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.templating.jsp.taglib;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;

import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tldgen.annotations.BodyContent;
import org.tldgen.annotations.Tag;

/**
 * Tag that converts text into PNG images, and outputs a div element containing a set of img elements. The font face,
 * text color, text size and background color can be set via the tag attributes. <br />
 * <br />
 * The images are saved under the node specified by the attribute parentContentNodeName. Under this parent node, a new
 * node is created, with the name specified by the attribute imageContentNodeName. Under this node, each image will have
 * it own node. The names of the image node are based on the text that they contain. (Special characters such as &, /,
 * chinese characters, etc. are replaces by codes to ensure that these names are legal.) <br />
 * <br />
 * If the images for the specified text do not exist in the repository under the specified parent node, then the this
 * tag will create the images and save them. If the images for the text already exist, then they will not be recreated.
 * <br />
 * <br />
 * The text to be converted into images can be split in three ways. If the attribute textSplit is null or is set to
 * TEXT_SPLIT_NONE, then a single image will be created of the text on one line. If textSplit is set to
 * TEXT_SPLIT_WORDS, then the text is plit into words (i.e. wherever there is a space). Finally, if textSplit is set to
 * TEXT_SPLIT_CHARACTERS, then a seperate image is created for each letter. <br />
 * <br />
 * The tag outputs a div that contains one or more img's. The CSS class applied to the div is specified by the divCSS
 * attribute. The CSS applied to the images depends on how the text was split. For text that was not split, the CSS
 * applied is set to CSS_TEXT_IMAGE, for words it is CSS_WORD_IMAGE, and for characters it is CSS_CHARACTER_IMAGE. Any
 * spacing that is required between images will need to be set in your css stylesheet. <br />
 * <br />
 * The textFontFace attribute may either be a font name of a font installed on the server, or it may be a path to a TTF
 * file. The class to generate PNG images from TrueType font strings is originally by Philip McCarthy -
 * http://chimpen.com (http://chimpen.com/things/archives/001139.php). I have made a couple of small changes. <br />
 * <br />
 *
 * @jsp.tag name="txt2img" body-content="empty"
 *
 * @author Patrick Janssen
 * @author Fabrizio Giustina
 * @version 1.0
 */
@Tag(name="txt2img", bodyContent=BodyContent.EMPTY)

public class TextToImageTag extends BaseImageTag {

    /**
     * The image that is created can first be created at a larger size, and then scaled down. This overcomes kerning
     * problems on the Windows platform, which results in very irregular spacing between characters. If you are not
     * using Windows, this can be set to 1.
     */
    private static final double SCALE_FACTOR = SystemUtils.IS_OS_WINDOWS ? 4 : 1;

    /**
     * The CSS class applied to images of individual characters.
     */
    private static final String CSS_CHARACTER_IMAGE = "character-image";

    /**
     * The CSS class appled to images of words.
     */
    private static final String CSS_WORD_IMAGE = "word-image";

    /**
     * The CSS class applied to images of whole sentances, or other text.
     */
    private static final String CSS_TEXT_IMAGE = "text-image";

    /**
     * The text will not be split.
     */
    private static final String TEXT_SPLIT_NONE = "none";

    /**
     * The text will be split into words.
     */
    private static final String TEXT_SPLIT_WORDS = "words";

    /**
     * The text will be split into characters.
     */
    private static final String TEXT_SPLIT_CHARACTERS = "characters";

    private static final Logger log = LoggerFactory.getLogger(BaseImageTag.class);

    private String text;
    private String textFontFace;
    private int textFontSize;
    private String textFontColor;
    private String textBackColor;
    private String textSplit;
    private String divCSS;

    /**
     * The text to be converted.
     * @jsp.attribute required="true" rtexprvalue="true"
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * The name of the new contentNode to create, under which all image nodes will be saved.
     * @jsp.attribute required="true" rtexprvalue="true"
     */
    @Override
    public void setImageContentNodeName(String imageContentNodeName) {
        this.imageContentNodeName = imageContentNodeName;
    }

    /**
     * The name of the parent of the new contentNode.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    @Override
    public void setParentContentNodeName(String parentContentNodeName) {
        this.parentContentNodeName = parentContentNodeName;
    }

    /**
     * The font face of the text, e.g. 'Helvetica'. Default is 'Helvetica'.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setTextFontFace(String textFontFace) {
        this.textFontFace = textFontFace;
    }

    /**
     * The size of the text, in points, e.g. 12. Default is '12'.
     * @jsp.attribute required="false" rtexprvalue="true" type="int"
     */
    public void setTextFontSize(int textFontSize) {
        this.textFontSize = textFontSize;
    }

    /**
     * The color of the text in hexadecimal format, e.g. 'ff0044'. Default is '000000' (black).
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setTextFontColor(String textFontColor) {
        this.textFontColor = textFontColor;
    }

    /**
     * The color of the background in hexadecimal format, e.g. 'ff0044'. Default is 'ffffff' (white).
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setTextBackColor(String textBackColor) {
        this.textBackColor = textBackColor;
    }

    /**
     * The method used to split the text into sub-strings: 'none', 'words', or 'characters'. Default is 'none'.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setTextSplit(String textSplit) {
        this.textSplit = textSplit;
    }

    /**
     * The CSS class that will be applied to the div that contains these text images.
     * Defaults to "text-box".
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setDivCSS(String divCSS) {
        this.divCSS = divCSS;
    }

    /**
     * @see info.magnolia.cms.taglibs.util.BaseImageTag#getFilename()
     */
    @Override
    protected String getFilename() {
        return "textImage";
    }

    /**
     * Initialize settings.
     */
    public void setUp() {

        // check that all the necessary attributes are set
        if (this.text == null) {
            this.text = "Test Test Test";
        }
        if (this.textFontFace == null) {
            this.textFontFace = SystemUtils.IS_OS_WINDOWS ? "Arial" : "Helvetica";
        }
        if (this.textFontSize == 0) {
            this.textFontSize = 12;
        }
        if (this.textFontColor == null) {
            this.textFontColor = "000000";
        }
        if (this.textBackColor == null) {
            this.textBackColor = "ffffff";
        }
        if (this.textSplit == null) {
            this.textSplit = TEXT_SPLIT_NONE;
        }
        else if (!((this.textSplit.equals(TEXT_SPLIT_WORDS)) || (this.textSplit.equals(TEXT_SPLIT_CHARACTERS)))) {
            this.textSplit = TEXT_SPLIT_NONE;
        }
        if (this.divCSS == null) {
            this.divCSS = "text-box";
        }
    }

    @Override
    public void doTag() throws JspException {

        this.setUp();

        try {
            Content imageContentNode = ContentUtil.asContent(getImageContentNode());

            String[] subStrings = this.getTextSubStrings(this.text);
            String[] imageURIs = this.getImageURIs(
                subStrings,
                (HttpServletRequest) ((PageContext) this.getJspContext()).getRequest(),
                imageContentNode);
            this.drawTextImages(imageURIs, subStrings);
        }
        catch (PathNotFoundException e) {
            log.error("PathNotFoundException occured during text-to-image conversion: " + e.getMessage(), e);
        }
        catch (AccessDeniedException e) {
            log.error("AccessDeniedException occured during text-to-image conversion: " + e.getMessage(), e);
        }
        catch (RepositoryException e) {
            log.error("RepositoryException occured during text-to-image conversion: " + e.getMessage(), e);
        }
        catch (FileNotFoundException e) {
            log.error("FileNotFoundException occured during text-to-image conversion: " + e.getMessage(), e);
        }
        catch (IOException e) {
            log.error("IOException occured during text-to-image conversion: " + e.getMessage(), e);
        }
        catch (FontFormatException e) {
            log.error("FontFormatException occured during text-to-image conversion: " + e.getMessage(), e);
        }
        this.cleanUp();
    }

    /**
     * Set objects to null.
     */
    public void cleanUp() {
        this.parentContentNodeName = null;
        this.imageContentNodeName = null;
        this.text = null;
        this.textFontFace = null;
        this.textFontSize = 0;
        this.textFontColor = null;
        this.textBackColor = null;
        this.textSplit = null;
        this.divCSS = null;
    }

    /**
     * Draws a div box that contains the text images.
     * @param imageURLs an array of urls
     * @param subStrings an array of strings
     * @throws IOException jspwriter exception
     */
    private void drawTextImages(String[] imageURIs, String[] subStrings) throws IOException {
        JspWriter out = this.getJspContext().getOut();

        if (this.divCSS != null) {
            out.print("<div class=\"");
            out.print(this.divCSS);
            out.print("\">");
        }

        for (int i = 0; i < imageURIs.length; i++) {
            out.print("<img class=\"");
            if (this.textSplit.equals(TEXT_SPLIT_CHARACTERS)) {
                out.print(CSS_CHARACTER_IMAGE);
            }
            else if (this.textSplit.equals(TEXT_SPLIT_WORDS)) {
                out.print(CSS_WORD_IMAGE);
            }
            else {
                out.print(CSS_TEXT_IMAGE);
            }
            out.print("\" src=\"");
            out.print(imageURIs[i]);
            out.print("\" alt=\"");
            out.print(subStrings[i]);
            out.print("\" />");
        }

        if (this.divCSS != null) {
            out.print("</div>");
        }
    }

    /**
     * Splits a string into words or characters, depending on the textSplit attribute. For words, spaces at either end
     * are removed.
     * @param The string to split
     * @return An array of words
     */
    private String[] getTextSubStrings(String text) {
        String[] subStrings = null;
        if (this.textSplit.equals(TEXT_SPLIT_CHARACTERS)) {
            subStrings = new String[text.length()];
            for (int i = 0; i < text.length(); i++) {
                subStrings[i] = text.substring(i, i + 1);
            }
        }
        else if (this.textSplit.equals(TEXT_SPLIT_WORDS)) {
            StringTokenizer st = new StringTokenizer(text, " "); // Split sentence into words
            subStrings = new String[st.countTokens()];
            for (int i = 0; st.hasMoreTokens(); i++) {
                subStrings[i] = st.nextToken().trim();
            }
        }
        else {
            subStrings = new String[]{text};
        }
        return subStrings;
    }

    /**
     * Get an array of image URIs, one URI for each text string.
     * @param The array of text strings.
     * @return An array of URIs pointing to the images.
     */
    private String[] getImageURIs(String[] subStrings, HttpServletRequest req, Content imageContentNode)
        throws PathNotFoundException, AccessDeniedException, RepositoryException, FileNotFoundException, IOException,
        FontFormatException {

        String[] imageURIs = new String[subStrings.length];
        for (int i = 0; i < subStrings.length; i++) {
            // Create a unique image node name
            String tmpImgNodeName = subStrings[i]
                + this.textBackColor
                + this.textFontColor
                + this.textFontFace
                + this.textFontSize;
            String imageNodeName = this.convertToSimpleString(tmpImgNodeName);
            // If the image node with this name does not exist, then create it.
            if (!imageContentNode.hasContent(imageNodeName)) {
                File image = createImage(subStrings[i]);

                // Create the node that will contain the image
                Content imageNode = imageContentNode.createContent(imageNodeName, ItemType.CONTENTNODE);

                this.createImageNode(image, imageNode);
            }
            // Save the URI for this image in the array
            String contextPath = req.getContextPath();
            String handle = imageContentNode.getHandle();
            String imageURI = contextPath
                + handle
                + "/"
                + imageNodeName
                + "/"
                + getFilename()
                + "."
                + PROPERTIES_EXTENSION_VALUE;
            imageURIs[i] = imageURI;
        }
        return imageURIs;
    }

    /**
     * Creates an image from a word. The file is saved in the location specified by TEMP_IMAGE_PATH.
     * @param subString The text.
     * @return An input stream.
     */
    private File createImage(String subString) throws FileNotFoundException, IOException, FontFormatException {

        // Create file
        File imageFile = File.createTempFile(getClass().getName(), "png");
        imageFile.createNewFile();

        // create the image
        // due to kerning problems, the image is being created 4 times to big
        // then being scaled down to the right size
        Text2PngFactory tpf = new Text2PngFactory();
        tpf.setFontFace(this.textFontFace);
        tpf.setFontSize((int) (this.textFontSize * SCALE_FACTOR));
        int[] textRGB = this.convertHexToRGB(this.textFontColor);
        int[] backRGB = this.convertHexToRGB(this.textBackColor);
        tpf.setTextRGB(textRGB[0], textRGB[1], textRGB[2]);
        tpf.setBackgroundRGB(backRGB[0], backRGB[1], backRGB[2]);
        tpf.setText(subString);

        BufferedImage bigImgBuff = (BufferedImage) tpf.createImage();
        if (SCALE_FACTOR != 1) {
            BufferedImage smallImgBuff = this.scaleImage(bigImgBuff, (1.0 / SCALE_FACTOR));
            ImageIO.write(smallImgBuff, "png", imageFile);
            smallImgBuff = null;
        }
        else {
            ImageIO.write(bigImgBuff, "png", imageFile);
        }
        bigImgBuff = null;
        return imageFile;
    }

    /**
     * Create an image file that is a scaled version of the original image.
     * @param the original BufferedImage
     * @param the scale factor
     * @return the new BufferedImage
     */
    private BufferedImage scaleImage(BufferedImage oriImgBuff, double scaleFactor) {

        // get the dimesnions of the original image
        int oriWidth = oriImgBuff.getWidth();
        int oriHeight = oriImgBuff.getHeight();
        // get the width and height of the new image
        int newWidth = new Double(oriWidth * scaleFactor).intValue();
        int newHeight = new Double(oriHeight * scaleFactor).intValue();
        // create the thumbnail as a buffered image
        Image newImg = oriImgBuff.getScaledInstance(newWidth, newHeight, Image.SCALE_AREA_AVERAGING);
        BufferedImage newImgBuff = new BufferedImage(
            newImg.getWidth(null),
            newImg.getHeight(null),
            BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImgBuff.createGraphics();
        g.drawImage(newImg, 0, 0, null);
        g.dispose();
        // return the newImgBuff
        return newImgBuff;
    }

}
