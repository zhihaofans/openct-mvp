package cc.metapro.openct.emptyroom;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.RoomInfo;

/**
 * Created by jeffrey on 16/12/23.
 */

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<RoomInfo> mRoomInfos;

    private Context mContext;

    public RoomAdapter(Context context) {
        mContext = context;
        mRoomInfos = new ArrayList<>(0);
    }

    @Override
    public RoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RoomViewHolder holder, int position) {
        RoomInfo info = mRoomInfos.get(position);
        holder.setRoomPlace(info.getPlace());
        holder.setAllAvailableTime(mContext, info.getAllAvailTime());
    }

    @Override
    public int getItemCount() {
        return mRoomInfos.size();
    }

    public void setRoomInfos(List<RoomInfo> roomInfos) {
        if (roomInfos != null) {
            mRoomInfos = roomInfos;
        } else {
            mRoomInfos = new ArrayList<>(0);
        }
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_room_place)
        TextView mRoomPlace;

        public RoomViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setRoomPlace(String place) {
            mRoomPlace.setText(place);
        }

        public void setAllAvailableTime(final Context context, final String availTime) {
            mRoomPlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder ab = new AlertDialog.Builder(context);
                    ab.setTitle("教室空闲时间");
                    ab.setMessage(availTime);
                    ab.show();
                    ab.setPositiveButton("返回", null);
                }
            });
        }
    }
}
