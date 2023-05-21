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
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    private static final Gson gson = new Gson();

    public static Map<String, Object> complete(String prompt, String apiKey, String model) {
        String json = "{" +
                "\"model\":"+gson.toJson(model)+"," +
                "\"prompt\":"+gson.toJson(prompt)+"," +
                "\"max_tokens\":3000," +
                "\"temperature\":0," +
                "\"top_p\": 1," +
                "\"n\": 1" +
                "}";
        String type = "";
        if(model.contains("gpt")){
            type="/chat";
            json = "{" +
                    "\"model\":"+gson.toJson(model)+"," +
                    "\"messages\":[{\"role\":\"user\",\"content\":"+gson.toJson(prompt)+"}]," +
                    "\"max_tokens\":3000," +
                    "\"temperature\":0," +
                    "\"top_p\": 1," +
                    "\"n\": 1" +
                    "}";
        }

        String result = post("https://api.openai.com/v1"+type+"/completions", json, apiKey);
        try {
            return gson.fromJson(result, HashMap.class);
        } catch (Exception e) {
            return new HashMap<>() {{
                put("error", e.getMessage() + " " + result);
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