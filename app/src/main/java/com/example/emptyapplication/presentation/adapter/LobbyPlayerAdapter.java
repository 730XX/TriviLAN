package com.example.emptyapplication.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emptyapplication.R;
import com.example.emptyapplication.databinding.ItemPlayerLobbyBinding;
import com.example.emptyapplication.domain.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying connected players in the host lobby.
 */
public class LobbyPlayerAdapter extends RecyclerView.Adapter<LobbyPlayerAdapter.LobbyPlayerViewHolder> {

    private final List<Player> players = new ArrayList<>();

    public LobbyPlayerAdapter() {
    }

    public LobbyPlayerAdapter(List<Player> initialPlayers) {
        if (initialPlayers != null) {
            this.players.addAll(initialPlayers);
        }
    }

    public void setPlayers(@NonNull List<Player> newPlayers) {
        players.clear();
        if (newPlayers != null) {
            players.addAll(newPlayers);
        }
        notifyDataSetChanged();
    }

    public void addPlayer(@NonNull Player player) {
        players.add(player);
        notifyItemInserted(players.size() - 1);
    }

    public void removePlayer(@NonNull String playerId) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId().equals(playerId)) {
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
    public LobbyPlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlayerLobbyBinding binding = ItemPlayerLobbyBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new LobbyPlayerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LobbyPlayerViewHolder holder, int position) {
        Player player = players.get(position);
        holder.bind(player);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public static class LobbyPlayerViewHolder extends RecyclerView.ViewHolder {

        private final ItemPlayerLobbyBinding binding;

        public LobbyPlayerViewHolder(@NonNull ItemPlayerLobbyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(@NonNull Player player) {
            binding.tvPlayerName.setText(player.getName());

            int avatarRes = player.getAvatarResId();
            if (avatarRes != 0) {
                binding.ivPlayerAvatar.setImageResource(avatarRes);
            } else {
                binding.ivPlayerAvatar.setImageResource(R.drawable.avatar_tom);
            }

            binding.tvHostBadge.setVisibility(player.isHost() ? View.VISIBLE : View.GONE);
        }
    }
}
