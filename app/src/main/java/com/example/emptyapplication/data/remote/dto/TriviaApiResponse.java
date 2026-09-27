package com.example.emptyapplication.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TriviaApiResponse {

    @SerializedName("response_code")
    private int responseCode;

    @SerializedName("results")
    private List<QuestionDto> results;

    public TriviaApiResponse() {
    }

    public TriviaApiResponse(int responseCode, List<QuestionDto> results) {
        this.responseCode = responseCode;
        this.results = results;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public List<QuestionDto> getResults() {
        return results;
    }
}
