package cn.winfxk.android.myclouds;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import cn.pedant.SweetAlert.BaseSweet;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.winfxk.android.myclouds.main.Main;
import cn.winfxk.android.myclouds.tool.Toast;

public class MainHandle extends Handler {
    private MainActivity activity;

    protected MainHandle(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 2:
                activity.startActivity(new Intent(activity, Main.class));
                activity.LoginDialog.dismiss();
                activity.finish();
                break;
            case 1:
                activity.LoginDialog.setType(SweetAlertDialog.SUCCESS_TYPE);
                activity.LoginDialog.setMessage("登录成功！即将跳转....");
                activity.LoginDialog.setConfirm("确定", BaseSweet.NoClose);
                activity.LoginDialog.setCancel("取消", BaseSweet.NoClose);
                break;
            case 0:
                activity.PermissionsDialog.setType(SweetAlertDialog.SUCCESS_TYPE);
                activity.PermissionsDialog.setMessage("权限获取成功！");
                activity.PermissionsDialog.setCancelText("下一步");
                activity.PermissionsDialog.setCancelClickListener(sweetAlertDialog -> {
                    activity.PermissionsDialog.dismiss();
                    activity.init();
                });
                break;
            case -1:
                activity.PermissionsDialog.setType(SweetAlertDialog.ERROR_TYPE);
                activity.PermissionsDialog.setMessage("无法获取所需权限！为确保App正常运行需要授予必要的权限！");
                activity.PermissionsDialog.setCancelText("退出");
                activity.PermissionsDialog.setCancelClickListener(sweetAlertDialog -> activity.finish());
                break;
            case -2:
                activity.LoginDialog.setType(SweetAlertDialog.ERROR_TYPE);
                activity.LoginDialog.setMessage("登录失败！");
                Toast.makeText(activity, msg.getData().getString("Error")).show();
                activity.LoginDialog.setConfirm("确定", BaseSweet.Listener);
                activity.LoginDialog.setCancel("取消", BaseSweet.Listener);
                break;
        }
    }
}
