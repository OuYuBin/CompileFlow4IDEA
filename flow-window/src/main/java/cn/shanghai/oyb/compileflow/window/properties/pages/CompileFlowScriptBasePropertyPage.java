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

package cn.shanghai.oyb.compileflow.window.properties.pages;

import cn.shanghai.oyb.compileflow.common.commands.AddCommand;
import cn.shanghai.oyb.compileflow.common.commands.RemoveCommand;
import cn.shanghai.oyb.compileflow.common.commands.SetCommand;
import cn.shanghai.oyb.compileflow.common.model.query.utils.PsiElementQueryUtil;
import cn.shanghai.oyb.compileflow.model.constants.CompileFlowXmlTagConstants;
import cn.shanghai.oyb.compileflow.ui.models.CompileFlowParamsModel;
import cn.shanghai.oyb.compileflow.ui.table.CompileFlowBpmParamsTable;
import cn.shanghai.oyb.compileflow.ui.table.cell.editor.CompileFlowClassCellEditor;
import cn.shanghai.oyb.compileflow.ui.table.models.CompileFlowPropertyModel;
import cn.shanghai.oyb.flow.core.editor.commands.GraphEditCommandStack;
import cn.shanghai.oyb.flow.core.editor.editdomain.GraphEditDomain;
import cn.shanghai.oyb.flow.core.models.XmlTagModelElement;
import cn.shanghai.oyb.flow.core.swing.CommonTabbedPane;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.roots.ToolbarPanel;
import com.intellij.util.ui.JBEmptyBorder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.FormsSetup;
import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.factories.ComponentFactory;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.FormLayout;
import icons.CompileFlowIcons;
import icons.CompileFlowImages;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static cn.shanghai.oyb.compileflow.model.constants.CompileFlowXmlTagConstants.*;

/**
 * ???????????????
 *
 * @author ouyubin
 */
public class CompileFlowScriptBasePropertyPage extends CompileFlowElementBasePropertyPage {

    private static final Logger LOG = Logger.getInstance(CompileFlowBpmBasePropertyPage.class);

    private JTextField nameTextField;

    private ComboBox<String> typeComboBox;

    private JTextField expressionTextField;

    private CompileFlowBpmParamsTable compileFlowParamsTable;

    private DocumentListener myNameDocumentListener;

    private DocumentListener myExpressDocumentListener;


    public CompileFlowScriptBasePropertyPage() {
        super();
    }


    /**
     * @param parent
     */
    @Override
    public void createControl(JComponent parent) {
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setHgap(0);
        borderLayout.setVgap(0);
        this.setLayout(borderLayout);

        FormBuilder formBuilder = FormBuilder.create();
        ComponentFactory componentFactory = FormsSetup.getComponentFactoryDefault();
        formBuilder.factory(componentFactory);
        JBLabel titleLabel = new JBLabel("???????????? ??ScriptTask??", CompileFlowImages.SCRIPT_IMAGE(), SwingConstants.LEFT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, UIUtil.getFontSize(UIUtil.FontSize.NORMAL) + JBUI.scale(2f)));

        FormLayout titleLayout = new FormLayout(
                DEFAULT_PROPERTY_PAGE_COLUMN_MARGIN() + ",pref," + DEFAULT_PROPERTY_PAGE_COLUMN_SPEC() + ",pref,pref:grow," +
                        "pref," + DEFAULT_PROPERTY_PAGE_COLUMN_MARGIN(),
                "10px" + ",pref," + "9px" + ",pref");
        JPanel panel = formBuilder.layout(titleLayout).add(titleLabel).xyw(2, 2, 4)
                .addSeparator(null).xyw(1, 4, 7)
                .build();
        this.add(panel, BorderLayout.NORTH);

