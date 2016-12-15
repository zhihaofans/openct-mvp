package cc.metapro.openct.libborrowinfo;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.List;

import cc.metapro.openct.BasePresenter;
import cc.metapro.openct.BaseView;
import cc.metapro.openct.data.BorrowInfo;
import cc.metapro.openct.utils.ActivityUtils;

/**
 * Created by jeffrey on 11/29/16.
 */

interface LibBorrowContract {

    interface View extends BaseView<Presenter> {

        void showDue(List<BorrowInfo> infos);

        void showAll(List<BorrowInfo> infos);

        void showOnResultFail();

        void showOnResultOk(int i);

        void showOnCodeEmpty();

        void setCAPTCHADialog(ActivityUtils.CaptchaDialogHelper captchaDialogHelper);

        void showOnCAPTCHALoaded(Drawable captcha);

        void showOnCAPTCHAFail();
    }

    interface Presenter extends BasePresenter {

        void loadOnlineBorrowInfos(Context context, String code);

        void loadLocalBorrowInfos(Context context);

        List<BorrowInfo> getBorrowInfos();

        void loadCAPTCHA();

        void storeBorrowInfos(Context context);

    }
}
