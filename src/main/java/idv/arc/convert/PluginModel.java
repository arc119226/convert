package idv.arc.convert;

import java.util.Arrays;
import java.util.List;

public class PluginModel {

    public static final String listApi="https://api.openai.com/v1/models";
    public static final String completions ="https://api.openai.com/v1/completions";
    public static final String chat ="https://api.openai.com/v1//chat/completions";
    public static final String edits = "https://api.openai.com/v1/edits";
    public static String apiKey;
    public static String currentModel ="text-davinci-003";
    public static List<String> allModel = List.of(currentModel);
    public static String prompt;
    public static String originCode="";
    public static String resultCode="";
    public static Boolean isWrap = false;
    public static Integer maxTokens = 3000;
    public static Double temperature = 0.0d;
    public static Integer topP = 1;

    public static Integer n = 1;

    public static final List<String> chatModelList = Arrays.asList(
            "gpt-4",
            "gpt-4-0314",
            "gpt-4-32k",
            "gpt-4-32k-0314",
            "gpt-3.5-turbo",
            "gpt-3.5-turbo-0301"
    );

    public static final List<String> completionsModelList = Arrays.asList(
            "babbage",
            "text-babbage-001",
            "davinci",
            "davinci-instruct-beta",
            "text-davinci-003",
            "text-davinci-002",
            "text-davinci-001",
            "curie",
            "curie-instruct-beta",
            "text-curie-001",
            "ada",
            "text-ada-001"
    );
    public static final List<String> etitsModelList = Arrays.asList(
          "text-davinci-edit-001",
          "code-davinci-edit-001"
    );
}
