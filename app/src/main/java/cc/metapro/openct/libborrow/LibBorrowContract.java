package cc.metapro.openct.libborrow;

import android.graphics.drawable.Drawable;

import java.util.List;

import cc.metapro.openct.BasePresenter;
import cc.metapro.openct.BaseView;
import cc.metapro.openct.data.BorrowInfo;

/**
 * Created by jeffrey on 11/29/16.
 */

interface LibBorrowContract {

    interface View extends BaseView<Presenter> {

        void showDue(List<BorrowInfo> infos);

        void onLoadBorrows(List<BorrowInfo> infos);

        void onCaptchaPicLoaded(Drawable captcha);

    }

    interface Presenter extends BasePresenter {

        void loadOnlineBorrows(String code);

        void loadLocalBorrows();

        void loadCAPTCHA();

        void storeBorrows();

        List<BorrowInfo> getBorrows();

    }
}
