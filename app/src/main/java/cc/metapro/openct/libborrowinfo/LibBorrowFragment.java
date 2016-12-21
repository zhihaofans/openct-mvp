package cc.metapro.openct.libborrowinfo;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cc.metapro.openct.R;
import cc.metapro.openct.data.BorrowInfo;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.RecyclerViewHelper;


public class LibBorrowFragment extends Fragment implements LibBorrowContract.View {

    @BindView(R.id.lib_borrow_recycler_view)
    RecyclerView mRecyclerView;

    private Unbinder mUnbinder;

    private LibBorrowContract.Presenter mPresenter;

    private ActivityUtils.CaptchaDialogHelper mCaptchaDialogHelper;

    private BorrowAdapter mBorrowAdapter;

    public LibBorrowFragment() {
        // Required empty public constructor
    }

    public static LibBorrowFragment newInstance() {
        return new LibBorrowFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.loadLocalBorrowInfos(getContext());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lib_borrow, container, false);

        mUnbinder = ButterKnife.bind(this, view);

        mBorrowAdapter = new BorrowAdapter(getContext());
        RecyclerViewHelper.setRecyclerView(getContext(), mRecyclerView, mBorrowAdapter);

        return view;
    }

    @Override
    public void onDestroy() {
        mPresenter.storeBorrowInfos(getContext());
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
    public void showAll(List<BorrowInfo> infos) {
        mBorrowAdapter.setNewBorrowInfos(infos);
        mBorrowAdapter.notifyDataSetChanged();
        ActivityUtils.dismissProgressDialog();
        Snackbar.make(getView(), "共有 " + infos.size() + " 条借阅信息", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showOnLoadBorrowInfoFail() {
        ActivityUtils.dismissProgressDialog();
        Snackbar.make(getView(), "没有借阅信息可以显示", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void setCAPTCHADialog(ActivityUtils.CaptchaDialogHelper captchaDialogHelper) {
        mCaptchaDialogHelper = captchaDialogHelper;
    }

    @Override
    public void showOnCAPTCHALoaded(Drawable captcha) {
        mCaptchaDialogHelper.getCAPTCHATextView().setBackgroundDrawable(captcha);
        mCaptchaDialogHelper.getCAPTCHATextView().setText("");
    }

    @Override
    public void showOnLoadCAPTCHAFail() {
        Toast.makeText(getContext(), "获取验证码失败, 再试一次", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showOnLoginFail() {
        ActivityUtils.dismissProgressDialog();
        Toast.makeText(getContext(), R.string.login_fail, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showOnNetworkError() {
        ActivityUtils.dismissProgressDialog();
        Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showOnNetworkTimeout() {
        ActivityUtils.dismissProgressDialog();
        Toast.makeText(getContext(), R.string.netowrk_timeout, Toast.LENGTH_SHORT).show();
    }
}
