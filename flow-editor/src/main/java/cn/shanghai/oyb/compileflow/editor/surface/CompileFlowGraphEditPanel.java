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

package cn.shanghai.oyb.compileflow.editor.surface;

import cn.shanghai.oyb.compileflow.editor.actions.CompileFlowCopyAction;
import cn.shanghai.oyb.compileflow.editor.actions.CompileFlowCutAction;
import cn.shanghai.oyb.compileflow.editor.cells.CompileFlowElementCell;
import cn.shanghai.oyb.compileflow.editor.component.CompileFlowGraphComponent;
import cn.shanghai.oyb.compileflow.editor.providers.CompileFlowDeleteProvider;
import cn.shanghai.oyb.compileflow.window.palette.manager.CompileFlowPaletteToolWindowManager;
import cn.shanghai.oyb.flow.core.editor.editpart.TGraphEditPart;
import cn.shanghai.oyb.flow.core.models.XmlTagModelElement;
import com.alibaba.compileflow.idea.CompileFlow;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.impl.PsiTreeChangeEventImpl;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import cn.shanghai.oyb.compileflow.common.actions.CommonAction;
import cn.shanghai.oyb.compileflow.common.editor.surface.CommonGraphEditPanel;
import cn.shanghai.oyb.compileflow.common.graph.CommonGraph;
import cn.shanghai.oyb.flow.core.editor.TGraphFileEditor;
import cn.shanghai.oyb.flow.core.editor.editpart.factory.TGraphEditPartFactory;
import cn.shanghai.oyb.flow.core.editor.models.cells.BaseCell;
import cn.shanghai.oyb.flow.core.window.manager.TToolWindowManager;
import cn.shanghai.oyb.jgraphx.component.GraphComponent;
import cn.shanghai.oyb.jgraphx.graph.Graph;
import cn.shanghai.oyb.jgraphx.model.GraphModel;
import cn.shanghai.oyb.compileflow.editor.actions.CompileFlowDeleteAction;
import cn.shanghai.oyb.compileflow.editor.actions.CompileFlowPasteAction;
import cn.shanghai.oyb.compileflow.editor.edges.CompileFlowTransitionEdge;
import cn.shanghai.oyb.compileflow.editor.editparts.factory.CompileFlowEditPartFactory;
import cn.shanghai.oyb.compileflow.editor.graph.CompileFlowGraph;
import cn.shanghai.oyb.compileflow.editor.providers.CompileFlowCopyProvider;
import cn.shanghai.oyb.compileflow.editor.providers.CompileFlowPasteProvider;
import cn.shanghai.oyb.compileflow.editor.xml.CompileFlowDocumentHandler;
import cn.shanghai.oyb.compileflow.editor.xml.codec.CompileFlowModelCodec;
import cn.shanghai.oyb.compileflow.window.properties.manager.CompileFlowPropertiesToolWindowManager;
import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.util.png.mxPngEncodeParam;
import com.mxgraph.util.png.mxPngImageEncoder;
import com.mxgraph.view.mxStylesheet;
import icons.CompileFlowIcons;
import icons.CompileFlowImages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;

import static cn.shanghai.oyb.compileflow.common.actions.CommonAction.COMMON_ACTION_PLACE;
import static com.intellij.psi.impl.PsiTreeChangeEventImpl.*;

/**
 * ?????????????????????????????????
 *
 * @author ouyubin
 */
public class CompileFlowGraphEditPanel extends CommonGraphEditPanel {

    private static final Logger LOG = Logger.getInstance(CompileFlowGraphEditPanel.class);
    /**
     * --????????????
     */
    private EventListenerList myListenerList;

    private CompileFlowPasteProvider myCompileFlowPasteProvider;

    public CompileFlowGraphEditPanel(@NotNull TGraphFileEditor editor, @NotNull Project project, @NotNull Module module, @NotNull VirtualFile file) {
        super(editor, project, module, file);
        this.myListenerList = new EventListenerList();
        myCompileFlowPasteProvider = new CompileFlowPasteProvider(this);

    }

    @Override
    public GraphComponent createGraphComponent(CommonGraph commonGraph) {
        return new CompileFlowGraphComponent(commonGraph, this);
    }

    @Override
    public TGraphEditPartFactory createEditPartFactory() {
        return new CompileFlowEditPartFactory(this);
    }

