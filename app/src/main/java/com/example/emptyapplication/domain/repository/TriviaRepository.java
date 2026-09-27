package com.example.emptyapplication.domain.repository;

import com.example.emptyapplication.domain.model.Question;

import java.util.List;

public interface TriviaRepository {
    interface Callback<T> {
        void onSuccess(T result);
        void onError(Throwable throwable);
    }
        void getQuestions(int amount, Callback<List<Question>> callback);
}
