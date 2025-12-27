/*
 * Copyright (C) 2025 AxionOS
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

import android.content.Context;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AxIconsHelper {

    private static final String THEMED_ICON_STYLE_SETTING = "themed_icon_style";
    private static final String STYLE_AOSP = "aosp";
    private static final String STYLE_AXION = "axion";
    
    private static final int AXICONS_PADDING_DP = 12;

    public static boolean isAxIconsEnabled(@Nullable Context context) {
        if (context == null) {
            return true;
        }
        
        String style = Settings.Secure.getString(
                context.getContentResolver(), THEMED_ICON_STYLE_SETTING);
        return !STYLE_AOSP.equals(style);
    }

    @NonNull
    public static Drawable wrapIconIfNeeded(@NonNull Context context, @NonNull Drawable icon) {
        if (!isAxIconsEnabled(context)) {
            return icon;
        }
        
        if (!(icon instanceof AdaptiveIconDrawable)) {
            return icon;
        }
        
        AdaptiveIconDrawable aid = (AdaptiveIconDrawable) icon;
        if (aid.getMonochrome() == null) {
            return icon;
        }
        
        return wrapMonochromeIcon(context, aid);
    }

    @NonNull
    private static AdaptiveIconDrawable wrapMonochromeIcon(
            @NonNull Context context, @NonNull AdaptiveIconDrawable aid) {
        Drawable mono = aid.getMonochrome().mutate();
        int paddingPx = getPaddingInPixels(context);
        Drawable wrappedMono = new InsetDrawable(mono, paddingPx);
        return new AdaptiveIconDrawable(aid.getBackground(), aid.getForeground(), wrappedMono);
    }

    public static int getPaddingInPixels(@NonNull Context context) {
        return (int) (AXICONS_PADDING_DP * context.getResources().getDisplayMetrics().density);
    }
}
