package cc.metapro.openct.libborrowinfo;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import com.google.common.base.Strings;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cc.metapro.openct.data.BorrowInfo;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.source.RequestType;
import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.utils.Constants;

import static cc.metapro.openct.utils.Constants.CAPTCHA_KEY;

/**
 * Created by jeffrey on 12/1/16.
 */

public class LibBorrowPresenter implements LibBorrowContract.Presenter, Loader.CallBack {

    public final static String BORROW_INFO_FILENAME = "borrow_info.json";
    public static String CAPTCHA_FILE_FULL_URI;
    private static LibBorrowContract.View mLibBorrowView;
    private static List<BorrowInfo> mBorrowInfos;

    private Loader mBorrowLoader, mCaptchaLoader;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case Constants.RESULT_OK:
                    mBorrowInfos = (List<BorrowInfo>) message.obj;
                    mLibBorrowView.showAll(mBorrowInfos);
                    mLibBorrowView.showOnResultOk(mBorrowInfos.size());
                    break;
                case Constants.RESULT_FAIL:
                    mLibBorrowView.showOnResultFail();
                    break;
                case Constants.CAPTCHA_IMG_OK:
                    Drawable drawable = BitmapDrawable.createFromPath(CAPTCHA_FILE_FULL_URI);
                    mLibBorrowView.showOnCAPTCHALoaded(drawable);
                    break;
                case Constants.CAPTCHA_IMG_FAIL:
                    mLibBorrowView.showOnCAPTCHAFail();
                    break;
            }
            return false;
        }
    });

    public LibBorrowPresenter(@NonNull LibBorrowContract.View libBorrowView, @NonNull String cachePath) {
        mLibBorrowView = libBorrowView;
        mBorrowLoader = new Loader(RequestType.LOAD_BORROW_INFO, this);
        mCaptchaLoader = new Loader(RequestType.LOAD_LIB_CAPTCHA, new Loader.CallBack() {
            @Override
            public void onResultOk(Object results) {
                Message message = new Message();
                message.what = Constants.CAPTCHA_IMG_OK;
                mHandler.sendMessage(message);
            }

            @Override
            public void onResultFail() {
                Message message = new Message();
                message.what = Constants.CAPTCHA_IMG_FAIL;
                mHandler.sendMessage(message);
            }
        });
        CAPTCHA_FILE_FULL_URI = cachePath + "/lib_captcha";
        mLibBorrowView.setPresenter(this);
    }

    @Override
    public void loadOnlineBorrowInfos(Context context, String code) {
        if (Strings.isNullOrEmpty(code)) {
            mLibBorrowView.showOnResultFail();
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

    @Override
    public void onResultOk(Object results) {
        Message message = new Message();
        message.what = Constants.RESULT_OK;
        message.obj = results;
        mHandler.sendMessage(message);
    }

    @Override
    public void onResultFail() {
        Message message = new Message();
        message.what = Constants.RESULT_FAIL;
        mHandler.sendMessage(message);
    }

}
