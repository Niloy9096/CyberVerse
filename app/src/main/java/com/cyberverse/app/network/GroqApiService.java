package com.cyberverse.app.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface GroqApiService {
    @POST("chat/completions")
    Call<GroqResponse> getChatCompletion(
            @Header("Authorization") String authorization,
            @Body GroqRequest request
    );
}
