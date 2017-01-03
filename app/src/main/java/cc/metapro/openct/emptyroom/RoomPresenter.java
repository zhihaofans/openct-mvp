package cc.metapro.openct.emptyroom;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import cc.metapro.openct.data.RoomInfo;
import cc.metapro.openct.data.ServerService.OpenCTService;
import cc.metapro.openct.data.ServerService.ServiceGenerator;
import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.utils.Constants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;

class RoomPresenter implements RoomContract.Presenter {

    private List<RoomInfo> mRoomInfos;

    private RoomContract.View mRoomFragment;

    RoomPresenter(@NonNull RoomContract.View roomFragment) {
        roomFragment.setPresenter(this);
        mRoomFragment = roomFragment;
    }

    @Override
    public List<RoomInfo> getRoomInfos() {
        return mRoomInfos;
    }

    @Override
    public void loadOnlineRoomInfos() {
        Observable.create(
                new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> e) throws Exception {
                        OpenCTService service = ServiceGenerator.createService(OpenCTService.class, ServiceGenerator.GSON_CONVERTER);

                        Call<List<RoomInfo>> call = service.listRoomInfos("njit");
                        try {
                            List<RoomInfo> contributors = call.execute().body();
                            if (contributors != null && contributors.size() != 0) {
                                mRoomInfos = contributors;
                                mRoomFragment.showRooms(-1, -1, -1, null);
                            }
                        } catch (IOException ioe) {
                            // handle errors
                        }
                    }
                }).subscribeOn(Schedulers.io())
                .subscribe();
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
