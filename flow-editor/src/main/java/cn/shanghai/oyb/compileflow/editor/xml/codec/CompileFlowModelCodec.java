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

package cn.shanghai.oyb.compileflow.editor.xml.codec;

import cn.shanghai.oyb.compileflow.editor.xml.visitor.CompileFlowTransitionEdgeVisitor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import cn.shanghai.oyb.compileflow.common.graph.CommonGraph;
import cn.shanghai.oyb.compileflow.common.model.parser.utils.XmlElementUtil;
import cn.shanghai.oyb.compileflow.common.model.visitor.EdgeMultiKey;
import cn.shanghai.oyb.flow.core.editor.editpart.TGraphEditPart;
import cn.shanghai.oyb.flow.core.editor.editpart.factory.TGraphEditPartFactory;
import cn.shanghai.oyb.flow.core.editor.models.cells.BaseCell;
import cn.shanghai.oyb.flow.core.editor.models.edges.BaseEdge;
import cn.shanghai.oyb.flow.core.internal.TAdaptable;
import cn.shanghai.oyb.flow.core.models.XmlTagModelElement;
import cn.shanghai.oyb.flow.core.models.factory.TModelElementFactory;
import cn.shanghai.oyb.jgraphx.graph.Graph;
import cn.shanghai.oyb.jgraphx.model.Cell;
import cn.shanghai.oyb.compileflow.editor.edges.CompileFlowTransitionEdge;
import cn.shanghai.oyb.compileflow.model.constants.CompileFlowXmlTagConstants;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ?????????????????????
 *
 * @author ouyubin
 */
public class CompileFlowModelCodec extends XmlRecursiveElementVisitor {

    private final Logger LOG = Logger.getInstance(CompileFlowModelCodec.class);

    private CommonGraph myGraph;

    private mxIGraphModel myGraphModel;

    private mxCell myRootCell;

    private mxICell myCurrentCell;

    private mxICell parentCell;

    private TGraphEditPart myCurrentFlowGraphEditPart;

    private XmlTagModelElement myCurrentXmlTagModelElement;

    private TGraphEditPart myRootGraphEditPart;

    private TGraphEditPartFactory myFlowEditPartFactory;

    private TModelElementFactory myFlowModelElementFactory;

    /**
     * ?????????????????????
     */
    private CompileFlowTransitionEdgeVisitor flowTransitionEdgeVisitor;


    public CompileFlowModelCodec(TAdaptable adapter) {
        this.myGraph = (CommonGraph) adapter.getAdapter(Graph.class);
        //--jgraphx root????????????
        this.myGraphModel = myGraph.getModel();
        this.myRootCell = (mxCell) myGraphModel.getRoot();
        this.parentCell = (mxCell) myGraphModel.getChildAt(myRootCell, 0);
        this.myFlowEditPartFactory = myGraph.getEditPartFactory();
        this.myFlowModelElementFactory = (TModelElementFactory) adapter.getAdapter(TModelElementFactory.class);
        this.flowTransitionEdgeVisitor = new CompileFlowTransitionEdgeVisitor(parentCell);
    }

    /**
     * ????????????,????????????????????????????????????????????????????????????
     */
    public void decode(XmlFile xmlFile) {
        //--????????????????????????
        XmlElementUtil.readComponentFile(xmlFile, this);
        //--?????????
        makeUpEdges();

    }

    public TGraphEditPart getRootGraphEditPart() {
        return myRootGraphEditPart;
    }

    private void makeUpEdges() {
        //--?????????????????????
        makeUpTransitionEdges();
    }

