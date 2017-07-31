package net.mready.picker;

import android.content.Context;
import android.content.Intent;

public final class PhotoPicker {

    private PhotoPicker() {
    }

    public static class Builder {

        private final Context context;
        private CharSequence title;
        private int maxWidth = -1;
        private int maxHeight = -1;
        private int quality = 70;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder maxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        public Builder maxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }

        public Builder quality(int quality) {
            this.quality = quality;
            return this;
        }

        public Builder title(int resId) {
            this.title = context.getString(resId);
            return this;
        }

        public Builder title(CharSequence title) {
            this.title = title;
            return this;
        }

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
