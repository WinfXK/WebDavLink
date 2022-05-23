package cn.winfxk.android.myclouds;

public class MainThread extends Thread {
    private MainActivity activity;
    private int Key;

    protected MainThread(MainActivity activity, int Key) {
        this.activity = activity;
        this.Key = Key;
    }

    @Override
    public void run() {
        switch (Key) {
            case 0:
                activity.havePermissions();
                break;
            case 1:
                activity.LoginThread();
                break;
        }
    }
}
