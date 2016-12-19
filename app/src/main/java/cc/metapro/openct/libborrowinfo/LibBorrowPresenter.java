package cc.metapro.openct.libborrowinfo;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.BorrowInfo;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.source.RequestType;
import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.utils.Constants;

import static cc.metapro.openct.utils.Constants.CAPTCHA_KEY;
import static cc.metapro.openct.utils.Constants.LOGIN_FAIL;

/**
 * Created by jeffrey on 12/1/16.
 */

public class LibBorrowPresenter implements LibBorrowContract.Presenter {

    public final static String BORROW_INFO_FILENAME = "borrow_info.json";
    public static String CAPTCHA_FILE_FULL_URI;
    private static LibBorrowContract.View mLibBorrowView;
    private static List<BorrowInfo> mBorrowInfos;

    private Loader mBorrowLoader = new Loader(RequestType.LOAD_BORROW_INFO, new Loader.CallBack() {
        @Override
        public void onResultOk(@Nullable Object results) {
            Message message = new Message();
            message.what = Constants.LIB_BORROW_OK;
            message.obj = results;
            mHandler.sendMessage(message);
        }

        @Override
        public void onResultFail(int failType) {
            mHandler.sendEmptyMessage(failType);
        }
    });

    private Loader mCaptchaLoader = new Loader(RequestType.LOAD_LIB_CAPTCHA, new Loader.CallBack() {
        @Override
        public void onResultOk(Object results) {
            Message message = new Message();
            message.what = Constants.CAPTCHA_IMG_OK;
            mHandler.sendMessage(message);
        }

        @Override
        public void onResultFail(int failType) {
            mHandler.sendEmptyMessage(failType);
        }
    });

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case Constants.LIB_BORROW_OK:
                    mBorrowInfos = (List<BorrowInfo>) message.obj;
                    mLibBorrowView.showAll(mBorrowInfos);
                    break;
                case Constants.LIB_BORROW_FAIL:
                    mLibBorrowView.showOnLoadBorrowInfoFail();
                    break;
                case Constants.CAPTCHA_IMG_OK:
                    Drawable drawable = BitmapDrawable.createFromPath(CAPTCHA_FILE_FULL_URI);
                    mLibBorrowView.showOnCAPTCHALoaded(drawable);
                    break;
                case Constants.CAPTCHA_IMG_FAIL:
                    mLibBorrowView.showOnLoadCAPTCHAFail();
                    break;
                case Constants.LOGIN_FAIL:
                    mLibBorrowView.showOnLoginFail();
                    break;
                case Constants.NETWORK_TIMEOUT:
                    mLibBorrowView.showOnNetworkTimeout();
                    break;
                case Constants.NETWORK_ERROR:
                    mLibBorrowView.showOnNetworkError();
                    break;
                case Constants.FILE_FETCH_ERROR:
                    mLibBorrowView.showOnLoadBorrowInfoFail();
                    break;
            }
            return false;
        }
    });

    LibBorrowPresenter(@NonNull LibBorrowContract.View libBorrowView, @NonNull String cachePath) {
        mLibBorrowView = libBorrowView;

        CAPTCHA_FILE_FULL_URI = cachePath + "/lib_captcha";
        mLibBorrowView.setPresenter(this);
    }

    @Override
    public void loadOnlineBorrowInfos(Context context, String code) {
        if (Strings.isNullOrEmpty(code)) {
            mLibBorrowView.showOnLoadBorrowInfoFail();
            return;
        }
        Map<String, String> loginMap = Loader.getLibStuInfo(context);
        if (loginMap == null) {
            return;
        }
        loginMap.put(CAPTCHA_KEY, code);
        mBorrowLoader.loadFromRemote(loginMap);
    }

    @Override
    public void loadLocalBorrowInfos(Context context) {
        mBorrowLoader.loadFromLocal(context);
    }

    @Override
    public List<BorrowInfo> getBorrowInfos() {
        return mBorrowInfos;
    }

    @Override
    public void loadCAPTCHA() {
        mCaptchaLoader.loadFromRemote(null);
    }

    @Override
    public void storeBorrowInfos(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String s = StoreHelper.getJsonText(mBorrowInfos);
                    StoreHelper.saveTextFile(context, BORROW_INFO_FILENAME, s);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void start() {

    }
}
