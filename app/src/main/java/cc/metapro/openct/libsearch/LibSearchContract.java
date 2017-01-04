package cc.metapro.openct.libsearch;

import java.util.List;

import cc.metapro.openct.BasePresenter;
import cc.metapro.openct.BaseView;
import cc.metapro.openct.data.BookInfo;

interface LibSearchContract {

    interface View extends BaseView<Presenter> {

        void showOnSearching();

        void onSearchResult(List<BookInfo> infos);

        void onNextPageResult(List<BookInfo> infos);

    }

    interface Presenter extends BasePresenter {

        void search();

        void nextPage();

    }
}
