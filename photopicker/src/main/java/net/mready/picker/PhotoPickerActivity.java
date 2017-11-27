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

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoPickerActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {
    public static final String EXTRA_REQ_WIDTH = "EXTRA_REQ_WIDTH";
    public static final String EXTRA_REQ_HEIGHT = "EXTRA_REQ_HEIGHT";
    public static final String EXTRA_COMPRESSION_QUALITY = "EXTRA_COMPRESSION_QUALITY";
    public static final String EXTRA_TITLE = "EXTRA_TITLE";

    private static final String EXPECTED_FILE_URI = "EXPECTED_FILE_URI";
    private static final int REQUEST_PICTURE = 123;
    private static final int REQUEST_PERMISSION_CAMERA = 1;

    private Uri expectedFileUri;
    private int reqWidth;
    private int reqHeight;
    private int quality;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reqWidth = getIntent().getIntExtra(EXTRA_REQ_WIDTH, -1);
        reqHeight = getIntent().getIntExtra(EXTRA_REQ_HEIGHT, -1);
        quality = getIntent().getIntExtra(EXTRA_COMPRESSION_QUALITY, 70);
        title = getIntent().getStringExtra(EXTRA_TITLE);

        if (title == null) {
            title = getString(R.string.default_picker_title);
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EXPECTED_FILE_URI)) {
                expectedFileUri = Uri.parse(savedInstanceState.getString(EXPECTED_FILE_URI));
            }
        } else {
            if (getIntent() != null && getIntent().getData() != null) {
                expectedFileUri = getIntent().getData();
            } else {
                expectedFileUri = Uri.fromFile(new File(getExternalCacheDir(), "picture" + System.currentTimeMillis() + ".jpg"));
            }

            if (shouldRequestPermission()) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
            } else {
                dispatchIntent();
            }
        }
    }

    private boolean shouldRequestPermission() {
        if (!hasCameraPermissionInManifest()) {
            return false;
        }

        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCameraPermissionInManifest() {
        try {
            final PackageInfo packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
            final String[] declaredPermissions = packageInfo.requestedPermissions;
            if (declaredPermissions != null && declaredPermissions.length > 0) {
                for (String p : declaredPermissions) {
                    if (p.equals(Manifest.permission.CAMERA)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchIntent();
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
                break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (expectedFileUri != null) {
            outState.putString(EXPECTED_FILE_URI, expectedFileUri.toString());
        }

        super.onSaveInstanceState(outState);
    }

    private Intent createChooserIntent() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, expectedFileUri);

        List<Intent> cameraIntents = new ArrayList<>();

        List<ResolveInfo> cameraList = getPackageManager().queryIntentActivities(captureIntent, 0);
        for (ResolveInfo resolveInfo : cameraList) {
            String packageName = resolveInfo.activityInfo.packageName;

            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
            intent.setPackage(packageName);

            cameraIntents.add(intent);
        }

        Intent pickerIntent = new Intent(Intent.ACTION_GET_CONTENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            pickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        pickerIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(pickerIntent, title);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        return chooserIntent;
    }

    private void dispatchIntent() {
        Intent intent = createChooserIntent();

        startActivityForResult(intent, REQUEST_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICTURE && resultCode == RESULT_OK) {
            handleResult(data);
        } else {
            finish();
        }
    }

    private void handleResult(Intent data) {
        Uri uri;

        if (data != null && data.getData() != null) {
            uri = BitmapUtils.copyToLocal(this, data.getData());
        } else {
            uri = expectedFileUri;
        }

        File file = new File(uri.getPath());
        if (file.exists()) {
            BitmapUtils.processImage(uri.getPath(), reqWidth, reqHeight, quality);

            Intent result = new Intent();
            result.setData(uri);
            setResult(RESULT_OK, result);
        }
        finish();
    }

}
