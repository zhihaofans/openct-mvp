package cc.metapro.openct.classtable;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.List;

import cc.metapro.openct.BasePresenter;
import cc.metapro.openct.BaseView;
import cc.metapro.openct.data.ClassInfo;

interface ClassContract {
    interface View extends BaseView<Presenter> {

        void updateClassInfos(List<ClassInfo> infos, int week);

        void onCAPTCHALoaded(Drawable captcha);

        void showOnCAPTCHAFail();

        void showOnResultFail();

        void showOnLoginFail();

        void showOnNetworkError();

        void showOnNetworkTimeout();

    }

    interface Presenter extends BasePresenter {

        void loadOnlineClassInfos(Context context, String code);

        void loadLocalClassInfos(Context context);

        void removeClassInfo(ClassInfo info);

        void loadCAPTCHA();

        void storeClassInfos(Context context);
    }
}
