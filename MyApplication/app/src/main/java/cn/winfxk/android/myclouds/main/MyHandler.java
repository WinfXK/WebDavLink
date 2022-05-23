package cn.winfxk.android.myclouds.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import cn.pedant.SweetAlert.BaseSweet;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.winfxk.android.myclouds.open.OpenImage;
import cn.winfxk.android.myclouds.tool.Toast;
import cn.winfxk.android.myclouds.tool.Tool;

public class MyHandler extends Handler {
    private Main activity;

    protected MyHandler(Main main) {
        activity = main;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case 3:
                Intent intent = null;
                switch (Tool.getExtension(activity.cacheFile.getName())) {
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
                data.putString("File", activity.cacheFile.getAbsolutePath());
                data.putString("Path", activity.cachePath);
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
