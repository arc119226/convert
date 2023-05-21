package idv.arc.convert;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static idv.arc.convert.OpenAIUtil.complete;

public class PluginView extends DialogWrapper {
    private JPasswordField apiKeyArea;
    private JButton listModelButton;
    private JBList<String> modelList;
    private JBTextField tokenField;
    private JBTextArea promptArea;
    private JBTextArea codeArea;
    private JBTextArea resultArea;
    private JButton convertButton;
    private JBCheckBox lineWrapCheckBox;
    private final Editor editor;

    protected PluginView(@Nullable Project project, Editor editor) {
        super(project);
        this.editor = editor;
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        super.getOKAction().putValue(Action.NAME, "Insert Code");
        super.setTitle("ChatGPT Fast Generate Code");
        JPanel dialogPanel = new JPanel(new BorderLayout());
        JPanel eastPanel = new JPanel(new BorderLayout());
        JPanel westPanel = new JPanel(new BorderLayout());
        JPanel northPanel = new JPanel(new BorderLayout());
        JPanel southPanel = new JPanel(new BorderLayout());
        JPanel centerPanel = new JPanel(new BorderLayout());

        JPanel generatePanel = new JPanel(new FlowLayout());
        JPanel lineWrapPanel = new JPanel(new FlowLayout());
        JPanel apiKeyPanel = new JPanel(new BorderLayout());
        JPanel modelPanel = new JPanel(new BorderLayout());
        JPanel tokenPanel = new JPanel(new BorderLayout());

        Dimension buttonSize = new Dimension(110, 50);
        convertButton = new JButton("Run");
        convertButton.setPreferredSize(buttonSize);
        convertButton.setMaximumSize(buttonSize);

        lineWrapCheckBox = new JBCheckBox("Wrap");
        lineWrapCheckBox.setPreferredSize(buttonSize);
        lineWrapCheckBox.setMaximumSize(buttonSize);

        apiKeyArea = new JPasswordField(PluginModel.apiKey,30);
        apiKeyArea.setEditable(true);

        modelList = new JBList<>(PluginModel.allModel);

        tokenField = new JBTextField(PluginModel.maxTokens.toString(), 5);

        promptArea = new JBTextArea(PluginModel.prompt, 5, 30);
        promptArea.setEditable(true);
        promptArea.getCaret().setVisible(true);
        promptArea.getCaret().setSelectionVisible(true);
        promptArea.setLineWrap(true);
        promptArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

        codeArea = new JBTextArea(PluginModel.originCode, 20, 60);
        codeArea.setEditable(true);
        codeArea.getCaret().setVisible(true);
        codeArea.getCaret().setSelectionVisible(true);
        codeArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

        resultArea = new JBTextArea(PluginModel.resultCode, 20, 60);
        resultArea.setEditable(true);
        resultArea.getCaret().setVisible(true);
        resultArea.getCaret().setSelectionVisible(true);
        resultArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

        generatePanel.add(convertButton);
        lineWrapPanel.add(lineWrapCheckBox);

        centerPanel.add(generatePanel, BorderLayout.NORTH);
        centerPanel.add(lineWrapPanel, BorderLayout.CENTER);

        apiKeyPanel.add(new JLabel("API Key :"), BorderLayout.WEST);
        apiKeyPanel.add(new JScrollPane(apiKeyArea), BorderLayout.CENTER);
        apiKeyPanel.add(listModelButton = new JButton("List"), BorderLayout.EAST);

        modelPanel.add(new JLabel("ALL Models    :"), BorderLayout.WEST);
        modelPanel.add(new JScrollPane(modelList), BorderLayout.CENTER);

        tokenPanel.add(new JLabel("Max Tokens   :"), BorderLayout.WEST);
        tokenPanel.add(tokenField, BorderLayout.CENTER);

        northPanel.add(apiKeyPanel, BorderLayout.NORTH);
        northPanel.add(modelPanel, BorderLayout.CENTER);
        northPanel.add(tokenPanel, BorderLayout.SOUTH);

        southPanel.add(new JLabel("Prompt   :"), BorderLayout.NORTH);
        southPanel.add(new JScrollPane(promptArea), BorderLayout.CENTER);

        westPanel.add(new JLabel("Code  :"), BorderLayout.NORTH);
        westPanel.add(new JScrollPane(codeArea), BorderLayout.CENTER);

        eastPanel.add(new JLabel("Result    :"), BorderLayout.NORTH);
        eastPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        dialogPanel.add(northPanel, BorderLayout.NORTH);
        dialogPanel.add(southPanel, BorderLayout.SOUTH);
        dialogPanel.add(westPanel, BorderLayout.WEST);
        dialogPanel.add(eastPanel, BorderLayout.EAST);
        dialogPanel.add(centerPanel, BorderLayout.CENTER);

        registerTokenFieldInputRule();
        registerListModelEvent();
        registerlineWropEvent();
        registerGenerateCodeEvent();

        lineWrapCheckBox.setSelected(PluginModel.isWrap);
        modelList.setSelectedValue(PluginModel.currentModel, true);
        modelList.setVisibleRowCount(3);

        return dialogPanel;
    }

