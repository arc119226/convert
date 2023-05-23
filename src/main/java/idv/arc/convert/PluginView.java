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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static idv.arc.convert.OpenAIUtil.callModel;

public class PluginView extends DialogWrapper {

    private JPanel dialogPanel;
    private JPanel eastPanel ;
    private JPanel westPanel;
    private JPanel northPanel;
    private JPanel southPanel;
    private JPanel centerPanel;

    private JPanel generatePanel;
    private JPanel lineWrapPanel;
    private JPanel apiKeyPanel;
    private JPanel modelPanel;
    private JPanel settingPanel;

    private JPasswordField apiKeyInput;
    private JButton listModelButton;
    private JBList<String> modelList;
    private JButton retrieveButton;
    private JBTextField tokenInput;
//    private JBTextField temperatureInput;
//    private JBTextField topPInput;
    private JBTextField nInput;
    private JBTextArea promptTextArea;
    private JBTextArea codeArea;
    private JBTextArea resultArea;
    private JButton runButton;
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

        dialogPanel = new JPanel(new BorderLayout());

        eastPanel = new JPanel(new BorderLayout());
        westPanel = new JPanel(new BorderLayout());
        northPanel = new JPanel(new BorderLayout());
        southPanel = new JPanel(new BorderLayout());
        centerPanel = new JPanel(new BorderLayout());

        generatePanel = new JPanel(new FlowLayout());
        lineWrapPanel = new JPanel(new FlowLayout());

        apiKeyPanel = new JPanel(new BorderLayout());
        modelPanel = new JPanel(new BorderLayout());
        settingPanel = new JPanel(new FlowLayout());

        Dimension buttonSize = new Dimension(110, 50);
        runButton = new JButton("Run");
        runButton.setPreferredSize(buttonSize);
        runButton.setMaximumSize(buttonSize);

        lineWrapCheckBox = new JBCheckBox("Wrap");
        lineWrapCheckBox.setPreferredSize(buttonSize);
        lineWrapCheckBox.setMaximumSize(buttonSize);

        listModelButton = new JButton("List");
        listModelButton.setPreferredSize(buttonSize);
        listModelButton.setMaximumSize(buttonSize);

        retrieveButton = new JButton("Retrieve");
        retrieveButton.setPreferredSize(buttonSize);
        retrieveButton.setMaximumSize(buttonSize);

        apiKeyInput = new JPasswordField(PluginModel.apiKey,30);
        apiKeyInput.setEditable(true);

        modelList = new JBList<>(PluginModel.allModel);

        tokenInput = new JBTextField(PluginModel.maxTokens.toString(), 5);
//        temperatureInput = new JBTextField(PluginModel.temperature.toString(), 5);
//        topPInput = new JBTextField(PluginModel.topP.toString(), 5);
        nInput = new JBTextField(PluginModel.n.toString(), 5);

        promptTextArea = new JBTextArea(PluginModel.prompt, 5, 30);
        promptTextArea.setEditable(true);
        promptTextArea.getCaret().setVisible(true);
        promptTextArea.getCaret().setSelectionVisible(true);
        promptTextArea.setLineWrap(true);
        promptTextArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

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

        generatePanel.add(runButton);
        lineWrapPanel.add(lineWrapCheckBox);

        centerPanel.add(generatePanel, BorderLayout.NORTH);
        centerPanel.add(lineWrapPanel, BorderLayout.CENTER);

        apiKeyPanel.add(new JLabel("API Key :"), BorderLayout.WEST);
        apiKeyPanel.add(new JScrollPane(apiKeyInput), BorderLayout.CENTER);
        apiKeyPanel.add(listModelButton, BorderLayout.EAST);

        modelPanel.add(new JLabel("ALL Models    :"), BorderLayout.WEST);
        modelPanel.add(new JScrollPane(modelList), BorderLayout.CENTER);
        modelList.add(retrieveButton, BorderLayout.EAST);

        settingPanel.add(new JLabel("Max Tokens   :"));
        settingPanel.add(tokenInput);
//        settingPanel.add(new JLabel("Temperature    :"));
//        settingPanel.add(temperatureInput);
//        settingPanel.add(new JLabel("Top P    :"));
//        settingPanel.add(topPInput);
        settingPanel.add(new JLabel("N    :"));
        settingPanel.add(nInput);

        northPanel.add(apiKeyPanel, BorderLayout.NORTH);
        northPanel.add(modelPanel, BorderLayout.CENTER);
        northPanel.add(settingPanel, BorderLayout.SOUTH);

        southPanel.add(new JLabel("Prompt   :"), BorderLayout.NORTH);
        southPanel.add(new JScrollPane(promptTextArea), BorderLayout.CENTER);

        westPanel.add(new JLabel("Code  :"), BorderLayout.NORTH);
        westPanel.add(new JScrollPane(codeArea), BorderLayout.CENTER);

