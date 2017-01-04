package cc.metapro.openct.libsearch;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cc.metapro.openct.R;
import cc.metapro.openct.customviews.EndlessRecyclerOnScrollListener;
import cc.metapro.openct.data.BookInfo;
import cc.metapro.openct.utils.RecyclerViewHelper;

public class SearchResultFragment extends Fragment implements LibSearchContract.View {

    @BindView(R.id.fab_up)
    FloatingActionButton mFabUp;

    @BindView(R.id.lib_result_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.lib_result_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    private Context mContext;
    private Unbinder mUnbinder;
    private BooksAdapter mAdapter;
    private LibSearchContract.Presenter mPresenter;
    private LinearLayoutManager mManager;

    @OnClick(R.id.fab_up)
    public void upToTop() {
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_result, container, false);

        mUnbinder = ButterKnife.bind(this, view);

        mContext = getContext();

        mAdapter = new BooksAdapter(getContext());
        mManager = RecyclerViewHelper.setRecyclerView(getContext(), mRecyclerView, mAdapter);
        setRecyclerViewManager(mManager);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.search();
            }
        });

        return view;
    }

    private void setRecyclerViewManager(final LinearLayoutManager manager) {
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mRecyclerView != null) {
                    mRecyclerView.clearOnScrollListeners();
                    mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(manager) {
                        @Override
                        public void onLoadMore(int currentPage) {
                            mPresenter.nextPage();
                        }
                    });
                }
            }
        }, 5000);
    }

    @Override
    public void onDestroy() {
        mUnbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void showOnSearching() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onSearchResult(List<BookInfo> infos) {
        mAdapter.notifyItemRangeRemoved(0, mAdapter.getItemCount() - 1);
        mAdapter.addNewBooks(infos);
        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
        mFabUp.setVisibility(View.VISIBLE);
        if (infos.size() > 0) {
            Toast.makeText(mContext, "找到了 " + infos.size() + " 条结果", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "没有查询到相关书籍", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNextPageResult(List<BookInfo> infos) {
        mAdapter.addBooks(infos);
        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
        if (infos.size() > 0) {
            Toast.makeText(mContext, "加载了 " + infos.size() + " 条结果", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "没有更多结果了", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setPresenter(LibSearchContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
