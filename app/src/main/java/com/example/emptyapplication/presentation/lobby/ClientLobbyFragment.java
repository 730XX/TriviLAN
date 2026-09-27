package com.example.emptyapplication.presentation.lobby;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.emptyapplication.R;
import com.example.emptyapplication.data.network.NetworkUtils;
import com.example.emptyapplication.data.network.SocketMessage;
import com.example.emptyapplication.data.network.TriviaClient;
import com.example.emptyapplication.databinding.FragmentClientLobbyBinding;
import com.example.emptyapplication.domain.model.Player;
import com.example.emptyapplication.domain.model.Room;
import com.example.emptyapplication.presentation.adapter.ClientLobbyPlayerAdapter;
import com.example.emptyapplication.presentation.adapter.RoomAdapter;
import com.example.emptyapplication.presentation.game.GameFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Fragment that allows players to discover and join trivia rooms as a client.
 */
public class ClientLobbyFragment extends Fragment implements RoomAdapter.OnRoomClickListener {

    private static final String ARG_PLAYER_NAME = "arg_player_name";
    private static final String ARG_AVATAR_RES_ID = "arg_avatar_res_id";

    private FragmentClientLobbyBinding binding;
    private RoomAdapter roomAdapter;
    private ClientLobbyPlayerAdapter playerAdapter;

    private TriviaClient triviaClient;
    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String playerName = "Jugador";
    private int avatarResId = R.drawable.avatar_tom;
    private String playerId;

    private final List<Player> connectedPlayers = new ArrayList<>();
    private ObjectAnimator pulseAnimator;
    private boolean isLaunchingGame = false;

    public static ClientLobbyFragment newInstance(String playerName, int avatarResId) {
        ClientLobbyFragment fragment = new ClientLobbyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLAYER_NAME, playerName);
        args.putInt(ARG_AVATAR_RES_ID, avatarResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playerId = UUID.randomUUID().toString();
        if (getArguments() != null) {
            playerName = getArguments().getString(ARG_PLAYER_NAME, "Jugador");
            avatarResId = getArguments().getInt(ARG_AVATAR_RES_ID, R.drawable.avatar_tom);
            if (avatarResId == 0) {
                avatarResId = R.drawable.avatar_tom;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentClientLobbyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupBackButton();
        setupRoomsList();
        setupConnectedPlayersList();
        setupScanButton();
        setupManualConnectButton();
    }

    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> handleBackNavigation());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding != null && binding.layoutConnected.getVisibility() == View.VISIBLE) {
                    disconnectAndShowRooms();
                } else {
                    setEnabled(false);
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void handleBackNavigation() {
        if (binding != null && binding.layoutConnected.getVisibility() == View.VISIBLE) {
            disconnectAndShowRooms();
        } else {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().finish();
            }
        }
    }

    private void setupRoomsList() {
        roomAdapter = new RoomAdapter(this);
        binding.rvRooms.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRooms.setAdapter(roomAdapter);
    }

    private void setupConnectedPlayersList() {
        playerAdapter = new ClientLobbyPlayerAdapter(playerName);
        playerAdapter.setCurrentPlayerId(playerId);
        binding.rvConnectedPlayers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvConnectedPlayers.setAdapter(playerAdapter);
    }

    private void setupScanButton() {
        binding.btnScanRooms.setOnClickListener(v -> scanRooms());
    }

    private void scanRooms() {
        binding.btnScanRooms.setEnabled(false);
        binding.btnScanRooms.setText("Escaneando red...");
        binding.layoutScanningIndicator.setVisibility(View.VISIBLE);

        mainHandler.postDelayed(() -> {
            if (!isAdded() || binding == null) {
                return;
            }

            List<Room> simulatedRooms = new ArrayList<>();
            String localIp = NetworkUtils.getLocalIpAddress();
            simulatedRooms.add(new Room("room-1", "Sala de Tom", localIp, 8888, 1, 8, 12));
            simulatedRooms.add(new Room("room-2", "Duelo Trivia Pro", "192.168.1.88", 8888, 3, 6, 24));
            simulatedRooms.add(new Room("room-3", "Amigos del Barrio", "192.168.1.105", 8888, 4, 8, 48));

            roomAdapter.setRooms(simulatedRooms);

            binding.btnScanRooms.setEnabled(true);
            binding.btnScanRooms.setText("Escanear salas");
            binding.layoutScanningIndicator.setVisibility(View.GONE);
        }, 1200);
    }

