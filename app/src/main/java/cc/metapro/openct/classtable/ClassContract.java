package cc.metapro.openct.classtable;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.List;

import cc.metapro.openct.BasePresenter;
import cc.metapro.openct.BaseView;
import cc.metapro.openct.data.ClassInfo;

/**
 * Created by jeffrey on 16/12/3.
 */

interface ClassContract {
    interface View extends BaseView<Presenter> {

        void updateClassInfos(List<ClassInfo> infos, int week);

        void showCurrentSem(List<ClassInfo> infos);

        void showToday(List<ClassInfo> infos, int week);

        void showSelectedWeek(List<ClassInfo> infos, int week);

        void showOnCAPTCHALoaded(Drawable captcha);

        void showOnCAPTCHAFail();

        void showOnResultOk();

        void showOnResultFail();

        void showOnCodeEmpty();
    }

    interface Presenter extends BasePresenter {

        void loadOnlineClassInfos(Context context, String code);

        void loadLocalClassInfos(Context context);

        void loadCAPTCHA();

        void storeClassInfos(Context context);
    }
}
