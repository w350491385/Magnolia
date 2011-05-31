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
package info.magnolia.ui.admincentral.dialog.field;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.vaadin.ui.Field;
import info.magnolia.ui.framework.editor.EditorDelegate;
import info.magnolia.ui.framework.editor.EditorError;
import info.magnolia.ui.framework.editor.HasEditorDelegate;
import info.magnolia.ui.framework.editor.HasEditorErrors;
import info.magnolia.ui.framework.editor.ValueEditor;
import info.magnolia.ui.model.dialog.definition.FieldDefinition;

/**
 * Adapter class that adapts a Vaadin Field as an Editor.
 *
 * @param <T> the type of the value that the editor works with.
 * @author tmattsson
 */
public class VaadinEditorAdapter<T> implements ValueEditor<T>, HasEditorDelegate, HasEditorErrors {

    private Field field;
    private FieldDefinition fieldDefinition;
    private EditorDelegate delegate;
    private Class<T> type;
    private AbstractDialogField abstractDialogField;

    public VaadinEditorAdapter(Field field, FieldDefinition fieldDefinition, Class<T> type, AbstractDialogField abstractDialogField) {
        this.field = field;
        this.fieldDefinition = fieldDefinition;
        this.type = type;
        this.abstractDialogField = abstractDialogField;
    }

    @Override
    public void setValue(T object) {
        // TODO special case for DateField
        if (type.equals(Date.class) && object instanceof Calendar) {
            object = (T) ((Calendar)object).getTime();
        }
        field.setValue(object);
    }

    @Override
    public T getValue() {
        T value = (T) field.getValue();

        value = (T) convertToEditorTypeIfNecessary(value, type);

        // TODO This is very rudimentary validation

        if (type.equals(String.class)) {
            // TODO quick hack to get us something to test with
            if ((fieldDefinition.isRequired() || fieldDefinition.getName().equals("title")) && StringUtils.isEmpty((String) value)) {
                delegate.recordError("Required", value);
            }
        }
        return value;
    }

    protected Object convertToEditorTypeIfNecessary(Object value, Class<?> targetType) {
        if (value == null) {
            return value;
        }
        if (targetType.equals(String.class)) {
            if (value instanceof String) {
                return value;
            }
            return value.toString();
        } else if (targetType.equals(Date.class)) {
            if (value instanceof Date) {
                return value;
            }
            if (value instanceof Calendar) {
                return ((Calendar)value).getTime();
            }
        } else if (targetType.equals(Calendar.class)) {
            if (value instanceof Calendar) {
                return value;
            }
            if (value instanceof Date) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime((Date) value);
                return calendar;
            }
        } else if (targetType.equals(Long.class)) {
            if (value instanceof Long) {
                return value;
            }
            if (value instanceof String) {
                return Long.valueOf((String) value);
            }
        } else if (targetType.equals(Double.class)) {
            if (value instanceof Double) {
                return value;
            }
            if (value instanceof String) {
                return Double.valueOf((String) value);
            }
        } else if (targetType.equals(Boolean.class)) {
            if (value instanceof Boolean) {
                return value;
            }
            if (value instanceof String) {
                return Boolean.valueOf((String) value);
            }
        } else if (targetType.equals(BigDecimal.class)) {
            if (value instanceof BigDecimal) {
                return value;
            }
            if (value instanceof String) {
                return new BigDecimal((String) value);
            }
        }

        throw new RuntimeException("Conversion failed value [" + value +
                "] of type [" + value.getClass().getName() +
                "] could not be converted to target type [" + targetType.getName() +
                "]");
    }

    @Override
    public void setDelegate(EditorDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void showErrors(List<EditorError> errors) {

        // Clear any previous error
        abstractDialogField.setError(null);

        for (EditorError error : errors) {
            if (error.getEditor() == this) {
                abstractDialogField.setError(error.getMessage());
                error.setConsumed(true);
            }
        }
    }

    @Override
    public String getPath() {
        return fieldDefinition.getName();
    }

    @Override
    public Class<T> getType() {
        return type;
    }
}
