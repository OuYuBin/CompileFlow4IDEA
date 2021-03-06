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

package cn.shanghai.oyb.flow.core.swing

import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.ui.treeStructure.Tree
import javax.swing.tree.{DefaultMutableTreeNode, DefaultTreeModel}

/**
  * @author ouyubin
  */
class CommonTree extends Tree with DataProvider{

  new DefaultTreeModel(new DefaultMutableTreeNode)
  setScrollsOnExpand(true)
  //installCellRenderer
  setRootVisible(false)
  setShowsRootHandles(true)

  override def getData(dataId: String): AnyRef = ???



}
