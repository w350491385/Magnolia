/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.mail.commands;

import info.magnolia.cms.mail.MailModule;
import info.magnolia.cms.mail.MailTemplate;
import info.magnolia.cms.mail.MgnlMailFactory;
import info.magnolia.cms.mail.templates.MgnlEmail;
import info.magnolia.cms.mail.util.MailUtil;
import info.magnolia.context.WebContext;
import info.magnolia.cms.util.AlertUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * the command for sending mail
 * @author jackie
 * @author niko
 */
public class MailCommand implements Command {

    public static Logger log = LoggerFactory.getLogger(MailCommand.class);

    public boolean execute(Context ctx) {
        if (log.isDebugEnabled()) {
            log.debug("starting sending mail");
        }

        try {
            MgnlMailFactory factory = MailModule.getInstance().getFactory();
            MgnlEmail email;
            if (log.isDebugEnabled())
                log.debug(Arrays.asList(ctx.entrySet().toArray()).toString());

            String template = (String) ctx.get("mailTemplate");

            //find attachments in parameters or form if we are using one
            List attachments = MailUtil.createAttachmentList(((WebContext)ctx).getParameters());

            if (StringUtils.isNotEmpty(template)) {
                log.debug("Command using mail template: " + template);
                //get parameters
                if(ctx.containsKey(MailTemplate.MAIL_PARAMETERS)) {
                    Map temp = MailUtil.convertToMap((String)ctx.get(MailTemplate.MAIL_PARAMETERS));
                    ctx.putAll(temp);
                }
                email = factory.getEmailFromTemplate(template, attachments, ctx);
                email.setBodyFromResourceFile();
            }
            else {
                log.debug("command using static parameters");

                email = factory.getEmail(((WebContext)ctx).getParameters(), attachments);
                email.setBody();

            }
            factory.getEmailHandler().sendMail(email);

            log.info("send mail successfully to:" + ctx.get(MailTemplate.MAIL_TO));
        }
        catch (Exception e) {
            log.debug("Could not send email:" + e.getMessage(), e);
            log.error("Could not send email:" + e.getMessage());
            AlertUtil.setMessage("Error: " + e.getMessage());
            return false;
        }

        return true;
    }

}
