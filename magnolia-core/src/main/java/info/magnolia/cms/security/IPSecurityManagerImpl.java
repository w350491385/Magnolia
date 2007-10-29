/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.security;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.content2bean.PropertyTypeDescriptor;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class IPSecurityManagerImpl implements IPSecurityManager {
    private static final String ALL = "*";
    private Map rules;

    public IPSecurityManagerImpl() {
        this.rules = new HashMap();
    }

    public boolean isAllowed(HttpServletRequest req) {
        final Rule rule = getRule(req.getRemoteAddr());
        return rule != null && rule.allowsMethod(req.getMethod());
    }

    public boolean isAllowed(String ip) {
        return getRule(ip) != null;
    }

    protected Rule getRule(String ip) {
        if (rules.containsKey(ip)) {
            return (Rule) rules.get(ip);
        } else {
            return (Rule) rules.get(ALL);
        }
    }

    public Map getRules() {
        return rules;
    }

    public void setRules(Map rules) {
        this.rules = rules;
    }

    public void addRule(String name, Rule rule) {
        rules.put(name, rule);
    }

    public static final class Observer extends FactoryUtil.ObservedObjectFactory {
        public Observer() {
            super(ContentRepository.CONFIG, "/server/IPConfig", IPSecurityManager.class);
        }

        protected info.magnolia.content2bean.Content2BeanTransformer getContent2BeanTransformer() {
            return new Content2BeanTransformer();
        }
    }

    public static final class Content2BeanTransformer extends Content2BeanTransformerImpl {

        public void setProperty(TransformationState state, PropertyTypeDescriptor descriptor, Map values) {
            final Object currentBean = state.getCurrentBean();
            if (currentBean instanceof IPSecurityManagerImpl) {
                final IPSecurityManagerImpl ipSecMan = (IPSecurityManagerImpl) currentBean;
                final Iterator it = values.values().iterator();
                while (it.hasNext()) {
                    final Object o = it.next();
                    if (o instanceof Rule) {
                        final Rule rule = (Rule) o;
                        ipSecMan.addRule(rule.getIP(), rule);
                    }
                }
            }
            super.setProperty(state, descriptor, values);
        }

        protected TypeDescriptor onResolveClass(TransformationState state) {
            if (state.getLevel() == 2) {
                return this.getTypeMapping().getTypeDescriptor(Rule.class);
            }
            return super.onResolveClass(state);
        }

    }

    public static final class Rule {
        private String name;
        private String ip;
        private Set methods;

        public Rule() {
            this.methods = Collections.EMPTY_SET;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIP() {
            return ip;
        }

        public void setIP(String ip) {
            this.ip = ip;
        }

        public boolean allowsMethod(String s) {
            return methods.contains(s);
        }

        public String getMethods() {
            throw new IllegalStateException("Just faking a getter for content2bean's sake.");
        }

        public void setMethods(String methods) {
            this.methods = new TreeSet(String.CASE_INSENSITIVE_ORDER);
            this.methods.addAll(Arrays.asList(methods.split(",")));
        }
    }

}