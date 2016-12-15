package cc.metapro.openct.classtable;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.source.RequestType;
import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.gradelist.GradePresenter;
import cc.metapro.openct.utils.Constants;

public class ClassPresenter implements ClassContract.Presenter {

    public final static String CLASS_INFO_FILENAME = "class_info.json";
    private ClassContract.View mClassView;
    private Loader mCAPTCHALoader, mClassInfoLoader;
    private int week = 1;
    private List<ClassInfo> mClassInfos;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case Constants.RESULT_OK:
                    mClassInfos = (List<ClassInfo>) message.obj;
                    mClassView.updateClassInfos(mClassInfos, week);
                    mClassView.showOnResultOk();
                    break;
                case Constants.RESULT_FAIL:
                    mClassView.showOnResultFail();
                    break;
                case Constants.CAPTCHA_IMG_OK:
                    Drawable drawable = BitmapDrawable.createFromPath(GradePresenter.CAPTCHA_FILE_FULL_URI);
                    mClassView.showOnCAPTCHALoaded(drawable);
                    break;
                case Constants.CAPTCHA_IMG_FAIL:
                    mClassView.showOnCAPTCHAFail();
                    break;
            }
            return false;
        }
    });

    public ClassPresenter(@NonNull ClassContract.View view, Context context, String path) {
        week = Loader.getCurrentWeek(context);
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
        mClassInfoLoader = new Loader(RequestType.LOAD_CLASS_TABLE, new Loader.CallBack() {
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
        GradePresenter.CAPTCHA_FILE_FULL_URI = path + "/cms_captcha";
        mClassView = view;
        mClassView.setPresenter(this);
    }

    @Override
    public void loadOnlineClassInfos(Context context, String code) {
        Map<String, String> loginMap = Loader.getCmsStuInfo(context);
        if (loginMap == null) {
            return;
        }
        loginMap.put(Constants.CAPTCHA_KEY, code);
        mClassInfoLoader.loadFromRemote(loginMap);
    }

    @Override
    public void loadLocalClassInfos(Context context) {
        mClassInfoLoader.loadFromLocal(context);
    }

    @Override
    public void loadCAPTCHA() {
        mCAPTCHALoader.loadFromRemote(null);
    }

    @Override
    public void storeClassInfos(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String s = StoreHelper.getJsonText(mClassInfos);
                    StoreHelper.saveTextFile(context, CLASS_INFO_FILENAME, s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void start() {

    }
}
