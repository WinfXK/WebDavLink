package cn.winfxk.android.myclouds;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import com.thegrizzlylabs.sardineandroid.DavResource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.BaseSweet;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.winfxk.android.myclouds.tool.Config;
import cn.winfxk.android.myclouds.tool.MyListBuilder;
import cn.winfxk.android.myclouds.tool.Toast;


public class MainActivity extends MyActivity {
    private final static String[] Permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET};
    protected SweetAlertDialog PermissionsDialog, LoginDialog;
    protected MainHandle handle;
    protected transient boolean hashPermissions = true;
    private EditText Email, Passwd;
    public transient boolean Login = false;
    public boolean ShowPasswd = false;
    public ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        handle = new MainHandle(this);
        Email = findViewById(R.id.editText1);
        Passwd = findViewById(R.id.editText2);
        findViewById(R.id.imageButton2).setOnClickListener(view -> Selectserver(this));
        findViewById(R.id.imageButton3).setOnClickListener(view -> startWEB(this, Pack.ServerLink.substring(0, Pack.ServerLink.length() - 5)));
        findViewById(R.id.button1).setOnClickListener(view -> Login());
        (imageView = findViewById(R.id.imageView3)).setOnClickListener(view -> {
            if (ShowPasswd) {
                ShowPasswd = false;
                imageView.setImageResource(R.drawable.pass_noshow);
                Passwd.setInputType(129);
            } else {
                ShowPasswd = true;
                imageView.setImageResource(R.drawable.pass_show);
                Passwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
        });
        Passwd.setOnEditorActionListener((v, actionId, event) -> (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) && Login());
        if (!hashPermissions()) {
            PermissionsDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            PermissionsDialog.setMessage("????????????????????????....");
            PermissionsDialog.setClickConceal(false);
            PermissionsDialog.setCancelClickListener(BaseSweet.NoClose);
            PermissionsDialog.show();
            new MainThread(this, 0).start();
        } else init();
    }

    protected void init() {
        hashPermissions = false;
        if (Pack.SystemConfig == null)
            Pack.SystemConfig = new Config(new File(getFilesDir(), "SystemConfig.yml"));
        if (Pack.CacheFileConfig == null)
            Pack.CacheFileConfig = new Config(new File(getCacheDir(), "Cachelist.yml"));
        if (Pack.ServerHosts == null) {
            Pack.ServerHosts = Pack.SystemConfig.getList("ServerHosts", new ArrayList<>());
            if (Pack.ServerHosts == null || Pack.ServerHosts.isEmpty()) {
                Pack.ServerHosts = new ArrayList<>();
                Pack.ServerHosts.add("http://Winfxk.cn:19130/dav/");
                Pack.ServerHosts.add("http://192.168.1.4:19130/dav/");
                Pack.SystemConfig.set("ServerHosts", Pack.ServerHosts);
                Pack.SystemConfig.save();
            }
        }
        if (Pack.ServerLink == null) {
            Pack.ServerLink = Pack.SystemConfig.getString("ServerLink");
            if (Pack.ServerLink == null || Pack.ServerLink.isEmpty()) {
                if (Pack.ServerHosts.size() <= 0) {
                    Toast.makeText(this, "????????????????????????????????????????????????").show();
                } else
                    Pack.ServerLink = Pack.ServerHosts.get(0);
                if (Pack.ServerLink == null || Pack.ServerLink.isEmpty())
                    Toast.makeText(this, "????????????????????????????????????????????????").show();
            }
            if (Pack.ServerLink != null && !Pack.ServerLink.isEmpty()) {
                Pack.SystemConfig.set("ServerLink", Pack.ServerLink);
                Pack.SystemConfig.save();
            }
        }
        if (Pack.SystemConfig.containsKey("User") && Pack.SystemConfig.containsKey("Passwd")) {
            String User = Pack.SystemConfig.getString("User");
            String Passwd = Pack.SystemConfig.getString("Passwd");
            if (User != null && !User.isEmpty() && Passwd != null && !Passwd.isEmpty()) {
                Email.setText(User);
                this.Passwd.setText(Passwd);
                Intent intent = getIntent();
                Bundle build = null;
                if (intent == null || (build = intent.getBundleExtra("Data")) == null || !build.getBoolean("NoLogin"))
                    Login();
            }
        }
    }

    protected void LoginThread() {
        String User = Email.getText().toString();
        String Pass = Passwd.getText().toString();
        Pack.sardine.setCredentials(User, Pass);
        try {
            List<DavResource> resources = Pack.sardine.list(Pack.ServerLink);
        } catch (Exception e) {
            e.printStackTrace();
            Message msg = new Message();
            msg.what = -2;
            Bundle data = new Bundle();
            data.putString("Error", "??????????????????????????????????????????????????????????????????????????????\n" + e.getMessage());
            msg.setData(data);
            handle.sendMessage(msg);
            return;
        }
        Pack.SystemConfig.set("User", User);
        Pack.SystemConfig.set("Passwd", Pass);
        Pack.SystemConfig.save();
        handle.sendEmptyMessage(1);
        try {
            Thread.sleep(800);
        } catch (
                InterruptedException e) {
            e.printStackTrace();
        }
        handle.sendEmptyMessage(2);
    }

    private boolean Login() {
        String User = Email.getText().toString();
        if (User.isEmpty()) {
            Toast.makeText(this, "????????????????????????").show();
            return false;
        }
        String Pass = Passwd.getText().toString();
        if (Pass.isEmpty()) {
            Toast.makeText(this, "??????????????????").show();
            return false;
        }
        Login = true;
        LoginDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        LoginDialog.setMessage("??????????????????...");
        LoginDialog.setClickConceal(false);
        LoginDialog.setConfirm("??????", BaseSweet.NoClose);
        LoginDialog.setCancel("??????", BaseSweet.NoClose);
        LoginDialog.show();
        new MainThread(this, 1).start();
        return true;
    }

    public static void Selectserver(Activity activity) {
        MyListBuilder builder = new MyListBuilder(activity);
        builder.setTitle("????????????????????????");
        builder.addSheetItem("??????", which -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
            View view = View.inflate(activity, R.layout.edtext, null);
            EditText editText = view.findViewById(R.id.editText1);
            dialog.setCancelable(false);
            dialog.setTitle("????????????");
            dialog.setView(view);
            dialog.setNegativeButton("??????", (dialogInterface, i) -> {
                String Link = editText.getText().toString();
                if (Link.isEmpty()) {
                    Toast.makeText(activity, "????????????????????????????????????").show();
                    return;
                }
                Pack.ServerHosts.add(Link);
                Pack.SystemConfig.set("ServerHosts", Pack.ServerHosts);
                Pack.SystemConfig.save();
                Toast.makeText(activity, "???????????????????????????" + Link).show();
            });
            dialog.setPositiveButton("??????", null);
            dialog.show();
        });
        builder.addSheetItem("??????", which -> {
            MyListBuilder builder1 = new MyListBuilder(activity);
            builder1.setTitle("??????????????????????????????");
            for (String Link : Pack.ServerHosts)
                if (Link != null && !Link.isEmpty())
                    builder1.addSheetItem(Link, which1 -> {
                        Pack.SystemConfig.set("ServerLink", Pack.ServerLink = Link);
                        Pack.SystemConfig.save();
                        Toast.makeText(activity, "???????????????????????????" + Link).show();
                    });
            builder1.show();
        });
        builder.addSheetItem("??????", which -> {
            if (Pack.ServerHosts.size() <= 1) {
                Toast.makeText(activity, "????????????????????????????????????????????????").show();
                return;
            }
            MyListBuilder builder1 = new MyListBuilder(activity);
            builder1.setTitle("??????????????????????????????");
            for (String Link : Pack.ServerHosts)
                if (Link != null && !Link.isEmpty())
                    builder1.addSheetItem(Link, which1 -> {
                        Pack.ServerHosts.remove(Link);
                        if (Link.equals(Pack.ServerLink)) {
                            Pack.ServerLink = Pack.ServerHosts.get(0);
                            Pack.SystemConfig.set("ServerLink", Pack.ServerLink);
                        }
                        Pack.SystemConfig.set("ServerHosts", Pack.ServerHosts);
                        Pack.SystemConfig.save();
                        Toast.makeText(activity, "?????????????????????" + Link).show();
                    });
            builder1.show();
        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        if (hashPermissions || Login) return;
        super.onBackPressed();
    }

    private boolean hashPermissions() {
        ArrayList<String> pmList = new ArrayList<>();
        for (String permission : Permissions)
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                pmList.add(permission);
        return pmList.size() <= 0;
    }

    protected void havePermissions() {
        while (!hashPermissions()) {
            try {
                Thread.sleep(1000);
                requestPermissions(Permissions, 321);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        handle.sendEmptyMessage(hashPermissions() ? 0 : -1);
    }

    public static void startWEB(Context context, String Link) {
        Uri uri = Uri.parse(Link);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }
}
