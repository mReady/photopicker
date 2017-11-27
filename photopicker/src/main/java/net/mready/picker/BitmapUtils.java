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

    static void processImage(String filePath, int maxWidth, int maxHeight, int quality) {
        Bitmap bitmap = null;
        Bitmap scaledBitmap = null;

        try {
            ExifInterface exif = new ExifInterface(filePath);

            bitmap = loadSampledBitmap(filePath, maxWidth, maxHeight);
            bitmap = normalizeRotation(bitmap, exif);

            if (maxWidth > 0 || maxHeight > 0) {
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
            } else {
                scaledBitmap = bitmap;
            }

            writeBitmap(scaledBitmap, filePath, quality);

            scaledBitmap.recycle();
            scaledBitmap = null;

        } catch (IOException e) {
            throw new RuntimeException(e);

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

    private static Bitmap normalizeRotation(Bitmap bitmap, ExifInterface exif) {
        Bitmap rotatedBitmap;

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
            return bitmap;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);

        rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        bitmap.recycle();

        return rotatedBitmap;
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

        if (reqWidth > 0 || reqHeight > 0) {
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        }

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
