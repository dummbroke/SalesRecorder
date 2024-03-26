package com.example.salesrecorder;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class TokenRefresher {

    public static String refreshToken(String refreshToken, String clientId, String clientSecret) {
        try {
            URL url = new URL("https://oauth2.googleapis.com/token");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            Map<Object, Object> data = new HashMap<>();
            data.put("refresh_token", refreshToken);
            data.put("client_id", clientId);
            data.put("client_secret", clientSecret);
            data.put("grant_type", "refresh_token");

            connection.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(buildFormDataFromMap(data));
            out.flush();
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            connection.disconnect();

            return parseAccessToken(content.toString());
        } catch (Exception e) {
            Log.e("TokenRefresher", "Error refreshing token", e);
            return null;
        }
    }

    private static String buildFormDataFromMap(Map<Object, Object> data) {
        StringJoiner sj = new StringJoiner("&");
        for (Map.Entry<Object, Object> entry : data.entrySet())
            sj.add(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        return sj.toString();
    }

    private static String parseAccessToken(String responseBody) {
        // Implementation depends on the JSON parsing library you are using.
        return ""; // Return the parsed access token.
    }
}
