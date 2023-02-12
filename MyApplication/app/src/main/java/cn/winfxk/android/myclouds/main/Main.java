package cn.winfxk.android.myclouds.main;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.thegrizzlylabs.sardineandroid.DavResource;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.winfxk.android.myclouds.MainActivity;
import cn.winfxk.android.myclouds.MyActivity;
import cn.winfxk.android.myclouds.Pack;
import cn.winfxk.android.myclouds.R;
import cn.winfxk.android.myclouds.ViewData;
import cn.winfxk.android.myclouds.open.FileData;
import cn.winfxk.android.myclouds.tool.MyListBuilder;
import cn.winfxk.android.myclouds.tool.PathSelect;
import cn.winfxk.android.myclouds.tool.Toast;
import cn.winfxk.android.myclouds.tool.Tool;

public class Main extends MyActivity implements RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener<Integer>, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    protected FileAdapter adapter;
    protected TextView FilePathView;
    protected MyHandler handler;
    protected SweetAlertDialog Filereload;
    protected FileData fileData = new FileData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        handler = new MyHandler(this);
        RapidFloatingActionLayout rfaLayout = findViewById(R.id.activity_main_rfal);
        RapidFloatingActionButton rfaBtn = findViewById(R.id.activity_main_rfab);
        RapidFloatingActionContentLabelList rfaContent = new RapidFloatingActionContentLabelList(this);
        ListView listView = findViewById(R.id.listView1);
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
        listView.setAdapter(adapter = new FileAdapter(this));
        FilePathView = findViewById(R.id.textView1);
        rfaContent.setOnRapidFloatingActionContentLabelListListener(this);
        List<RFACLabelItem> items = new ArrayList<>();
        items.add(new RFACLabelItem<Integer>().setLabel("上传文件").setResId(R.drawable.upload).setWrapper(0));
        items.add(new RFACLabelItem<Integer>().setLabel("新建文件").setResId(R.drawable.newfile).setWrapper(1));
        items.add(new RFACLabelItem<Integer>().setLabel("系统设置").setResId(R.drawable.setting).setWrapper(2));
        items.add(new RFACLabelItem<Integer>().setLabel("切换用户").setResId(R.drawable.sign).setWrapper(3));
        items.add(new RFACLabelItem<Integer>().setLabel("刷新列表").setResId(R.drawable.reload).setWrapper(4));
        rfaContent.setItems(items).setIconShadowColor(0xff888888);
        RapidFloatingActionHelper rfabHelper = new RapidFloatingActionHelper(this, rfaLayout, rfaBtn, rfaContent).build();
    }

    @Override
    public void onRFACItemLabelClick(int position, RFACLabelItem item) {
        Intent intent;
        switch (position) {
            case 3:
                intent = new Intent(this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("NoLogin", true);
                intent.putExtra("Data", bundle);
                startActivity(intent);
                fileList();
                break;
            case 4:
                adapter.reload(adapter.Path);
                break;
            case 1:
                PathSelect select = new PathSelect(this, true);
                select.setConfirmListener(list -> {
                    System.out.println(list);
                });
                select.show();
                break;
        }
    }

    @Override
    public void onRFACItemIconClick(int position, RFACLabelItem item) {
        onRFACItemLabelClick(position, item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (!(view.getTag() instanceof ViewData data)) return;
        if (data.Tag instanceof DavResource dav) {
            String path = dav.getPath();
            if (path != null && path.contains("/dav/"))
                path = path.substring(path.indexOf("/dav/") + 5);
            path = path == null ? "" : path;
            if (dav.isDirectory()) {
                adapter.reload(path);
            } else Filemake(view, path, dav);
            return;
        }
        adapter.reload((String) data.Tag);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (!(view.getTag() instanceof ViewData)) return false;
        ViewData data = (ViewData) view.getTag();
        if (data.Tag instanceof DavResource) {
            DavResource dav = (DavResource) data.Tag;
            String path = dav.getPath();
            if (path != null && path.contains("/dav/"))
                path = path.substring(path.indexOf("/dav/") + 5);
            path = path == null ? "" : path;
            return Filemake(view, path, dav);
        }
        return false;
    }

    private boolean Filemake(View view, String path, DavResource dav) {
        MyListBuilder builder = new MyListBuilder(this);
        fileData.cachePath = path;
        int lastIndexOf = path.lastIndexOf("/");
        path.length();
        String FileName = path.substring(lastIndexOf + 1);
        builder.addSheetItem("打开", which -> {
            if (dav.isDirectory()) {
                adapter.reload(path);
                return;
            }
            String cachePath = Pack.CacheFileConfig.getString(path);
            fileData.FileEx = Tool.getExtension(path);
            if (!Pack.CacheFileConfig.containsKey(path) || cachePath == null || !new File(cachePath).exists()) {
                StringBuilder Filename = new StringBuilder(Tool.getDate() + " " + Tool.getTime() + Tool.getRandString());
                fileData.cacheFile = new File(getCacheDir(), Filename + fileData.FileEx);
                while (fileData.cacheFile.exists()) {
                    Filename.append(Tool.getRandString());
                    fileData.cacheFile = new File(getCacheDir(), Filename + fileData.FileEx);
                }
                new Thread(() -> {
                    handler.sendEmptyMessage(1);
                    try {
                        InputStream stream = Pack.sardine.get(Pack.ServerLink + path);
                        FileOutputStream fos = new FileOutputStream(fileData.cacheFile);
                        Log.i("Tag", fileData.cacheFile.toString());
                        int len = 0;
                        byte[] buf = new byte[1024];
                        while ((len = stream.read(buf)) != -1)
                            fos.write(buf, 0, len);
                        stream.close();
                        fos.close();
                        Pack.CacheFileConfig.set(path, fileData.cacheFile.getAbsolutePath());
                        Pack.CacheFileConfig.save();
                        handler.sendEmptyMessage(3);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Message message = new Message();
                        Bundle dataa = new Bundle();
                        dataa.putString("error", "下载失败！！\n" + e.getMessage());
                        message.setData(dataa);
                        message.what = -1;
                        handler.sendMessage(message);
                    } finally {
                        handler.sendEmptyMessage(2);
                    }
                }).start();
            } else {
                fileData.cacheFile = new File(Pack.CacheFileConfig.getString(path));
                handler.sendEmptyMessage(3);
            }
        });
        builder.addSheetItem("重命名", which -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("重命名" + FileName);
            View view1 = View.inflate(this, R.layout.edtext, null);
            EditText editText = view1.findViewById(R.id.editText1);
            editText.setHint("请输入" + FileName + "的新名称");
            editText.setText(FileName);
            dialog.setView(view1);
            dialog.setCancelable(false);
            dialog.setNegativeButton("确定", (dialogInterface, i) -> {
                String newFileName = editText.getText().toString();
                if (newFileName.isEmpty()) {
                    Toast.makeText(this, "请输入" + FileName + "的新名称！").show();
                    return;
                }
                new Thread(() -> {
                    handler.sendEmptyMessage(1);
                    String NewFilePath = path.substring(0, path.lastIndexOf("/"));
                    try {
                        Pack.sardine.move(Pack.ServerLink + path, NewFilePath + newFileName);
                        Message message = new Message();
                        Bundle dataa = new Bundle();
                        dataa.putString("error", "重命名成功！！！");
                        message.setData(dataa);
                        message.what = -1;
                        handler.sendMessage(message);
                        adapter.reload(adapter.Path);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Message message = new Message();
                        Bundle dataa = new Bundle();
                        dataa.putString("error", "重命名失败！！\n" + e.getMessage());
                        message.setData(dataa);
                        message.what = -1;
                        handler.sendMessage(message);
                    } finally {
                        handler.sendEmptyMessage(2);
                    }
                }).start();
            });
            dialog.setPositiveButton("取消", (dialogInterface, i) -> {
            });
            dialog.show();
        });
        builder.addSheetItem("删除", which -> new Thread(() -> {
            handler.sendEmptyMessage(1);
            try {
                Pack.sardine.delete(Pack.ServerLink + path);
                handler.sendEmptyMessage(2);
                Message message = new Message();
                Bundle data1 = new Bundle();
                data1.putString("error", "删除成功！！");
                adapter.reload(adapter.Path);
                message.setData(data1);
                message.what = -1;
                handler.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
                handler.sendEmptyMessage(2);
                Message message = new Message();
                Bundle data1 = new Bundle();
                data1.putString("error", "删除失败！！\n" + e.getMessage());
                message.setData(data1);
                message.what = -1;
                handler.sendMessage(message);
            }
        }).start());
        builder.addSheetItem("下载", which -> {

        });
        builder.show();
        return true;
    }
}