        CommonTabbedPane tabbedPane = createTabPanel();
        JPanel propertyPanel = createAttributesPanel();
        propertyPanel.setBorder(new JBEmptyBorder(0));
        tabbedPane.addTab("??????", CompileFlowIcons.COMMON_ICON, propertyPanel);
        JPanel othersPanel = createOthersPanel();
        tabbedPane.addTab("??????", CompileFlowIcons.OTHERS_ICON, othersPanel);
        this.add(tabbedPane, BorderLayout.CENTER);
    }


    private CommonTabbedPane createTabPanel() {
        return new CommonTabbedPane();
    }


    private JBPanel createAttributesPanel() {
        JBPanel jbPanel = new JBPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        jbPanel.setLayout(gridBagLayout);
        FormLayout layout = new FormLayout(
                DEFAULT_PROPERTY_PAGE_COLUMN_MARGIN() + ",pref," + DEFAULT_PROPERTY_PAGE_COLUMN_SPEC() + ",pref,pref:grow," + DEFAULT_PROPERTY_PAGE_COLUMN_MARGIN(),
                DEFAULT_PROPERTY_PAGE_ROW_SPEC() + ",pref," + DEFAULT_PROPERTY_PAGE_ROW_SPEC() + ",pref," + DEFAULT_PROPERTY_PAGE_ROW_SPEC() + ",pref," + DEFAULT_PROPERTY_PAGE_ROW_SPEC() + ",pref," + DEFAULT_PROPERTY_PAGE_ROW_SPEC() + ",pref," + DEFAULT_PROPERTY_PAGE_ROW_SPEC() + ",fill:pref:grow,pref," + DEFAULT_PROPERTY_PAGE_ROW_SPEC());

        FormBuilder formBuilder = FormBuilder.create();
        ComponentFactory componentFactory = FormsSetup.getComponentFactoryDefault();
        formBuilder.factory(componentFactory);
        typeComboBox = new ComboBox<>();
        typeComboBox.addItem("ql");

        JBScrollPane paramsScrollPane = new JBScrollPane();
        compileFlowParamsTable = new CompileFlowBpmParamsTable(new CompileFlowParamsModel(this));

        final DefaultActionGroup group = new DefaultActionGroup();

        group.add(new AnAction(CompileFlowIcons.ADD_ICON) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                XmlTag xmlTag = ((XmlTagModelElement) myInput()).getXmlTag();
                String varName = "var";
                for (int i = 1; findPropertyMember(xmlTag, varName); i++) {
                    varName = "var" + i;
                }
                XmlTag varXmlTag = xmlTag.createChildTag(CompileFlowXmlTagConstants.VAR_TAG_NAME, null, null, true);
                varXmlTag.setAttribute("name", varName);
                varXmlTag.setAttribute(DATA_TYPE_ATTR_NAME, "java.lang.String");
                varXmlTag.setAttribute("desc", varName);
                varXmlTag.setAttribute("inOutType", "param");

                GraphEditDomain editDomain = (GraphEditDomain) getAdapter().getAdapter(GraphEditDomain.class);
                GraphEditCommandStack commandStack = editDomain.getCommandStack();
                AddCommand addCommand = new AddCommand(getAdapter(), xmlTag,
                        varXmlTag);
                commandStack.execute(addCommand);
            }

            private boolean findPropertyMember(XmlTag parentXmlTag, String propertyName) {
                long count =
                        Arrays.stream(parentXmlTag.getChildren()).filter(element -> (element instanceof XmlTag && StringUtils.equals(((XmlTag) element).getAttributeValue("name"), propertyName)) && ((XmlTag) element).getName().equals(CompileFlowXmlTagConstants.VAR_TAG_NAME)).count();
                return count > 0;
            }
        });
        group.add(new AnAction(CompileFlowIcons.REMOVE_ICON) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent event) {
                int row = compileFlowParamsTable.getSelectedRow();
                Object object = ((CompileFlowParamsModel) compileFlowParamsTable.getModel()).getParams().get(row);
                LOG.debug(object.toString());
                GraphEditDomain editDomain = (GraphEditDomain) getAdapter().getAdapter(GraphEditDomain.class);
                GraphEditCommandStack commandStack = editDomain.getCommandStack();
                RemoveCommand removeCommand = new RemoveCommand(getAdapter(),
                        (XmlTagModelElement) myInput(), (XmlTag) ((CompileFlowPropertyModel) object).getProperty());
                commandStack.execute(removeCommand);
            }
        });

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ToolbarPanel toolbarPanel = new ToolbarPanel(contentPane, group);
        toolbarPanel.setBorder(new CustomLineBorder(0, 0, 0, 0));

        JPanel panel = formBuilder.layout(layout).
                add(((DefaultComponentFactory) componentFactory).createSeparator(createSeparatorLabel("??????"))).xyw(2, 2, 4).
                add("??????:").xy(4, 4).
                add(nameTextField = new JTextField()).xy(5, 4).
                add("??????:").xy(4, 6).
                add(typeComboBox).xy(5, 6).
                add("?????????:").xy(4, 8).
                add(expressionTextField = new JTextField()).xy(5, 8).
                add(((DefaultComponentFactory) componentFactory).createSeparator(createSeparatorLabel("????????????"))).xyw(2, 10, 4).
                add(paramsScrollPane).xyw(4, 12, 2).
                add(toolbarPanel).xyw(4, 13, 2, "left,center").
                build();

        Dimension dimension = paramsScrollPane.getViewport().getPreferredSize();
        compileFlowParamsTable.setPreferredScrollableViewportSize(dimension);
        compileFlowParamsTable.setFillsViewportHeight(true);
        paramsScrollPane.setViewportView(compileFlowParamsTable);

        typeComboBox.addActionListener((event) -> {
            Object item = typeComboBox.getSelectedItem();
            Object object = myInput();
            XmlTag xmlTag = ((XmlTagModelElement) object).getXmlTag();
            GraphEditDomain componentEditDomain = (GraphEditDomain) getAdapter().getAdapter(GraphEditDomain.class);
            GraphEditCommandStack componentCommandStack = componentEditDomain.getCommandStack();
            SetCommand lftSetCommand = new SetCommand(getAdapter(), xmlTag,
                    "type"
                    , (String) item);
            componentCommandStack.execute(lftSetCommand);
        });


        myNameDocumentListener = new DocumentListener(NAME_ATTR_NAME);
        nameTextField.getDocument().addDocumentListener(myNameDocumentListener);

        myExpressDocumentListener = new DocumentListener(EXPRESSION_ATTR_NAME);
        expressionTextField.getDocument().addDocumentListener(myExpressDocumentListener);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = JBUI.insets(0);
        jbPanel.add(panel, gbc);
        return jbPanel;
    }


    @Override
    public void refresh() {
        super.refresh();
        Object object = myInput();
        if (object != null) {
            if (object instanceof XmlTagModelElement) {
                object = ((XmlTagModelElement) object).getXmlTag();
                String name = ((XmlTag) object).getAttribute(NAME_ATTR_NAME) == null ? "" : ((XmlTag) object).getAttribute(NAME_ATTR_NAME).getValue();
                nameTextField.getDocument().removeDocumentListener(myNameDocumentListener);
                if (!StringUtils.equals(name, nameTextField.getText())) {
                    nameTextField.setText(name);
                }
                nameTextField.getDocument().addDocumentListener(myNameDocumentListener);

                expressionTextField.getDocument().removeDocumentListener(myExpressDocumentListener);
                String expr = ((XmlTag) object).getAttribute(EXPRESSION_ATTR_NAME) == null ? "" : ((XmlTag) object).getAttribute(EXPRESSION_ATTR_NAME).getValue();
                if (!StringUtils.equals(StringEscapeUtils.unescapeXml(expr), expressionTextField.getText())) {
                    expressionTextField.setText(StringEscapeUtils.unescapeXml(expr));
                }
                expressionTextField.getDocument().addDocumentListener(myExpressDocumentListener);

                java.util.List vars = PsiElementQueryUtil.getPsiElementByTagName((XmlTag) object, CompileFlowXmlTagConstants.VAR_TAG_NAME);
                java.util.List<CompileFlowPropertyModel<Object>> params = new ArrayList();
                if (!vars.isEmpty()) {
                    for (Object var : vars) {
                        if (var instanceof XmlTag) {
                            params.add(new CompileFlowPropertyModel(var));
                        }
                    }
                }
                compileFlowParamsTable.getColumnModel().getColumn(1).setCellEditor(new CompileFlowClassCellEditor(adapter()));
                CompileFlowParamsModel compileFlowParamsModel = (CompileFlowParamsModel) compileFlowParamsTable.getModel();
                if (compileFlowParamsModel.getAdapter() == null)
                    compileFlowParamsModel.setAdapter(adapter());
                compileFlowParamsModel.setParams(params);

            }
        }

    }
}

