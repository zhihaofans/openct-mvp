package cc.metapro.openct.classtable;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.widget.TextView;
import android.widget.Toast;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

import java.io.File;
import java.io.FileOutputStream;
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
    public void loadOnline(final String code) {
        ActivityUtils.getProgressDialog(mContext, R.string.loading_class_infos).show();
        Observable
                .create(new ObservableOnSubscribe<List<ClassInfo>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<ClassInfo>> e) throws Exception {
                        Map<String, String> loginMap = Loader.getCmsStuInfo(mContext);
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
                            mClassInfos = infos;
                            mClassView.updateClasses(mClassInfos, week);
                            storeClasses();
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
                t.deActive();
                break;
            }
        }
        mClassView.updateClasses(mClassInfos, week);
    }

    @Override
    public void loadCaptcha(final TextView view) {
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
                        if (drawable != null) {
                            view.setBackground(drawable);
                            view.setText("");
                        }
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
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        DailyClassWidget.update(mContext);
                    }
                })
                .subscribe();
    }

    @Override
    public void exportCLasses() {
        ActivityUtils.getProgressDialog(mContext, R.string.creating_class_ical).show();
        Observable.create(new ObservableOnSubscribe<Calendar>() {
            @Override
            public void subscribe(ObservableEmitter<Calendar> e) throws Exception {
                try {
                    Calendar calendar = new Calendar();
                    calendar.getProperties().add(new ProdId("-//OpenCT Jeff//iCal4j 2.0//EN"));
                    calendar.getProperties().add(Version.VERSION_2_0);
                    calendar.getProperties().add(CalScale.GREGORIAN);
                    SparseArray<List<ClassInfo>> classMap = new SparseArray<>(7);
                    java.util.Calendar calendar1 = java.util.Calendar.getInstance();
                    int factor = calendar1.getFirstDayOfWeek();
                    if (factor == java.util.Calendar.SUNDAY) {
                        factor++;
                    }
                    for (int i = 0; i < 7; i++) {
                        List<ClassInfo> classes = new ArrayList<>();
                        classMap.put(i, classes);
                        for (int j = 0; j < mClassInfos.size() / 7; j++) {
                            ClassInfo c = mClassInfos.get(7 * j + i);
                            ClassInfo classInfo = c;
                            VEvent vEvent;

                            // add all subclass events
                            while (classInfo.hasSubClass()) {
                                classInfo = classInfo.getSubClassInfo();
                                vEvent = classInfo.getEvent(week, i + factor);
                                if (vEvent != null) {
                                    calendar.getComponents().add(vEvent);
                                }
                            }

                            vEvent = c.getEvent(week, i + factor);
                            if (vEvent != null) {
                                calendar.getComponents().add(vEvent);
                            }
                        }
                    }
                    calendar.validate();

                    File downloadDir = Environment.getExternalStorageDirectory();
                    if (!downloadDir.exists()) {
                        downloadDir.createNewFile();
                    }

                    File file = new File(downloadDir, "openct_classes.ics");
                    FileOutputStream fos = new FileOutputStream(file);
                    CalendarOutputter calOut = new CalendarOutputter();
                    calOut.output(calendar, fos);
                    e.onComplete();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    e.onError(ex);
                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        Toast.makeText(mContext, "创建成功, 文件 openct_classes.ics 保存在手机存储根目录中", Toast.LENGTH_LONG).show();
                    }
                })
                .onErrorReturn(new Function<Throwable, Calendar>() {
                    @Override
                    public Calendar apply(Throwable throwable) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        Toast.makeText(mContext, "创建日历信息时发生了异常\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        return new Calendar();
                    }
                })
                .subscribe();
    }

    @Override
    public void start() {
        loadLocalClasses();
    }
}
