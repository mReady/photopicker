package net.mready.photopickertest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import net.mready.picker.PhotoPicker;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96, getResources().getDisplayMetrics());

        findViewById(R.id.btn_take_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new PhotoPicker.Builder(getApplicationContext())
                        .maxHeight(size * 3)
                        .maxWidth(size * 3)
                        .build();

                startActivityForResult(photoPickerIntent, REQ_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE && resultCode == Activity.RESULT_OK) {
            ((ImageView) findViewById(R.id.iv_picture)).setImageURI(data.getData());
        }
    }
}
