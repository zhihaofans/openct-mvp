package cc.metapro.openct.gradelist;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.common.base.Strings;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.data.ServerService.ServiceGenerator;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.university.UniversityService;
import cc.metapro.openct.utils.Constants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class GradePresenter implements GradeContract.Presenter {

    private GradeContract.View mGradeFragment;
    private List<GradeInfo> mGradeInfos = new ArrayList<>(0);

    GradePresenter(GradeContract.View view, String path) {
        mGradeFragment = view;
        mGradeFragment.setPresenter(this);
        if (Strings.isNullOrEmpty(Constants.CAPTCHA_FILE)) {
            Constants.CAPTCHA_FILE = path + "/" + Constants.CAPTCHA_FILENAME;
        }
    }

    @Override
    public void loadOnlineGradeInfos(final Context context, final String code) {
        Observable.create(new ObservableOnSubscribe<List<GradeInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<GradeInfo>> e) throws Exception {
                Map<String, String> loginMap = Loader.getCmsStuInfo(context);
                if (loginMap == null) {
                    return;
                }
                loginMap.put(Constants.CAPTCHA_KEY, code);
                e.onNext(Loader.getCms().getGradeInfos(loginMap));
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<GradeInfo>>() {
                    @Override
                    public void accept(List<GradeInfo> infos) throws Exception {
                        if (infos.size() == 0) {
                            mGradeFragment.showOnResultFail();
                        } else {
                            mGradeInfos = infos;
                            mGradeFragment.showAll(mGradeInfos);
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
    public void loadLocalGradeInfos(final Context context) {
        Observable.create(new ObservableOnSubscribe<List<GradeInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<GradeInfo>> e) throws Exception {
                DBManger manger = DBManger.getInstance(context);
                e.onNext(manger.getGradeInfos());
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<GradeInfo>>() {
                    @Override
                    public void accept(List<GradeInfo> gradeInfos) throws Exception {
                        if (gradeInfos.size() == 0) {
                            mGradeFragment.showOnResultFail();
                        } else {
                            mGradeInfos = gradeInfos;
                            mGradeFragment.showAll(mGradeInfos);
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
    public void loadCETGradeInfos(final Map<String, String> queryMap) {
        Observable
                .create(new ObservableOnSubscribe<Map<String, String>>() {
                    @Override
                    public void subscribe(ObservableEmitter<Map<String, String>> e) throws Exception {
                        UniversityService service = ServiceGenerator
                                .createService(UniversityService.class, ServiceGenerator.HTML);

                        String res = service.queryCet("http://www.chsi.com.cn/cet/",
                                queryMap.get(Constants.CET_NUM_KEY),
                                queryMap.get(Constants.CET_NAME_KEY), "t")
                                .execute().body();

                        Document document = Jsoup.parse(res);
                        Elements elements = document.select("table[class=cetTable]");
                        Element targetTable = elements.first();
                        Elements tds = targetTable.getElementsByTag("td");
                        String name = tds.get(0).text();
                        String school = tds.get(1).text();
                        String type = tds.get(2).text();
                        String num = tds.get(3).text();
                        String time = tds.get(4).text();
                        String grade = tds.get(5).text();

                        Map<String, String> results = new HashMap<>(6);
                        results.put(Constants.CET_NAME_KEY, name);
                        results.put(Constants.CET_SCHOOL_KEY, school);
                        results.put(Constants.CET_TYPE_KEY, type);
                        results.put(Constants.CET_NUM_KEY, num);
                        results.put(Constants.CET_TIME_KEY, time);
                        results.put(Constants.CET_GRADE_KEY, grade);

                        e.onNext(results);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Map<String, String>>() {
                    @Override
                    public void accept(Map<String, String> stringMap) throws Exception {
                        mGradeFragment.showCETGrade(stringMap);
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mGradeFragment.showOnCETGradeFail();
                    }
                }).subscribe();
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
                        mGradeFragment.showOnCAPTCHAFail();
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        Drawable drawable = BitmapDrawable.createFromPath(Constants.CAPTCHA_FILE);
                        mGradeFragment.showOnCAPTCHALoaded(drawable);
                    }
                })
                .subscribe();
    }

    @Override
    public void storeGradeInfos(final Context context) {
        Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(ObservableEmitter e) throws Exception {
                DBManger manger = DBManger.getInstance(context);
                manger.updateGradeInfos(mGradeInfos);
            }
        }).subscribeOn(Schedulers.newThread()).subscribe();
    }

    @Override
    public void clearGradeInfos() {
        mGradeInfos = new ArrayList<>(0);
    }

    @Override
    public void start() {

    }
}
