package cn.winfxk.android.myclouds.open;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import cn.winfxk.android.myclouds.R;
import cn.winfxk.android.myclouds.tool.Toast;

public class OpenImage extends Activity implements View.OnClickListener {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_image);
        ImageView imageView = findViewById(R.id.imageView1);
        TextView textView = findViewById(R.id.textView1);
        ImageButton imageButton = findViewById(R.id.imageButton1);
        imageButton.setOnClickListener(this);
        Intent intent = getIntent();
        Bundle data = intent.getBundleExtra("Data");
        textView.setText(data.getString("Path"));
        try {
            Bitmap image = BitmapFactory.decodeStream(new FileInputStream(data.getString("File")));
            int width = image.getWidth();
            int height = image.getHeight();
            int MaxRow = Math.max(width, height);
            Point point = new Point();
            getWindowManager().getDefaultDisplay().getRealSize(point);
            int screenWidth = point.x;
            int screenHeight = point.y;
            int MaxNew = (int) (Math.max(screenWidth, screenHeight) / 1.5);
            if (MaxNew < MaxRow) {
                Matrix matrix = new Matrix();
                float f = (float) MaxNew / (float) MaxRow;
                matrix.postScale(f, f);
                image = Bitmap.createBitmap(image, 0, 0, width, height, matrix, false);
            }
            imageView.setImageBitmap(image);
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "无法打开图片！").show();
        }
    }

    @Override
    public void onClick(View view) {
        finish();
    }
}
