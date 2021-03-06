/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.compileflow.idea.graph.toolbar.action;

import java.awt.event.ActionEvent;

import javax.swing.*;

import com.alibaba.compileflow.idea.graph.mxgraph.Graph;
import com.alibaba.compileflow.idea.graph.mxgraph.GraphComponent;
import com.alibaba.compileflow.idea.graph.mxgraph.export.ExportImageUtil;
import com.alibaba.compileflow.idea.graph.util.DialogUtil;

import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author xuan
 * @since 2020/8/7
 */
public class PngEncodeAction extends AbstractAction {

    private Graph graph;
    private GraphComponent graphComponent;
    private VirtualFile virtualFile;

    public PngEncodeAction(Graph graph, GraphComponent graphComponent, VirtualFile virtualFile) {
        this.graph = graph;
        this.graphComponent = graphComponent;
        this.virtualFile = virtualFile;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String savePath = virtualFile.getParent().getPath() + "/" + virtualFile.getNameWithoutExtension() + ".png";
        DialogUtil.prompt("Input image path", savePath, (path) -> {
            ExportImageUtil.exportImage(2048, 2048, path, graph, graphComponent);
            return null;
        });
    }

}
