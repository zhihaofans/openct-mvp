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
    private int week = 1;
    private List<ClassInfo> mClassInfos;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case Constants.GET_CLASS_OK:
                    mClassInfos = (List<ClassInfo>) message.obj;
                    mClassView.updateClassInfos(mClassInfos, week);
                    break;
                case Constants.GET_CLASS_FAIL:
                    mClassView.showOnResultFail();
                    break;
                case Constants.CAPTCHA_IMG_OK:
                    Drawable drawable = BitmapDrawable.createFromPath(GradePresenter.CAPTCHA_FILE_FULL_URI);
                    mClassView.onCAPTCHALoaded(drawable);
                    break;
                case Constants.CAPTCHA_IMG_FAIL:
                    mClassView.showOnCAPTCHAFail();
                    break;
                case Constants.LOGIN_FAIL:
                    mClassView.showOnLoginFail();
                    break;
                case Constants.NETWORK_TIMEOUT:
                    mClassView.showOnNetworkTimeout();
                    break;
                case Constants.NETWORK_ERROR:
                    mClassView.showOnNetworkError();
                    break;
                case Constants.FILE_FETCH_ERROR:
                    mClassView.showOnResultFail();
                    break;
            }
            return false;
        }
    });

    private Loader mCAPTCHALoader = new Loader(RequestType.LOAD_CMS_CAPTCHA, new Loader.CallBack() {
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

    private Loader mClassInfoLoader = new Loader(RequestType.LOAD_CLASS_TABLE, new Loader.CallBack() {
        @Override
        public void onResultOk(Object results) {
            Message message = new Message();
            message.what = Constants.GET_CLASS_OK;
            message.obj = results;
            mHandler.sendMessage(message);
        }

        @Override
        public void onResultFail(int failType) {
            mHandler.sendEmptyMessage(failType);
        }
    });

    ClassPresenter(@NonNull ClassContract.View view, Context context, String path) {
        week = Loader.getCurrentWeek(context);
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
    public void removeClassInfo(ClassInfo info) {
        for (ClassInfo c : mClassInfos) {
            if (c.contains(info)) {
                ClassInfo t = c;
                while (!t.equals(info) && c.hasSubClass()) {
                    t = t.getSubClassInfo();
                }
                t.deactive();
                break;
            }
        }
        mClassView.updateClassInfos(mClassInfos, week);
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
