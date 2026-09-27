package com.example.emptyapplication.presentation.lobby;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.emptyapplication.R;
import com.example.emptyapplication.data.network.NetworkUtils;
import com.example.emptyapplication.databinding.FragmentHostLobbyBinding;
import com.example.emptyapplication.domain.model.Player;
import com.example.emptyapplication.presentation.adapter.LobbyPlayerAdapter;

import com.example.emptyapplication.data.network.SocketMessage;
import com.example.emptyapplication.data.network.TriviaServer;
import com.example.emptyapplication.data.repository.TriviaRepositoryImpl;
import com.example.emptyapplication.domain.model.Question;
import com.example.emptyapplication.domain.repository.TriviaRepository;
import com.example.emptyapplication.presentation.game.GameFragment;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fragment that displays and manages the host lobby screen before starting a trivia match.
 */
public class HostLobbyFragment extends Fragment {

    private static final String ARG_HOST_NAME = "arg_host_name";
    private static final String ARG_AVATAR_RES_ID = "arg_avatar_res_id";

    private FragmentHostLobbyBinding binding;
    private LobbyPlayerAdapter playerAdapter;
    private final List<Player> playerList = new ArrayList<>();

    private int selectedQuestionCount = 10;
    private String selectedDifficulty = "Media";
    private TriviaServer triviaServer;
    private TriviaRepository triviaRepository;
    private boolean isLaunchingGame = false;

    public static HostLobbyFragment newInstance(String hostName, int avatarResId) {
        HostLobbyFragment fragment = new HostLobbyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HOST_NAME, hostName);
        args.putInt(ARG_AVATAR_RES_ID, avatarResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHostLobbyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        triviaRepository = new TriviaRepositoryImpl();

        setupRoomIp();
        setupCopyButton();
        setupQuestionCountChips();
        setupDifficultyChips();
        setupPlayersRecyclerView();
        setupBackButton();
        setupTriviaServer();
        setupStartGameButton();
    }

    private void setupRoomIp() {
        String ipAddress = NetworkUtils.getLocalIpAddress() + ":8888";
        binding.tvRoomIp.setText(ipAddress);
    }

