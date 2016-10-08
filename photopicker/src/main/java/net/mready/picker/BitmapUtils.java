package net.mready.picker;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class BitmapUtils {

    static void scaleBitmap(String filePath, int maxWidth, int maxHeight) {
        if (maxWidth <= 0 && maxHeight <= 0) {
            return;
        }

        Bitmap bitmap = null;
        Bitmap scaledBitmap = null;

        try {
            bitmap = loadSampledBitmap(filePath, maxWidth, maxHeight);

            float bitmapRatio = (float) bitmap.getWidth() / (float) bitmap.getHeight();

            int scaleWidth = bitmap.getWidth();
            int scaleHeight = bitmap.getHeight();

            if (maxWidth > 0 && maxHeight > 0) {
                if (bitmapRatio > 1) {
                    if (bitmap.getWidth() > maxWidth) {
                        scaleWidth = maxWidth;
                        scaleHeight = (int) (maxWidth / bitmapRatio);
                    } else if (bitmap.getHeight() > maxHeight) {
                        scaleWidth = (int) (maxHeight * bitmapRatio);
                        scaleHeight = maxHeight;

                    }
                } else {
                    if (bitmap.getHeight() > maxHeight) {
                        scaleWidth = (int) (maxHeight * bitmapRatio);
                        scaleHeight = maxHeight;
                    } else if (bitmap.getWidth() > maxWidth) {
                        scaleWidth = maxWidth;
                        scaleHeight = (int) (maxWidth / bitmapRatio);
                    }
                }
            } else if (maxWidth > 0) {
                if (bitmapRatio > 1) {
                    if (bitmap.getWidth() > maxWidth) {
                        scaleWidth = maxWidth;
                        scaleHeight = (int) (maxWidth / bitmapRatio);
                    }
                } else {
                    if (bitmap.getWidth() > maxWidth) {
                        scaleWidth = maxWidth;
                        scaleHeight = (int) (maxWidth / bitmapRatio);
                    }
                }
            } else if (maxHeight > 0) {
                if (bitmapRatio > 1) {
                    if (bitmap.getHeight() > maxHeight) {
                        scaleWidth = (int) (maxHeight * bitmapRatio);
                        scaleHeight = maxHeight;

                    }
                } else {
                    if (bitmap.getHeight() > maxHeight) {
                        scaleWidth = (int) (maxHeight * bitmapRatio);
                        scaleHeight = maxHeight;
                    }
                }
            }

            scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, true);

            if (bitmap != scaledBitmap) {
                bitmap.recycle();
                bitmap = null;
            }

            writeBitmap(scaledBitmap, filePath, 100);

            scaledBitmap.recycle();
            scaledBitmap = null;

        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }

            if (scaledBitmap != null && !scaledBitmap.isRecycled()) {
                scaledBitmap.recycle();
            }
        }
    }

    static Uri copyToLocal(Context context, Uri uri) {
        if (!ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            return uri;
        }

        File outFile = new File(context.getExternalCacheDir(), "picture" + System.currentTimeMillis() + ".jpg");

        InputStream is = null;
        OutputStream os = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            os = new FileOutputStream(outFile);

            copy(is, os);

            return Uri.fromFile(outFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    static void normalizeRotation(String filePath) {
        Bitmap bitmap = null;
        Bitmap rotatedBitmap = null;

        try {
            ExifInterface exif = new ExifInterface(filePath);

            String orientationString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientationString != null ? Integer.parseInt(orientationString) : ExifInterface.ORIENTATION_NORMAL;

            int rotationAngle = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotationAngle = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotationAngle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotationAngle = 270;
                    break;
            }

            if (rotationAngle == 0) {
                return;
            }

            bitmap = BitmapFactory.decodeFile(filePath);

            Matrix matrix = new Matrix();
            matrix.postRotate(rotationAngle);

            rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            bitmap.recycle();
            bitmap = null;

            writeBitmap(rotatedBitmap, filePath, 100);

            rotatedBitmap.recycle();

        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }

            if (rotatedBitmap != null && !rotatedBitmap.isRecycled()) {
                rotatedBitmap.recycle();
            }
        }
    }

    static void compressBitmap(String filePath, int quality) {
        Bitmap bitmap = null;

        try {
            bitmap = BitmapFactory.decodeFile(filePath);
            writeBitmap(bitmap, filePath, quality);
            bitmap.recycle();
            bitmap = null;
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    private static void writeBitmap(Bitmap bitmap, String filePath, int quality) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static Bitmap loadSampledBitmap(String filePath, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[4096];

        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }
}
