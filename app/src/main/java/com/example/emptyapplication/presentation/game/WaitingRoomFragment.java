package com.example.emptyapplication.presentation.game;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.emptyapplication.R;
import com.example.emptyapplication.databinding.FragmentWaitingRoomBinding;

import java.util.Locale;

public class WaitingRoomFragment extends Fragment {

    private static final String ARG_FINISHED_COUNT = "arg_finished_count";
    private static final String ARG_TOTAL_PLAYERS = "arg_total_players";

    private FragmentWaitingRoomBinding binding;
    private int finishedCount = 1;
    private int totalPlayers = 1;
    private Animation floatAnimation;

    public interface OnAllPlayersFinishedListener {
        void onAllPlayersFinished();
    }

    private OnAllPlayersFinishedListener listener;

    public static WaitingRoomFragment newInstance(int finished, int total) {
        WaitingRoomFragment fragment = new WaitingRoomFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FINISHED_COUNT, finished);
        args.putInt(ARG_TOTAL_PLAYERS, total);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnAllPlayersFinishedListener(OnAllPlayersFinishedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            finishedCount = getArguments().getInt(ARG_FINISHED_COUNT, 1);
            totalPlayers = getArguments().getInt(ARG_TOTAL_PLAYERS, 1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWaitingRoomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startFloatingAnimation();
        updateProgress(finishedCount, totalPlayers, false);
    }

    private void startFloatingAnimation() {
        if (binding == null || getContext() == null) return;
        floatAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.float_slow);
        binding.ivHourglass.startAnimation(floatAnimation);
    }

    public void updateProgress(int finished, int total) {
        updateProgress(finished, total, true);
    }

    public void updateProgress(int finished, int total, boolean animate) {
        this.finishedCount = finished;
        this.totalPlayers = Math.max(1, total);

        if (binding == null) return;

        int targetPercentage = Math.min(100, Math.max(0, (int) Math.round(((double) finishedCount / (double) totalPlayers) * 100)));

        if (animate) {
            ObjectAnimator progressAnimator = ObjectAnimator.ofInt(
                    binding.pbPlayers,
                    "progress",
                    binding.pbPlayers.getProgress(),
                    targetPercentage
            );
            progressAnimator.setDuration(500);
            progressAnimator.setInterpolator(new DecelerateInterpolator());
            progressAnimator.start();
        } else {
            binding.pbPlayers.setProgress(targetPercentage);
        }

        binding.tvPlayersProgress.setText(
                String.format(Locale.getDefault(), "%d de %d jugadores terminaron", finishedCount, totalPlayers)
        );

        if (finishedCount >= totalPlayers) {
            if (listener != null) {
                listener.onAllPlayersFinished();
            } else {
                binding.getRoot().postDelayed(() -> {
                    if (isAdded() && getActivity() != null) {
                        PodiumFragment podiumFragment = new PodiumFragment();
                        com.example.emptyapplication.presentation.navigation.FragmentNavigator.navigateTo(
                                getParentFragmentManager(),
                                podiumFragment
                        );
                    }
                }, 1500);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (floatAnimation != null) {
            floatAnimation.cancel();
            floatAnimation = null;
        }
        binding = null;
    }
}
