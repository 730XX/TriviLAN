package com.example.emptyapplication.presentation.game;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.emptyapplication.R;
import com.example.emptyapplication.data.repository.TriviaRepositoryImpl;
import com.example.emptyapplication.databinding.FragmentGameBinding;
import com.example.emptyapplication.domain.model.Question;
import com.example.emptyapplication.domain.repository.TriviaRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Fragment that displays and manages the trivia game question loop, countdown timer,
 * scoring, animations, and transitions.
 */
public class GameFragment extends Fragment {

    private static final String ARG_QUESTIONS_JSON = "arg_questions_json";
    private static final String ARG_QUESTION_COUNT = "arg_question_count";
    private static final String ARG_DIFFICULTY = "arg_difficulty";

    private static final long TIMER_DURATION_MS = 15000L;
    private static final long TIMER_INTERVAL_MS = 100L;

    private FragmentGameBinding binding;
    private final Gson gson = new Gson();
    private final Handler navigationHandler = new Handler(Looper.getMainLooper());

    private final List<Question> questions = new ArrayList<>();
    private final List<String> currentOptions = new ArrayList<>();

    private int currentIndex = 0;
    private int currentScore = 0;
    private long currentTimeLeftMs = TIMER_DURATION_MS;
    private boolean isAnsweringAllowed = false;

    private CountDownTimer countDownTimer;
    private TriviaRepository triviaRepository;

    private LinearLayout[] optionButtons;
    private TextView[] letterBadges;
    private TextView[] optionTexts;

    public static GameFragment newInstance(List<Question> questions) {
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        if (questions != null) {
            args.putString(ARG_QUESTIONS_JSON, new Gson().toJson(questions));
        }
        fragment.setArguments(args);
        return fragment;
    }

