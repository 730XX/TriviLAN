package com.example.emptyapplication.presentation.adapter;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.example.emptyapplication.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Model representing an avatar option in the meme avatar selector.
 */
public class AvatarItemAdapter {

    private final String id;
    @DrawableRes
    private final int drawableResId;

    public AvatarItemAdapter(@NonNull String id, @DrawableRes int drawableResId) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.drawableResId = drawableResId;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @DrawableRes
    public int getDrawableResId() {
        return drawableResId;
    }

    /**
     * Provides default fallback meme avatars including ic_launcher_round.
     */
    @NonNull
    public static List<AvatarItemAdapter> getDefaultAvatars() {
        List<AvatarItemAdapter> avatars = new ArrayList<>();
        avatars.add(new AvatarItemAdapter("avatar_tom", R.drawable.avatar_tom));
        avatars.add(new AvatarItemAdapter("avatar_saul", R.drawable.avatar_saul));
        avatars.add(new AvatarItemAdapter("avatar_moai", R.drawable.avatar_moai));
        avatars.add(new AvatarItemAdapter("avatar_daokes", R.drawable.avatar_doakes));
        avatars.add(new AvatarItemAdapter("avatar_doge", R.drawable.ic_avatar_doge));
        avatars.add(new AvatarItemAdapter("avatar_cat", R.drawable.ic_avatar_cat));
        avatars.add(new AvatarItemAdapter("avatar_frog", R.drawable.ic_avatar_frog));
        avatars.add(new AvatarItemAdapter("avatar_cool", R.drawable.ic_avatar_cool));


        return Collections.unmodifiableList(avatars);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvatarItemAdapter that = (AvatarItemAdapter) o;
        return drawableResId == that.drawableResId && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, drawableResId);
    }

    @NonNull
    @Override
    public String toString() {
        return "AvatarItem{" +
                "id='" + id + '\'' +
                ", drawableResId=" + drawableResId +
                '}';
    }
}
