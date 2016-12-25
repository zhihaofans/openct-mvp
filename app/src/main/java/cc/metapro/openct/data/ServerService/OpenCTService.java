package cc.metapro.openct.data.ServerService;

import android.support.annotation.NonNull;

import java.util.List;

import cc.metapro.openct.data.RoomInfo;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by jeffrey on 16/12/24.
 */

public interface OpenCTService {


    /**
     * generate query url like
     * http://openct.metapro.cc/emptyroom?school={school}
     */
    @GET("/emptyroom")
    Call<List<RoomInfo>> listRoomInfos(
            @NonNull @Query("school") String school
    );

}
