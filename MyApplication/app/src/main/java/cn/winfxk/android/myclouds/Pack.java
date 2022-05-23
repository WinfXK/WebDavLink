package cn.winfxk.android.myclouds;

import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import java.util.List;

import cn.winfxk.android.myclouds.tool.Config;

public class Pack {
    public static Sardine sardine = new OkHttpSardine();
    public static Config SystemConfig;
    public static List<String> ServerHosts;
    public static String ServerLink;
    public static Config CacheFileConfig;
}
