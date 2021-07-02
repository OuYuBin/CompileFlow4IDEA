/*
 * Copyright (c) 2021. Ou Yubin
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.shanghai.oyb.compileflow.editor.editparts;

import cn.shanghai.oyb.compileflow.editor.geometry.CompileFlowGeometryConstants;
import cn.shanghai.oyb.jgraphx.geometry.Geometry;
import com.intellij.psi.xml.XmlTag;
import cn.shanghai.oyb.flow.core.editor.editpart.TGraphEditPart;
import cn.shanghai.oyb.flow.core.editor.models.cells.BaseCell;
import cn.shanghai.oyb.flow.core.editor.models.edges.BaseEdge;
import cn.shanghai.oyb.flow.core.editor.models.edges.EdgeElement;
import cn.shanghai.oyb.flow.core.geometry.BaseGeometry;
import cn.shanghai.oyb.flow.core.internal.TAdaptable;
import cn.shanghai.oyb.flow.core.models.XmlTagModelElement;
import cn.shanghai.oyb.jgraphx.model.Cell;
import cn.shanghai.oyb.compileflow.editor.cells.CompileFlowSubBpmCell;
import cn.shanghai.oyb.compileflow.editor.edges.CompileFlowTransitionEdge;
import cn.shanghai.oyb.compileflow.model.constants.CompileFlowXmlTagConstants;
import com.mxgraph.model.mxICell;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.beans.PropertyChangeEvent;
import java.util.List;

/**
 * 流程开发编辑部件
 *
 * @author ouyubin
 */
public class CompileFlowSubBpmGraphEditPart extends CompileFlowElementEditPart {

    public CompileFlowSubBpmGraphEditPart(Object model, TAdaptable adapter) {
        super(model, adapter);
    }

    @Override
    public BaseCell createCell(Geometry geometry) {
        CompileFlowSubBpmCell compileFlowSubBpmCell = new CompileFlowSubBpmCell(getModel(), geometry);
        return compileFlowSubBpmCell;
    }

    @NotNull
    public BaseGeometry createGeometry() {
        int x = 0;
        int y = 0;
        //-设置初始大小
        double width = CompileFlowGeometryConstants.SUB_BPM_GEOMETRY[0];
        double height = CompileFlowGeometryConstants.SUB_BPM_GEOMETRY[1];
        return new BaseGeometry(x, y, width, height);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        String propertyName = event.getPropertyName();
        if (StringUtils.equals(propertyName, XmlTagModelElement.ADD_TARGET_CONNECTION_PROP())) {
            addTargetConnection();
        } else if (StringUtils.equals(propertyName, XmlTagModelElement.ADD_SOURCE_CONNECTION_PROP())) {
            addSourceConnection();
        } else if (StringUtils.equals(propertyName, XmlTagModelElement.SET_META_PROP())) {
            refreshVisual();
        }
        super.refreshVisual();
    }


    public void addSourceConnection() {
        TGraphEditPart editPart = getRootEditPart();
        mxICell parentCell = editPart.getMyCell().getParent();
        if (parentCell instanceof Cell) {
            CompileFlowTransitionEdge flowTransitionEdge = new CompileFlowTransitionEdge(null);
            flowTransitionEdge.setEdge(true);
            flowTransitionEdge.getGeometry().setRelative(true);
            ((Cell) parentCell).insertLftEdge(flowTransitionEdge);
            EdgeElement edgeElement = new EdgeElement(getMyCell(), null);
            flowTransitionEdge.setValue(edgeElement);
            this.getMyCell().insertLftEdge(flowTransitionEdge, true);
        }
    }


    public void addTargetConnection() {
        TGraphEditPart editPart = getRootEditPart();
        mxICell parentCell = editPart.getMyCell().getParent();
        if (parentCell instanceof Cell) {
            List componentEdges = ((Cell) parentCell).getMyEdges();
            if (!componentEdges.isEmpty()) {
                for (Object edge : componentEdges) {
                    Object object = ((BaseEdge) edge).getValue();
                    if (object instanceof EdgeElement) {
                        EdgeElement edgeElement = (EdgeElement) object;
                        XmlTagModelElement xmlTagElement = (XmlTagModelElement) edgeElement.getStartCell().getValue();
                        String currentName = ((XmlTagModelElement) getModel()).getXmlTag().getAttributeValue("id");
                        XmlTag[] transitionXmlTags = xmlTagElement.getXmlTag().findSubTags(CompileFlowXmlTagConstants.TRANSITION_TAG_NAME);
                        for (XmlTag xmlTag : transitionXmlTags) {
                            String toValue = xmlTag.getAttributeValue("to");
                            if (StringUtils.equals(toValue, currentName)) {
                                XmlTagModelElement flowXmlTagElement = new XmlTagModelElement(xmlTag);
                                ((BaseEdge) edge).setValue(flowXmlTagElement);
                                this.getMyCell().insertLftEdge((CompileFlowTransitionEdge) edge, false);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void refreshVisual() {
//        Object object = getModel();
//        if (object instanceof LFTXmlTagModelElement) {
//            LFTModelMetaElement flowElementMeta=((LFTXmlTagModelElement)object).getMeta();
//            if(flowElementMeta!=null){
//                getGeometry().setX(flowElementMeta.getX());
//                getGeometry().setY(flowElementMeta.getY());
//            }else {
////                object = ((LFTXmlTagModelElement) object).getXmlTag();
////                XmlAttribute xmlAttribute = ((XmlTag) object).getAttribute("meta");
////                if (xmlAttribute != null) {
////                    String meta = xmlAttribute.getValue();
////                    if (meta != null && !"".equals(meta)) {
////                        String[] metas = meta.split(",");
////                        Map<String, String> metaMap = new HashMap<>();
////                        for (String metaInfo : metas) {
////                            String[] info = metaInfo.split(":");
////                            metaMap.put(info[0], info[1]);
////                        }
////                        double x = metaMap.get("x") == null ? 0 : Double.parseDouble(metaMap.get("x"));
////                        double y = metaMap.get("y") == null ? 0 : Double.parseDouble(metaMap.get("y"));
////                        getGeometry().setX(x);
////                        getGeometry().setY(y);
////                    } else {
////                        double width = this.getGeometry().getWidth();
////                        double height = this.getGeometry().getHeight();
////                        mxRectangle rectangle =
////                                ((FlowGraphComponent) adapter().getAdapter(LFTMXGraphComponent.class)).getLayoutAreaSize();
////                        double x = rectangle.getCenterX() - width / 2;
////                        double y = rectangle.getCenterY() - height / 2;
////                        getGeometry().setX(x);
////                        getGeometry().setY(y);
////                    }
//                //} else {
//                    double width = this.getGeometry().getWidth();
//                    double height = this.getGeometry().getHeight();
//                    mxRectangle rectangle =
//                            ((FlowGraphComponent) adapter().getAdapter(LFTMXGraphComponent.class)).getLayoutAreaSize();
//                    double x = rectangle.getCenterX() - width / 2;
//                    double y = rectangle.getCenterY() - height / 2;
//                    getGeometry().setX(x);
//                    getGeometry().setY(y);
//                //}
//            }
//        }
        super.refreshVisual();
    }
}
