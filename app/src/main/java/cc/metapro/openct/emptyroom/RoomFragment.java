package cc.metapro.openct.emptyroom;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cc.metapro.openct.R;
import cc.metapro.openct.data.RoomInfo;
import cc.metapro.openct.utils.RecyclerViewHelper;

public class RoomFragment extends Fragment implements RoomContract.View {

    @BindView(R.id.room_info_refresh)
    SwipeRefreshLayout mRefreshLayout;

    @BindView(R.id.room_info_recycler)
    RecyclerView mRecyclerView;

    private Unbinder mUnbinder;

    private RoomAdapter mRoomAdapter;

    private RoomContract.Presenter mPresenter;

    public RoomFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mRoomAdapter = new RoomAdapter(getContext());
        RecyclerViewHelper.setRecyclerView(getContext(), mRecyclerView, mRoomAdapter);
        setRefreshLayout();
        return view;
    }

    private void setRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadOnlineRoomInfos();
            }
        });
    }

    @Override
    public void onDestroy() {
        mUnbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void showRooms(int week, int day, int time, String place) {
        List<RoomInfo> roomInfos = mPresenter.getRoomInfos();
        if (roomInfos == null) return;
        List<RoomInfo> results = new ArrayList<>();
        for (RoomInfo r : roomInfos) {
            if (r.isWanted(place) && r.isAvailable(week, day, time)) {
                results.add(r);
            }
        }
        if (mRefreshLayout.isRefreshing())
            mRefreshLayout.setRefreshing(false);
        mRoomAdapter.setRoomInfos(results);
        mRoomAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPresenter(RoomContract.Presenter presenter) {
        mPresenter = presenter;
    }

}
