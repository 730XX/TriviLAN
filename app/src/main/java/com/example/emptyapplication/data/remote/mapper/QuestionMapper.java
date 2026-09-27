package com.example.emptyapplication.data.remote.mapper;

import android.os.Build;
import android.text.Html;

import com.example.emptyapplication.data.remote.dto.QuestionDto;
import com.example.emptyapplication.domain.model.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestionMapper {

    private QuestionMapper() {
        // Prevent instantiation
    }

    public static Question mapToDomain(QuestionDto dto) {
        if (dto == null) {
            return null;
        }

        String question = cleanHtml(dto.getQuestion());
        String category = cleanHtml(dto.getCategory());
        String difficulty = cleanHtml(dto.getDifficulty());
        String correctAnswer = cleanHtml(dto.getCorrectAnswer());

        List<String> options = new ArrayList<>();
        if (dto.getIncorrectAnswers() != null) {
            for (String incorrectAnswer : dto.getIncorrectAnswers()) {
                options.add(cleanHtml(incorrectAnswer));
            }
        }
        options.add(correctAnswer);
        Collections.shuffle(options);

        return new Question(question, category, difficulty, options, correctAnswer);
    }

    public static List<Question> mapToDomainList(List<QuestionDto> dtos) {
        if (dtos == null) {
            return Collections.emptyList();
        }

        List<Question> questions = new ArrayList<>();
        for (QuestionDto dto : dtos) {
            Question question = mapToDomain(dto);
            if (question != null) {
                questions.add(question);
            }
        }
        return questions;
    }

    @SuppressWarnings("deprecation")
    private static String cleanHtml(String text) {
        if (text == null) {
            return "";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(text).toString();
        }
    }
}
