package cn.winfxk.android.myclouds.tool;

import android.app.Dialog;
import android.content.Context;
import android.os.Environment;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import cn.winfxk.android.myclouds.R;

/**
 * 路径选择器
 */
public class PathSelect extends Dialog implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private static final File BaseFile = Environment.getExternalStorageDirectory();
    private final Context context;
    /**
     * 用于显示文件路径的TextView
     */
    private final TextView PathTextView, TitleTextView;
    /**
     * 两个按钮
     */
    private final Button Confirm, Cancel;
    private OnClickListener ConfirmListener, CancelListener;
    /**
     * 用于输入文件名称的编辑框
     */
    private final EditText editText;
    public static boolean Download = true, Upload = false;
    /**
     * 默认打开的路径
     */
    private File MainPath;
    /**
     * 文件选择模式：true->选择保存路径，false->选择文件列表
     */
    private final boolean Model;
    private final boolean Check;
    private final List<File> list = new ArrayList<>();
    private final Adapter adapter;
    private final String FileName;
    private String Title = "";
    private final RelativeLayout.LayoutParams params;

    public PathSelect(Context context) {
        this(context, false, BaseFile, true, null);
    }


    /**
     * @param context 界面
     * @param model   文件选择模式：true->选择保存路径，false->选择文件列表
     */
    public PathSelect(Context context, boolean model) {
        this(context, model, BaseFile, !model, null);
    }

    /**
     * 创建一个文件选择窗口
     *
     * @param context  界面
     * @param Model    文件选择模式：true->选择保存路径，false->选择文件列表
     * @param file     打开的路径
     * @param Check    是否允许复选
     * @param FileName 如果是保存文件，则文件的默认名称
     */
    public PathSelect(@NonNull Context context, boolean Model, File file, boolean Check, String FileName) {
        super(context, R.style.alert_dialog);
        setContentView(R.layout.path_select);
        this.context = context;
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        PathTextView = findViewById(R.id.textView1);
        ListView listView = findViewById(R.id.listView1);
        Confirm = findViewById(R.id.button1);
        Cancel = findViewById(R.id.button2);
        editText = findViewById(R.id.editText1);
        TitleTextView = findViewById(R.id.textView2);
        params = (RelativeLayout.LayoutParams) TitleTextView.getLayoutParams();
        Confirm.setOnClickListener(this);
        Cancel.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        if (!Model) {
            editText.setEnabled(false);
            editText.setHint("");
        } else if (FileName != null) editText.setText(FileName);
        this.Model = Model;
        this.MainPath = file == null ? BaseFile : file;
        this.Check = Check;
        listView.setAdapter(adapter = new Adapter());
        this.FileName = FileName;
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(context.getString(titleId));
    }

    @Override
    public void setTitle(@Nullable CharSequence title) {
        if (title == null || title.toString().isEmpty()) {
            TitleTextView.setText("");
            Title = null;
        } else {
            this.Title = title.toString();
            TitleTextView.setText(title);
        }
        if (isShowing())
            reloadTitleView();
    }

    @Override
    public void onClick(View view) {
        int ID = view.getId();
        if (Model)
            if (list.size() == 0) {
                list.add(new File(MainPath, editText.getText().toString()));
            } else if (list.get(0).isDirectory()) {
                list.set(0, new File(editText.getText().toString(), FileName));
            } else {
                list.clear();
                list.add(new File(editText.getText().toString()));
            }
        if (ID == R.id.button1) {
            if (ConfirmListener != null) ConfirmListener.onClick(list);
            dismiss();
        } else if (ID == R.id.button2) {
            if (CancelListener != null) CancelListener.onClick(list);
            dismiss();
        } else if (ID == R.id.checkBox1)
            onCheched((CheckBox) view);
    }

    /**
     * 更新标题状态，如果没有标题将会隐藏
     */
    private void reloadTitleView() {
        if (Title == null || Title.isEmpty()) {
            params.height = 0;
            TitleTextView.setText("");
            Title = null;
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            TitleTextView.setText(Title);
        }
        TitleTextView.setLayoutParams(params);
    }

    @Override
    public void show() {
        reloadTitleView();
        super.show();
    }

    /**
     * 返回对话框标题
     * @return
     */
    public String getTitle() {
        return Title;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Data data = (Data) view.getTag();
        if (!data.isItem || data.file.isDirectory()) {
            MainPath = data.file;
            adapter.notifyDataSetChanged();
            return;
        }
        data.box.setChecked(!data.box.isChecked());
        onCheched(data.box);
    }

    /**
     * 选中目录/文件处理方法
     * @param view
     */
    public void onCheched(CheckBox view) {
        Data data = (Data) view.getTag();
        if (view.isChecked()) {
            if (!Check) {
                adapter.notifyDataSetChanged();
                list.clear();
            }
            if (Model)
                editText.setText(data.file.getAbsolutePath());
            list.add(data.file);
        } else list.remove(data.file);
        if (!Model && !isParent(data.file)) {
            StringBuilder s = new StringBuilder();
            for (File file : list)
                s.append((s.length() == 0) ? "" : ";").append(file.getAbsolutePath());
            editText.setText(s.toString());
        }
    }

    /**
     * 检索父目录是否已被选中
     * @param file
     * @return
     */
    private boolean isParent(File file) {
        for (File file1 : list)
            if (file.getAbsolutePath().equals(file1.getAbsolutePath()) || file.getAbsolutePath().contains(file1.getAbsolutePath()))
                return true;
        return false;
    }

    @Deprecated
    @Override
    public void setCancelMessage(@Nullable Message msg) {
        super.setCancelMessage(msg);
    }

    /**
     * 长按也能调用选中事件
     * @param adapterView
     * @param view
     * @param i
     * @param l
     * @return
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (Model) return false;
        onCheched(((Data) view.getTag()).box);
        return true;
    }

    /**
     * 处理文件列表
     */
    private class Adapter extends BaseAdapter {
        private List<File> files;

        private Adapter() {
            reload();
        }

        private void reload() {
            files = new ArrayList<>();
            if (!MainPath.getAbsolutePath().equals(BaseFile.getAbsolutePath()))
                files.add(null);
            if (Model && list.size() == 0) editText.setText(MainPath.getAbsolutePath());
            files.addAll(Arrays.asList(Objects.requireNonNull(MainPath.listFiles())));
            setPathString(MainPath);
        }

        @Override
        public int getCount() {
            return files.size();
        }

        @Override
        public File getItem(int i) {
            return files.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Data data;
            if (view == null) {
                data = new Data();
                view = data.view = View.inflate(context, R.layout.path_select_item, null);
                data.box = view.findViewById(R.id.checkBox1);
                data.icon = view.findViewById(R.id.imageView1);
                data.Title = view.findViewById(R.id.textView1);
                data.Hint = view.findViewById(R.id.textView2);
                view.setTag(data);
            } else data = (Data) view.getTag();
            File file1 = files.get(i);
            if (file1 == null) {
                data.file = MainPath.getParentFile();
                data.icon.setImageResource(R.drawable.back_dir);
                data.Title.setText("返回上级");
                data.Hint.setText("");
                data.icon.setContentDescription("返回上级");
                data.isItem = false;
                data.box.setVisibility(View.INVISIBLE);
                return view;
            }
            data.file = file1;
            data.box.setEnabled(true);
            data.box.setChecked(list.contains(file1));
            for (File file : list)
                if (!data.file.getAbsolutePath().equals(file.getAbsolutePath()) && data.file.getAbsolutePath().contains(file.getAbsolutePath())) {
                    data.box.setEnabled(false);
                    data.box.setChecked(true);
                    break;
                }
            data.icon.setContentDescription(file1.getName());
            data.icon.setImageResource(file1.isDirectory() ? R.drawable.dir_ico : Tool.FileIcon(file1.getName()));
            data.Title.setText(file1.getName());
            data.isItem = true;
            data.box.setTag(data);
            data.box.setOnClickListener(PathSelect.this);
            data.box.setVisibility(!(file1.isDirectory() && Model) ? View.VISIBLE : View.INVISIBLE);
            data.Hint.setText(file1.isDirectory() ? "" : Tool.getSize(file1.length()));
            return view;
        }

        @Override
        public void notifyDataSetChanged() {
            reload();
            super.notifyDataSetChanged();
        }
    }

    public interface OnClickListener {
        void onClick(List<File> list);
    }

    /**
     * 临时封装界面元素
     */
    private static class Data implements Cloneable {
        public CheckBox box;
        public ImageView icon;
        public TextView Title, Hint;
        public View view;
        public File file;
        public boolean isItem;

        @NonNull
        @Override
        protected Data clone() throws CloneNotSupportedException {
            Data data = (Data) super.clone();
            data.file = new File(file.getAbsolutePath());
            return data;
        }
    }

    /**
     * 设置标题显示的路径
     * @param file
     */
    private void setPathString(File file) {
        if (file == null) return;
        String string = file.getAbsolutePath().replace(BaseFile.getAbsolutePath(), "");
        PathTextView.setText(string.isEmpty() ? "/" : string);
    }

    /**
     * 设置取消按钮显示的文本
     * @param Text
     */
    public void setCancelText(String Text) {
        Cancel.setText(Text);
    }

    /**
     * 设置确认按钮显示的文本
     * @param Text
     */
    public void setConfirmText(String Text) {
        Confirm.setText(Text);
    }

    /**
     * 设置取消按钮的文本和事件
     * @param Text
     * @param cancelListener
     */
    public void setCancel(String Text, OnClickListener cancelListener) {
        CancelListener = cancelListener;
        Cancel.setText(Text);
    }

    /**
     * 设置确认按钮的文本和事件
     * @param Text
     * @param confirmListener
     */
    public void setConfirm(String Text, OnClickListener confirmListener) {
        ConfirmListener = confirmListener;
        Confirm.setText(Text);
    }

    /**
     * 设置取消按钮的事件
     * @param cancelListener
     */
    public void setCancelListener(OnClickListener cancelListener) {
        CancelListener = cancelListener;
    }

    /**
     * 设置确认按钮的事件
     * @param confirmListener
     */
    public void setConfirmListener(OnClickListener confirmListener) {
        ConfirmListener = confirmListener;
    }

}
