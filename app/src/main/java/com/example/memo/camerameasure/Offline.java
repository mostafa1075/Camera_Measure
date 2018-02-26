package com.example.memo.camerameasure;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.TreeSet;

public class Offline extends AppCompatActivity {
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    //Interface Variables
    SeekBar rbar;
    Button btnref, btn_save_image, btn_mode;
    TextView textView;
    ImageView circle_1, circle_2, touch_original, touch_tmp, cropimage;

    //popup menu Interface Variables
    AlertDialog.Builder mBuilder;
    View mView;
    RadioButton rb_cutom;
    Spinner spinner;
    EditText editText;
    ArrayAdapter<CharSequence> adapter;
    Switch aSwitch;

    //Variables
    float x = 0, y = 0, pixel_ratio, ref_len;
    int circles_h = 0, circles_w = 0, small_local_pos_x = 0, small_local_pos_y = 0, crop_img_w = 0, crop_img_h = 0, threshold = 50;
    Bitmap bmp;
    Canvas c;
    final static String message_key = "message.message"; //connects 2 messages
    String recived_image_path, input_unit, out_unit = "cm";
    Boolean refrence = false, hold, hold2, mode = true; //True If REgion Growing  , False Custom Mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intialize_Variables();

        final ImageButton imgClose = (ImageButton) findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
                startActivity(new Intent(Offline.this, Offline.class));
            }
        });
        rbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                threshold = i;
                textView.setText(String.valueOf(threshold));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

        touch_original.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                Update_Bullets_Dimensions(event);

                Check_Either_Bullet_will_Move();

                Reset_Bullets_Properties_When_Action_Up(event);

                return true;
            }
        });

        btnref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (btnref.getText().toString().equalsIgnoreCase("Keep This Measured Line"))
                    keeping_measured_line();
                if (btnref.getText().toString().equalsIgnoreCase("Take Reference Length"))
                    Check_Case_User_Use();
            }
        });

        btn_save_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Save_Image_in_Gallary();
            }
        });

        btn_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Set_initial_Configuration_Of_Popup_Menu();
                final RadioButton rb_region_growing = (RadioButton) mView.findViewById(R.id.rb_regiongrowing);
                final RadioButton rb_coin = (RadioButton) mView.findViewById(R.id.rb_coin);
                final RadioButton rb_a4 = (RadioButton) mView.findViewById(R.id.rb_a4);

                rb_cutom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Set_Configuration_Of_Custom_Radio_Button(rb_region_growing, rb_coin, rb_a4);
                    }
                });
                rb_region_growing.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Set_Configuration_Of_RegionGrowing_Radio_Button(rb_region_growing, rb_coin, rb_a4);
                    }
                });

                rb_a4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Set_Reference_Configurations_Of_Radio_Button(rb_region_growing, rb_coin, rb_a4, true, false, true, false);
                    }
                });

                rb_coin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Set_Reference_Configurations_Of_Radio_Button(rb_region_growing, rb_coin, rb_a4, true, true, false, false);
                    }
                });


                adapter = ArrayAdapter.createFromResource(Offline.this, R.array.unit_choices, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (rb_cutom.isChecked()) {
                            ref_len = Float.parseFloat(editText.getText().toString());
                            mode = false;
                        }
                        Check_Measure_Type();
                        Set_Real_Reference_Length(rb_coin, rb_a4);
                        if (rb_region_growing.isChecked()) {
                            rbar.setVisibility(textView.VISIBLE);
                        }

                    }
                });
                Set_Final_Configuration_Of_Popup_Menu(rb_region_growing);
            }
        });
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (aSwitch.isChecked()) {
                    aSwitch.setText("Cm");
                    out_unit = "cm";
                } else {
                    aSwitch.setText("Inch");
                    out_unit = "in";
                }

            }
        });
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * I-reset configurations's bullets.
     */
    void Reset_Bullets_Properties_When_Action_Up(MotionEvent event) {
        if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
            circle_1.setAlpha(0.3f);
            circle_2.setAlpha(0.3f);
            hold = false;
            hold2 = false;
        }
    }

    /**
     * I-get the positions X,Y of the circle from screen coordinates.
     * II-get width and height of circle and crop scene.
     */
    void Update_Bullets_Dimensions(MotionEvent event) {
        x = event.getX();
        y = event.getY();
        circles_w = circle_1.getWidth();
        circles_h = circle_1.getHeight();
        crop_img_w = cropimage.getWidth();
        crop_img_h = cropimage.getHeight();
    }

    /**
     * I-Check the touch X,Y within the region of any circle to move.
     * II-Update zooming scene around the area that user touched on it.
     */
    void Check_Either_Bullet_will_Move() {
        if (move_circles(x, y) == 1 || hold == true) {
            hold = true;
            circle_1.setX(x - (circles_w / 2));
            circle_1.setY(y - (circles_h / 2));
            circle_1.setAlpha(0.8f);
            circle_2.setAlpha(0.3f);
            crop_with_circle_move();

            if (!mode)
                crearPunto((circle_1.getX() + (circles_w / 2)), (circle_1.getY() + (circles_h / 2)),
                        (circle_2.getX() + (circles_w / 2)), (circle_2.getY() + (circles_h / 2)), Color.YELLOW);
        } else if (move_circles(x, y) == 2 || hold2 == true) {
            hold2 = true;
            circle_2.setX(x - (circles_w / 2));
            circle_2.setY(y - (circles_h / 2));
            circle_1.setAlpha(0.3f);
            circle_2.setAlpha(0.8f);
            crop_with_circle_move();

            crearPunto((circle_1.getX() + (circles_w / 2)), (circle_1.getY() + (circles_h / 2))
                    , (circle_2.getX() + (circles_w / 2))
                    , (circle_2.getY() + (circles_h / 2)), Color.YELLOW);
        }
    }

    /**
     * I-check the case (custom,region growing) that user will use it.
     * II-get pixel ratio.
     * III-update image with measured lines.
     */
    void Check_Case_User_Use() {
        //custom case.
        if (!mode) {
            float CoorRefDistance = (float) Math.sqrt(Math.pow(circle_1.getX() - circle_2.getX(), 2)
                    + Math.pow(circle_1.getY() - circle_2.getY(), 2));
            get_Pixel_Ratio(CoorRefDistance);
        }
        //region growing case.
        else if (mode) {
            bmp = Bitmap.createBitmap(touch_original.getWidth(), touch_original.getHeight(), Bitmap.Config.ARGB_8888);
            c = new Canvas(bmp);
            touch_tmp.draw(c);
            float CoorRefDistance = (float) regionGrowing((int) x, (int) y, threshold);
            get_Pixel_Ratio(CoorRefDistance);
            touch_original.setImageBitmap(bmp);
            circle_2.setVisibility(View.VISIBLE);
            mode = false;
        }
        keeping_measured_line();
        btnref.setText("Keep This Measured Line");
        btnref.setBackgroundResource(R.drawable.keep_measured);
        refrence = true;
    }

    /**
     * I-calculate pixel ratio about real reference length and coordination reference length.
     */
    void get_Pixel_Ratio(float CoorRefDistance) {
        pixel_ratio = ref_len / CoorRefDistance;
    }

    /**
     * I-save image in gallary.
     */
    void Save_Image_in_Gallary() {
        OutputStream outStream = null;
        File file = new File(recived_image_path);
        File image_path = new File(Environment.getExternalStoragePublicDirectory(""), "Camera Measure/" + file.getName());
        try {
            outStream = new FileOutputStream(image_path);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
            galleryAddPic(image_path.getAbsolutePath());

        } catch (Exception e) {
        }
        Toast.makeText(Offline.this, "Image Saved", Toast.LENGTH_SHORT).show();
    }

    /**
     * I-adjust the view of popup menu modes.
     */
    void Set_initial_Configuration_Of_Popup_Menu() {
        mBuilder = new AlertDialog.Builder(Offline.this);
        mView = getLayoutInflater().inflate(R.layout.mode_dialog, null);

        rb_cutom = (RadioButton) mView.findViewById(R.id.rb_custom);
        spinner = (Spinner) mView.findViewById(R.id.spinner);
        editText = (EditText) mView.findViewById(R.id.editText);
    }

    /**
     * I-adjust the view of custom radio button.
     */
    void Set_Configuration_Of_Custom_Radio_Button(RadioButton rb_region_growing, RadioButton rb_coin, RadioButton rb_a4) {
        rb_region_growing.setChecked(false);
        rb_coin.setChecked(false);
        rb_a4.setChecked(false);
        rb_cutom.setChecked(true);

        rb_coin.setVisibility(View.INVISIBLE);
        rb_a4.setVisibility(View.INVISIBLE);
        spinner.setVisibility(View.VISIBLE);
        editText.setVisibility(View.VISIBLE);

        btn_mode.setVisibility(View.INVISIBLE);
        circle_2.setVisibility(View.VISIBLE);
        circle_1.setVisibility(View.VISIBLE);
        btnref.setVisibility(View.VISIBLE);
        aSwitch.setVisibility(View.VISIBLE);
        btn_save_image.setVisibility(View.VISIBLE);
        circle_2.setAlpha(0.3f);

        rb_cutom.setY(rb_region_growing.getBottom());

    }

    /**
     * I-adjust the view of RegionGrowing radio button.
     */
    void Set_Configuration_Of_RegionGrowing_Radio_Button(RadioButton rb_region_growing, RadioButton rb_coin, RadioButton rb_a4) {
        rb_region_growing.setChecked(true);
        rb_coin.setChecked(false);
        rb_a4.setChecked(false);
        rb_cutom.setChecked(false);

        btn_mode.setVisibility(View.INVISIBLE);
        circle_1.setVisibility(View.VISIBLE);
        btnref.setVisibility(View.VISIBLE);
        btn_save_image.setVisibility(View.VISIBLE);
        aSwitch.setVisibility(View.VISIBLE);

        rb_cutom.setVisibility(View.VISIBLE);
        rb_coin.setVisibility(View.VISIBLE);
        rb_a4.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.INVISIBLE);
        editText.setVisibility(View.INVISIBLE);
        rb_cutom.setY(spinner.getY());
    }

    /**
     * I-update configurations of radio button according to reference's type.
     */
    void Set_Reference_Configurations_Of_Radio_Button(RadioButton rb_region_growing, RadioButton rb_coin, RadioButton rb_a4, boolean rgbool, boolean coinbool, boolean a4bool, boolean custombool) {
        rb_region_growing.setChecked(rgbool);
        rb_coin.setChecked(coinbool);
        rb_a4.setChecked(a4bool);
        rb_cutom.setChecked(custombool);
    }

    /**
     * I-check measure type(cm,inch).
     */
    void Check_Measure_Type() {
        if (spinner.getSelectedItem().toString().equalsIgnoreCase("CM"))
            input_unit = "cm";
        else
            input_unit = "in";
    }

    /**
     * I-set the real reference value.
     */
    void Set_Real_Reference_Length(RadioButton rb_coin, RadioButton rb_a4) {
        if (rb_coin.isChecked())
            ref_len = 2.2f;//length
        if (rb_a4.isChecked())
            ref_len = 36.37f;//diameter

    }

    /**
     * I-show the dialog.
     */
    void Set_Final_Configuration_Of_Popup_Menu(RadioButton rb_region_growing) {
        mBuilder.setView(mView);
        AlertDialog alertDialog = mBuilder.create();
        alertDialog.setTitle("Modes");
        alertDialog.show();
    }

    /**
     * I-choose which circle will be move.
     */
    int move_circles(float x_t, float y_t) {


        small_local_pos_x = (int) circle_1.getX();
        small_local_pos_y = (int) circle_1.getY();
        if (x_t > small_local_pos_x && x_t < (small_local_pos_x + circles_w))
            if (y_t > small_local_pos_y && y_t < (small_local_pos_y + circles_h))
                return 1;
        small_local_pos_x = (int) circle_2.getX();
        small_local_pos_y = (int) circle_2.getY();
        if (x_t > small_local_pos_x && x_t < (small_local_pos_x + circles_w))
            if (y_t > small_local_pos_y && y_t < (small_local_pos_y + circles_h))
                return 2;
        return 0;
    }

    /**
     * I-crop area around touched point.
     */
    void crop_with_circle_move() {
        int x_s = (int) x - (crop_img_w / 2);
        int y_s = (int) y - (crop_img_h / 2);
        int x_e = crop_img_w, y_e = crop_img_h;
        if (x_s < 0) {
            x_e = crop_img_w + x_s;
            x_s = 0;
        }
        if (y_s < 0) {
            y_e = crop_img_h + y_s;
            y_s = 0;
        }
        if (x_s + crop_img_w > touch_original.getWidth()) {
            x_e = touch_original.getWidth() - x_s;
        }
        if (y_s + crop_img_h > touch_original.getHeight()) {
            y_e = touch_original.getHeight() - y_s;
        }

        touch_tmp.setDrawingCacheEnabled(true);
        touch_tmp.buildDrawingCache(true);
        Bitmap bitmap = touch_tmp.getDrawingCache();

        Bitmap tmp = Bitmap.createBitmap(bitmap, x_s, y_s, x_e, y_e);
        cropimage.setImageBitmap(tmp);

    }

    /**
     * I-darw measured line.
     */
    void crearPunto(float x, float y, float xend, float yend, int color) {
        bmp = Bitmap.createBitmap(touch_original.getWidth(), touch_original.getHeight(), Bitmap.Config.ARGB_8888);
        c = new Canvas(bmp);
        touch_tmp.draw(c);
        Paint p = new Paint();
        Paint p1 = new Paint();
        p1.setStrokeWidth(5);
        p1.setColor(Color.BLUE);
        p.setStrokeWidth(5);
        p.setColor(color);
        c.drawCircle(x, y, 10, p1);
        c.drawCircle(xend, yend, 10, p1);
        c.drawLine(x, y, xend, yend, p);
        if (refrence) {
            float actual_dist = (float) Math.sqrt(Math.pow(circle_1.getX() - circle_2.getX(), 2)
                    + Math.pow(circle_1.getY() - circle_2.getY(), 2));
            actual_dist *= pixel_ratio;
            if (input_unit == "cm" && out_unit == "in")
                actual_dist *= 0.393701;
            else if (input_unit == "in" && out_unit == "cm")
                actual_dist /= 0.393701;
            Paint text_paint = new Paint();
            text_paint.setColor(Color.BLUE);
            text_paint.setStyle(Paint.Style.FILL_AND_STROKE);
            text_paint.setTextSize(30);
            text_paint.setStrokeWidth(3);
            float text_x = Math.abs((circle_1.getX() - circle_2.getX()) / 2) + Math.min(circle_1.getX(), circle_2.getX());
            float text_y = Math.abs((circle_1.getY() - circle_2.getY()) / 2) + Math.min(circle_1.getY(), circle_2.getY());
            c.drawText(String.valueOf(String.format("%.2f", actual_dist) + " " + out_unit), text_x, text_y, text_paint);
        }
        touch_original.setImageBitmap(bmp);
    }

    /**
     * I-save measured line.
     */
    void keeping_measured_line() {
        circle_1.setX(touch_original.getWidth() / 2);
        circle_1.setY(0);
        circle_2.setX(touch_original.getWidth() / 2);
        circle_2.setY(touch_original.getHeight() / 2);
        touch_tmp.setImageBitmap(bmp);
    }

    /**
     * I-add the photo to gallary.
     */
    void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    /**
     * I-get the dimensions of object automatically using region growing
     */
    double regionGrowing(int x, int y, int Threshold) {
        TreeSet<Integer> open = new TreeSet<Integer>(), closed = new TreeSet<Integer>();
        int count = 0;
        //scale image and x,y
        Bitmap image = scaleDown(bmp, 800, true);
        x = (int) (x * (float) image.getWidth() / (float) bmp.getWidth());
        y = (int) (y * (float) image.getHeight() / (float) bmp.getHeight());
        //get Seed Node RGB values
        int color = image.getPixel(x, y);
        int A = Color.alpha(color), R = Color.red(color), G = Color.green(color), B = Color.blue(color);
        int Xmin = x, Xmax = x, Ymin = y, Ymax = y, Ymin2 = y, Ymax2 = y;
        int seedNode = x * image.getWidth() + y;
        open.add(seedNode);
        while (!open.isEmpty()) {
            // remove current node from open list and add to closed list
            int currentNode = open.first();
            closed.add(currentNode);
            open.remove(currentNode);
            //get current node coordinates
            int xCurr = currentNode / image.getWidth(), yCurr = currentNode % image.getWidth();
            int upColor = 0, downColor = 0, leftColor = 0, rightColor = 0;
            if (xCurr - 1 > 0) upColor = image.getPixel(xCurr - 1, yCurr);
            if (xCurr + 1 < image.getWidth()) downColor = image.getPixel(xCurr + 1, yCurr);
            if (yCurr - 1 > 0) leftColor = image.getPixel(xCurr, yCurr - 1);
            if (yCurr + 1 < image.getHeight()) rightColor = image.getPixel(xCurr, yCurr + 1);
            //Check upper Pixel
            if (xCurr - 1 > 0 && Math.abs(R - Color.red((upColor))) <= Threshold
                    && Math.abs(B - Color.blue((upColor))) <= Threshold && Math.abs(G - Color.green((upColor))) <= Threshold
                    && Math.abs(A - Color.alpha(upColor)) <= Threshold &&
                    !closed.contains(currentNode - image.getWidth()) && !open.contains(currentNode - image.getWidth()))
                open.add((xCurr - 1) * image.getWidth() + yCurr);
            //Check lower Pixel
            if (xCurr + 1 < image.getWidth() && Math.abs(R - Color.red((downColor))) <= Threshold
                    && Math.abs(B - Color.blue((downColor))) <= Threshold
                    && Math.abs(G - Color.green((downColor))) <= Threshold
                    && Math.abs(A - Color.alpha(downColor)) <= Threshold
                    && !closed.contains(currentNode + image.getWidth()) && !open.contains(currentNode + image.getWidth()))
                open.add((xCurr + 1) * image.getWidth() + yCurr);
            //Check left Pixel
            if (yCurr - 1 > 0 && Math.abs(R - Color.red((leftColor))) <= Threshold
                    && Math.abs(B - Color.blue((leftColor))) <= Threshold && Math.abs(G - Color.green((leftColor))) <= Threshold
                    && Math.abs(A - Color.alpha(leftColor)) <= Threshold && !closed.contains(currentNode - 1)
                    && !open.contains(currentNode - 1))
                open.add((xCurr) * image.getWidth() + yCurr - 1);
            //Check right Pixel
            if (yCurr + 1 < image.getHeight() && Math.abs(R - Color.red((rightColor))) <= Threshold
                    && Math.abs(B - Color.blue((rightColor))) <= Threshold
                    && Math.abs(G - Color.green((rightColor))) <= Threshold
                    && Math.abs(A - Color.alpha(rightColor)) <= Threshold && !closed.contains(currentNode + 1)
                    && !open.contains(currentNode + 1))
                open.add((xCurr) * image.getWidth() + yCurr + 1);

            image.setPixel(xCurr, yCurr, Color.RED);
            //check for min & max x,y
            if (xCurr < Xmin) {
                Xmin = xCurr;
                Ymin = Ymin2 = yCurr;
            } else if (xCurr == Xmin) {
                Ymin = Math.min(Ymin, yCurr);
                Ymin2 = Math.max(Ymin2, yCurr);
            }
            if (xCurr > Xmax) {
                Xmax = xCurr;
                Ymax = Ymax2 = yCurr;
            } else if (xCurr == Xmax) {
                Ymax = Math.max(Ymax, yCurr);
                Ymax2 = Math.min(Ymax2, yCurr);
            }
        }
        image.setPixel(Xmin, Ymin, Color.BLUE);
        image.setPixel(Xmax, Ymax, Color.BLUE);
        image.setPixel(Xmin, Ymin2, Color.GREEN);
        image.setPixel(Xmax, Ymax2, Color.GREEN);
        Xmax = (int) (Xmax * ((float) bmp.getWidth() / (float) image.getWidth()));
        Xmin = (int) (Xmin * ((float) bmp.getWidth() / (float) image.getWidth()));
        Ymax = (int) (Ymax * ((float) bmp.getHeight() / (float) image.getHeight()));
        Ymin = (int) (Ymin * ((float) bmp.getHeight() / (float) image.getHeight()));
        Ymax2 = (int) (Ymax2 * ((float) bmp.getHeight() / (float) image.getHeight()));
        Ymin2 = (int) (Ymin2 * ((float) bmp.getHeight() / (float) image.getHeight()));
        double dist1 = Math.sqrt(Math.pow(Xmax - Xmin, 2) + Math.pow(Ymax - Ymin, 2));
        double dist2 = Math.sqrt(Math.pow(Xmax - Xmin, 2) + Math.pow(Ymax2 - Ymin2, 2));
        bmp = scaleDown(image, Math.max(bmp.getWidth(), bmp.getHeight()), true);
        return Math.max(dist1, dist2);

    }

    /**
     * I-scale the bitmap down.
     */
    static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    void Intialize_Variables() {

        rbar = (SeekBar) findViewById(R.id.region_bar);
        btnref = (Button) findViewById(R.id.btn_ref);
        btn_save_image = (Button) findViewById(R.id.btn_save_image);
        btn_mode = (Button) findViewById(R.id.btn_mode);
        textView = (TextView) findViewById(R.id.textView);

        touch_original = (ImageView) findViewById(R.id.imageView);
        touch_tmp = (ImageView) findViewById(R.id.img_tmp);
        Intent intent = getIntent();
        recived_image_path = intent.getStringExtra(message_key);

        touch_original.setImageBitmap(BitmapFactory.decodeFile(recived_image_path));
        touch_tmp.setImageBitmap(BitmapFactory.decodeFile(recived_image_path));

        //initiate Popup menu interface variable
        aSwitch = (Switch) findViewById(R.id.switch2);

        hold = hold2 = false;


        cropimage = (ImageView) findViewById(R.id.imageView2);
        circle_1 = (ImageView) findViewById(R.id.imv_circle1);
        circle_2 = (ImageView) findViewById(R.id.imv_circle2);
        circle_1.setAlpha(0.3f);
        circle_2.setAlpha(0.3f);
        circle_2.setVisibility(View.INVISIBLE);
        circle_1.setVisibility(View.INVISIBLE);

        rbar.setMax(100);
        rbar.setProgress(50);

    }
}