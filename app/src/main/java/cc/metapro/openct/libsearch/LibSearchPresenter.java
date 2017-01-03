package cc.metapro.openct.libsearch;

import android.support.annotation.NonNull;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.BookInfo;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.Constants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.google.common.base.Preconditions.checkNotNull;

class LibSearchPresenter implements LibSearchContract.Presenter {

    private final LibSearchContract.View mLibSearchView;

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
        mLibSearchView.showOnSearching();
        Observable
                .create(new ObservableOnSubscribe<List<BookInfo>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<BookInfo>> e) throws Exception {
                        Map<String, String> map = new HashMap<>(2);
                        map.put(Constants.SEARCH_TYPE, mSpinner.getSelectedItem().toString());
                        map.put(Constants.SEARCH_CONTENT, mEditText.getText().toString());
                        List<BookInfo> bookInfos = Loader.getLibrary().search(map);
                        e.onNext(bookInfos);
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<BookInfo>>() {
                    @Override
                    public void accept(List<BookInfo> infos) throws Exception {
                        mLibSearchView.showOnSearchResultOk(infos);
                    }
                })
                .onErrorReturn(new Function<Throwable, List<BookInfo>>() {
                    @Override
                    public List<BookInfo> apply(Throwable throwable) throws Exception {
                        mLibSearchView.showOnSearchResultFail();
                        return new ArrayList<>();
                    }
                })
                .subscribe();

    }

    @Override
    public void getNextPage() {
        mLibSearchView.showOnSearching();
        Observable
                .create(new ObservableOnSubscribe<List<BookInfo>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<BookInfo>> e) throws Exception {
                        List<BookInfo> infos = Loader.getLibrary().getNextPage();
                        e.onNext(infos);
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<BookInfo>>() {
                    @Override
                    public void accept(List<BookInfo> infos) throws Exception {
                        if (infos.size() == 0) {
                            mLibSearchView.showOnNextPageFail();
                        } else {
                            mLibSearchView.showOnNextPageOk(infos);
                        }
                    }
                })
                .onErrorReturn(new Function<Throwable, List<BookInfo>>() {
                    @Override
                    public List<BookInfo> apply(Throwable throwable) throws Exception {
                        mLibSearchView.showOnNextPageFail();
                        return new ArrayList<>();
                    }
                })
                .subscribe();
    }

    @Override
    public void start() {

    }
}
