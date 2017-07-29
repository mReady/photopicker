package net.mready.picker;

import android.content.Context;
import android.content.Intent;

public final class PhotoPicker {

    private PhotoPicker() {
    }

    public static class Builder {

        private final Intent photoPickerIntent;
        private final Context context;

        public Builder(Context context) {
            this.context = context;
            photoPickerIntent = new Intent(context, PhotoPickerActivity.class);
        }

        public Builder maxWidth(int maxWidth) {
            photoPickerIntent.putExtra(PhotoPickerActivity.EXTRA_REQ_WIDTH, maxWidth);
            return this;
        }

        public Builder maxHeight(int maxHeight) {
            photoPickerIntent.putExtra(PhotoPickerActivity.EXTRA_REQ_HEIGHT, maxHeight);
            return this;
        }

        public Builder quality(int quality) {
            photoPickerIntent.putExtra(PhotoPickerActivity.EXTRA_COMPRESSION_QUALITY, quality);
            return this;
        }

        public Builder title(int resId) {
            photoPickerIntent.putExtra(PhotoPickerActivity.EXTRA_TITLE, context.getString(resId));
            return this;
        }

        public Builder title(CharSequence title) {
            photoPickerIntent.putExtra(PhotoPickerActivity.EXTRA_TITLE, title);
            return this;
        }

        public Intent build() {
            return photoPickerIntent;
        }

    }
}
