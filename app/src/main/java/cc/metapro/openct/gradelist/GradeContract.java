package cc.metapro.openct.gradelist;

import android.graphics.drawable.Drawable;

import java.util.List;
import java.util.Map;

import cc.metapro.openct.BasePresenter;
import cc.metapro.openct.BaseView;
import cc.metapro.openct.data.GradeInfo;

interface GradeContract {

    interface View extends BaseView<Presenter> {

        void onLoadGrades(List<GradeInfo> infos);

        void onCaptchaPicLoaded(Drawable captcha);

        void showCETDialog();

        void onLoadCETGrade(Map<String, String> resultMap);

    }

    interface Presenter extends BasePresenter {

        void loadRemoteGrades(String code);

        void loadLocalGrades();

        void loadCETGrade(Map<String, String> queryMap);

        void loadCAPTCHA();

        void storeGrades();

        void clearGrades();
    }
}
