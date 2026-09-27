package com.example.emptyapplication.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuestionDto {

    @SerializedName("question")
    private String question;

    @SerializedName("category")
    private String category;

    @SerializedName("difficulty")
    private String difficulty;

    @SerializedName("correct_answer")
    private String correctAnswer;

    @SerializedName("incorrect_answers")
    private List<String> incorrectAnswers;

    public QuestionDto() {
    }

    public QuestionDto(String question, String category, String difficulty, String correctAnswer, List<String> incorrectAnswers) {
        this.question = question;
        this.category = category;
        this.difficulty = difficulty;
        this.correctAnswer = correctAnswer;
        this.incorrectAnswers = incorrectAnswers;
    }

    public String getQuestion() {
        return question;
    }

    public String getCategory() {
        return category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public List<String> getIncorrectAnswers() {
        return incorrectAnswers;
    }
}
