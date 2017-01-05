package cc.metapro.openct;

import android.app.Application;
import android.widget.Toast;

import com.google.common.base.Strings;

import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class OpenCT extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (Strings.isNullOrEmpty(Constants.CAPTCHA_FILE)) {
            Constants.CAPTCHA_FILE = getCacheDir().getPath() + "/" + Constants.CAPTCHA_FILENAME;
        }

        Observable
                .create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                        Loader.loadUniversity(OpenCT.this);
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(new Function<Throwable, Integer>() {
                    @Override
                    public Integer apply(Throwable throwable) throws Exception {
                        Toast.makeText(OpenCT.this, "FATAL: 加载学校信息失败", Toast.LENGTH_LONG).show();
                        return 0;
                    }
                })
                .subscribe();

        ActivityUtils.encryptionCheck(OpenCT.this);
    }
}
