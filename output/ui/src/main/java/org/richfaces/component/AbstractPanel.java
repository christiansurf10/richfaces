/**
 * License Agreement.
 *
 *  JBoss RichFaces - Ajax4jsf Component Library
 *
 * Copyright (C) 2007  Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */
package org.richfaces.component;

import javax.faces.component.UIComponentBase;

import org.richfaces.cdk.annotations.Attribute;
import org.richfaces.cdk.annotations.JsfComponent;
import org.richfaces.cdk.annotations.JsfRenderer;
import org.richfaces.cdk.annotations.Tag;
import org.richfaces.cdk.annotations.TagType;

/**
 * JSF component class
 *
 */
@JsfComponent(tag = @Tag(type = TagType.Facelets), renderer = @JsfRenderer(type = "org.richfaces.PanelRenderer"), attributes = {
        "core-props.xml", "events-mouse-props.xml", "events-key-props.xml" })
public abstract class AbstractPanel extends UIComponentBase {
    public static final String COMPONENT_TYPE = "org.richfaces.Panel";
    public static final String COMPONENT_FAMILY = "org.richfaces.Panel";

    @Attribute
    public abstract String getHeader();

    @Attribute
    public abstract String getHeaderClass();

    @Attribute
    public abstract String getBodyClass();

    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }
}
