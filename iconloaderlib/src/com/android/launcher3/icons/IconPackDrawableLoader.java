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

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class IconPackDrawableLoader {

    @Nullable
    public static Drawable loadDrawable(
            @NonNull Context context,
            @NonNull String packPackage,
            @NonNull String drawableName,
            int density) {
        try {
            Resources packRes = context.getPackageManager()
                    .getResourcesForApplication(packPackage);
            int resId = packRes.getIdentifier(drawableName, "drawable", packPackage);
            if (resId == 0) return null;
            Drawable d = density != 0
                    ? packRes.getDrawableForDensity(resId, density, null)
                    : packRes.getDrawable(resId, null);
            if (d == null) return null;
            return wrap(context, d, packPackage);
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            return null;
        }
    }

    @NonNull
    private static Drawable wrap(@NonNull Context context, @NonNull Drawable d,
            @NonNull String packPackage) {
        if (d instanceof BitmapDrawable) {
            Bitmap bmp = ((BitmapDrawable) d).getBitmap();
            if (bmp != null) {
                return new FullBleedBitmapDrawable(context.getResources(), bmp, packPackage);
            }
        }
        return d;
    }
}
