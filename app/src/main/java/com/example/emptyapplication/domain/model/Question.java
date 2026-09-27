package com.example.emptyapplication.domain.model;

import java.io.Serializable;
import java.util.List;

public class Question implements Serializable {

    private final String question; // el enunciado
    private final String category; // categoría
    private final String difficulty; // fácil, medio, difícil
    private final List<String> options; // las 4 opciones ya barajadas
    private final String correctAnswer; // la respuesta correcta

    public Question(String question, String category, String difficulty, List<String> options, String correctAnswer) {
        this.question = question;
        this.category = category;
        this.difficulty = difficulty;
        this.options = options;
        this.correctAnswer = correctAnswer;
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

    public List<String> getOptions() {
        return options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