    private void makeUpTransitionEdges() {
        HashMap<EdgeMultiKey, CompileFlowTransitionEdge> wireEdgeMap = flowTransitionEdgeVisitor.getTransitionEdgeMap();
        for (Iterator<Map.Entry<EdgeMultiKey, CompileFlowTransitionEdge>> iter = wireEdgeMap.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<EdgeMultiKey, CompileFlowTransitionEdge> entry = iter.next();
            EdgeMultiKey edgeMultiKey = entry.getKey();
            CompileFlowTransitionEdge flowTransitionEdge = entry.getValue();
            String source = (String) edgeMultiKey.getKey(0);
            String target = (String) edgeMultiKey.getKey(1);
            List<BaseCell> sourceOrTargetCells = flowTransitionEdgeVisitor.getSourceCells();
            List<BaseCell> sourceCells =
                    sourceOrTargetCells.stream().filter(cell -> StringUtils.equals(
                            ((XmlTagModelElement) cell.getValue()).getXmlTag().getAttributeValue("id"), source)).collect(Collectors.toList());
            if (!sourceCells.isEmpty()) {
                for (BaseCell sourceCell : sourceCells) {
                    List<BaseCell> targetCells = flowTransitionEdgeVisitor.getTargetCells();
                    targetCells =
                            targetCells.stream().filter(cell -> StringUtils.equals(((XmlTagModelElement) cell.getValue()).getXmlTag().getAttributeValue("id"), target)).collect(Collectors.toList());
                    for (BaseCell targetCell : targetCells) {
                        ((Cell) parentCell).insertLftEdge(flowTransitionEdge);
                        sourceCell.insertLftEdge(flowTransitionEdge, true);
                        targetCell.insertLftEdge(flowTransitionEdge, false);
                    }
                }
            }
        }
    }

    /**
     * ??????????????????
     *
     * @return
     */
    public mxCell getRootCell() {
        return myRootCell;
    }

    /**
     * ????????????jGraphx?????????????????????,????????????????????????????????????jGraphx??????????????????
     *
     * @param xmlTag
     */
    @Override
    public void visitXmlTag(XmlTag xmlTag) {
        TGraphEditPart componentEditPart = null;
        BaseCell cell = null;
        String tagName = xmlTag.getName();
        LOG.info("??????????" + tagName + "?????????????????????");

        XmlTagModelElement xmlTagModelElement = null;
        if (StringUtils.equals(tagName, CompileFlowXmlTagConstants.TRANSITION_TAG_NAME)) {
            XmlTagModelElement xmlTagElement = new XmlTagModelElement(xmlTag);
            //--??????????????????????????????
            cell = new CompileFlowTransitionEdge(xmlTagElement);
        } else {
            //--???????????????????????????????????????
            xmlTagModelElement = createModelElement(xmlTag);
            if (myCurrentXmlTagModelElement == null) {
                myCurrentXmlTagModelElement = xmlTagModelElement;
            }
            componentEditPart = createGraphEditPart(xmlTagModelElement);
        }

        //--?????????????????????????????????,???????????????????????????????????????????????????,????????????????????????????????????????????????
        if (componentEditPart != null) {
            if (myRootGraphEditPart == null) {
                myRootGraphEditPart = componentEditPart;
            }
            if (myCurrentFlowGraphEditPart != null) {
                //--?????????????????????
                myCurrentFlowGraphEditPart.addChild(componentEditPart);
                if (xmlTagModelElement != null)
                    myCurrentXmlTagModelElement.addChild(xmlTagModelElement);
            }
            myCurrentXmlTagModelElement=xmlTagModelElement;
            myCurrentFlowGraphEditPart = componentEditPart;
            cell = componentEditPart.getMyCell();
        }

        if (cell != null) {
            //--????????????????????????
            if (cell instanceof BaseEdge) {
                cell.setParent(myCurrentCell);
                cell.setEdge(true);
            }
            //--??????????????????????????????
            if (myCurrentCell == null) {
                parentCell.insert(cell);
                cell.setParent(parentCell);
            }
            myCurrentCell = cell;
            //--?????????????????????
            cell.accept(flowTransitionEdgeVisitor);
        }

        //--????????????
        super.visitXmlTag(xmlTag);
        //--?????????????????????????????????cell
        if (cell != null) {
            myCurrentCell = cell.getParent();
        }
        //--?????????????????????????????????????????????
        if (componentEditPart != null) {
            myCurrentFlowGraphEditPart = componentEditPart.getParentEditPart();
        }
        if(xmlTagModelElement!=null)
            myCurrentXmlTagModelElement=xmlTagModelElement.getParent();
    }


    private TGraphEditPart createGraphEditPart(XmlTagModelElement xmlTagModelElement) {
        return myFlowEditPartFactory.getGraphEditPart(xmlTagModelElement);
    }

    private XmlTagModelElement createModelElement(XmlTag xmlTag) {
        return myFlowModelElementFactory.getModel(xmlTag);
    }


}
