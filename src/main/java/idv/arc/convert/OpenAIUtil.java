package idv.arc.convert;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OpenAIUtil {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(3, TimeUnit.MINUTES)
            .connectTimeout(3, TimeUnit.MINUTES)
            .writeTimeout(3, TimeUnit.MINUTES)
            .readTimeout(3, TimeUnit.MINUTES)
            .build();
    private static final Gson gson = new Gson();
    public static Map<String, Object> list(String apiKey) {
        Request request = new Request.Builder()
                .url(PluginModel.listApi)
                .get()
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();
        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            return gson.fromJson(response.body().string(), HashMap.class);
        } catch (IOException e) {
            return gson.fromJson("{\"error\":\""+e.getMessage()+"\"}", HashMap.class);
        }
    }

    public static Map<String, Object> callModel(String prompt,String originCode, String apiKey, String model) {
        if(prompt==null||prompt.trim().isEmpty()){
            return new HashMap<>() {{
                put("error", "prompt is empty");
            }};
        }
        if(originCode==null||originCode.trim().isEmpty()){
            return new HashMap<>() {{
                put("error", "originCode is empty");
            }};
        }
        String json;
        String result;
        if(PluginModel.chatModelList.contains(model)){
            json = "{" +
                    "\"model\":"+gson.toJson(model)+"," +
                    "\"messages\":[{\"role\":\"user\",\"content\":"+gson.toJson(prompt+"\n"+originCode)+"}]," +
                    "\"max_tokens\":"+PluginModel.maxTokens+"," +
                    "\"temperature\":"+PluginModel.temperature+"," +
                    "\"top_p\":"+PluginModel.topP+"," +
                    "\"n\":"+PluginModel.n+
                    "}";
            result = post(PluginModel.chat,json, apiKey);
        }else if(PluginModel.completionsModelList.contains(model)){
            json = "{" +
                    "\"model\":"+gson.toJson(model)+"," +
                    "\"prompt\":"+gson.toJson(prompt+"\n"+originCode)+"," +
                    "\"max_tokens\":"+PluginModel.maxTokens+"," +
                    "\"temperature\":"+PluginModel.temperature+"," +
                    "\"top_p\":"+PluginModel.topP+"," +
                    "\"n\":" +PluginModel.n+
                    "}";
            result = post(PluginModel.completions,json, apiKey);
        }else if(PluginModel.etitsModelList.contains(model)){
            json ="{"+
                    "\"model\":"+gson.toJson(model)+"," +
                    "\"input\":"+ gson.toJson(originCode)+"," +
                    "\"instruction\":"+gson.toJson(prompt)+"," +
                    "\"temperature\":"+PluginModel.temperature+"," +
                    "\"top_p\":"+PluginModel.topP+"," +
                    "\"n\":" +PluginModel.n+
                "}";
            result = post(PluginModel.edits,json, apiKey);
        }else{
            return new HashMap<>() {{
                put("error", model+" not support");
            }};
        }
        try {
            return gson.fromJson(result, HashMap.class);
        } catch (Exception e) {
            String finalResult = result;
            return new HashMap<>() {{
                put("error", e.getMessage() + " " + finalResult);
            }};
        }
    }

    public static String post(String url, String json, String apiKey) {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();
        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            return response.body().string();
        } catch (IOException e) {
            return "{\"error\":\""+e.getMessage()+"\"}";
        }
    }
}
