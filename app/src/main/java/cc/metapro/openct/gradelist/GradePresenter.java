package cc.metapro.openct.gradelist;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.source.RequestType;
import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.utils.Constants;

/**
 * Created by jeffrey on 16/12/2.
 */

public class GradePresenter implements GradeContract.Presenter {

    public final static String GRADE_INFO_FILENAME = "grade_info.json";
    public static String CAPTCHA_FILE_FULL_URI;
    private GradeContract.View mGradeFragment;
    private Loader mGradeLoader, mCAPTCHALoader;
    private List<GradeInfo> mGradeInfos;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case Constants.RESULT_OK:
                    mGradeInfos = (List<GradeInfo>) message.obj;
                    mGradeFragment.showAll(mGradeInfos);
                    mGradeFragment.showOnResultOk();
                    break;
                case Constants.RESULT_FAIL:
                    mGradeFragment.showOnResultFail();
                    break;
                case Constants.CAPTCHA_IMG_OK:
                    Drawable drawable = BitmapDrawable.createFromPath(CAPTCHA_FILE_FULL_URI);
                    mGradeFragment.showOnCAPTCHALoaded(drawable);
                    break;
                case Constants.CAPTCHA_IMG_FAIL:
                    mGradeFragment.showOnCAPTCHAFail();
                    break;
            }
            return false;
        }
    });

    public GradePresenter(GradeContract.View view, String path) {
        mGradeFragment = view;
        mGradeFragment.setPresenter(this);
        mGradeLoader = new Loader(RequestType.LOAD_GRADE_TABLE, new Loader.CallBack() {
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
        });
        mCAPTCHALoader = new Loader(RequestType.LOAD_CMS_CAPTCHA, new Loader.CallBack() {
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
        CAPTCHA_FILE_FULL_URI = path + "/cms_captcha";
    }

    @Override
    public void loadOnlineGradeInfos(Context context, String code) {
        Map<String, String> loginMap = Loader.getCmsStuInfo(context);
        if (loginMap == null) {
            return;
        }
        loginMap.put(Constants.CAPTCHA_KEY, code);
        mGradeLoader.loadFromRemote(loginMap);
    }

    @Override
    public void loadLocalGradeInfos(Context context) {
        mGradeLoader.loadFromLocal(context);
    }

    @Override
    public void loadCAPTCHA() {
        mCAPTCHALoader.loadFromRemote(null);
    }

    @Override
    public void storeGradeInfos(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String s = StoreHelper.getJsonText(mGradeInfos);
                    StoreHelper.saveTextFile(context, GRADE_INFO_FILENAME, s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void clearGradeInfos() {
        mGradeInfos = new ArrayList<>(0);
    }

    @Override
    public void start() {

    }
}
