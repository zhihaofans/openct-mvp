package cc.metapro.openct.libsearch;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.R;
import cc.metapro.openct.data.BookInfo;
import cc.metapro.openct.utils.RecyclerViewHelper;

public class SearchResultFragment extends Fragment implements LibSearchContract.View {

    private RecyclerView mRecyclerView;

    private BooksAdapter mAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private LibSearchContract.Presenter mPresenter;

    private AppCompatSpinner mSpinner;

    private AppCompatEditText mEditText;

    private LinearLayoutManager mManager;

    private FloatingActionButton fabUp;

    public SearchResultFragment() {
        // Required empty public constructor
    }

    public static SearchResultFragment newInstance() {
        return new SearchResultFragment();
    }

    public void setViews(@NonNull AppCompatSpinner spinner, @NonNull AppCompatEditText editText) {
        mSpinner = spinner;
        mEditText = editText;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search_result, container, false);

        FloatingActionButton fabSearch = (FloatingActionButton) view.findViewById(R.id.fab_search);
        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String> map = new HashMap<String, String>(2);
                map.put(LibSearchPresnter.TYPE, mSpinner.getSelectedItem().toString());
                map.put(LibSearchPresnter.CONTENT, mEditText.getText().toString());
                mPresenter.search(map);
            }
        });

        fabUp = (FloatingActionButton) view.findViewById(R.id.fab_up);
        fabUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerView.scrollToPosition(0);
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.lib_result_recycler_view);

        mAdapter = new BooksAdapter(getContext());
        mManager = RecyclerViewHelper.setRecyclerView(getContext(), mRecyclerView, mAdapter);
        setRecyclerViewManager(mManager);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.lib_result_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Map<String, String> map = new HashMap<String, String>(2);
                map.put(LibSearchPresnter.TYPE, mSpinner.getSelectedItem().toString());
                map.put(LibSearchPresnter.CONTENT, mEditText.getText().toString());
                mPresenter.search(map);
            }
        });

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    Map<String, String> map = new HashMap<String, String>(2);
                    map.put(LibSearchPresnter.TYPE, mSpinner.getSelectedItem().toString());
                    map.put(LibSearchPresnter.CONTENT, mEditText.getText().toString());
                    mPresenter.search(map);
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    private void setRecyclerViewManager(final LinearLayoutManager manager) {
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.clearOnScrollListeners();
                mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(manager) {
                    @Override
                    public void onLoadMore(int currentPage) {
                        mPresenter.getNextPage();
                    }
                });
            }
        }, 5000);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void showOnSearching() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void showOnSearchResultOk(List<BookInfo> infos) {
        mAdapter.notifyItemRangeRemoved(0, mAdapter.getItemCount() - 1);
        mAdapter.addNewBooks(infos);
        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
        fabUp.setVisibility(View.VISIBLE);
        Snackbar.make(getView(), "加载了 " + infos.size() + " 条结果", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showOnSearchResultFail() {
        mSwipeRefreshLayout.setRefreshing(false);
        Snackbar.make(getView(), "没有查询到相关结果", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showOnLoadMoreOk(List<BookInfo> infos) {
        mAdapter.addBooks(infos);
        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
        Snackbar.make(getView(), "新增了 " + infos.size() + " 条结果", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showOnLoadMoreFail() {
        mSwipeRefreshLayout.setRefreshing(false);
        Snackbar.make(getView(), "没有更多结果了", Snackbar.LENGTH_SHORT).show();
        setRecyclerViewManager(mManager);
    }

    @Override
    public void setPresenter(LibSearchContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