    public JLabel createTitleLabel() {
        JBLabel titleLabel = new JBLabel("Alibaba Compile Flow", CompileFlowImages.WORK_FLOW_IMAGE(), SwingConstants.LEFT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, UIUtil.getFontSize(UIUtil.FontSize.NORMAL) + JBUI.scale(2f)));
        return titleLabel;
    }

    /**
     * ?????????????????????
     *
     * @return
     */
    @Override
    public TToolWindowManager getPropertyToolWindowManager() {
        Project project = getProject();
        return CompileFlowPropertiesToolWindowManager.getInstance(project);
    }

    @Override
    public TToolWindowManager getPaletteToolWindowManager() {
        return CompileFlowPaletteToolWindowManager.getInstance(getProject());
    }

    @Override
    public String getEditorText() {
        return null;
    }


    @Override
    public Object getData(@NotNull String dataId) {
        if (PlatformDataKeys.DELETE_ELEMENT_PROVIDER.is(dataId)) {
            return new CompileFlowDeleteProvider(this);
        } else if (PlatformDataKeys.COPY_PROVIDER.is(dataId)) {
            return new CompileFlowCopyProvider(this);
        } else if (PlatformDataKeys.PASTE_PROVIDER.is(dataId)) {
            return myCompileFlowPasteProvider;
        }
        return null;
    }

    @Nullable
    @Override
    public mxCell getModel() {
        CompileFlowDocumentHandler compileFlowDocumentHandler = new CompileFlowDocumentHandler(this, myXmlFile);
        compileFlowDocumentHandler.readFromFile();
        CompileFlowModelCodec flowModelCodec = compileFlowDocumentHandler.getComponentCodec();
        rootGraphEditPart = flowModelCodec.getRootGraphEditPart();
        return flowModelCodec.getRootCell();
    }

    public CommonGraph createGraph(GraphModel graphModel, mxStylesheet stylesheet) {
        return new CompileFlowGraph(graphModel, stylesheet);
    }

    @Override
    public void showGraphPopupMenu(MouseEvent event) {
        super.showGraphPopupMenu(event);
        Point point = event.getPoint();
        Object cell = myComponentGraphComponent.getCellAt(event.getX(), event.getY(), myComponentGraphComponent.isSwimlaneSelectionEnabled());
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        if (cell instanceof CompileFlowElementCell) {
            actionGroup.add(new CompileFlowCutAction((BaseCell) cell, this, "??????", "??????", CompileFlowIcons.CUT_ICON));
            actionGroup.add(new CompileFlowCopyAction((BaseCell) cell, this, "??????", "??????", CompileFlowIcons.COPY_ICON));
        }
        CompileFlowPasteAction compileFlowPasteAction = new CompileFlowPasteAction((BaseCell) cell, this, "??????", "??????", CompileFlowIcons.PASTE_ICON, point);
        //--??????Action?????????????????????
        AnActionEvent actionEvent = AnActionEvent.createFromDataContext(COMMON_ACTION_PLACE, compileFlowPasteAction.getTemplatePresentation(), dataId -> {
            if (CommonAction.PASTE_KEY.is(dataId)) {
                return cell;
            }
            return null;
        });
        compileFlowPasteAction.update(actionEvent);
        actionGroup.add(compileFlowPasteAction);

        if (cell instanceof CompileFlowElementCell || cell instanceof CompileFlowTransitionEdge) {
            actionGroup.add(new Separator());
            actionGroup.add(new CompileFlowDeleteAction((BaseCell) cell, this, "??????", "??????", CompileFlowIcons.DELETE_ICON));
        }

//        actionGroup.add(new Separator());
//        actionGroup.add(new CompileFlowSelectAllAction((LFTCell) cell, this, "????????????", "????????????", CompileFlowIcons.SELECT_ALL_ICON));

        if (actionGroup.getChildrenCount() > 0) {
            ActionManager.getInstance().createActionPopupMenu(ActionPlaces.UNKNOWN, actionGroup).getComponent().show(event.getComponent(), event.getX(), event.getY());
        }

    }

    public DefaultActionGroup createEditActionGroup() {

        DefaultActionGroup group = new DefaultActionGroup();
        AnAction linkHomeAction = new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                BrowserUtil.browse(CompileFlow.HOME_PAGE);
            }
        };
        linkHomeAction.getTemplatePresentation().setIcon(CompileFlowIcons.HOME_ICON);
        linkHomeAction.getTemplatePresentation().setText("??????????????????");
        group.add(linkHomeAction);
        group.add(new Separator());

        AnAction exportImageAction = new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                FileSaverDialog saveFileDialog = FileChooserFactory.getInstance()
                        .createSaveFileDialog(new FileSaverDescriptor("??????????????????", "?????????",
                                "png"), getProject());
                VirtualFileWrapper virtualFileWrapper = saveFileDialog.save(getFile().getNameWithoutExtension() + ".png");
                File pngFile = virtualFileWrapper.getFile();
                GraphComponent component = (GraphComponent) getAdapter(GraphComponent.class);
                Graph graph = (Graph) getAdapter(Graph.class);
                BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, JBColor.white,
                        component.isAntiAlias(), null, component.getCanvas());
                mxCodec codec = new mxCodec();
                String xml = null;

                try {
                    xml = URLEncoder.encode(mxXmlUtils.getXml(codec.encode(graph.getModel())), "UTF-8");
                } catch (UnsupportedEncodingException unsupportedEncodingException) {
                    unsupportedEncodingException.printStackTrace();
                }
                mxPngEncodeParam param = mxPngEncodeParam.getDefaultEncodeParam(image);
                param.setCompressedText(new String[]{"mxGraphModel", xml});

                try (FileOutputStream outputStream = new FileOutputStream(pngFile)) {
                    mxPngImageEncoder encoder = new mxPngImageEncoder(outputStream, param);
                    encoder.encode(image);
                } catch (FileNotFoundException fileNotFoundException) {
                    //fileNotFoundException.printStackTrace();
                    try {
                        throw new Throwable(String.format("?????????????????????=> s%", fileNotFoundException.getMessage()));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                } catch (IOException ioException) {
                    //ioException.printStackTrace();
                    try {
                        throw new Throwable(String.format("IO????????????=> s%", ioException.getMessage()));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }

            }
        };
        exportImageAction.getTemplatePresentation().setIcon(CompileFlowIcons.EXPORT_IMAGE_ICON);
        exportImageAction.getTemplatePresentation().setText("????????????");
        group.add(exportImageAction);
        group.add(new Separator());
        AnAction zoomInAction = new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                GraphComponent component = (GraphComponent) getAdapter(GraphComponent.class);
                component.zoomIn();
            }
        };
        zoomInAction.getTemplatePresentation().setIcon(CompileFlowIcons.ZOOM_IN_ICON);
        zoomInAction.getTemplatePresentation().setText("??????");
        zoomInAction.getTemplatePresentation().setDescription("??????");
        group.add(zoomInAction);

        AnAction zoomOutAction = new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                GraphComponent component = (GraphComponent) getAdapter(GraphComponent.class);
                component.zoomOut();
            }
        };
        zoomOutAction.getTemplatePresentation().setIcon(CompileFlowIcons.ZOOM_OUT_ICON);
        zoomOutAction.getTemplatePresentation().setText("??????");
        zoomOutAction.getTemplatePresentation().setDescription("??????");
        group.add(zoomOutAction);
        return group;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void revalidateFile() {
        super.revalidateFile();
        LOG.info("????????????????????????????...");
        PsiTreeChangeEvent event = compileFlowPsiTreeChangeListener.getEvent();
        PsiEventType type = ((PsiTreeChangeEventImpl) event).getCode();
        switch (type) {
            case CHILD_ADDED:
                XmlTag parent = (XmlTag) event.getParent();
                XmlTag child = (XmlTag) event.getChild();
                XmlTag xmlTag = ((XmlTagModelElement) rootGraphEditPart.getModel()).getXmlTag();
                if (xmlTag.isValid() && xmlTag == parent) {
                    //--???????????????????????????????????????????????????
                    for (TGraphEditPart childEditPart : rootGraphEditPart.getChildrenEditPart()) {
                        XmlTag childXmlTag = ((XmlTagModelElement) childEditPart.getModel()).getXmlTag();
                        if (!childXmlTag.isValid()) {
                            ((XmlTagModelElement) childEditPart.getModel()).setXmlTag((XmlTag) child);
                            break;
                        }
                    }
                } else {
                    recursiveValidate(rootGraphEditPart, parent);
                }
                break;
        }
    }

    /**
     * ????????????
     *
     * @param parentEditPart
     * @param parent
     */
    public void recursiveValidate(TGraphEditPart parentEditPart, PsiElement parent) {
        for (TGraphEditPart editPart : parentEditPart.getChildrenEditPart()) {
            XmlTag xmlTag = ((XmlTagModelElement) editPart.getModel()).getXmlTag();
            if (xmlTag.isValid() && xmlTag == parent) {
                for (TGraphEditPart childEditPart : editPart.getChildrenEditPart()) {
                    XmlTag childXmlTag = ((XmlTagModelElement) childEditPart.getModel()).getXmlTag();
                    if (!childXmlTag.isValid()) {
                        ((XmlTagModelElement) childEditPart.getModel()).setXmlTag(childXmlTag);
                        break;
                    }
                }
            } else {
                recursiveValidate(editPart, parent);
            }
        }
    }
}
