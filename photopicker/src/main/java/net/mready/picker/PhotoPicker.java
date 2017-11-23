/*
 * Copyright (C) 2017 mReady
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

package net.mready.picker;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;

public final class PhotoPicker {

    private PhotoPicker() {
    }

    public static class Builder {

        private final Context context;
        private CharSequence title;
        private int maxWidth = -1;
        private int maxHeight = -1;
        private int quality = 70;

        /**
         * Creates a builder for Photo Picker
         *
         * @param context the parent context
         */
        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Specifies the maximum width of the resulting photo
         *
         * @param maxWidth maximum width
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder maxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        /**
         * Specifies the maximum height of the resulting photo
         *
         * @param maxHeight maximum height
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder maxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }

        /**
         * Specifies the quality of the resulting photo
         *
         * @param quality image quality
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder quality(int quality) {
            this.quality = quality;
            return this;
        }

        /**
         * Sets the picker dialog title using a resource id
         *
         * @param resId title resource
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder title(@StringRes int resId) {
            this.title = context.getString(resId);
            return this;
        }

        /**
         * Sets the picker dialog title using a CharSequence
         *
         * @param title title
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder title(CharSequence title) {
            this.title = title;
            return this;
        }

        /**
         * Builds the picker intent
         *
         * @return Photo Picker Intent
         */
        public Intent build() {
            Intent photoPickerIntent = new Intent(context, PhotoPickerActivity.class);
            photoPickerIntent.putExtra(PhotoPickerActivity.EXTRA_REQ_WIDTH, maxWidth);
            photoPickerIntent.putExtra(PhotoPickerActivity.EXTRA_REQ_HEIGHT, maxHeight);
            photoPickerIntent.putExtra(PhotoPickerActivity.EXTRA_COMPRESSION_QUALITY, quality);
            photoPickerIntent.putExtra(PhotoPickerActivity.EXTRA_TITLE, title);
            return photoPickerIntent;

        }

    }
}
