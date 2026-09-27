package com.example.emptyapplication.presentation.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emptyapplication.R;
import com.example.emptyapplication.databinding.ItemAvatarBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying and selecting meme avatars.
 */
public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder> {

    @FunctionalInterface
    public interface OnAvatarSelectedListener {
        void onAvatarSelected(@NonNull AvatarItemAdapter avatar, int position);
    }

    private final List<AvatarItemAdapter> items = new ArrayList<>();
    @Nullable
    private OnAvatarSelectedListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public AvatarAdapter(@NonNull List<AvatarItemAdapter> items, @Nullable OnAvatarSelectedListener listener) {
        if (items != null) {
            this.items.addAll(items);
        }
        this.listener = listener;
        if (!this.items.isEmpty()) {
            this.selectedPosition = 0;
        }
    }

    public AvatarAdapter(@NonNull List<AvatarItemAdapter> items) {
        this(items, null);
    }

    public void setOnAvatarSelectedListener(@Nullable OnAvatarSelectedListener listener) {
        this.listener = listener;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    @Nullable
    public AvatarItemAdapter getSelectedAvatar() {
        if (selectedPosition >= 0 && selectedPosition < items.size()) {
            return items.get(selectedPosition);
        }
        return null;
    }

    public void setSelectedPosition(int position) {
        if (position < 0 || position >= items.size() || position == selectedPosition) {
            return;
        }
        int previousPosition = selectedPosition;
        selectedPosition = position;
        if (previousPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousPosition);
        }
        notifyItemChanged(selectedPosition);
    }

    public void setItems(@NonNull List<AvatarItemAdapter> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        if (selectedPosition >= items.size()) {
            selectedPosition = items.isEmpty() ? RecyclerView.NO_POSITION : 0;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAvatarBinding binding = ItemAvatarBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AvatarViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AvatarViewHolder holder, int position) {
        AvatarItemAdapter item = items.get(position);
        holder.bind(item, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class AvatarViewHolder extends RecyclerView.ViewHolder {

        private final ItemAvatarBinding binding;

        public AvatarViewHolder(@NonNull ItemAvatarBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION || position == selectedPosition) {
                    return;
                }
                int previousPosition = selectedPosition;
                selectedPosition = position;

                if (previousPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previousPosition);
                }
                notifyItemChanged(selectedPosition);

                if (listener != null) {
                    listener.onAvatarSelected(items.get(position), position);
                }
            });
        }

        public void bind(@NonNull AvatarItemAdapter item, boolean isSelected) {
            binding.ivAvatar.setImageResource(item.getDrawableResId());
            binding.ivAvatar.setSelected(isSelected);

            Context context = itemView.getContext();
            float strokeWidth = context.getResources().getDimension(
                    isSelected ? R.dimen.avatar_stroke_selected : R.dimen.avatar_stroke_unselected
            );
            int strokeColor = ContextCompat.getColor(
                    context,
                    isSelected ? R.color.primary : R.color.border
            );

            binding.ivAvatar.setStrokeWidth(strokeWidth);
            binding.ivAvatar.setStrokeColor(ColorStateList.valueOf(strokeColor));
        }
    }
}
