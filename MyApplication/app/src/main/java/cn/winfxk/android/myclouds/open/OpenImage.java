package cn.winfxk.android.myclouds.open;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
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
            imageView.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(data.getString("File"))));
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "无法打开图片！").show();
        }
    }

    @Override
    public void onClick(View view) {
        finish();
    }
}
