package cc.metapro.openct.libborrowinfo;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.BorrowInfo;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.Constants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class LibBorrowPresenter implements LibBorrowContract.Presenter {

    private static LibBorrowContract.View mLibBorrowView;
    private static List<BorrowInfo> mBorrowInfos = new ArrayList<>(0);

    LibBorrowPresenter(@NonNull LibBorrowContract.View libBorrowView, @NonNull String path) {
        mLibBorrowView = libBorrowView;
        if (Strings.isNullOrEmpty(Constants.CAPTCHA_FILE)) {
            Constants.CAPTCHA_FILE = path + "/" + Constants.CAPTCHA_FILENAME;
        }

        mLibBorrowView.setPresenter(this);
    }

    @Override
    public void loadOnlineBorrowInfos(final Context context, final String code) {
        Observable.create(new ObservableOnSubscribe<List<BorrowInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BorrowInfo>> e) throws Exception {
                if (Strings.isNullOrEmpty(code)) {
                    mLibBorrowView.showOnLoadBorrowInfoFail();
                    return;
                }
                Map<String, String> loginMap = Loader.getLibStuInfo(context);
                if (loginMap == null) {
                    return;
                }
                loginMap.put(Constants.CAPTCHA_KEY, code);
                List<BorrowInfo> infos = Loader.getLibrary().getBorrowInfo(loginMap);
                e.onNext(infos);
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<BorrowInfo>>() {
                    @Override
                    public void accept(List<BorrowInfo> infos) throws Exception {
                        if (infos.size() == 0) {
                            mLibBorrowView.showOnLoadBorrowInfoFail();
                        } else {
                            mBorrowInfos = infos;
                            mLibBorrowView.showAll(mBorrowInfos);
                        }
                    }
                })
                .onErrorReturn(new Function<Throwable, List<BorrowInfo>>() {
                    @Override
                    public List<BorrowInfo> apply(Throwable throwable) throws Exception {
                        String s = throwable.getMessage();
                        switch (s) {
                            case Constants.LOGIN_FAIL :
                                mLibBorrowView.showOnLoginFail();
                                break;
                            default:
                                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
                        }
                        return new ArrayList<>();
                    }
                })
                .subscribe();
    }

    @Override
    public void loadLocalBorrowInfos(final Context context) {
        Observable.create(new ObservableOnSubscribe<List<BorrowInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BorrowInfo>> e) throws Exception {
                DBManger manger = DBManger.getInstance(context);
                List<BorrowInfo> borrowInfos = manger.getBorrowInfos();
                e.onNext(borrowInfos);
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<BorrowInfo>>() {
                    @Override
                    public void accept(List<BorrowInfo> infos) throws Exception {
                        if (infos.size() == 0) {
                            mLibBorrowView.showOnLoadBorrowInfoFail();
                        } else {
                            mBorrowInfos = infos;
                            mLibBorrowView.showAll(mBorrowInfos);
                        }
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .subscribe();
    }

    @Override
    public List<BorrowInfo> getBorrowInfos() {
        return mBorrowInfos;
    }

    @Override
    public void loadCAPTCHA() {
        Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(ObservableEmitter e) throws Exception {
                Loader.getLibrary().getCAPTCHA(Constants.CAPTCHA_FILE);
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mLibBorrowView.showOnLoadCAPTCHAFail();
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        Drawable drawable = BitmapDrawable.createFromPath(Constants.CAPTCHA_FILE);
                        mLibBorrowView.showOnCAPTCHALoaded(drawable);
                    }
                })
                .subscribe();
    }

    @Override
    public void storeBorrowInfos(final Context context) {
        Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(ObservableEmitter e) throws Exception {
                DBManger manger = DBManger.getInstance(context);
                manger.updateBorrowInfos(mBorrowInfos);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.newThread()).subscribe();
    }

    @Override
    public void start() {

    }
}