        eastPanel.add(new JLabel("Result    :"), BorderLayout.NORTH);
        eastPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        dialogPanel.add(northPanel, BorderLayout.NORTH);
        dialogPanel.add(southPanel, BorderLayout.SOUTH);
        dialogPanel.add(westPanel, BorderLayout.WEST);
        dialogPanel.add(eastPanel, BorderLayout.EAST);
        dialogPanel.add(centerPanel, BorderLayout.CENTER);

        retrieveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

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
        ((AbstractDocument) tokenInput.getDocument()).setDocumentFilter(new DocumentFilter() {
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
//        ((AbstractDocument) temperatureInput.getDocument()).setDocumentFilter(new DocumentFilter() {
//            @Override
//            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
//                //matchs 0.0~2.0
//                if(string.matches("^[0-1](\\.\\d+)?$") || string.matches("^2(\\.0)?$")){
//                    super.insertString(fb, offset, string, attr);
//                }
//            }
//
//            @Override
//            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
//                //matchs 0.0~2.0
//                if(text.matches("^[0-1](\\.\\d+)?$") || text.matches("^2(\\.0)?$")){
//                    super.replace(fb, offset, length, text, attrs);
//                }
//            }
//        });
//        ((AbstractDocument) topPInput.getDocument()).setDocumentFilter(new DocumentFilter() {
//            @Override
//            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
//                //matchs 0.0~1.0
//                if(string.matches("^[0-1](\\.\\d+)?$")){
//                    super.insertString(fb, offset, string, attr);
//                }
//            }
//
//            @Override
//            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
//                //matchs 0.0~1.0
//                if(text.matches("^[0-1](\\.\\d+)?$")){
//                    super.replace(fb, offset, length, text, attrs);
//                }
//            }
//        });
        ((AbstractDocument) nInput.getDocument()).setDocumentFilter(new DocumentFilter() {
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

                PluginModel.apiKey = new String(apiKeyInput.getPassword());
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
                listModelButton.update(listModelButton.getGraphics());
            }
        }).start());
    }

    private void registerGenerateCodeEvent() {
        runButton.addActionListener(e -> new Thread(() -> {
            try{
                resultArea.setText("");
                runButton.setText("Run...");
                runButton.update(runButton.getGraphics());
                runButton.setEnabled(false);

                updatePluginModel();

                Map<String,Object> result = callModel(PluginModel.prompt,PluginModel.originCode, PluginModel.apiKey, PluginModel.currentModel);
                if(result.get("error")!=null){
                    PluginModel.resultCode = result.get("error").toString();
                    resultArea.append(PluginModel.resultCode);
                    resultArea.update(resultArea.getGraphics());
                }else{
                    if(result.get("choices")!=null){
                        List<Map<String,Object>> choices = (List<Map<String,Object>>) result.get("choices");

                        if(null!=choices){
                            if(PluginModel.completionsModelList.contains(PluginModel.currentModel)){
                                choices.forEach(choice -> {
                                    Map<String,Object> message = choice;
                                    PluginModel.resultCode += message.get("text").toString().trim();
                                    PluginModel.resultCode += "\n";
                                });
                                PluginModel.resultCode = choices.get(0).get("text").toString().trim();
                            }else if(PluginModel.chatModelList.contains(PluginModel.currentModel)){
                                choices.forEach(choice -> {
                                    Map<String,Object> message = (Map<String,Object>)choice.get("message");
                                    PluginModel.resultCode += message.get("content").toString().trim();
                                    PluginModel.resultCode += "\n";
                                });
                            }else if(PluginModel.etitsModelList.contains(PluginModel.currentModel)){
                                choices.forEach(choice -> {
                                    Map<String,Object> message = choice;
                                    PluginModel.resultCode += message.get("text").toString().trim();
                                    PluginModel.resultCode += "\n";
                                });
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
                runButton.setText("Run");
                runButton.setEnabled(true);
                runButton.update(runButton.getGraphics());
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
            String selectedText = resultArea.getSelectedText();
            String pastText;
            if(resultArea.getSelectedText()!=null){
                pastText = selectedText;
            }else {
                pastText = text;
            }

            Document document = editor.getDocument();
            CaretModel caretModel = editor.getCaretModel();
            Project project = editor.getProject();
            WriteCommandAction.runWriteCommandAction(project, () -> document.insertString(caretModel.getOffset(), pastText));
            updatePluginModel();
            super.doOKAction();
            close(DialogWrapper.OK_EXIT_CODE);
        }
    }

    private void updatePluginModel() {
        PluginModel.apiKey = new String(apiKeyInput.getPassword());
        PluginModel.prompt = promptTextArea.getText();
        PluginModel.currentModel = modelList.getSelectedValue();
        PluginModel.isWrap = lineWrapCheckBox.isSelected();
        PluginModel.maxTokens = Integer.parseInt(tokenInput.getText());
//        PluginModel.temperature = Double.parseDouble(temperatureInput.getText());
//        PluginModel.topP = Integer.parseInt(topPInput.getText());
        PluginModel.n = Integer.parseInt(nInput.getText());
        PluginModel.originCode = codeArea.getText();
        PluginModel.resultCode = resultArea.getText();
    }
}
