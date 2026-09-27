package com.example.emptyapplication.data.repository;

import com.example.emptyapplication.data.remote.api.RetrofitClient;
import com.example.emptyapplication.data.remote.api.TriviaApiService;
import com.example.emptyapplication.data.remote.dto.TriviaApiResponse;
import com.example.emptyapplication.data.remote.mapper.QuestionMapper;
import com.example.emptyapplication.domain.model.Question;
import com.example.emptyapplication.domain.repository.TriviaRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class TriviaRepositoryImpl implements TriviaRepository {

    private final TriviaApiService apiService;

    public TriviaRepositoryImpl(TriviaApiService apiService) {
        this.apiService = apiService;
    }

    public TriviaRepositoryImpl() {
        this(RetrofitClient.getInstance());
    }

    @Override
    public void getQuestions(int amount, Callback<List<Question>> callback) {
        apiService.getQuestions(amount, "multiple").enqueue(new retrofit2.Callback<TriviaApiResponse>() {
            @Override
            public void onResponse(Call<TriviaApiResponse> call, Response<TriviaApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TriviaApiResponse body = response.body();
                    if (body.getResponseCode() != 0) {
                        callback.onError(new Exception("Trivia API returned code: " + body.getResponseCode()));
                    } else {
                        List<Question> questions = QuestionMapper.mapToDomainList(body.getResults());
                        callback.onSuccess(questions);
                    }
                } else {
                    callback.onError(new Exception("Network request failed with code: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<TriviaApiResponse> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
}