    public static GameFragment newInstance(String questionsJson) {
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUESTIONS_JSON, questionsJson);
        fragment.setArguments(args);
        return fragment;
    }

    public static GameFragment newInstance(int questionCount, String difficulty) {
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_QUESTION_COUNT, questionCount);
        args.putString(ARG_DIFFICULTY, difficulty);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        triviaRepository = new TriviaRepositoryImpl();

        initViews();
        setupOptionClickListeners();
        loadQuestions();
    }

    private void initViews() {
        optionButtons = new LinearLayout[]{
                binding.btnOption0,
                binding.btnOption1,
                binding.btnOption2,
                binding.btnOption3
        };

        letterBadges = new TextView[]{
                binding.tvLetter0,
                binding.tvLetter1,
                binding.tvLetter2,
                binding.tvLetter3
        };

        optionTexts = new TextView[]{
                binding.tvText0,
                binding.tvText1,
                binding.tvText2,
                binding.tvText3
        };
    }

    private void setupOptionClickListeners() {
        for (int i = 0; i < optionButtons.length; i++) {
            final int optionIndex = i;
            optionButtons[i].setOnClickListener(v -> onOptionClicked(optionIndex));
        }
    }

    private void loadQuestions() {
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_QUESTIONS_JSON)) {
            String json = args.getString(ARG_QUESTIONS_JSON);
            if (json != null && !json.trim().isEmpty()) {
                try {
                    Type listType = new TypeToken<List<Question>>() {}.getType();
                    List<Question> parsed = gson.fromJson(json, listType);
                    if (parsed != null && !parsed.isEmpty()) {
                        questions.clear();
                        questions.addAll(parsed);
                        currentIndex = 0;
                        currentScore = 0;
                        showQuestion();
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }

        int count = 10;
        if (args != null && args.containsKey(ARG_QUESTION_COUNT)) {
            count = args.getInt(ARG_QUESTION_COUNT, 10);
        }

        fetchQuestionsFromRepository(count);
    }

    private void fetchQuestionsFromRepository(int count) {
        triviaRepository.getQuestions(count, new TriviaRepository.Callback<List<Question>>() {
            @Override
            public void onSuccess(List<Question> result) {
                if (!isAdded()) return;
                navigationHandler.post(() -> {
                    if (!isAdded() || binding == null) return;
                    if (result != null && !result.isEmpty()) {
                        questions.clear();
                        questions.addAll(result);
                    } else {
                        questions.clear();
                        questions.addAll(getDemoQuestions());
                    }
                    currentIndex = 0;
                    currentScore = 0;
                    showQuestion();
                });
            }

            @Override
            public void onError(Throwable throwable) {
                if (!isAdded()) return;
                navigationHandler.post(() -> {
                    if (!isAdded() || binding == null) return;
                    questions.clear();
                    questions.addAll(getDemoQuestions());
                    currentIndex = 0;
                    currentScore = 0;
                    showQuestion();
                });
            }
        });
    }

    private void showQuestion() {
        if (!isAdded() || binding == null) return;

        if (currentIndex >= questions.size()) {
            finishGame();
            return;
        }

        Question question = questions.get(currentIndex);

        // Update HUD
        binding.tvRoundCounter.setText("Pregunta " + (currentIndex + 1) + "/" + questions.size());
        binding.tvScore.setText(currentScore + " pts");

        // Update Question Card
        binding.tvCategory.setText(question.getCategory());
        binding.tvDifficulty.setText(question.getDifficulty());
        binding.tvQuestionText.setText(question.getQuestion());

        // Prepare 4 shuffled options
        currentOptions.clear();
        currentOptions.addAll(question.getOptions());
        Collections.shuffle(currentOptions);

        // Reset option buttons appearance
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i].setEnabled(true);
            optionButtons[i].setAlpha(1.0f);
            optionButtons[i].setBackgroundResource(R.drawable.bg_btn_option_idle);
            optionButtons[i].clearAnimation();

            letterBadges[i].setBackgroundResource(R.drawable.bg_letter_badge);
            letterBadges[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.foreground));

            if (i < currentOptions.size()) {
                optionTexts[i].setText(currentOptions.get(i));
                optionButtons[i].setVisibility(View.VISIBLE);
            } else {
                optionButtons[i].setVisibility(View.GONE);
            }
        }

        // Start 15-second countdown timer
        startTimer();
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        isAnsweringAllowed = true;
        currentTimeLeftMs = TIMER_DURATION_MS;

        binding.pbTimer.setMax((int) TIMER_DURATION_MS);
        binding.pbTimer.setProgress((int) TIMER_DURATION_MS);
        binding.tvTimerText.setText("15s");

        updateTimerColor(15);

        countDownTimer = new CountDownTimer(TIMER_DURATION_MS, TIMER_INTERVAL_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isAdded() || binding == null) return;

                currentTimeLeftMs = millisUntilFinished;
                binding.pbTimer.setProgress((int) millisUntilFinished);

                int secondsRemaining = (int) Math.ceil(millisUntilFinished / 1000.0);
                binding.tvTimerText.setText(secondsRemaining + "s");

                updateTimerColor(secondsRemaining);
            }

            @Override
            public void onFinish() {
                if (!isAdded() || binding == null) return;

                currentTimeLeftMs = 0;
                binding.pbTimer.setProgress(0);
                binding.tvTimerText.setText("0s");

                updateTimerColor(0);
                handleTimeout();
            }
        };

        countDownTimer.start();
    }

    private void updateTimerColor(int secondsRemaining) {
        if (!isAdded() || getContext() == null || binding == null) return;

        int colorRes;
        if (secondsRemaining > 5) {
            colorRes = R.color.success;
        } else if (secondsRemaining > 3) {
            colorRes = R.color.warning;
        } else {
            colorRes = R.color.destructive;
        }

        int color = ContextCompat.getColor(requireContext(), colorRes);
        binding.tvTimerText.setTextColor(color);
        binding.pbTimer.setProgressTintList(ColorStateList.valueOf(color));
    }

    private void handleTimeout() {
        if (!isAnsweringAllowed) return;
        isAnsweringAllowed = false;

        disableOptionButtons();

        Question question = questions.get(currentIndex);
        int correctIndex = findOptionIndex(question.getCorrectAnswer());

        if (correctIndex != -1) {
            optionButtons[correctIndex].setBackgroundResource(R.drawable.bg_btn_option_success);
            letterBadges[correctIndex].setBackgroundResource(R.drawable.bg_letter_badge_correct);
        }

        for (int i = 0; i < optionButtons.length; i++) {
            if (i != correctIndex) {
                optionButtons[i].setAlpha(0.45f);
            }
        }

        navigationHandler.postDelayed(this::nextQuestion, 1200);
    }

    private void onOptionClicked(int selectedIndex) {
        if (!isAnsweringAllowed) return;
        isAnsweringAllowed = false;

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        disableOptionButtons();

        Question question = questions.get(currentIndex);
        String selectedAnswer = selectedIndex < currentOptions.size() ? currentOptions.get(selectedIndex) : null;
        boolean isCorrect = selectedAnswer != null && selectedAnswer.equals(question.getCorrectAnswer());

        int correctIndex = findOptionIndex(question.getCorrectAnswer());

        if (isCorrect) {
            optionButtons[selectedIndex].setBackgroundResource(R.drawable.bg_btn_option_success);
            letterBadges[selectedIndex].setBackgroundResource(R.drawable.bg_letter_badge_correct);

            int timeLeftSeconds = (int) Math.max(0, Math.ceil(currentTimeLeftMs / 1000.0));
            int pointsEarned = 100 + (timeLeftSeconds * 10);
            currentScore += pointsEarned;
            binding.tvScore.setText(currentScore + " pts");

            for (int i = 0; i < optionButtons.length; i++) {
                if (i != selectedIndex) {
                    optionButtons[i].setAlpha(0.45f);
                }
            }
        } else {
            optionButtons[selectedIndex].setBackgroundResource(R.drawable.bg_btn_option_error);
            letterBadges[selectedIndex].setBackgroundResource(R.drawable.bg_letter_badge_wrong);

            Animation shakeAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.shake);
            optionButtons[selectedIndex].startAnimation(shakeAnim);

            if (correctIndex != -1) {
                optionButtons[correctIndex].setBackgroundResource(R.drawable.bg_btn_option_success);
                letterBadges[correctIndex].setBackgroundResource(R.drawable.bg_letter_badge_correct);
            }

            for (int i = 0; i < optionButtons.length; i++) {
                if (i != selectedIndex && i != correctIndex) {
                    optionButtons[i].setAlpha(0.45f);
                }
            }
        }

        navigationHandler.postDelayed(this::nextQuestion, 1400);
    }

    private void nextQuestion() {
        if (!isAdded() || binding == null) return;
        currentIndex++;
        showQuestion();
    }

    private void finishGame() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        if (getContext() != null) {
            Toast.makeText(
                    requireContext(),
                    "¡Ronda completada! Puntuación final: " + currentScore + " pts",
                    Toast.LENGTH_SHORT
            ).show();
        }

        navigationHandler.postDelayed(() -> {
            if (isAdded() && getActivity() != null) {
                WaitingRoomFragment waitingRoomFragment = WaitingRoomFragment.newInstance(1, 1);
                com.example.emptyapplication.presentation.navigation.FragmentNavigator.navigateTo(
                        getParentFragmentManager(),
                        waitingRoomFragment
                );
            }
        }, 1200);
    }

    private int findOptionIndex(String answer) {
        if (answer == null) return -1;
        for (int i = 0; i < currentOptions.size(); i++) {
            if (answer.equals(currentOptions.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private void disableOptionButtons() {
        for (LinearLayout button : optionButtons) {
            button.setEnabled(false);
        }
    }

    public static List<Question> getDemoQuestions() {
        List<Question> list = new ArrayList<>();
        list.add(new Question(
                "¿Cuál es la capital de Francia?",
                "Geografía",
                "Fácil",
                Arrays.asList("París", "Londres", "Berlín", "Madrid"),
                "París"
        ));
        list.add(new Question(
                "¿En qué año llegó el ser humano a la Luna?",
                "Historia",
                "Media",
                Arrays.asList("1969", "1965", "1972", "1959"),
                "1969"
        ));
        list.add(new Question(
                "¿Cuál es el elemento químico más abundante en el universo?",
                "Ciencia",
                "Media",
                Arrays.asList("Hidrógeno", "Helio", "Oxígeno", "Carbono"),
                "Hidrógeno"
        ));
        list.add(new Question(
                "¿Quién pintó la Mona Lisa?",
                "Arte",
                "Fácil",
                Arrays.asList("Leonardo da Vinci", "Pablo Picasso", "Vincent van Gogh", "Miguel Ángel"),
                "Leonardo da Vinci"
        ));
        list.add(new Question(
                "¿Cuántos huesos tiene el cuerpo humano adulto?",
                "Ciencia",
                "Difícil",
                Arrays.asList("206", "198", "214", "220"),
                "206"
        ));
        return list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        navigationHandler.removeCallbacksAndMessages(null);
        binding = null;
    }
}
