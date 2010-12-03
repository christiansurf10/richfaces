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
package org.richfaces.component;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UpdateModelException;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseId;

import org.ajax4jsf.model.DataComponentState;
import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.ExtendedDataModel;
import org.richfaces.application.MessageFactory;
import org.richfaces.application.ServiceTracker;
import org.richfaces.appplication.FacesMessages;
import org.richfaces.cdk.annotations.Attribute;
import org.richfaces.cdk.annotations.EventName;
import org.richfaces.cdk.annotations.JsfComponent;
import org.richfaces.cdk.annotations.JsfRenderer;
import org.richfaces.cdk.annotations.Signature;
import org.richfaces.cdk.annotations.Tag;
import org.richfaces.component.util.MessageUtil;
import org.richfaces.context.ExtendedVisitContext;
import org.richfaces.context.ExtendedVisitContextMode;
import org.richfaces.event.TreeSelectionChangeEvent;
import org.richfaces.event.TreeSelectionChangeListener;
import org.richfaces.event.TreeSelectionChangeSource;
import org.richfaces.event.TreeToggleEvent;
import org.richfaces.event.TreeToggleListener;
import org.richfaces.event.TreeToggleSource;
import org.richfaces.model.DeclarativeTreeDataModelImpl;
import org.richfaces.model.DeclarativeTreeModel;
import org.richfaces.model.SwingTreeNodeDataModelImpl;
import org.richfaces.model.TreeDataModel;
import org.richfaces.model.TreeDataModelTuple;
import org.richfaces.model.TreeDataVisitor;
import org.richfaces.renderkit.MetaComponentRenderer;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * @author Nick Belaevski
 * 
 */
@JsfComponent(
    type = AbstractTree.COMPONENT_TYPE,
    family = AbstractTree.COMPONENT_FAMILY, 
    tag = @Tag(name = "tree", handler = "org.richfaces.view.facelets.TreeHandler"),
    renderer = @JsfRenderer(type = "org.richfaces.TreeRenderer"),
    attributes = {"events-props.xml", "core-props.xml", "i18n-props.xml"}
)
//TODO add rowData caching for wrapper events
public abstract class AbstractTree extends UIDataAdaptor implements MetaComponentResolver, MetaComponentEncoder, TreeSelectionChangeSource, TreeToggleSource {

    public static final String COMPONENT_TYPE = "org.richfaces.Tree";

    public static final String COMPONENT_FAMILY = "org.richfaces.Tree";

    public static final String SELECTION_META_COMPONENT_ID = "selection";

    private static final String DEFAULT_TREE_NODE_CREATED = AbstractTree.class.getName() + ":DEFAULT_TREE_NODE_CREATED";

    private static final class MatchingTreeNodePredicate implements Predicate<UIComponent> {

        private String type;

        public MatchingTreeNodePredicate(String type) {
            super();
            this.type = type;
        }

        public boolean apply(UIComponent input) {
            if (!(input instanceof AbstractTreeNode)) {
                return false;
            }

            String nodeType = ((AbstractTreeNode) input).getType();
            if (type == null && nodeType == null) {
                return true;
            }

            return type != null && type.equals(nodeType);
        }
    };

    private enum PropertyKeys {
        selection
    }
    
    @SuppressWarnings("unused")
    @Attribute(generate = false, signature = @Signature(returnType = Void.class, parameters = TreeSelectionChangeEvent.class))
    private MethodExpression selectionChangeListener;

    @SuppressWarnings("unused")
    @Attribute(generate = false, signature = @Signature(returnType = Void.class, parameters = TreeToggleListener.class))
    private MethodExpression toggleListener;

    private transient TreeRange treeRange;
    
    public AbstractTree() {
        setKeepSaved(true);
        setRendererType("org.richfaces.TreeRenderer");
    }

    protected TreeRange getTreeRange() {
        if (treeRange == null) {
            treeRange = new TreeRange(this);
        }
        
        return treeRange;
    }
    
    public abstract Object getValue();

    public abstract boolean isImmediate();

    public abstract String getIconLeaf();

    public abstract String getIconExpanded();

    public abstract String getIconCollapsed();

    public abstract String getNodeClass();

    public abstract String getHandleClass();

