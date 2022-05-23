package cn.winfxk.android.myclouds.main;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;

import com.thegrizzlylabs.sardineandroid.DavResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.winfxk.android.myclouds.Pack;
import cn.winfxk.android.myclouds.R;
import cn.winfxk.android.myclouds.ViewData;
import cn.winfxk.android.myclouds.tool.Toast;
import cn.winfxk.android.myclouds.tool.Tool;

public class FileAdapter extends BaseAdapter {
    private Main activity;
    protected transient boolean host = false;
    public transient List<DavResource> list = new ArrayList<>();
    public String Path;

    protected FileAdapter(Main main) {
        activity = main;
        reload("");
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewData data;
        if (view == null) {
            view = View.inflate(activity, R.layout.main_filelist_item, null);
            data = new ViewData();
            data.imageView1 = view.findViewById(R.id.imageView1);
            data.textView1 = view.findViewById(R.id.textView1);
            data.textView2 = view.findViewById(R.id.textView2);
            data.checkBox1 = view.findViewById(R.id.checkBox1);
            view.setTag(data);
        } else data = (ViewData) view.getTag();
        DavResource dav = list.get(i);
        if (list.size() <= 0) {
            data.textView1.setText("目录为空！");
            data.textView2.setText("");
            data.imageView1.setImageResource(R.drawable.dir_ico);
            data.imageView1.setContentDescription("目录为空！");
            data.Tag = Path;
            return view;
        }
        if (Path != null && !Path.isEmpty() && i == 0) {
            data.textView1.setText("返回上级");
            data.textView2.setText("");
            data.imageView1.setContentDescription("返回上级！");
            data.imageView1.setImageResource(R.drawable.back_dir);
            String path = dav.getPath();
            if (path != null && path.contains("/dav/")) {
                int index = path.indexOf("/dav/") + 5;
                path = path.substring(Math.max(index, 0));
                if (path.split("/").length > 1) {
                    path = new File(path).getParent();
                } else path = "";
            }
            data.Tag = path;
            return view;
        }
        data.imageView1.setContentDescription(dav.getDisplayName());
        data.imageView1.setImageResource(dav.isDirectory() ? R.drawable.dir_ico : Tool.FileIcon(dav.getDisplayName()));
        data.textView1.setText(dav.getDisplayName());
        data.textView2.setText(dav.isDirectory() ? "" : Tool.getSize(dav.getContentLength()));
        data.Tag = dav;
        return view;
    }

    /**
     * 更新文件列表
     *
     * @param Path
     */
    public synchronized void reload(@NonNull String Path) {
        if (host) {
            Toast.makeText(activity, "正在加载请稍后！").show();
            return;
        }
        if (!Path.isEmpty() && !Path.endsWith("/"))
            Path = Path + "/";
        activity.handler.sendEmptyMessage(1);
        host = true;
        String finalPath = Path;
        new Thread(() -> {
            list.clear();
            try {
                list.addAll(Pack.sardine.list(Pack.ServerLink + finalPath));
                if (finalPath.isEmpty() || finalPath.equals("/"))
                    list.remove(0);
                this.Path = finalPath;
                activity.handler.sendEmptyMessage(0);
            } catch (IOException e) {
                e.printStackTrace();
                Message message = new Message();
                Bundle data = new Bundle();
                data.putString("error", "无法更新文件列表！\n" + e.getMessage());
                message.setData(data);
                message.what = -1;
                activity.handler.sendMessage(message);
            } finally {
                activity.handler.sendEmptyMessage(2);
                host = false;
            }
        }).start();
    }
}
