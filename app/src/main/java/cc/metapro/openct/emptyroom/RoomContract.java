package cc.metapro.openct.emptyroom;

import android.content.Context;

import java.util.List;

import cc.metapro.openct.BasePresenter;
import cc.metapro.openct.BaseView;
import cc.metapro.openct.data.RoomInfo;

/**
 * Created by jeffrey on 16/12/23.
 */

interface RoomContract {
    interface View extends BaseView<Presenter> {

        void showRooms(int week, int day, int time, String place);

    }

    interface Presenter extends BasePresenter {

        List<RoomInfo> getRoomInfos();

        void loadOnlineRoomInfos();

        void loadLocalRoomInfos(Context context);

        void storeRoomInfos(final Context context);

    }
}
