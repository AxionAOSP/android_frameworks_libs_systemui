/*
 * Copyright (C) 2025-2026 AxionOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.icons;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.ThemeEngine;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ThemedIconSettings {

    public static final String KEY_ICON_SCALE = "themed_icon_scale";
    public static final String KEY_BACKGROUND_COLOR = "themed_icon_background_color";
    public static final String KEY_BACKGROUND_COLOR_SOURCE = "themed_icon_background_color_source";
    public static final String KEY_FOREGROUND_COLOR = "themed_icon_foreground_color";
    public static final String KEY_FOREGROUND_COLOR_SOURCE = "themed_icon_foreground_color_source";
    public static final String KEY_COLOR_PRESET = "themed_icon_color_preset";
    public static final String KEY_THEMED_ICONS = "themed_icons";

    public static final String COLOR_PRESET_AXICONS = "axicons";
    public static final String COLOR_PRESET_AOSP = "aosp";
    public static final String COLOR_PRESET_CUSTOM = "custom";

    public static final int DEFAULT_ICON_SCALE = 72;
    public static final int MIN_ICON_SCALE = 48;
    public static final int MAX_ICON_SCALE = 100;

    private ThemedIconSettings() { }

    public static boolean hasActiveIconPack(@Nullable Context context) {
        try {
            ThemeEngine engine = ThemeEngine.getInstance(context);
            return engine != null && engine.hasActiveIconPack();
        } catch (Throwable t) {
            return false;
        }
    }

    @Nullable
    public static String getIconPackPackage(@Nullable Context context) {
        try {
            ThemeEngine engine = ThemeEngine.getInstance(context);
            return engine != null ? engine.getIconPackPackage() : null;
        } catch (Throwable t) {
            return null;
        }
    }

    @NonNull
    public static String getState(@Nullable Context context) {
        if (context == null) {
            return "";
        }
        String packPkg = getIconPackPackage(context);
        return "," + (packPkg == null ? "" : packPkg)
                + "," + getIconScale(context)
                + "," + getBackgroundColor(context)
                + "," + getBackgroundColorSource(context)
                + "," + getForegroundColor(context)
                + "," + getForegroundColorSource(context)
                + "," + getColorPreset(context);
    }

    @Nullable
    public static Drawable loadIconPackDrawable(@Nullable Context context,
            @NonNull ComponentName component, int density) {
        if (context == null) {
            return null;
        }
        try {
            ThemeEngine engine = ThemeEngine.getInstance(context);
            if (engine == null) {
                return null;
            }

            Drawable globalIcon = engine.getIconPackDrawable(component, density);
            if (globalIcon != null) {
                return wrapAsFullBleed(context, globalIcon, engine.getIconPackPackage());
            }
        } catch (Throwable t) {
        }
        return null;
    }

    @NonNull
    private static Drawable wrapAsFullBleed(@NonNull Context context,
            @NonNull Drawable icon, @Nullable String iconPackPackage) {
        if (icon instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
            if (bitmap != null) {
                return new FullBleedBitmapDrawable(
                        context.getResources(), bitmap, iconPackPackage);
            }
        }
        return icon;
    }

    public static boolean isIconPackDrawable(@Nullable Drawable drawable) {
        return drawable instanceof FullBleedBitmapDrawable;
    }

    public static boolean isThemedIconsEnabled(@Nullable Context context) {
        return context != null && Settings.Secure.getInt(
                context.getContentResolver(), KEY_THEMED_ICONS, 0) == 1;
    }

    public static int getIconScale(@NonNull Context context) {
        return clamp(Settings.Secure.getInt(
                context.getContentResolver(), KEY_ICON_SCALE, DEFAULT_ICON_SCALE),
                MIN_ICON_SCALE, MAX_ICON_SCALE);
    }

    public static int getIconInsetInPixels(@NonNull Context context, int size) {
        if (size <= 0) {
            return 0;
        }
        return Math.round(size * (1f - (getIconScale(context) / 100f)) / 2f);
    }

    public static float getIconInsetFraction(@NonNull Context context) {
        return (1f - (getIconScale(context) / 100f)) / 2f;
    }

    public static int getBackgroundColor(@NonNull Context context) {
        String source = getBackgroundColorSource(context);
        if (COLOR_PRESET_AOSP.equals(source)) {
            return getAospBackgroundColor(context);
        }
        if (COLOR_PRESET_CUSTOM.equals(source)) {
            return getColor(context, KEY_BACKGROUND_COLOR, getAxBackgroundColor(context));
        }
        return getAxBackgroundColor(context);
    }

    public static int getForegroundColor(@NonNull Context context) {
        String source = getForegroundColorSource(context);
        if (COLOR_PRESET_AOSP.equals(source)) {
            return getAospForegroundColor(context);
        }
        if (COLOR_PRESET_CUSTOM.equals(source)) {
            return getColor(context, KEY_FOREGROUND_COLOR, getAxForegroundColor(context));
        }
        return getAxForegroundColor(context);
    }

    public static int getAdaptiveBackgroundColor(@NonNull Context context) {
        int background = getBackgroundColor(context);
        int foreground = getForegroundColor(context);
        if (matchesDefaultColors(context, background, foreground, COLOR_PRESET_AXICONS)) {
            return context.getResources().getColor(
                    R.color.ax_themed_icon_adaptive_background_color);
        }
        if (matchesDefaultColors(context, background, foreground, COLOR_PRESET_AOSP)) {
            return context.getResources().getColor(R.color.themed_icon_adaptive_background_color);
        }
        return background;
    }

    public static int getAxBackgroundColor(@NonNull Context context) {
        return context.getResources().getColor(R.color.ax_themed_icon_background_color);
    }

    public static int getAxBackgroundColor(@NonNull Context context, boolean night) {
        return getResourceColor(context, R.color.ax_themed_icon_background_color, night);
    }

    public static int getAxForegroundColor(@NonNull Context context) {
        return context.getResources().getColor(R.color.ax_themed_icon_color);
    }

    public static int getAxForegroundColor(@NonNull Context context, boolean night) {
        return getResourceColor(context, R.color.ax_themed_icon_color, night);
    }

    public static int getAospBackgroundColor(@NonNull Context context) {
        return context.getResources().getColor(R.color.themed_icon_background_color);
    }

    public static int getAospBackgroundColor(@NonNull Context context, boolean night) {
        return getResourceColor(context, R.color.themed_icon_background_color, night);
    }

    public static int getAospForegroundColor(@NonNull Context context) {
        return context.getResources().getColor(R.color.themed_icon_color);
    }

    public static int getAospForegroundColor(@NonNull Context context, boolean night) {
        return getResourceColor(context, R.color.themed_icon_color, night);
    }

    @NonNull
    public static String getColorPreset(@NonNull Context context) {
        String backgroundSource = getBackgroundColorSource(context);
        String foregroundSource = getForegroundColorSource(context);
        if (backgroundSource.equals(foregroundSource)) {
            return backgroundSource;
        }

        int background = getBackgroundColor(context);
        int foreground = getForegroundColor(context);
        if (matchesDefaultColors(context, background, foreground, COLOR_PRESET_AXICONS)) {
            return COLOR_PRESET_AXICONS;
        }
        if (matchesDefaultColors(context, background, foreground, COLOR_PRESET_AOSP)) {
            return COLOR_PRESET_AOSP;
        }
        return COLOR_PRESET_CUSTOM;
    }

    @NonNull
    public static String getBackgroundColorSource(@NonNull Context context) {
        return getColorSource(context, KEY_BACKGROUND_COLOR_SOURCE, getLegacyColorPreset(context));
    }

    @NonNull
    public static String getForegroundColorSource(@NonNull Context context) {
        return getColorSource(context, KEY_FOREGROUND_COLOR_SOURCE, getLegacyColorPreset(context));
    }

    @NonNull
    private static String getLegacyColorPreset(@NonNull Context context) {
        String preset = Settings.Secure.getString(context.getContentResolver(), KEY_COLOR_PRESET);
        if (isColorPreset(preset)) {
            return preset;
        }

        Integer background = parseColor(Settings.Secure.getString(
                context.getContentResolver(), KEY_BACKGROUND_COLOR));
        Integer foreground = parseColor(Settings.Secure.getString(
                context.getContentResolver(), KEY_FOREGROUND_COLOR));
        if (background == null && foreground == null) {
            return COLOR_PRESET_AXICONS;
        }
        if (background != null && foreground != null) {
            if (matchesDefaultColors(context, background, foreground, COLOR_PRESET_AXICONS)) {
                return COLOR_PRESET_AXICONS;
            }
            if (matchesDefaultColors(context, background, foreground, COLOR_PRESET_AOSP)) {
                return COLOR_PRESET_AOSP;
            }
        }
        return COLOR_PRESET_CUSTOM;
    }

    @NonNull
    private static String getColorSource(@NonNull Context context, @NonNull String key,
            @NonNull String defaultSource) {
        String source = Settings.Secure.getString(context.getContentResolver(), key);
        return isColorPreset(source) ? source : defaultSource;
    }

    private static boolean isColorPreset(@Nullable String preset) {
        return COLOR_PRESET_AXICONS.equals(preset)
                || COLOR_PRESET_AOSP.equals(preset)
                || COLOR_PRESET_CUSTOM.equals(preset);
    }

    private static boolean matchesDefaultColors(@NonNull Context context, int background,
            int foreground, @NonNull String preset) {
        if (COLOR_PRESET_AOSP.equals(preset)) {
            return matchesDefaultColors(background, foreground,
                    getAospBackgroundColor(context, false), getAospForegroundColor(context, false))
                    || matchesDefaultColors(background, foreground,
                            getAospBackgroundColor(context, true),
                            getAospForegroundColor(context, true));
        }
        return matchesDefaultColors(background, foreground,
                getAxBackgroundColor(context, false), getAxForegroundColor(context, false))
                || matchesDefaultColors(background, foreground,
                        getAxBackgroundColor(context, true), getAxForegroundColor(context, true));
    }

    private static boolean matchesDefaultColors(int background, int foreground,
            int defaultBackground, int defaultForeground) {
        return background == defaultBackground && foreground == defaultForeground;
    }

    private static int getColor(@NonNull Context context, @NonNull String key, int defaultColor) {
        Integer value = parseColor(Settings.Secure.getString(context.getContentResolver(), key));
        return value != null ? value : defaultColor;
    }

    @Nullable
    private static Integer parseColor(@Nullable String value) {
        try {
            return value != null ? (int) Long.parseLong(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int getResourceColor(@NonNull Context context, int resId, boolean night) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.uiMode = (configuration.uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
                | (night ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO);
        return context.createConfigurationContext(configuration).getResources().getColor(resId);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
