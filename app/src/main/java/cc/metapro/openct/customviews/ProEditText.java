package cc.metapro.openct.customviews;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by jeffrey on 11/29/16.
 */

public class ProEditText extends AppCompatEditText {

    protected Drawable mRightDrawable;
    private RightPicOnclickListener rightPicOnclickListener;

    public ProEditText(Context context) {
        super(context);
        init(context);
    }

    public ProEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        mRightDrawable = getCompoundDrawables()[2];

        if (mRightDrawable == null) {
            return;
        }

        mRightDrawable.setBounds(0, 0, mRightDrawable.getIntrinsicWidth(), mRightDrawable.getIntrinsicHeight());
    }

    public void setRightPicOnclickListener(RightPicOnclickListener rightPicOnclickListener) {
        this.rightPicOnclickListener = rightPicOnclickListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawables()[2] != null) {
                boolean touchable = event.getX() > (getWidth() - getTotalPaddingRight())
                        && (event.getX() < ((getWidth() - getPaddingRight())));
                if (touchable) {

                    setFocusableInTouchMode(false);
                    setFocusable(false);

                    if (rightPicOnclickListener != null) {
                        rightPicOnclickListener.rightPicClick();
                    }
                } else {
                    setFocusableInTouchMode(true);
                    setFocusable(true);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public interface RightPicOnclickListener {
        void rightPicClick();
    }
}