    public abstract String getIconClass();

    public abstract String getLabelClass();

    @Attribute(events = @EventName("nodetoggle"))
    public abstract String getOnnodetoggle();

    @Attribute(events = @EventName("beforenodetoggle"))
    public abstract String getOnbeforenodetoggle();

    @Attribute(events = @EventName("selectionchange"))
    public abstract String getOnselectionchange();

    @Attribute(events = @EventName("beforeselectionchange"))
    public abstract String getOnbeforeselectionchange();

    @Attribute(defaultValue = "SwitchType.DEFAULT")
    public abstract SwitchType getToggleType();

    @Attribute(defaultValue = "SwitchType.client")
    public abstract SwitchType getSelectionType();

    public abstract String getNodeType();

    public abstract String getToggleNodeEvent();
    
    public abstract Object getExecute();
    
    public abstract Object getRender();
    
    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public Collection<Object> getSelection() {
        @SuppressWarnings("unchecked")
        Collection<Object> selection = (Collection<Object>) getStateHelper().eval(PropertyKeys.selection);
        if (selection == null) {
            selection = new HashSet<Object>();

            ValueExpression ve = getValueExpression(PropertyKeys.selection.toString());
            if (ve != null) {
                ve.setValue(getFacesContext().getELContext(), selection);
            } else {
                getStateHelper().put(PropertyKeys.selection, selection);
            }
        }

        return selection;
    }

    public void setSelection(Collection<Object> selection) {
        getStateHelper().put(PropertyKeys.selection, selection);
    }

    @Override
    protected DataComponentState createComponentState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Converter getRowKeyConverter() {
        Converter converter = super.getRowKeyConverter();
        if (converter == null) {
            converter = getTreeDataModel().getRowKeyConverter();
        }
        return converter;
    }

    protected Iterator<UIComponent> findMatchingTreeNodeComponent(String nodeType, UIComponent parentComponent) {
        Iterator<UIComponent> children = parentComponent.getChildren().iterator();
        if (parentComponent != this) {
            children = Iterators.concat(children, this.getChildren().iterator());
        }
        
        return Iterators.filter(children, new MatchingTreeNodePredicate(nodeType));
    }

    protected UIComponent getCurrentComponent() {
        ExtendedDataModel<?> dataModel = getExtendedDataModel();
        if (dataModel instanceof DeclarativeTreeModel) {
            return ((DeclarativeTreeModel) dataModel).getCurrentComponent();
        }
        
        return this;
    }
    
