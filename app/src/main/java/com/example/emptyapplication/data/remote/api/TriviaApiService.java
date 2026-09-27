package com.example.emptyapplication.data.remote.api;

import com.example.emptyapplication.data.remote.dto.TriviaApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TriviaApiService {

    @GET("api.php")
    Call<TriviaApiResponse> getQuestions(@Query("amount") int amount, @Query("type") String type);
}
