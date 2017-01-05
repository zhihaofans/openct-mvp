package cc.metapro.openct.libborrow;

import java.util.List;

import cc.metapro.openct.BaseView;
import cc.metapro.openct.LoginPresenter;
import cc.metapro.openct.data.BorrowInfo;

interface LibBorrowContract {

    interface View extends BaseView<Presenter> {

        void showDue(List<BorrowInfo> infos);

        void onLoadBorrows(List<BorrowInfo> infos);

    }

    interface Presenter extends LoginPresenter {

        void loadLocalBorrows();

        void storeBorrows();

        List<BorrowInfo> getBorrows();

    }
}
