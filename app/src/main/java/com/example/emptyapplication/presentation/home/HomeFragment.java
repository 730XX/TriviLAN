package com.example.emptyapplication.presentation.home;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.emptyapplication.R;
import com.example.emptyapplication.databinding.FragmentHomeBinding;
import com.example.emptyapplication.presentation.adapter.AvatarAdapter;
import com.example.emptyapplication.presentation.adapter.AvatarItemAdapter;
import com.example.emptyapplication.presentation.lobby.ClientLobbyFragment;
import com.example.emptyapplication.presentation.lobby.HostLobbyFragment;
import com.example.emptyapplication.presentation.navigation.FragmentNavigator;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private AvatarAdapter avatarAdapter;
    private AvatarItemAdapter selectedAvatar;
    private final List<ObjectAnimator> waveAnimators = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupWavyTitle();
        setupAvatarRecyclerView();
        setupActionButtons();
    }

    private void setupWavyTitle() {
        if (binding == null || getContext() == null) return;

        String title = "TriviaLAN";
        binding.llTitleContainer.removeAllViews();
        waveAnimators.clear();

        int startColor = Color.parseColor("#9B8AFB"); // Lavender violet
        int endColor = Color.parseColor("#63B9FF");   // Bright Cyan
        ArgbEvaluator evaluator = new ArgbEvaluator();
        Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.outfit_bold);

        for (int i = 0; i < title.length(); i++) {
            TextView tvChar = new TextView(requireContext());
            tvChar.setText(String.valueOf(title.charAt(i)));
            tvChar.setTextAppearance(R.style.TextAppearance_TriviaLAN_DisplayLarge);
            if (typeface != null) {
                tvChar.setTypeface(typeface);
            }

            float fraction = (float) i / (title.length() - 1);
            int color = (int) evaluator.evaluate(fraction, startColor, endColor);
            tvChar.setTextColor(color);

            binding.llTitleContainer.addView(tvChar);

            ObjectAnimator animator = ObjectAnimator.ofFloat(tvChar, "translationY", 0f, -12f, 0f);
            animator.setDuration(1600);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setStartDelay(i * 45L);
            animator.start();
            waveAnimators.add(animator);
        }
    }

    private void setupAvatarRecyclerView() {
        if (binding == null || getContext() == null) return;

        binding.rvAvatars.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        avatarAdapter = new AvatarAdapter(AvatarItemAdapter.getDefaultAvatars(), (item, position) -> {
            selectedAvatar = item;
        });
        binding.rvAvatars.setAdapter(avatarAdapter);
    }

    private void setupActionButtons() {
        if (binding == null) return;

        binding.btnCreateRoom.setOnClickListener(v -> {
            String name = getPlayerName();
            if (name.isEmpty()) {
                binding.tilPlayerName.setError("¡Ingresa tu nombre!");
                return;
            }
            binding.tilPlayerName.setError(null);

            AvatarItemAdapter avatar = selectedAvatar != null ? selectedAvatar : avatarAdapter.getSelectedAvatar();
            int avatarResId = avatar != null ? avatar.getDrawableResId() : R.drawable.avatar_tom;

            HostLobbyFragment fragment = HostLobbyFragment.newInstance(name, avatarResId);
            FragmentNavigator.navigateTo(getParentFragmentManager(), fragment);
        });

        binding.btnJoinRoom.setOnClickListener(v -> {
            String name = getPlayerName();
            if (name.isEmpty()) {
                binding.tilPlayerName.setError("¡Ingresa tu nombre!");
                return;
            }
            binding.tilPlayerName.setError(null);

            AvatarItemAdapter avatar = selectedAvatar != null ? selectedAvatar : avatarAdapter.getSelectedAvatar();
            int avatarResId = avatar != null ? avatar.getDrawableResId() : R.drawable.avatar_tom;

            ClientLobbyFragment fragment = ClientLobbyFragment.newInstance(name, avatarResId);
            FragmentNavigator.navigateTo(getParentFragmentManager(), fragment);
        });
    }

    private String getPlayerName() {
        if (binding == null || binding.etPlayerName.getText() == null) {
            return "";
        }
        return binding.etPlayerName.getText().toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (ObjectAnimator animator : waveAnimators) {
            animator.cancel();
        }
        waveAnimators.clear();
        binding = null;
    }
}
