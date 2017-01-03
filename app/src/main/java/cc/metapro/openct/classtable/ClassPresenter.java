package cc.metapro.openct.classtable;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.Constants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ClassPresenter implements ClassContract.Presenter {

    private ClassContract.View mClassView;
    private int week = 1;
    private List<ClassInfo> mClassInfos = new ArrayList<>(0);

    ClassPresenter(@NonNull ClassContract.View view, Context context, String path) {
        week = Loader.getCurrentWeek(context);
        if (Strings.isNullOrEmpty(Constants.CAPTCHA_FILE)) {
            Constants.CAPTCHA_FILE = path + "/" + Constants.CAPTCHA_FILENAME;
        }
        mClassView = view;
        mClassView.setPresenter(this);
    }

    @Override
    public void loadOnlineClassInfos(final Context context, final String code) {
        Observable.create(new ObservableOnSubscribe<List<ClassInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<ClassInfo>> e) throws Exception {
                Map<String, String> loginMap = Loader.getCmsStuInfo(context);
                if (loginMap == null) {
                    return;
                }
                loginMap.put(Constants.CAPTCHA_KEY, code);
                e.onNext(Loader.getCms().getClassInfos(loginMap));
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<ClassInfo>>() {
                    @Override
                    public void accept(List<ClassInfo> infos) throws Exception {
                        if (infos.size() == 0) {
                            mClassView.showOnResultFail();
                        } else {
                            mClassInfos = infos;
                            mClassView.updateClassInfos(mClassInfos, week);
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
    public void loadLocalClassInfos(final Context context) {
        Observable.create(new ObservableOnSubscribe<List<ClassInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<ClassInfo>> e) throws Exception {
                DBManger manger = DBManger.getInstance(context);
                e.onNext(manger.getClassInfos());
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<ClassInfo>>() {
                    @Override
                    public void accept(List<ClassInfo> classInfos) throws Exception {
                        if (classInfos.size() == 0) {
                            mClassView.showOnResultFail();
                        } else {
                            mClassInfos = classInfos;
                            mClassView.updateClassInfos(mClassInfos, week);
                        }
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).subscribe();
    }

    @Override
    public void removeClassInfo(ClassInfo info) {
        for (ClassInfo c : mClassInfos) {
            if (c.contains(info)) {
                ClassInfo t = c;
                while (!t.equals(info) && c.hasSubClass()) {
                    t = t.getSubClassInfo();
                }
                t.deactive();
                break;
            }
        }
        mClassView.updateClassInfos(mClassInfos, week);
    }

    @Override
    public void loadCAPTCHA() {
        Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(ObservableEmitter e) throws Exception {
                Loader.getCms().getCAPTCHA(Constants.CAPTCHA_FILE);
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mClassView.showOnCAPTCHAFail();
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        Drawable drawable = BitmapDrawable.createFromPath(Constants.CAPTCHA_FILE);
                        mClassView.onCAPTCHALoaded(drawable);
                    }
                })
                .subscribe();
    }

    @Override
    public void storeClassInfos(final Context context) {
        Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(ObservableEmitter e) throws Exception {
                DBManger manger = DBManger.getInstance(context);
                manger.updateClassInfos(mClassInfos);
            }
        }).subscribeOn(Schedulers.newThread()).subscribe();
    }

    @Override
    public void start() {

    }
}