    private void registerTokenFieldInputRule() {
        ((AbstractDocument) tokenField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string.matches("\\d*")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("\\d*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    private void registerListModelEvent() {
        listModelButton.addActionListener(e -> new Thread(() -> {
            try{
                listModelButton.setText("Processing...");
                listModelButton.update(listModelButton.getGraphics());
                listModelButton.setEnabled(false);

                PluginModel.apiKey = new String(apiKeyArea.getPassword());
                Map<String,Object> results = OpenAIUtil.list(PluginModel.apiKey);
                List<Map<String,Object>> dataList = (List<Map<String, Object>>) results.get("data");
                PluginModel.allModel = dataList.stream().map(data -> (String) data.get("id")).collect(Collectors.toList());
                modelList.setListData(PluginModel.allModel.toArray(new String[0]));
                modelList.setSelectedValue(PluginModel.allModel.get(0), true);
                PluginModel.currentModel = PluginModel.allModel.get(0);
            }catch (Exception ex){
                resultArea.append(ex.getMessage());
                resultArea.update(resultArea.getGraphics());
            }finally {
                listModelButton.setText("List");
                listModelButton.setEnabled(true);
            }
        }).start());
    }

    private void registerGenerateCodeEvent() {
        convertButton.addActionListener(e -> new Thread(() -> {
            try{
                resultArea.setText("");
                convertButton.setText("Run...");
                convertButton.update(convertButton.getGraphics());
                convertButton.setEnabled(false);

                updatePluginModel();

                Map<String,Object> result = complete(PluginModel.prompt+"\n"+PluginModel.originCode, PluginModel.apiKey, PluginModel.currentModel);
                if(result.get("error")!=null){
                    PluginModel.resultCode = result.get("error").toString();
                    resultArea.append(PluginModel.resultCode);
                    resultArea.update(resultArea.getGraphics());
                }else{
                    if(result.get("choices")!=null){
                        List<Map<String,Object>> choices = (List<Map<String,Object>>) result.get("choices");
                        if(null!=choices){
                            if (PluginModel.currentModel.contains("gpt")){
                                Map<String,Object> message = (Map<String,Object>)choices.get(0).get("message");
                                PluginModel.resultCode = message.get("content").toString().trim();
                            }else {
                                PluginModel.resultCode = choices.get(0).get("text").toString().trim();
                            }
                        }else{
                            PluginModel.resultCode = "text is null";
                        }
                        resultArea.append(PluginModel.resultCode);
                        resultArea.update(resultArea.getGraphics());
                    }else{
                        PluginModel.resultCode = "choices is null";
                        resultArea.append(PluginModel.resultCode);
                        resultArea.update(resultArea.getGraphics());
                    }
                }
            }catch(Exception ex){
                PluginModel.resultCode =ex.getMessage();
                resultArea.append(PluginModel.currentModel+"-"+PluginModel.resultCode);
                resultArea.update(resultArea.getGraphics());
            }finally {
                convertButton.setText("Run");
                convertButton.setEnabled(true);
                convertButton.update(convertButton.getGraphics());
            }
        }).start());
    }

    private void registerlineWropEvent() {
        lineWrapCheckBox.addItemListener(e -> {
            PluginModel.isWrap = lineWrapCheckBox.isSelected();
            if (e.getStateChange() == ItemEvent.SELECTED) {
                codeArea.setLineWrap(true);
                codeArea.update(codeArea.getGraphics());
                resultArea.setLineWrap(true);
                resultArea.update(resultArea.getGraphics());
            } else {
                codeArea.setLineWrap(false);
                codeArea.update(codeArea.getGraphics());
                resultArea.setLineWrap(false);
                resultArea.update(resultArea.getGraphics());
            }
        });
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (codeArea.getText().trim().isEmpty()) {
            return new ValidationInfo("Text cannot be empty", codeArea);
        }
        return null;
    }

    @Override
    protected void doOKAction() {
        if (doValidate() == null) {
            String text = resultArea.getText();
            Document document = editor.getDocument();
            CaretModel caretModel = editor.getCaretModel();
            Project project = editor.getProject();
            WriteCommandAction.runWriteCommandAction(project, () -> document.insertString(caretModel.getOffset(), text));
            updatePluginModel();
            super.doOKAction();
            close(DialogWrapper.OK_EXIT_CODE);
        }
    }

    private void updatePluginModel() {
        PluginModel.apiKey = new String(apiKeyArea.getPassword());
        PluginModel.prompt = promptArea.getText();
        PluginModel.currentModel = modelList.getSelectedValue();
        PluginModel.isWrap = lineWrapCheckBox.isSelected();
        PluginModel.maxTokens = Integer.parseInt(tokenField.getText());
        PluginModel.originCode = codeArea.getText();
        PluginModel.resultCode = resultArea.getText();
    }
}
