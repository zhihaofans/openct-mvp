package cc.metapro.openct.classtable;

import android.graphics.drawable.Drawable;

import java.util.List;

import cc.metapro.openct.BasePresenter;
import cc.metapro.openct.BaseView;
import cc.metapro.openct.data.ClassInfo;

interface ClassContract {
    interface View extends BaseView<Presenter> {

        void updateClasses(List<ClassInfo> infos, int week);

        void onCaptchaPicLoaded(Drawable captcha);

    }

    interface Presenter extends BasePresenter {

        void loadOnlineClasses(String code);

        void loadLocalClasses();

        void removeClassInfo(ClassInfo info);

        void loadCAPTCHA();

        void storeClasses();
    }
}
