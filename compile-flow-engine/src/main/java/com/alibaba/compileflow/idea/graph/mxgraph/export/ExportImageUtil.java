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

package com.alibaba.compileflow.idea.graph.mxgraph.export;

import com.alibaba.compileflow.idea.graph.mxgraph.Graph;
import com.alibaba.compileflow.idea.graph.mxgraph.GraphComponent;
import com.alibaba.compileflow.idea.graph.toolbar.action.VersionActionDialog;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.JBColor;
import com.mxgraph.io.mxCodec;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.util.png.mxPngEncodeParam;
import com.mxgraph.util.png.mxPngImageEncoder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;

/**
 * @author xuan
 * @since 2020/8/7
 */
public class ExportImageUtil {

    private static final Logger logger = Logger.getInstance(VersionActionDialog.class);

    public static void exportImage(int w, int h, String filePath, Graph graph, GraphComponent graphComponent) {
        try {
            //1. init file dir
            File pngFile = new File(filePath);
            File dir = pngFile.getParentFile();
            if (!dir.exists()) {
                dir.mkdir();
            }

            //2. create image
            BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, JBColor.white,
                graphComponent.isAntiAlias(), null, graphComponent.getCanvas());
            mxCodec codec = new mxCodec();
            String xml = URLEncoder.encode(mxXmlUtils.getXml(codec.encode(graph.getModel())), "UTF-8");
            mxPngEncodeParam param = mxPngEncodeParam.getDefaultEncodeParam(image);
            param.setCompressedText(new String[] {"mxGraphModel", xml});

            //3. output image
            FileOutputStream outputStream = new FileOutputStream(pngFile);
            mxPngImageEncoder encoder = new mxPngImageEncoder(outputStream, param);
            encoder.encode(image);
        } catch (Exception e) {
            logger.error("output image exception.", e);
        }
    }

}
