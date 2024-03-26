package com.example.salesrecorder;
import com.google.gson.annotations.SerializedName;

public class TokenResponse {

    @SerializedName("access_token")
    private String accessToken;

    // You can add other fields based on your API response

    public String getAccessToken() {
        return accessToken;
    }
}
