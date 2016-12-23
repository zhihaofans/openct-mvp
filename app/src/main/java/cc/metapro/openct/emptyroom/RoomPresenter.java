package cc.metapro.openct.emptyroom;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import cc.metapro.openct.data.RoomInfo;
import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.utils.Constants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by jeffrey on 16/12/23.
 */

public class RoomPresenter implements RoomContract.Presenter {

    private List<RoomInfo> mRoomInfos;

    public RoomPresenter(@NonNull RoomContract.View roomFragment) {
        roomFragment.setPresenter(this);
    }

    @Override
    public List<RoomInfo> getRoomInfos() {
        return mRoomInfos;
    }

    @Override
    public void loadOnlineRoomInfos() {

    }

    @Override
    public void loadLocalRoomInfos(Context context) {

    }

    @Override
    public void storeRoomInfos(final Context context) {
        Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(ObservableEmitter e) throws Exception {
                String s = StoreHelper.getJsonText(mRoomInfos);
                StoreHelper.saveTextFile(context, Constants.STU_GRADE_INFOS_FILE, s);
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void start() {

    }
}
