package com.example.emptyapplication.data.remote.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://opentdb.com/";
    private static volatile TriviaApiService apiService;

    private RetrofitClient() {
        // Prevent instantiation
    }

    public static TriviaApiService getInstance() {
        if (apiService == null) {
            synchronized (RetrofitClient.class) {
                if (apiService == null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    apiService = retrofit.create(TriviaApiService.class);
                }
            }
        }
        return apiService;
    }

    public static TriviaApiService getApiService() {
        return getInstance();
    }
}
