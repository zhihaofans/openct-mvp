package cc.metapro.openct.libsearch;

import java.util.List;

import cc.metapro.openct.BasePresenter;
import cc.metapro.openct.BaseView;
import cc.metapro.openct.data.BookInfo;

interface LibSearchContract {

    interface View extends BaseView<Presenter> {

        void showOnSearching();

        void showOnSearchResultOk(List<BookInfo> infos);

        void showOnSearchResultFail();

        void showOnNextPageOk(List<BookInfo> infos);

        void showOnNextPageFail();

        void showOnNetworkError();

        void showOnNetworkTimeout();

    }

    interface Presenter extends BasePresenter {

        void search();

        void getNextPage();

    }
}
