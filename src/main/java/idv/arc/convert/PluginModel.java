package idv.arc.convert;

import java.util.Arrays;
import java.util.List;

public class PluginModel {
    public static final String listApi="https://api.openai.com/v1/models";
    public static final String api="https://api.openai.com/v1%s/completions";
    public static String apiKey;
    public static String currentModel ="text-davinci-003";
    public static List<String> allModel = Arrays.asList(currentModel);
    public static String prompt;
    public static String originCode="";
    public static String resultCode="";
    public static Boolean isWrap = false;

    public static Integer maxTokens = 3000;
}
