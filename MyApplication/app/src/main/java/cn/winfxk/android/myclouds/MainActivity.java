package cn.winfxk.android.myclouds;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
            PermissionsDialog.setMessage("程序需要一些权限....");
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
                    Toast.makeText(this, "无法设置服务器地址！请手动设置！").show();
                } else
                    Pack.ServerLink = Pack.ServerHosts.get(0);
                if (Pack.ServerLink == null || Pack.ServerLink.isEmpty())
                    Toast.makeText(this, "无法设置服务器地址！请手动设置！").show();
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
        Pack.ExternalStorage = new File(Environment.getExternalStorageDirectory(), "Winfxk Cloud");
        if (!Pack.ExternalStorage.exists()) Pack.ExternalStorage.mkdirs();
        Pack.ExternalCache = new File(Pack.ExternalStorage, "Cache");
        if (!Pack.ExternalCache.exists()) Pack.ExternalCache.mkdirs();
    }

    protected void LoginThread() {
        String User = Email.getText().toString();
        String Pass = Passwd.getText().toString();
        Pack.sardine.setCredentials(User, Pass);
        try {
            List<DavResource> resources = Pack.sardine.list(Pack.ServerLink);
            if (resources == null) throw new RuntimeException("登陆验证失败！");
        } catch (Exception e) {
            e.printStackTrace();
            Message msg = new Message();
            msg.what = -2;
            Bundle data = new Bundle();
            data.putString("Error", "登录失败！请尝试更换用户名、密码或服务器地址后重试！\n" + e.getMessage());
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
            Toast.makeText(this, "请输入邮件地址！").show();
            return false;
        }
        String Pass = Passwd.getText().toString();
        if (Pass.isEmpty()) {
            Toast.makeText(this, "请输入密码！").show();
            return false;
        }
        Login = true;
        LoginDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        LoginDialog.setMessage("正在尝试登录...");
        LoginDialog.setClickConceal(false);
        LoginDialog.setConfirm("确定", BaseSweet.NoClose);
        LoginDialog.setCancel("取消", BaseSweet.NoClose);
        LoginDialog.show();
        new MainThread(this, 1).start();
        return true;
    }

    public static void Selectserver(Activity activity) {
        MyListBuilder builder = new MyListBuilder(activity);
        builder.setTitle("服务器地址编辑器");
        builder.addSheetItem("添加", which -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
            View view = View.inflate(activity, R.layout.edtext, null);
            EditText editText = view.findViewById(R.id.editText1);
            dialog.setCancelable(false);
            dialog.setTitle("添加地址");
            dialog.setView(view);
            dialog.setNegativeButton("确定", (dialogInterface, i) -> {
                String Link = editText.getText().toString();
                if (Link.isEmpty()) {
                    Toast.makeText(activity, "您还没有输入服务器地址！").show();
                    return;
                }
                Pack.ServerHosts.add(Link);
                Pack.SystemConfig.set("ServerHosts", Pack.ServerHosts);
                Pack.SystemConfig.save();
                Toast.makeText(activity, "添加服务器备选地址" + Link).show();
            });
            dialog.setPositiveButton("取消", null);
            dialog.show();
        });
        builder.addSheetItem("选择", which -> {
            MyListBuilder builder1 = new MyListBuilder(activity);
            builder1.setTitle("请选择想要使用的地址");
            for (String Link : Pack.ServerHosts)
                if (Link != null && !Link.isEmpty())
                    builder1.addSheetItem(Link, which1 -> {
                        Pack.SystemConfig.set("ServerLink", Pack.ServerLink = Link);
                        Pack.SystemConfig.save();
                        Toast.makeText(activity, "将服务器地址设置为" + Link).show();
                    });
            builder1.show();
        });
        builder.addSheetItem("删除", which -> {
            if (Pack.ServerHosts.size() <= 1) {
                Toast.makeText(activity, "程序运行至少需要一个服务器地址！").show();
                return;
            }
            MyListBuilder builder1 = new MyListBuilder(activity);
            builder1.setTitle("请选择想要删除的地址");
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
                        Toast.makeText(activity, "删除服务器地址" + Link).show();
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
