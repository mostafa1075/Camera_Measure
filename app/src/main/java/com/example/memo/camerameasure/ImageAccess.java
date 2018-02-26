package com.example.memo.camerameasure;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by FM on 4/7/2017.
 */

public class ImageAccess extends ActionBarActivity {
    Button btn_take_image, btn_load_image;
    final static String message_key = "message.message"; //connects 2 messages

    public static File image;
    public static String mcurpath;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                galleryAddPic();
                Intent message_intent = new Intent(this, Offline.class);
                message_intent.putExtra(message_key, mcurpath);
                startActivity(message_intent);
            }
        }

        if (requestCode == 0 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Intent message_intent = new Intent(this, Offline.class);
            message_intent.putExtra(message_key, picturePath);
            startActivity(message_intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity1);
        btn_take_image = (Button) findViewById(R.id.btn_take_image);
        btn_load_image = (Button) findViewById(R.id.btn_load_image);

        ShapeDrawable shapedrawable = new ShapeDrawable();
        shapedrawable.setShape(new RectShape());
        shapedrawable.getPaint().setColor(Color.LTGRAY);
        shapedrawable.getPaint().setStrokeWidth(10f);
        shapedrawable.getPaint().setStyle(Paint.Style.STROKE);
        btn_take_image.setBackground(shapedrawable);
        btn_load_image.setBackground(shapedrawable);
        btn_take_image.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                getOutputMediaFile();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                startActivityForResult(intent, 1);
            }

        });

        btn_load_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        });

    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(""), "Camera Measure");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        image = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".JPG");
        mcurpath = image.getAbsolutePath();
        return null;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mcurpath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}
