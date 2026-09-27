package com.example.emptyapplication.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emptyapplication.R;
import com.example.emptyapplication.databinding.ItemPlayerClientLobbyBinding;
import com.example.emptyapplication.domain.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RecyclerView adapter for displaying players connected to the room from the client perspective.
 */
public class ClientLobbyPlayerAdapter extends RecyclerView.Adapter<ClientLobbyPlayerAdapter.ClientPlayerViewHolder> {

    private final List<Player> players = new ArrayList<>();
    private String currentPlayerName;
    private String currentPlayerId;

    public ClientLobbyPlayerAdapter() {
    }

    public ClientLobbyPlayerAdapter(String currentPlayerName) {
        this.currentPlayerName = currentPlayerName;
    }

    public void setCurrentPlayerName(String currentPlayerName) {
        this.currentPlayerName = currentPlayerName;
        notifyDataSetChanged();
    }

    public void setCurrentPlayerId(String currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
        notifyDataSetChanged();
    }

    public void setPlayers(List<Player> newPlayers) {
        players.clear();
        if (newPlayers != null) {
            players.addAll(newPlayers);
        }
        notifyDataSetChanged();
    }

    public void addPlayer(Player player) {
        if (player != null) {
            players.add(player);
            notifyItemInserted(players.size() - 1);
        }
    }

    public void removePlayer(String playerId) {
        for (int i = 0; i < players.size(); i++) {
            if (Objects.equals(players.get(i).getId(), playerId)) {
                players.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    @NonNull
    @Override
    public ClientPlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlayerClientLobbyBinding binding = ItemPlayerClientLobbyBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ClientPlayerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientPlayerViewHolder holder, int position) {
        holder.bind(players.get(position), currentPlayerName, currentPlayerId);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public static class ClientPlayerViewHolder extends RecyclerView.ViewHolder {

        private final ItemPlayerClientLobbyBinding binding;

        public ClientPlayerViewHolder(@NonNull ItemPlayerClientLobbyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Player player, String currentName, String currentId) {
            binding.tvName.setText(player.getName());

            int avatarRes = player.getAvatarResId();
            if (avatarRes != 0) {
                binding.ivAvatar.setImageResource(avatarRes);
            } else {
                binding.ivAvatar.setImageResource(R.drawable.avatar_tom);
            }

            binding.tvHostBadge.setVisibility(player.isHost() ? View.VISIBLE : View.GONE);

            boolean isCurrentPlayer = (currentId != null && currentId.equals(player.getId()))
                    || (currentName != null && currentName.equalsIgnoreCase(player.getName()));
            binding.tvYouBadge.setVisibility(isCurrentPlayer ? View.VISIBLE : View.GONE);
        }
    }
}
