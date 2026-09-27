package com.example.emptyapplication.presentation.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.emptyapplication.R;
import com.example.emptyapplication.databinding.FragmentPodiumBinding;
import com.example.emptyapplication.domain.model.Player;
import com.example.emptyapplication.presentation.adapter.PodiumRestAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PodiumFragment extends Fragment {

    private static final String ARG_PLAYERS_JSON = "arg_players_json";
    private static final String ARG_YOU_JSON = "arg_you_json";

    private FragmentPodiumBinding binding;
    private final Gson gson = new Gson();
    private final NumberFormat numberFormat = NumberFormat.getInstance(new Locale("es", "ES"));

    private final List<Player> players = new ArrayList<>();
    private Player youPlayer;
    private PodiumRestAdapter restAdapter;

    public interface PodiumNavigationListener {
        void onRematch();
        void onHome();
    }

    private PodiumNavigationListener listener;

    public static PodiumFragment newInstance(List<Player> players, Player you) {
        PodiumFragment fragment = new PodiumFragment();
        Bundle args = new Bundle();
        if (players != null) {
            args.putString(ARG_PLAYERS_JSON, new Gson().toJson(players));
        }
        if (you != null) {
            args.putString(ARG_YOU_JSON, new Gson().toJson(you));
        }
        fragment.setArguments(args);
        return fragment;
    }

    public static PodiumFragment newInstance(String playersJson, String youJson) {
        PodiumFragment fragment = new PodiumFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLAYERS_JSON, playersJson);
        args.putString(ARG_YOU_JSON, youJson);
        fragment.setArguments(args);
        return fragment;
    }

    public void setPodiumNavigationListener(PodiumNavigationListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String playersJson = getArguments().getString(ARG_PLAYERS_JSON);
            if (playersJson != null && !playersJson.isEmpty()) {
                Type listType = new TypeToken<List<Player>>() {}.getType();
                List<Player> parsed = gson.fromJson(playersJson, listType);
                if (parsed != null) {
                    players.addAll(parsed);
                }
            }

            String youJson = getArguments().getString(ARG_YOU_JSON);
            if (youJson != null && !youJson.isEmpty()) {
                youPlayer = gson.fromJson(youJson, Player.class);
            }
        }

        if (players.isEmpty()) {
            setupDemoPlayers();
        }
    }

    private void setupDemoPlayers() {
        players.add(new Player("1", "Tú", 2450, true, R.drawable.avatar_tom, true, 90, 2.8));
        players.add(new Player("2", "Maru", 1820, true, R.drawable.avatar_saul, false, 80, 3.4));
        players.add(new Player("3", "Santi", 1250, true, R.drawable.avatar_doakes, false, 70, 4.1));
        players.add(new Player("4", "Nico", 940, true, R.drawable.avatar_moai, false, 60, 4.8));
        if (youPlayer == null) {
            youPlayer = players.get(0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPodiumBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        displayPodiumAndStats();
        setupActionButtons();
    }

    private void setupRecyclerView() {
        binding.rvRestPlayers.setLayoutManager(new LinearLayoutManager(requireContext()));
        restAdapter = new PodiumRestAdapter();
        binding.rvRestPlayers.setAdapter(restAdapter);
    }

    private void displayPodiumAndStats() {
        List<Player> ranked = new ArrayList<>(players);
        Collections.sort(ranked, (p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));

        // Animate podium pillars
        Animation popInAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in);

        // 1st place
        if (!ranked.isEmpty()) {
            Player first = ranked.get(0);
            binding.tvNameFirst.setText(first.getName());
            binding.tvScoreFirst.setText(numberFormat.format(first.getScore()) + " pts");
            binding.ivAvatarFirst.setImageResource(first.getAvatarResId() != 0 ? first.getAvatarResId() : R.drawable.avatar_tom);
            binding.pillarFirst.startAnimation(popInAnim);
            binding.stepFirst.setVisibility(View.VISIBLE);
        } else {
            binding.stepFirst.setVisibility(View.INVISIBLE);
        }

        // 2nd place
        if (ranked.size() > 1) {
            Player second = ranked.get(1);
            binding.tvNameSecond.setText(second.getName());
            binding.tvScoreSecond.setText(numberFormat.format(second.getScore()) + " pts");
            binding.ivAvatarSecond.setImageResource(second.getAvatarResId() != 0 ? second.getAvatarResId() : R.drawable.avatar_tom);
            binding.pillarSecond.startAnimation(popInAnim);
            binding.stepSecond.setVisibility(View.VISIBLE);
        } else {
            binding.stepSecond.setVisibility(View.INVISIBLE);
        }

        // 3rd place
        if (ranked.size() > 2) {
            Player third = ranked.get(2);
            binding.tvNameThird.setText(third.getName());
            binding.tvScoreThird.setText(numberFormat.format(third.getScore()) + " pts");
            binding.ivAvatarThird.setImageResource(third.getAvatarResId() != 0 ? third.getAvatarResId() : R.drawable.avatar_tom);
            binding.pillarThird.startAnimation(popInAnim);
            binding.stepThird.setVisibility(View.VISIBLE);
        } else {
            binding.stepThird.setVisibility(View.INVISIBLE);
        }

        // Rest of players (4th onwards)
        if (ranked.size() > 3) {
            List<Player> rest = ranked.subList(3, ranked.size());
            restAdapter.setPlayers(rest);
            binding.cardRestPlayers.setVisibility(View.VISIBLE);
        } else {
            binding.cardRestPlayers.setVisibility(View.GONE);
        }

        // Stats card
        Player you = youPlayer != null ? youPlayer : (!ranked.isEmpty() ? ranked.get(0) : null);
        if (you != null) {
            binding.tvAccuracyValue.setText(you.getAccuracy() + "%");
            binding.tvAvgTimeValue.setText(String.format(Locale.getDefault(), "%.1fs", you.getAvgTime()));
        }
    }

    private void setupActionButtons() {
        binding.btnRematch.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRematch();
            } else {
                // Default rematch behavior: pop back to lobby/game
                if (getParentFragmentManager().getBackStackEntryCount() > 1) {
                    getParentFragmentManager().popBackStack();
                } else {
                    navigateToHome();
                }
            }
        });

        binding.btnHome.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHome();
            } else {
                navigateToHome();
            }
        });
    }

    private void navigateToHome() {
        if (isAdded() && getActivity() != null) {
            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
