package cc.metapro.openct.libsearch;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.BookInfo;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.source.RequestType;
import cc.metapro.openct.utils.Constants;

import static com.google.common.base.Preconditions.checkNotNull;

public class LibSearchPresenter implements LibSearchContract.Presenter {

    private final LibSearchContract.View mLibSearchView;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case Constants.LIB_SEARCH_OK:
                    List<BookInfo> list = (List<BookInfo>) message.obj;
                    mLibSearchView.showOnSearchResultOk(list);
                    break;
                case Constants.LIB_SEARCH_FAIL:
                    mLibSearchView.showOnSearchResultFail();
                    break;
                case Constants.NEXT_PAGE_OK:
                    List<BookInfo> more = (List<BookInfo>) message.obj;
                    mLibSearchView.showOnNextPageOk(more);
                    break;
                case Constants.NEXT_PAGE_FAIL:
                    mLibSearchView.showOnNextPageFail();
                    break;
                case Constants.NETWORK_TIMEOUT:
                    mLibSearchView.showOnNetworkTimeout();
                    break;
                case Constants.NETWORK_ERROR:
                    mLibSearchView.showOnNetworkError();
                    break;
                case Constants.UNKNOWN_ERROR:
                    mLibSearchView.showOnNextPageFail();
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
                    message.what = Constants.LIB_SEARCH_OK;
                    message.obj = infos;
                    mHandler.sendMessage(message);
                }

                @Override
                public void onResultFail(int failType) {
                    mHandler.sendEmptyMessage(failType);
                }
            });

    private Loader mGetNextPageLoader =
            new Loader(RequestType.GET_LIB_NEXT_PAGE, new Loader.CallBack() {
                @Override
                public void onResultOk(Object results) {
                    List<BookInfo> bookInfos = (List<BookInfo>) results;
                    Message message = new Message();
                    message.what = Constants.NEXT_PAGE_OK;
                    message.obj = bookInfos;
                    mHandler.sendMessage(message);
                }

                @Override
                public void onResultFail(int failType) {
                    mHandler.sendEmptyMessage(failType);
                }

            });

    private Spinner mSpinner;
    private EditText mEditText;

    LibSearchPresenter(@NonNull LibSearchContract.View libSearchView, Spinner spinner, EditText editText) {
        mLibSearchView = checkNotNull(libSearchView, "libSearchView can't be null");
        mSpinner = spinner;
        mEditText = editText;
        mLibSearchView.setPresenter(this);
    }

    @Override
    public void search() {
        Map<String, String> map = new HashMap<>(2);
        map.put(Constants.SEARCH_TYPE, mSpinner.getSelectedItem().toString());
        map.put(Constants.SEARCH_CONTENT, mEditText.getText().toString());
        mLibSearchView.showOnSearching();
        mSearchLibLoader.loadFromRemote(map);
    }

    @Override
    public void getNextPage() {
        mLibSearchView.showOnSearching();
        mGetNextPageLoader.loadFromRemote(null);
    }

    @Override
    public void start() {

    }
}
