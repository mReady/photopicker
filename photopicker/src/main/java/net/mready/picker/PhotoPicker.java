package net.mready.picker;

import android.content.Context;
import android.content.Intent;

public final class PhotoPicker {
    private PhotoPicker() {
    }

    public static Intent buildIntent(Context context) {
        return new Intent(context, PhotoPickerActivity.class);
    }

    public static Intent buildIntent(Context context, int quality) {
        Intent intent = new Intent(context, PhotoPickerActivity.class);
        intent.putExtra(PhotoPickerActivity.EXTRA_COMPRESSION_QUALITY, quality);

        return intent;
    }

    public static Intent buildIntent(Context context, int maxWidth, int maxHeight) {
        Intent intent = new Intent(context, PhotoPickerActivity.class);
        intent.putExtra(PhotoPickerActivity.EXTRA_REQ_WIDTH, maxWidth);
        intent.putExtra(PhotoPickerActivity.EXTRA_REQ_HEIGHT, maxHeight);

        return intent;
    }

    public static Intent buildIntent(Context context, int maxWidth, int maxHeight, int quality) {
        Intent intent = new Intent(context, PhotoPickerActivity.class);
        intent.putExtra(PhotoPickerActivity.EXTRA_REQ_WIDTH, maxWidth);
        intent.putExtra(PhotoPickerActivity.EXTRA_REQ_HEIGHT, maxHeight);
        intent.putExtra(PhotoPickerActivity.EXTRA_COMPRESSION_QUALITY, quality);

        return intent;
    }
}
