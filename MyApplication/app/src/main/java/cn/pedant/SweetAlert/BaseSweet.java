package cn.pedant.SweetAlert;

/**
 * @author Winfxk
 * @Createdate 2021/04/24 10:48:35
 */
public class BaseSweet implements OnSweetClickListener {
    public static final BaseSweet Listener = new BaseSweet(true);
    public static final BaseSweet NoClose = new BaseSweet(false);
    private boolean isClose = true;

    public BaseSweet(boolean isClose) {
        this.isClose = isClose;
    }

    public BaseSweet() {
    }

    @Override
    public void onClick(SweetAlertDialog sweetAlertDialog) {
        if (isClose)
            sweetAlertDialog.dismiss();
    }
}
