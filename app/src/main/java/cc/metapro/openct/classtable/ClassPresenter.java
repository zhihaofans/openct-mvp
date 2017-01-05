package cc.metapro.openct.classtable;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.R;
import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.widget.DailyClassWidget;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

class ClassPresenter implements ClassContract.Presenter {

    private ClassContract.View mClassView;
    private int week = 1;
    private List<ClassInfo> mClassInfos = new ArrayList<>(0);
    private Context mContext;

    ClassPresenter(@NonNull ClassContract.View view, Context context) {
        week = Loader.getCurrentWeek(context);
        mClassView = view;
        mClassView.setPresenter(this);
        mContext = context;
    }

    @Override
    public void loadOnlineClasses(final String code) {
        Observable
                .create(new ObservableOnSubscribe<List<ClassInfo>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<ClassInfo>> e) throws Exception {
                        Map<String, String> loginMap = Loader.getCmsStuInfo(mContext);
                        if (loginMap.size() == 0) {
                            throw new Exception(mContext.getString(R.string.enrich_cms_info));
                        }
                        loginMap.put(Constants.CAPTCHA_KEY, code);
                        e.onNext(Loader.getCms().getClassInfos(loginMap));
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<ClassInfo>>() {
                    @Override
                    public void accept(List<ClassInfo> infos) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        if (infos.size() == 0) {
                            Toast.makeText(mContext, R.string.no_classes_avail, Toast.LENGTH_SHORT).show();
                        } else {
                            storeClasses();
                            DailyClassWidget.update(mContext);
                            mClassInfos = infos;
                            mClassView.updateClasses(mClassInfos, week);
                        }
                    }
                })
                .onErrorReturn(new Function<Throwable, List<ClassInfo>>() {
                    @Override
                    public List<ClassInfo> apply(Throwable throwable) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        return new ArrayList<>(0);
                    }
                })
                .subscribe();
    }

    @Override
    public void loadLocalClasses() {
        Observable
                .create(new ObservableOnSubscribe<List<ClassInfo>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<ClassInfo>> e) throws Exception {
                        DBManger manger = DBManger.getInstance(mContext);
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
                            Toast.makeText(mContext, R.string.no_local_classes_avail, Toast.LENGTH_SHORT).show();
                        } else {
                            DailyClassWidget.update(mContext);
                            mClassInfos = classInfos;
                            mClassView.updateClasses(mClassInfos, week);
                        }
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_SHORT).show();
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
        mClassView.updateClasses(mClassInfos, week);
    }

    @Override
    public void loadCAPTCHA() {
        Observable
                .create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter e) throws Exception {
                        Loader.getCms().getCAPTCHA(Constants.CAPTCHA_FILE);
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(new Function<Throwable, String>() {
                    @Override
                    public String apply(Throwable throwable) throws Exception {
                        Toast.makeText(mContext, "获取验证码失败\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        return "";
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        Drawable drawable = BitmapDrawable.createFromPath(Constants.CAPTCHA_FILE);
                        if (drawable != null)
                            mClassView.onCaptchaPicLoaded(drawable);
                    }
                })
                .subscribe();
    }

    @Override
    public void storeClasses() {
        Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(ObservableEmitter e) throws Exception {
                DBManger manger = DBManger.getInstance(mContext);
                manger.updateClassInfos(mClassInfos);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.newThread()).subscribe();
    }

    @Override
    public void start() {
        loadLocalClasses();
    }
}
