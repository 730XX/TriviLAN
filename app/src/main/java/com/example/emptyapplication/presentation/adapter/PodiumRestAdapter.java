package com.example.emptyapplication.presentation.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emptyapplication.R;
import com.example.emptyapplication.databinding.ItemPodiumRestBinding;
import com.example.emptyapplication.domain.model.Player;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PodiumRestAdapter extends RecyclerView.Adapter<PodiumRestAdapter.ViewHolder> {

    private final List<Player> players = new ArrayList<>();
    private final NumberFormat numberFormat = NumberFormat.getInstance(new Locale("es", "ES"));

    public void setPlayers(List<Player> newPlayers) {
        players.clear();
        if (newPlayers != null) {
            players.addAll(newPlayers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPodiumRestBinding binding = ItemPodiumRestBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player player = players.get(position);
        int rank = position + 4; // Starts from 4th place
        holder.binding.tvPosition.setText(String.valueOf(rank));
        holder.binding.tvName.setText(player.getName());
        holder.binding.tvScore.setText(numberFormat.format(player.getScore()) + " pts");

        int avatarRes = player.getAvatarResId() != 0 ? player.getAvatarResId() : R.drawable.avatar_tom;
        holder.binding.ivAvatar.setImageResource(avatarRes);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemPodiumRestBinding binding;

        ViewHolder(ItemPodiumRestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
