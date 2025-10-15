package com.way2.news;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public final class AppGlideModuleImpl extends AppGlideModule {
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        // Customize Glide options if needed
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull com.bumptech.glide.Registry registry) {
        // Register custom components if needed
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}


