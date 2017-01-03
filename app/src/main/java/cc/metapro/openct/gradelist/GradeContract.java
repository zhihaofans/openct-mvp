package cc.metapro.openct.gradelist;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.List;
import java.util.Map;

import cc.metapro.openct.BasePresenter;
import cc.metapro.openct.BaseView;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.utils.ActivityUtils;

interface GradeContract {

    interface View extends BaseView<Presenter> {

        void showAll(List<GradeInfo> infos);

        void showOnResultFail();

        void setCAPTCHADialogHelper(ActivityUtils.CaptchaDialogHelper captchaDialogHelper);

        void showOnCAPTCHALoaded(Drawable captcha);

        void showOnCAPTCHAFail();

        void showCETQueryDialog();

        void showCETGrade(Map<String, String> resultMap);

        void showOnCETGradeFail();

        void showOnLoginFail();

        void showOnNetworkError();

        void showOnNetworkTimeout();

    }

    interface Presenter extends BasePresenter {

        void loadOnlineGradeInfos(Context context, String code);

        void loadLocalGradeInfos(Context context);

        void loadCETGradeInfos(Map<String, String> queryMap);

        void loadCAPTCHA();

        void storeGradeInfos(Context context);

        void clearGradeInfos();
    }
}
