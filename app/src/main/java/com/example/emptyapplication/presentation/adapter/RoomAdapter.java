package com.example.emptyapplication.presentation.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emptyapplication.databinding.ItemRoomRowBinding;
import com.example.emptyapplication.domain.model.Room;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying discovered LAN rooms.
 */
public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    public interface OnRoomClickListener {
        void onRoomClick(Room room);
    }

    private final List<Room> rooms = new ArrayList<>();
    private final OnRoomClickListener listener;

    public RoomAdapter(OnRoomClickListener listener) {
        this.listener = listener;
    }

    public RoomAdapter(List<Room> initialRooms, OnRoomClickListener listener) {
        this.listener = listener;
        if (initialRooms != null) {
            this.rooms.addAll(initialRooms);
        }
    }

    public void setRooms(List<Room> newRooms) {
        rooms.clear();
        if (newRooms != null) {
            rooms.addAll(newRooms);
        }
        notifyDataSetChanged();
    }

    public void addRoom(Room room) {
        if (room != null) {
            rooms.add(room);
            notifyItemInserted(rooms.size() - 1);
        }
    }

    public void clearRooms() {
        int size = rooms.size();
        rooms.clear();
        notifyItemRangeRemoved(0, size);
    }

    public List<Room> getRooms() {
        return new ArrayList<>(rooms);
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRoomRowBinding binding = ItemRoomRowBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new RoomViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        holder.bind(rooms.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {

        private final ItemRoomRowBinding binding;

        public RoomViewHolder(@NonNull ItemRoomRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Room room, OnRoomClickListener listener) {
            binding.tvRoomHost.setText(room.getHost());
            String details = room.getIp() + ":" + room.getPort() + " · " + room.getPing() + " ms";
            binding.tvRoomDetails.setText(details);
            String players = room.getPlayers() + "/" + room.getMaxPlayers();
            binding.tvRoomPlayers.setText(players);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRoomClick(room);
                }
            });
        }
    }
}