    public AbstractTreeNode findTreeNodeComponent() {
        FacesContext facesContext = getFacesContext();

        String nodeType = getNodeType();
        
        Iterator<UIComponent> nodesItr = findMatchingTreeNodeComponent(nodeType, getCurrentComponent());
        
        if (nodesItr.hasNext()) {
            Iterator<UIComponent> renderedNodesItr = Iterators.filter(nodesItr, ComponentPredicates.isRendered());
            if (renderedNodesItr.hasNext()) {
                return (AbstractTreeNode) renderedNodesItr.next();
            }
            
            return null;
        }
        
        if (Strings.isNullOrEmpty(nodeType)) {
            if (getAttributes().put(DEFAULT_TREE_NODE_CREATED, Boolean.TRUE) != null) {
                return null;
            }

            //TODO default TreeNode is created when getRowKey() == null but it's not used for presentational purposes
            Application application = facesContext.getApplication();
            AbstractTreeNode treeNode = (AbstractTreeNode) application.createComponent(AbstractTreeNode.COMPONENT_TYPE);
            treeNode.setId("__defaultTreeNode");

            getChildren().add(treeNode);

            UIComponent text = application.createComponent(HtmlOutputText.COMPONENT_TYPE);
            text.setValueExpression("value", application.getExpressionFactory().createValueExpression(facesContext.getELContext(), 
                "#{" + getVar() + "}", String.class));
            treeNode.getChildren().add(text);

            return treeNode;
        }

        return null;
    }

    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException {
        super.broadcast(event);

        if (event instanceof TreeSelectionChangeEvent) {
            TreeSelectionChangeEvent selectionEvent = (TreeSelectionChangeEvent) event;

            final Collection<Object> newSelection = selectionEvent.getNewSelection();

            Collection<Object> selectionCollection = getSelection();

            Iterables.removeIf(selectionCollection, new Predicate<Object>() {
                public boolean apply(Object input) {
                    return !newSelection.contains(input);
                };
            });

            if (!newSelection.isEmpty()) {
                Iterables.addAll(selectionCollection, newSelection);
            }
        } else if (event instanceof TreeToggleEvent) {
            TreeToggleEvent toggleEvent = (TreeToggleEvent) event;
            AbstractTreeNode treeNodeComponent = findTreeNodeComponent();

            boolean newExpandedValue = toggleEvent.isExpanded();

            FacesContext context = getFacesContext();
            ValueExpression expression = treeNodeComponent.getValueExpression(AbstractTreeNode.PropertyKeys.expanded.toString());
            if (expression != null) {
                ELContext elContext = context.getELContext();
                Exception caught = null;
                FacesMessage message = null;
                try {
                    expression.setValue(elContext, newExpandedValue);
                } catch (ELException e) {
                    caught = e;
                    String messageStr = e.getMessage();
                    Throwable result = e.getCause();
                    while (null != result &&
                        result.getClass().isAssignableFrom(ELException.class)) {
                        messageStr = result.getMessage();
                        result = result.getCause();
                    }
                    if (null == messageStr) {
                        MessageFactory messageFactory = ServiceTracker.getService(MessageFactory.class);
                        message =
                            messageFactory.createMessage(context, FacesMessages.UIINPUT_UPDATE,
                                MessageUtil.getLabel(context, this));
                    } else {
                        message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            messageStr,
                            messageStr);
                    }
                } catch (Exception e) {
                    caught = e;
                    MessageFactory messageFactory = ServiceTracker.getService(MessageFactory.class);
                    message =
                        messageFactory.createMessage(context, FacesMessages.UIINPUT_UPDATE,
                            MessageUtil.getLabel(context, this));
                }
                if (caught != null) {
                    assert(message != null);
                    UpdateModelException toQueue = new UpdateModelException(message, caught);
                    ExceptionQueuedEventContext eventContext =
                        new ExceptionQueuedEventContext(context,
                            toQueue,
                            this,
                            PhaseId.UPDATE_MODEL_VALUES);
                    context.getApplication().publishEvent(context,
                        ExceptionQueuedEvent.class,
                        eventContext);
                }
            } else {
                treeNodeComponent.setExpanded(newExpandedValue);
            }
        }
    }

    @Override
    protected boolean visitFixedChildren(VisitContext visitContext, VisitCallback callback) {
        if (visitContext instanceof ExtendedVisitContext) {
            ExtendedVisitContext extendedVisitContext = (ExtendedVisitContext) visitContext;

            if (ExtendedVisitContextMode.RENDER == extendedVisitContext.getVisitMode()) {
                VisitResult result = extendedVisitContext.invokeMetaComponentVisitCallback(this, callback, SELECTION_META_COMPONENT_ID);
                if (result != VisitResult.ACCEPT) {
                    return result == VisitResult.COMPLETE;
                }
            }
        }

        return super.visitFixedChildren(visitContext, callback);
    }

    void decodeMetaComponent(FacesContext context, String metaComponentId) {
        ((MetaComponentRenderer) getRenderer(context)).decodeMetaComponent(context, this, metaComponentId);
    }

    public void encodeMetaComponent(FacesContext context, String metaComponentId) throws IOException {
        ((MetaComponentRenderer) getRenderer(context)).encodeMetaComponent(context, this, metaComponentId);
    }

    public String resolveClientId(FacesContext facesContext, UIComponent contextComponent, String metaComponentId) {
        if (SELECTION_META_COMPONENT_ID.equals(metaComponentId)) {
            return getClientId(facesContext) + MetaComponentResolver.META_COMPONENT_SEPARATOR_CHAR + metaComponentId;
        }

        return null;
    }

    public String substituteUnresolvedClientId(FacesContext facesContext, UIComponent contextComponent,
        String metaComponentId) {

        return null;
    }

    @Override
    protected Iterator<UIComponent> dataChildren() {
        AbstractTreeNode treeNodeComponent = findTreeNodeComponent();
        if (treeNodeComponent != null) {
            return Iterators.<UIComponent>singletonIterator(treeNodeComponent);
        } else {
            return Iterators.<UIComponent>emptyIterator();
        }
    }

    public void addTreeSelectionChangeListener(TreeSelectionChangeListener listener) {
        addFacesListener(listener);
    }

    @Attribute(hidden = true)
    public TreeSelectionChangeListener[] getTreeSelectionChangeListeners() {
        return (TreeSelectionChangeListener[]) getFacesListeners(TreeSelectionChangeListener.class);
    }

    public void removeTreeSelectionChangeListener(TreeSelectionChangeListener listener) {
        removeFacesListener(listener);
    }

    public void addTreeToggleListener(TreeToggleListener listener) {
        addFacesListener(listener);
    }

    @Attribute(hidden = true)
    public TreeToggleListener[] getTreeToggleListeners() {
        return (TreeToggleListener[]) getFacesListeners(TreeToggleListener.class);
    }

    public void removeTreeToggleListener(TreeToggleListener listener) {
        removeFacesListener(listener);
    }

    @Attribute(hidden = true)
    public boolean isExpanded() {
        if (getRowKey() == null) {
            return true;
        }

        AbstractTreeNode treeNode = findTreeNodeComponent();
        if (treeNode == null) {
            return false;
        }

        return treeNode.isExpanded();
    }

    //TODO review
    protected TreeDataModel<?> getTreeDataModel() {
        return (TreeDataModel<?>) getExtendedDataModel();
    }
    
    @Attribute(hidden = true)
    public boolean isLeaf() {
        return getTreeDataModel().isLeaf();
    }
    
    @Override
    public void walk(final FacesContext faces, final DataVisitor visitor, final Object argument) {
        walkModel(faces, new TreeDataVisitor() {
            
            public void enterNode() {
                visitor.process(faces, getRowKey(), argument);
            }

            public void exitNode() {
            }

            public void beforeChildrenVisit() {
            }

            public void afterChildrenVisit() {
            }
            
        });
    }
    
    @Override
    protected ExtendedDataModel<?> createExtendedDataModel() {
        ExtendedDataModel<?> dataModel;
        
        Object value = getValue();
        if (value == null) {
            dataModel = new DeclarativeTreeDataModelImpl(this);
        } else {
            dataModel = new SwingTreeNodeDataModelImpl();
            dataModel.setWrappedData(getValue());
        }
        
        return dataModel;
    }
    
    public void walkModel(FacesContext context, TreeDataVisitor dataVisitor) {
        TreeDataModel<?> model = getTreeDataModel();

        if (!getTreeRange().shouldProcessNode()) {
            return;
        }
        
        boolean isRootNode = (getRowKey() == null);
        
        if (!isRootNode) {
            dataVisitor.enterNode();
        }

        walkModelChildren(context, dataVisitor, model);

        if (!isRootNode) {
            dataVisitor.exitNode();
        }
    }

    private void walkModelChildren(FacesContext context, TreeDataVisitor dataVisitor, TreeDataModel<?> model) {
        if (!getTreeRange().shouldIterateChildren()) {
            return;
        }
        
        dataVisitor.beforeChildrenVisit();
        
        Iterator<TreeDataModelTuple> childrenTuples = model.children();
        while (childrenTuples.hasNext()) {
            TreeDataModelTuple tuple = childrenTuples.next();
            
            restoreFromSnapshot(context, tuple);
            
            if (!getTreeRange().shouldProcessNode()) {
                continue;
            }
            
            dataVisitor.enterNode();
            
            walkModelChildren(context, dataVisitor, model);

            dataVisitor.exitNode();
        }
        
        dataVisitor.afterChildrenVisit();
    }
    
    @Override
    protected void resetDataModel() {
        super.resetDataModel();
        treeRange = null;
    }
    
    public TreeDataModelTuple createSnapshot() {
        return getTreeDataModel().createSnapshot();
    }

    public void restoreFromSnapshot(FacesContext context, TreeDataModelTuple tuple) {
        getTreeDataModel().restoreFromSnapshot(tuple);
        setRowKey(context, tuple.getRowKey());
    }
    
}
