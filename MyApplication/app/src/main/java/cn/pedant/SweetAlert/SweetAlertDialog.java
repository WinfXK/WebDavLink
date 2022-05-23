package cn.pedant.SweetAlert;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.List;

import cn.winfxk.android.myclouds.R;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class SweetAlertDialog extends Dialog implements View.OnClickListener {
    private Activity context;
    private View mDialogView;
    private AnimationSet mModalInAnim;
    private AnimationSet mModalOutAnim;
    private Animation mOverlayOutAnim;
    private Animation mErrorInAnim;
    private AnimationSet mErrorXInAnim;
    private AnimationSet mSuccessLayoutAnimSet;
    private Animation mSuccessBowAnim;
    private TextView mTitleTextView;
    private TextView mContentTextView;
    private String mTitleText;
    private String mContentText;
    private boolean mShowCancel;
    private boolean mShowContent;
    private String mCancelText;
    private String mConfirmText;
    private int mAlertType;
    private FrameLayout mErrorFrame;
    private FrameLayout mSuccessFrame;
    private FrameLayout mProgressFrame;
    private SuccessTickView mSuccessTick;
    private ImageView mErrorX;
    private View mSuccessLeftMask;
    private View mSuccessRightMask;
    private Drawable mCustomImgDrawable;
    private ImageView mCustomImage;
    private Button mConfirmButton;
    private Button mCancelButton;
    private ProgressHelper mProgressHelper;
    private FrameLayout mWarningFrame;
    private OnSweetClickListener mCancelClickListener;
    private OnSweetClickListener mConfirmClickListener;
    private boolean mCloseFromCancel;
    private boolean ClickConceal = false;
    public static final int NORMAL_TYPE = 0;
    public static final BaseSweet BaseSweet = cn.pedant.SweetAlert.BaseSweet.Listener;
    /**
     * 错误
     */
    public static final int ERROR_TYPE = 1;
    /**
     * 成功
     */
    public static final int SUCCESS_TYPE = 2;
    /**
     * 警告
     */
    public static final int WARNING_TYPE = 3;
    /**
     * 自定义图标
     */
    public static final int CUSTOM_IMAGE_TYPE = 4;
    public static final int PROGRESS_TYPE = 5;

    public SweetAlertDialog(Activity context) {
        this(context, NORMAL_TYPE);
        this.context = context;
    }

    @Override
    public void show() {
        if (context != null && !context.isDestroyed())
            super.show();
    }

    @Override
    public void dismiss() {
        if (context != null && !context.isDestroyed())
            super.dismiss();
    }

    public SweetAlertDialog(Activity context, int alertType) {
        super(context, R.style.alert_dialog);
        this.context = context;
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        mProgressHelper = new ProgressHelper(context);
        mAlertType = alertType;
        mErrorInAnim = OptAnimationLoader.loadAnimation(getContext(), R.anim.error_frame_in);
        mErrorXInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.error_x_in);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            List<Animation> childAnims = mErrorXInAnim.getAnimations();
            int idx = 0;
            for (; idx < childAnims.size(); idx++)
                if (childAnims.get(idx) instanceof AlphaAnimation)
                    break;
            if (idx < childAnims.size())
                childAnims.remove(idx);
        }
        mSuccessBowAnim = OptAnimationLoader.loadAnimation(getContext(), R.anim.success_bow_roate);
        mSuccessLayoutAnimSet = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(),
                R.anim.success_mask_layout);
        mModalInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.modal_in);
        mModalOutAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.modal_out);
        mModalOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mDialogView.setVisibility(View.GONE);
                mDialogView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCloseFromCancel) {
                            SweetAlertDialog.super.cancel();
                        } else
                            SweetAlertDialog.super.dismiss();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        // dialog overlay fade out
        mOverlayOutAnim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                WindowManager.LayoutParams wlp = getWindow().getAttributes();
                wlp.alpha = 1 - interpolatedTime;
                getWindow().setAttributes(wlp);
            }
        };
        mOverlayOutAnim.setDuration(120);
    }

    public static SweetAlertDialog make(Activity activity, String Message, int type) {
        SweetAlertDialog dialog = new SweetAlertDialog(activity, type);
        dialog.setMessage(Message);
        dialog.setConfirmText("OK");
        return dialog;
    }

    public static SweetAlertDialog make(Activity activity, String Message, String Button, int type) {
        SweetAlertDialog dialog = new SweetAlertDialog(activity, type);
        dialog.setMessage(Message);
        dialog.setConfirmText(Button);
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_dialog);
        mDialogView = getWindow().getDecorView().findViewById(android.R.id.content);
        mTitleTextView = (TextView) findViewById(R.id.title_text);
        mContentTextView = (TextView) findViewById(R.id.content_text);
        mErrorFrame = (FrameLayout) findViewById(R.id.error_frame);
        mErrorX = (ImageView) mErrorFrame.findViewById(R.id.error_x);
        mSuccessFrame = (FrameLayout) findViewById(R.id.success_frame);
        mProgressFrame = (FrameLayout) findViewById(R.id.progress_dialog);
        mSuccessTick = (SuccessTickView) mSuccessFrame.findViewById(R.id.success_tick);
        mSuccessLeftMask = mSuccessFrame.findViewById(R.id.mask_left);
        mSuccessRightMask = mSuccessFrame.findViewById(R.id.mask_right);
        mCustomImage = (ImageView) findViewById(R.id.custom_image);
        mWarningFrame = (FrameLayout) findViewById(R.id.warning_frame);
        mConfirmButton = (Button) findViewById(R.id.confirm_button);
        mCancelButton = (Button) findViewById(R.id.cancel_button);
        mProgressHelper.setProgressWheel((ProgressWheel) findViewById(R.id.progressWheel));
        mConfirmButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        setMessage(mTitleText);
        setContentText(mContentText);
        setCancelText(mCancelText);
        setConfirmText(mConfirmText);
        setType(mAlertType, true);
    }

    private void restore() {
        mCustomImage.setVisibility(View.GONE);
        mErrorFrame.setVisibility(View.GONE);
        mSuccessFrame.setVisibility(View.GONE);
        mWarningFrame.setVisibility(View.GONE);
        mProgressFrame.setVisibility(View.GONE);
        mConfirmButton.setVisibility(View.VISIBLE);
        mConfirmButton.setBackgroundResource(R.drawable.blue_button_background);
        mErrorFrame.clearAnimation();
        mErrorX.clearAnimation();
        mSuccessTick.clearAnimation();
        mSuccessLeftMask.clearAnimation();
        mSuccessRightMask.clearAnimation();
    }

    private void playAnimation() {
        if (mAlertType == ERROR_TYPE) {
            mErrorFrame.startAnimation(mErrorInAnim);
            mErrorX.startAnimation(mErrorXInAnim);
        } else if (mAlertType == SUCCESS_TYPE) {
            mSuccessTick.startTickAnim();
            mSuccessRightMask.startAnimation(mSuccessBowAnim);
        }
    }

    private void setType(int alertType, boolean fromCreate) {
        mAlertType = alertType;
        if (mDialogView != null) {
            if (!fromCreate) {
                restore();
            }
            switch (mAlertType) {
                case ERROR_TYPE:
                    mErrorFrame.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS_TYPE:
                    mSuccessFrame.setVisibility(View.VISIBLE);
                    mSuccessLeftMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(0));
                    mSuccessRightMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(1));
                    break;
                case WARNING_TYPE:
                    mConfirmButton.setBackgroundResource(R.drawable.red_button_background);
                    mWarningFrame.setVisibility(View.VISIBLE);
                    break;
                case CUSTOM_IMAGE_TYPE:
                    setCustomImage(mCustomImgDrawable);
                    break;
                case PROGRESS_TYPE:
                    mProgressFrame.setVisibility(View.VISIBLE);
                    mConfirmButton.setVisibility(View.GONE);
                    break;
            }
            if (!fromCreate)
                playAnimation();
        }
    }

    public int getAlerType() {
        return mAlertType;
    }

    public void setType(int alertType) {
        setType(alertType, false);
    }

    public String getTitleText() {
        return mTitleText;
    }

    public SweetAlertDialog setMessage(String text) {
        mTitleText = text;
        if (mTitleTextView != null && mTitleText != null)
            mTitleTextView.setText(mTitleText);
        return this;
    }

    public SweetAlertDialog setCustomImage(Drawable drawable) {
        mCustomImgDrawable = drawable;
        if (mCustomImage != null && mCustomImgDrawable != null) {
            mCustomImage.setVisibility(View.VISIBLE);
            mCustomImage.setImageDrawable(mCustomImgDrawable);
        }
        return this;
    }

    public SweetAlertDialog setCustomImage(int resourceId) {
        if (Build.VERSION.SDK_INT >= 23)
            return setCustomImage(getContext().getResources().getDrawable(resourceId, getContext().getTheme()));
        return setCustomImage(getContext().getResources().getDrawable(resourceId));
    }

    public String getContentText() {
        return mContentText;
    }

    public SweetAlertDialog setContentText(String text) {
        mContentText = text;
        if (mContentTextView != null && mContentText != null) {
            showContentText(true);
            mContentTextView.setText(mContentText);
        }
        return this;
    }

    public boolean isShowCancelButton() {
        return mShowCancel;
    }

    public SweetAlertDialog showCancelButton(boolean isShow) {
        mShowCancel = isShow;
        if (mCancelButton != null)
            mCancelButton.setVisibility(mShowCancel ? View.VISIBLE : View.GONE);
        return this;
    }

    public boolean isShowContentText() {
        return mShowContent;
    }

    public SweetAlertDialog showContentText(boolean isShow) {
        mShowContent = isShow;
        if (mContentTextView != null)
            mContentTextView.setVisibility(mShowContent ? View.VISIBLE : View.GONE);
        return this;
    }

    public String getCancelText() {
        return mCancelText;
    }

    public SweetAlertDialog setCancel(String text, OnSweetClickListener listener) {
        mCancelText = text;
        if (mCancelButton != null && mCancelText != null) {
            showCancelButton(true);
            mCancelButton.setText(mCancelText);
        }
        setCancelClickListener(listener);
        return this;
    }

    public SweetAlertDialog setCancelText(String text) {
        mCancelText = text;
        if (mCancelButton != null && mCancelText != null) {
            showCancelButton(true);
            mCancelButton.setText(mCancelText);
        }
        return this;
    }

    public String getConfirmText() {
        return mConfirmText;
    }

    public SweetAlertDialog setConfirmText(String text) {
        mConfirmText = text;
        if (mConfirmButton != null && mConfirmText != null)
            mConfirmButton.setText(mConfirmText);
        return this;
    }

    public SweetAlertDialog setConfirm(String text, OnSweetClickListener listener) {
        mConfirmText = text;
        if (mConfirmButton != null && mConfirmText != null)
            mConfirmButton.setText(mConfirmText);
        setConfirmClickListener(listener);
        return this;
    }

    public SweetAlertDialog setCancelClickListener(OnSweetClickListener listener) {
        mCancelClickListener = listener;
        return this;
    }

    public SweetAlertDialog setConfirmClickListener(OnSweetClickListener listener) {
        mConfirmClickListener = listener;
        return this;
    }

    @Override
    protected void onStart() {
        mDialogView.startAnimation(mModalInAnim);
        playAnimation();
    }

    @Override
    public void cancel() {
        dismissWithAnimation(true);
    }

    public void dismissWithAnimation() {
        dismissWithAnimation(false);
    }

    private void dismissWithAnimation(boolean fromCancel) {
        mCloseFromCancel = fromCancel;
        mConfirmButton.startAnimation(mOverlayOutAnim);
        mDialogView.startAnimation(mModalOutAnim);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancel_button) {
            if (mCancelClickListener != null)
                mCancelClickListener.onClick(SweetAlertDialog.this);
            if (ClickConceal)
                dismissWithAnimation();
        } else if (v.getId() == R.id.confirm_button) {
            if (mConfirmClickListener != null)
                mConfirmClickListener.onClick(SweetAlertDialog.this);
            if (ClickConceal)
                dismissWithAnimation();
        }
    }

    public boolean getClickConceal() {
        return ClickConceal;
    }

    /**
     * 设置是否点击后窗口隐藏
     *
     * @param clickConceal
     */
    public SweetAlertDialog setClickConceal(boolean clickConceal) {
        ClickConceal = clickConceal;
        return this;
    }

    public ProgressHelper getProgressHelper() {
        return mProgressHelper;
    }
}