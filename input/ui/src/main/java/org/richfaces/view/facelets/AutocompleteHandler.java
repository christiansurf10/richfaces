/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.richfaces.view.facelets;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.MetaRule;
import javax.faces.view.facelets.MetaRuleset;
import javax.faces.view.facelets.Metadata;
import javax.faces.view.facelets.MetadataTarget;
import javax.faces.view.facelets.TagAttribute;

import org.richfaces.component.AbstractAutocomplete;

/**
 * @author Nick Belaevski
 */
//TODO nick - this should be generated by CDK
public class AutocompleteHandler extends ComponentHandler {

    private static final MetaRule AUTOCOMPLETE_METHOD_META_RULE = new MetaRule() {

        @Override
        public Metadata applyRule(String name, TagAttribute attribute,
                                  MetadataTarget meta) {
            if (meta.isTargetInstanceOf(AbstractAutocomplete.class)) {
                if ("autocompleteMethod".equals(name)) {
                    return new MethodMetadata(attribute, FacesContext.class,
                        UIComponent.class, String.class) {
                        public void applyMetadata(FaceletContext ctx, Object instance) {
                            ((AbstractAutocomplete) instance).setAutocompleteMethod(getMethodExpression(ctx));
                        }
                    };
                }

                if ("converter".equals(name)) {
                    return new ConverterMetadata(attribute) {
                        public void applyMetadata(FaceletContext ctx, Object instance) {
                            ((AbstractAutocomplete) instance).setConverter(this.getConverter(ctx,
                                (AbstractAutocomplete) instance,
                                this.getAttr().getValueExpression(ctx, Converter.class)));
                        }
                    };
                }

                if ("itemConverter".equals(name)) {
                    return new ConverterMetadata(attribute) {
                        public void applyMetadata(FaceletContext ctx, Object instance) {
                            ((AbstractAutocomplete) instance).setItemConverter(this.getConverter(ctx,
                                (AbstractAutocomplete) instance,
                                this.getAttr().getValueExpression(ctx, Converter.class)));
                        }
                    };
                }

            }

            return null;
        }
    };

    public AutocompleteHandler(ComponentConfig config) {
        super(config);
    }

    @Override
    protected MetaRuleset createMetaRuleset(Class type) {
        MetaRuleset metaRuleset = super.createMetaRuleset(type);
        metaRuleset.addRule(AUTOCOMPLETE_METHOD_META_RULE);
        return metaRuleset;
    }

    abstract static class ConverterMetadata extends Metadata {

        private final TagAttribute attr;

        public ConverterMetadata(TagAttribute attr) {
            this.attr = attr;
        }

        public TagAttribute getAttr() {
            return attr;
        }

        public Converter getConverter(FaceletContext ctx,
                                      AbstractAutocomplete component, ValueExpression converter) {
            ValueExpression ve = null;
            Converter c = null;
            if (converter != null) {
                ve = converter;
                try {
                    c = (Converter) ve.getValue(ctx);
                } catch (Exception e) {
                    // ok
                }

            }
            if (c == null) {
                c = this.createConverter(ctx, component);
            }
            if (c == null) {
                // throw new TagException(this.getTag(), "No Converter was
                // created");
            }
            return c;
        }

        private String getConverterId(FaceletContext ctx) {
            return this.getAttr().getValue(ctx);
        }

        private Converter createConverter(FaceletContext ctx,
                                          AbstractAutocomplete component) {
            return ctx.getFacesContext().getApplication().createConverter(
                getConverterId(ctx));
        }

    }
}