    private void setupCopyButton() {
        binding.btnCopyIp.setOnClickListener(v -> {
            String ipText = binding.tvRoomIp.getText().toString();
            Context context = getContext();
            if (context != null) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("TriviaLAN IP", ipText);
                    clipboard.setPrimaryClip(clip);
                }
            }

            binding.tvCopyText.setText("¡Copiado!");
            binding.ivCopyIcon.setImageResource(R.drawable.check);
            binding.btnCopyIp.postDelayed(() -> {
                if (isAdded() && binding != null) {
                    binding.tvCopyText.setText("Copiar IP");
                    binding.ivCopyIcon.setImageResource(R.drawable.copy);
                }
            }, 2000);
        });
    }

    private void setupQuestionCountChips() {
        TextView[] chips = new TextView[]{
                binding.btnCount5,
                binding.btnCount10,
                binding.btnCount15,
                binding.btnCount20
        };
        int[] counts = new int[]{5, 10, 15, 20};

        for (int i = 0; i < chips.length; i++) {
            final int count = counts[i];
            final TextView chip = chips[i];
            chip.setOnClickListener(v -> {
                selectedQuestionCount = count;
                updateQuestionCountUi(chips, counts);
            });
        }
        updateQuestionCountUi(chips, counts);
    }

    private void updateQuestionCountUi(TextView[] chips, int[] counts) {
        for (int i = 0; i < chips.length; i++) {
            boolean isSelected = counts[i] == selectedQuestionCount;
            chips[i].setBackgroundResource(isSelected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            int textColorRes = isSelected ? R.color.primary_foreground : R.color.muted_foreground;
            chips[i].setTextColor(ContextCompat.getColor(requireContext(), textColorRes));
        }
    }

    private void setupDifficultyChips() {
        TextView[] chips = new TextView[]{
                binding.btnDiffEasy,
                binding.btnDiffMedium,
                binding.btnDiffHard,
                binding.btnDiffMixed
        };
        String[] difficulties = new String[]{"Fácil", "Media", "Difícil", "Mixta"};

        for (int i = 0; i < chips.length; i++) {
            final String diff = difficulties[i];
            final TextView chip = chips[i];
            chip.setOnClickListener(v -> {
                selectedDifficulty = diff;
                updateDifficultyUi(chips, difficulties);
            });
        }
        updateDifficultyUi(chips, difficulties);
    }

    private void updateDifficultyUi(TextView[] chips, String[] difficulties) {
        for (int i = 0; i < chips.length; i++) {
            boolean isSelected = difficulties[i].equalsIgnoreCase(selectedDifficulty);
            chips[i].setBackgroundResource(isSelected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            int textColorRes = isSelected ? R.color.primary_foreground : R.color.muted_foreground;
            chips[i].setTextColor(ContextCompat.getColor(requireContext(), textColorRes));
        }
    }

    private void setupPlayersRecyclerView() {
        binding.rvLobbyPlayers.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        playerAdapter = new LobbyPlayerAdapter();
        binding.rvLobbyPlayers.setAdapter(playerAdapter);

        String hostName = "Host";
        int avatarResId = R.drawable.avatar_tom;
        if (getArguments() != null) {
            hostName = getArguments().getString(ARG_HOST_NAME, "Host");
            avatarResId = getArguments().getInt(ARG_AVATAR_RES_ID, R.drawable.avatar_tom);
            if (avatarResId == 0) {
                avatarResId = R.drawable.avatar_tom;
            }
        }

        playerList.clear();
        Player hostPlayer = new Player(
                UUID.randomUUID().toString(),
                hostName,
                0,
                false,
                avatarResId,
                true
        );
        playerList.add(hostPlayer);
        playerAdapter.setPlayers(playerList);
        updatePlayerCountHeader();
    }

    private void updatePlayerCountHeader() {
        if (binding != null) {
            binding.tvPlayerCountTitle.setText(
                    getString(R.string.lobby_players_format, playerList.size())
            );
        }
    }

    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().finish();
            }
        });
    }

    private void setupTriviaServer() {
        if (triviaServer != null) {
            triviaServer.stop();
        }
        triviaServer = new TriviaServer();
        triviaServer.start(8888, new TriviaServer.ServerListener() {
            @Override
            public void onClientConnected(String clientId) {
            }

            @Override
            public void onMessageReceived(String clientId, SocketMessage message) {
                if (message != null && SocketMessage.TYPE_JOIN.equals(message.getType())) {
                    try {
                        Player player = new Gson().fromJson(message.getPayload(), Player.class);
                        if (player != null && getActivity() != null) {
                            requireActivity().runOnUiThread(() -> {
                                boolean exists = false;
                                for (Player p : playerList) {
                                    if (p.getId().equals(player.getId())) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists) {
                                    playerList.add(player);
                                    if (playerAdapter != null) {
                                        playerAdapter.setPlayers(playerList);
                                    }
                                    updatePlayerCountHeader();
                                }
                                SocketMessage updateMessage = new SocketMessage(
                                        SocketMessage.TYPE_LOBBY_UPDATE,
                                        new Gson().toJson(playerList)
                                );
                                if (triviaServer != null) {
                                    triviaServer.broadcast(updateMessage);
                                }
                            });
                        }
                    } catch (Exception ignored) {}
                }
            }

            @Override
            public void onClientDisconnected(String clientId) {
            }

            @Override
            public void onError(Throwable throwable) {
            }
        });
    }

    private void setupStartGameButton() {
        binding.btnStartGame.setOnClickListener(v -> {
            binding.btnStartGame.setEnabled(false);
            binding.btnStartGame.setText("Cargando preguntas...");
            Toast.makeText(
                    requireContext(),
                    "Iniciando partida (" + selectedQuestionCount + " preguntas, " + selectedDifficulty + ")...",
                    Toast.LENGTH_SHORT
            ).show();

            triviaRepository.getQuestions(selectedQuestionCount, new TriviaRepository.Callback<List<Question>>() {
                @Override
                public void onSuccess(List<Question> result) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        if (!isAdded()) return;
                        List<Question> gameQuestions = (result != null && !result.isEmpty())
                                ? result
                                : GameFragment.getDemoQuestions();
                        startGameWithQuestions(gameQuestions);
                    });
                }

                @Override
                public void onError(Throwable throwable) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        if (!isAdded()) return;
                        startGameWithQuestions(GameFragment.getDemoQuestions());
                    });
                }
            });
        });
    }

    private void startGameWithQuestions(List<Question> questions) {
        isLaunchingGame = true;
        String questionsJson = new Gson().toJson(questions);
        if (triviaServer != null) {
            triviaServer.broadcast(new SocketMessage(SocketMessage.TYPE_START_GAME, questionsJson));
        }

        GameFragment gameFragment = GameFragment.newInstance(questions);
        com.example.emptyapplication.presentation.navigation.FragmentNavigator.navigateTo(
                getParentFragmentManager(),
                gameFragment
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (triviaServer != null && !isLaunchingGame) {
            triviaServer.stop();
            triviaServer = null;
        }
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (triviaServer != null) {
            triviaServer.stop();
            triviaServer = null;
        }
    }
}