    private void setupManualConnectButton() {
        binding.btnManualConnect.setOnClickListener(v -> {
            String ip = binding.etHostIp.getText() != null ? binding.etHostIp.getText().toString().trim() : "";
            String portStr = binding.etHostPort.getText() != null ? binding.etHostPort.getText().toString().trim() : "";

            if (ip.isEmpty()) {
                binding.tilHostIp.setError("Ingresa la IP del host");
                return;
            }
            binding.tilHostIp.setError(null);

            int port = 8888;
            if (!portStr.isEmpty()) {
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException e) {
                    binding.tilHostPort.setError("Puerto inválido");
                    return;
                }
            }
            binding.tilHostPort.setError(null);

            connectToHost(ip, port, null);
        });
    }

    @Override
    public void onRoomClick(Room room) {
        if (room == null) return;
        binding.etHostIp.setText(room.getIp());
        binding.etHostPort.setText(String.valueOf(room.getPort()));
        connectToHost(room.getIp(), room.getPort(), room.getHost());
    }

    private void connectToHost(String hostIp, int port, @Nullable String hostName) {
        if (triviaClient != null) {
            triviaClient.disconnect();
        }
        triviaClient = new TriviaClient();

        binding.btnManualConnect.setEnabled(false);
        Toast.makeText(requireContext(), "Conectando a " + hostIp + ":" + port + "...", Toast.LENGTH_SHORT).show();

        triviaClient.connect(hostIp, port, new TriviaClient.ClientListener() {
            @Override
            public void onConnected() {
                mainHandler.post(() -> {
                    if (!isAdded() || binding == null) return;

                    binding.btnManualConnect.setEnabled(true);
                    binding.layoutDisconnected.setVisibility(View.GONE);
                    binding.layoutConnected.setVisibility(View.VISIBLE);

                    startPulseAnimation();

                    connectedPlayers.clear();
                    if (hostName != null && !hostName.isEmpty()) {
                        Player hostPlayer = new Player(UUID.randomUUID().toString(), hostName, 0, false, R.drawable.avatar_tom, true);
                        connectedPlayers.add(hostPlayer);
                    }
                    Player myPlayer = new Player(playerId, playerName, 0, false, avatarResId, false);
                    connectedPlayers.add(myPlayer);

                    playerAdapter.setPlayers(connectedPlayers);
                    updateConnectedPlayerCount();

                    // Send JOIN message to host
                    SocketMessage joinMessage = new SocketMessage(SocketMessage.TYPE_JOIN, gson.toJson(myPlayer));
                    triviaClient.sendMessage(joinMessage);
                });
            }

            @Override
            public void onMessageReceived(SocketMessage message) {
                mainHandler.post(() -> {
                    if (!isAdded() || binding == null || message == null) return;

                    if (SocketMessage.TYPE_LOBBY_UPDATE.equals(message.getType())) {
                        try {
                            Type listType = new TypeToken<List<Player>>() {}.getType();
                            List<Player> updatedPlayers = gson.fromJson(message.getPayload(), listType);
                            if (updatedPlayers != null && !updatedPlayers.isEmpty()) {
                                connectedPlayers.clear();
                                connectedPlayers.addAll(updatedPlayers);
                                playerAdapter.setPlayers(connectedPlayers);
                                updateConnectedPlayerCount();
                            }
                        } catch (Exception ignored) {}
                    } else if (SocketMessage.TYPE_START_GAME.equals(message.getType())) {
                        Toast.makeText(requireContext(), "¡El host ha iniciado la partida!", Toast.LENGTH_SHORT).show();
                        isLaunchingGame = true;
                        String payload = message.getPayload();
                        GameFragment gameFragment;
                        if (payload != null && !payload.trim().isEmpty()) {
                            gameFragment = GameFragment.newInstance(payload);
                        } else {
                            gameFragment = GameFragment.newInstance(10, "Media");
                        }
                        com.example.emptyapplication.presentation.navigation.FragmentNavigator.navigateTo(
                                getParentFragmentManager(),
                                gameFragment
                        );
                    }
                });
            }

            @Override
            public void onDisconnected() {
                mainHandler.post(() -> {
                    if (!isAdded() || binding == null) return;
                    Toast.makeText(requireContext(), "Desconectado de la sala", Toast.LENGTH_SHORT).show();
                    disconnectAndShowRooms();
                });
            }

            @Override
            public void onError(Throwable throwable) {
                mainHandler.post(() -> {
                    if (!isAdded() || binding == null) return;
                    binding.btnManualConnect.setEnabled(true);
                    String errorMsg = throwable.getMessage() != null ? throwable.getMessage() : "Error de red";
                    Toast.makeText(requireContext(), "Error al conectar: " + errorMsg, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateConnectedPlayerCount() {
        if (binding != null) {
            String title = String.format(Locale.getDefault(), "JUGADORES EN LA SALA (%d)", connectedPlayers.size());
            binding.tvConnectedPlayersTitle.setText(title);
        }
    }

    private void startPulseAnimation() {
        stopPulseAnimation();
        if (binding == null) return;

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.3f, 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.3f, 1.0f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.35f, 0.9f, 0.35f);

        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(binding.viewPulseCircle, scaleX, scaleY, alpha);
        pulseAnimator.setDuration(1600);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.RESTART);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.start();
    }

    private void stopPulseAnimation() {
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
    }

    private void disconnectAndShowRooms() {
        if (triviaClient != null) {
            triviaClient.disconnect();
            triviaClient = null;
        }
        stopPulseAnimation();
        if (binding != null) {
            binding.layoutConnected.setVisibility(View.GONE);
            binding.layoutDisconnected.setVisibility(View.VISIBLE);
            binding.btnManualConnect.setEnabled(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopPulseAnimation();
        if (triviaClient != null && !isLaunchingGame) {
            triviaClient.disconnect();
            triviaClient = null;
        }
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (triviaClient != null) {
            triviaClient.disconnect();
            triviaClient = null;
        }
    }
}
