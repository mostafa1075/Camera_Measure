package com.example.memo.camerameasure;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by EL-MUSLIM on 4/27/2017.
 */

public class main_menu extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.options);

        Button btn_offline = (Button) findViewById(R.id.btn_off);
        Button btn_online = (Button) findViewById(R.id.btn_on);

        ShapeDrawable shapedrawable = new ShapeDrawable();
        shapedrawable.setShape(new RectShape());
        shapedrawable.getPaint().setColor(Color.LTGRAY);
        shapedrawable.getPaint().setStrokeWidth(10f);
        shapedrawable.getPaint().setStyle(Paint.Style.STROKE);
        btn_offline.setBackground(shapedrawable);
        btn_online.setBackground(shapedrawable);
        btn_offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(main_menu.this, ImageAccess.class));
            }
        });
        btn_online.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(main_menu.this, Online.class));
            }
        });
    }
}