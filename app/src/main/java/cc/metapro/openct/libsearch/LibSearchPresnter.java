package cc.metapro.openct.libsearch;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.BookInfo;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.source.RequestType;
import cc.metapro.openct.utils.Constants;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by jeffrey on 11/29/16.
 */

public class LibSearchPresnter implements LibSearchContract.Presenter {

    public final static String PAGE_INDEX = "page_index", TYPE = "type", CONTENT = "content";

    private static int mNextPageIndex;

    private static Map<String, String> mLastSearchKvs;

    private final LibSearchContract.View mLibSearchView;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case Constants.RESULT_OK:
                    List<BookInfo> list = (List<BookInfo>) message.obj;
                    mLibSearchView.showOnSearchResultOk(list);
                    break;
                case Constants.RESULT_FAIL:
                    mLibSearchView.showOnSearchResultFail();
                    break;
                case Constants.MORE_OK:
                    List<BookInfo> more = (List<BookInfo>) message.obj;
                    mLibSearchView.showOnLoadMoreOk(more);
                    break;
                case Constants.MORE_FAIL:
                    mLibSearchView.showOnLoadMoreFail();
                    break;
            }
            return false;
        }
    });

    private Loader mSearchLibLoader =
            new Loader(RequestType.SEARCH_LIB, new Loader.CallBack() {
                @Override
                public void onResultOk(Object results) {
                    List<BookInfo> infos = (List<BookInfo>) results;
                    Message message = new Message();
                    message.what = Constants.RESULT_OK;
                    message.obj = infos;
                    mHandler.sendMessage(message);
                }

                @Override
                public void onResultFail() {
                    Message message = new Message();
                    message.what = Constants.RESULT_FAIL;
                    mHandler.sendMessage(message);
                }
            });

    private Loader mGetNextPageLoader =
            new Loader(RequestType.GET_LIB_RESULT_PAGE, new Loader.CallBack() {
                @Override
                public void onResultOk(Object results) {
                    List<BookInfo> bookInfos = (List<BookInfo>) results;
                    Message message = new Message();
                    message.what = Constants.MORE_OK;
                    message.obj = bookInfos;
                    mHandler.sendMessage(message);
                }

                @Override
                public void onResultFail() {
                    if (mNextPageIndex > 1) {
                        mNextPageIndex--;
                    } else {
                        mNextPageIndex = 2;
                    }
                    Message message = new Message();
                    message.what = Constants.MORE_FAIL;
                    mHandler.sendMessage(message);
                }
            });

    public LibSearchPresnter(@NonNull LibSearchContract.View libSearchView) {
        mLibSearchView = checkNotNull(libSearchView, "libSearchView can't be null");

        mLibSearchView.setPresenter(this);
    }

    @Override
    public void search(Map<String, String> kvs) {
        try {
            mLibSearchView.showOnSearching();
            mNextPageIndex = 2;
            mLastSearchKvs = kvs;
            mSearchLibLoader.loadFromRemote(mLastSearchKvs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getNextPage() {
        try {
            mLibSearchView.showOnSearching();
            mLastSearchKvs.put(PAGE_INDEX, mNextPageIndex + "");
            mGetNextPageLoader.loadFromRemote(mLastSearchKvs);
            mNextPageIndex++;
        } catch (Exception e) {
            e.printStackTrace();
            mLibSearchView.showOnLoadMoreFail();
        }
    }

    @Override
    public void start() {

    }
}
