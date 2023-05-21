package idv.arc.convert;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Map;

import static idv.arc.convert.OpenAIUtil.complete;

public class PluginView extends DialogWrapper {

    private JPasswordField apiKeyArea;
    private JTextField modelArea;
    private JTextArea promptArea;
    private JTextArea textArea;
    private JTextArea resultArea;
    private JButton convertButton;
    private JCheckBox lineWrapCheckBox;

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
        JPanel apiKeyPanel = new JPanel(new BorderLayout());
        JPanel modelPanel = new JPanel(new BorderLayout());
        JPanel southPanel = new JPanel(new BorderLayout());

        Dimension buttonSize = new Dimension(100, 50);
        convertButton = new JButton("Generate");
        convertButton.setPreferredSize(buttonSize);
        convertButton.setMaximumSize(buttonSize);

        lineWrapCheckBox = new JCheckBox("Line Wrap");
        lineWrapCheckBox.setPreferredSize(buttonSize);
        lineWrapCheckBox.setMaximumSize(buttonSize);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(convertButton);
        JPanel lineWrapPanel = new JPanel(new FlowLayout());
        lineWrapPanel.add(lineWrapCheckBox);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(buttonPanel, BorderLayout.NORTH);
        centerPanel.add(lineWrapPanel, BorderLayout.CENTER);

        apiKeyArea = new JPasswordField(PluginModel.apiKey,30);
        apiKeyArea.setEditable(true);
        modelArea = new JTextField(PluginModel.model,30);
        modelArea.setEditable(true);

        promptArea = new JTextArea(PluginModel.prompt, 5, 30);
        promptArea.setEditable(true);
        promptArea.getCaret().setVisible(true);
        promptArea.getCaret().setSelectionVisible(true);
        promptArea.setLineWrap(true);
        promptArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

        textArea = new JTextArea(PluginModel.originCode, 20, 60);
        textArea.setEditable(true);
        textArea.getCaret().setVisible(true);
        textArea.getCaret().setSelectionVisible(true);
        textArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

        resultArea = new JTextArea(PluginModel.resultCode, 20, 60);
        resultArea.setEditable(true);
        resultArea.getCaret().setVisible(true);
        resultArea.getCaret().setSelectionVisible(true);
        resultArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

        apiKeyPanel.add(new JLabel("API Key:"), BorderLayout.WEST);
        apiKeyPanel.add(new JScrollPane(apiKeyArea), BorderLayout.CENTER);
        modelPanel.add(new JLabel("Model:"), BorderLayout.WEST);
        modelPanel.add(new JScrollPane(modelArea), BorderLayout.CENTER);

        northPanel.add(apiKeyPanel, BorderLayout.NORTH);
        northPanel.add(modelPanel, BorderLayout.SOUTH);

        southPanel.add(new JLabel("Prompt:"), BorderLayout.NORTH);
        southPanel.add(new JScrollPane(promptArea), BorderLayout.CENTER);

        westPanel.add(new JLabel("Code:"), BorderLayout.NORTH);
        westPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        eastPanel.add(new JLabel("Result:"), BorderLayout.NORTH);
        eastPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        dialogPanel.add(northPanel, BorderLayout.NORTH);
        dialogPanel.add(southPanel, BorderLayout.SOUTH);
        dialogPanel.add(westPanel, BorderLayout.WEST);
        dialogPanel.add(eastPanel, BorderLayout.EAST);
        dialogPanel.add(centerPanel, BorderLayout.CENTER);

        registerlineWropEvent();
        registerGenerateCodeEvent();

        return dialogPanel;
    }

    private void registerGenerateCodeEvent() {
        convertButton.addActionListener(e -> new Thread(){
            @Override
            public void run() {
                try{
                    resultArea.setText("");
                    convertButton.setText("Processing...");
                    convertButton.update(convertButton.getGraphics());
                    convertButton.setEnabled(false);

                    PluginModel.apiKey = new String(apiKeyArea.getPassword());
                    PluginModel.prompt = promptArea.getText();
                    PluginModel.model = modelArea.getText();
                    PluginModel.originCode = textArea.getText();

                    Map<String,Object> result = complete(PluginModel.prompt+"\n"+PluginModel.originCode, PluginModel.apiKey, PluginModel.model);
                    if(result.get("error")!=null){
                        PluginModel.resultCode = result.get("error").toString();
                        resultArea.append(PluginModel.resultCode);
                        resultArea.update(resultArea.getGraphics());
                    }else{
                        if(result.get("choices")!=null){
                            List<Map<String,Object>> choices = (List<Map<String,Object>>) result.get("choices");
                            if(null!=choices){
                                if (PluginModel.model.contains("gpt")){
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
                    resultArea.append(PluginModel.resultCode);
                    resultArea.update(resultArea.getGraphics());
                }finally {
                    convertButton.setText("Generate");
                    convertButton.update(convertButton.getGraphics());
                    convertButton.setEnabled(true);
                }
            }
        }.start());
    }

    private void registerlineWropEvent() {
        lineWrapCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                textArea.setLineWrap(true);
                textArea.update(textArea.getGraphics());
                resultArea.setLineWrap(true);
                resultArea.update(resultArea.getGraphics());
            } else {
                textArea.setLineWrap(false);
                textArea.update(textArea.getGraphics());
                resultArea.setLineWrap(false);
                resultArea.update(resultArea.getGraphics());
            }
        });
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (textArea.getText().trim().isEmpty()) {
            return new ValidationInfo("Text cannot be empty", textArea);
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
            PluginModel.apiKey = new String(apiKeyArea.getPassword());
            PluginModel.prompt = promptArea.getText();
            PluginModel.model = modelArea.getText();
            super.doOKAction();
            close(DialogWrapper.OK_EXIT_CODE);
        }
    }
}
