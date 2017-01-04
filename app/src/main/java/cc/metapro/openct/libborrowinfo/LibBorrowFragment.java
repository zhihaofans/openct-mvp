package cc.metapro.openct.libborrowinfo;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cc.metapro.openct.R;
import cc.metapro.openct.data.BorrowInfo;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.RecyclerViewHelper;


public class LibBorrowFragment extends Fragment implements LibBorrowContract.View {

    @BindView(R.id.lib_borrow_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.fab_refresh)
    FloatingActionButton mFab;
    private Context mContext;
    private Unbinder mUnbinder;
    private AlertDialog mCAPTCHADialog;
    private BorrowAdapter mBorrowAdapter;
    private LibBorrowContract.Presenter mPresenter;
    private ActivityUtils.CaptchaDialogHelper mCaptchaDialogHelper;

    @OnClick(R.id.fab_refresh)
    public void load() {
        if (Loader.libNeedCAPTCHA()) {
            mCAPTCHADialog.show();
            mPresenter.loadCAPTCHA();
        } else {
            ActivityUtils.getProgressDialog(mContext, null, R.string.loading_borrow_info).show();
            mPresenter.loadOnlineBorrows("");
        }
    }

    @Override
    public void onResume() {
        mPresenter.start();
        super.onResume();
    }

    @Override
    public void
    onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View
    onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lib_borrow, container, false);
        mContext = getContext();
        mUnbinder = ButterKnife.bind(this, view);

        mBorrowAdapter = new BorrowAdapter(getContext());
        RecyclerViewHelper.setRecyclerView(getContext(), mRecyclerView, mBorrowAdapter);

        mCaptchaDialogHelper = new ActivityUtils.CaptchaDialogHelper() {
            @Override
            public void loadCAPTCHA() {
                mPresenter.loadCAPTCHA();
            }

            @Override
            public void showOnCodeEmpty() {
                Toast.makeText(mContext, "请输入验证码", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void loadOnlineInfo() {
                ActivityUtils.getProgressDialog(mContext, null, R.string.loading_borrow_info).show();
                mPresenter.loadOnlineBorrows(getCode());
            }
        };
        mCAPTCHADialog = ActivityUtils.getCAPTCHADialog(mContext, mCaptchaDialogHelper, "刷新");

        return view;
    }

    @Override
    public void onDestroy() {
        mPresenter.storeBorrows();
        mUnbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void setPresenter(LibBorrowContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showDue(List<BorrowInfo> infos) {
        List<BorrowInfo> dueInfo = new ArrayList<>(infos.size());
        for (BorrowInfo b : infos) {
            if (b.isExceeded()) {
                dueInfo.add(b);
            }
        }
        mBorrowAdapter.setNewBorrowInfos(dueInfo);
        mBorrowAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoadBorrows(List<BorrowInfo> infos) {
        mBorrowAdapter.setNewBorrowInfos(infos);
        mBorrowAdapter.notifyDataSetChanged();
        ActivityUtils.dismissProgressDialog();
        Toast.makeText(mContext, "共有 " + infos.size() + " 条借阅信息", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCaptchaPicLoaded(Drawable captcha) {
        TextView textView = mCaptchaDialogHelper.getCAPTCHATextView();
        textView.setText("");
        textView.setBackground(captcha);
    }

}
