package cn.winfxk.android.myclouds.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import cn.pedant.SweetAlert.BaseSweet;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.winfxk.android.myclouds.R;
import cn.winfxk.android.myclouds.open.OpenImage;
import cn.winfxk.android.myclouds.tool.Toast;
import cn.winfxk.android.myclouds.tool.Tool;

public class MyHandler extends Handler {
    private Main activity;
    protected int ImageButtonHeight, ImageButtonMargin, SBImageButtonMargin;
    protected RelativeLayout.LayoutParams lp;
    private int ImageButtonMarginLive;

    protected MyHandler(Main main) {
        activity = main;
        ImageButtonHeight = main.getResources().getDimensionPixelOffset(R.dimen.main_imagebutton_hw);
        lp = (RelativeLayout.LayoutParams) main.imageButton.getLayoutParams();
        if (lp == null)
            lp = new RelativeLayout.LayoutParams(ImageButtonHeight, ImageButtonHeight);
        SBImageButtonMargin = (ImageButtonMarginLive = (ImageButtonMargin = main.getResources().getDimensionPixelOffset(R.dimen.main_imagebutton_mb)) + ImageButtonHeight) * -1;
        new Thread(this::ImageState).start();
    }

    /**
     * 对按钮状态进行更新
     */
    private void ImageState() {
        while (true)
            try {
                Thread.sleep(1);
                if (!activity.adapter.host)
                    if (activity.adapter.Path.isEmpty() && ImageButtonMarginLive > SBImageButtonMargin)
                        sendEmptyMessage(4);
                    else if (!activity.adapter.Path.isEmpty() && ImageButtonMarginLive < ImageButtonMargin)
                        sendEmptyMessage(5);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        Intent intent = null;
        switch (msg.what) {
            case 7:
                activity.finish();
                break;
            case 6:
                Toast.makeText(activity, "再次点击退出程序...").show();
                break;
            case 5:
                lp.setMargins(0, 0, 0, ImageButtonMarginLive += 2);
                activity.imageButton.setLayoutParams(lp);
                break;
            case 4:
                lp.setMargins(0, 0, 0, ImageButtonMarginLive -= 2);
                activity.imageButton.setLayoutParams(lp);
                break;
            case 3:
                switch (activity.fileData.FileEx) {
                    case "image":
                        intent = new Intent(activity, OpenImage.class);
                        break;
                    default:

                }
                if (intent == null) {
                    Toast.makeText(activity, "无法打开这个文件！").show();
                    return;
                }
                Bundle data = new Bundle();
                data.putString("File", activity.fileData.cacheFile.getAbsolutePath());
                data.putString("Path", activity.fileData.cachePath);
                intent.putExtra("Data", data);
                activity.startActivity(intent);
                break;
            case 2:
                if (activity.Filereload.isShowing())
                    activity.Filereload.dismiss();
                break;
            case 1:
                if (activity.Filereload == null) {
                    activity.Filereload = new SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE);
                    activity.Filereload.setClickConceal(false);
                    activity.Filereload.setCancel("取消", BaseSweet.NoClose);
                    activity.Filereload.setConfirm("确定", BaseSweet.NoClose);
                }
                activity.Filereload.setMessage("加载中...");
                if (!activity.Filereload.isShowing())
                    activity.Filereload.show();
                break;
            case 0:
                activity.adapter.notifyDataSetChanged();
                activity.FilePathView.setText(activity.adapter.Path == null || activity.adapter.Path.isEmpty() ? "根目录" : activity.adapter.Path);
                break;
            case -1:
                Toast.makeText(activity, Tool.objToString(msg.getData().getString("error"), "未知错误！")).show();
                break;
        }
    }
}
