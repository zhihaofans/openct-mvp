package cc.metapro.openct.libborrowinfo;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.data.BorrowInfo;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;


public class LibBorrowFragment extends Fragment implements LibBorrowContract.View {

    private LibBorrowContract.Presenter mPresenter;

    private AppCompatTextView mCAPTCHA;

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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lib_borrow, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.lib_borrow_recycler_view);

        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

        mBorrowAdapter = new BorrowAdapter(getContext());
        recyclerView.setAdapter(new AlphaInAnimationAdapter(mBorrowAdapter));

        SlideInLeftAnimator animator = new SlideInLeftAnimator();
        animator.setInterpolator(new OvershootInterpolator());
        recyclerView.setItemAnimator(animator);

        return view;
    }

    @Override
    public void onDestroy() {
        mPresenter.storeBorrowInfos(getContext());
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
    }

    @Override
    public void showOnResultFail() {
        LibBorrowActivity.mProgressDialog.dismiss();
        Snackbar.make(getView(), "当前没有借阅信息", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showOnResultOk(int i) {
        LibBorrowActivity.mProgressDialog.dismiss();
        Snackbar.make(getView(), "共有 " + i + " 条借阅信息", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showOnCodeEmpty() {
        Toast.makeText(getContext(), "请输入验证码", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setCAPTCHA(AppCompatTextView textView) {
        mCAPTCHA = textView;
    }

    @Override
    public void showOnCAPTCHALoaded(Drawable captcha) {
        mCAPTCHA.setText("");
        mCAPTCHA.setBackgroundDrawable(captcha);
    }

    @Override
    public void showOnCAPTCHAFail() {
        Toast.makeText(getContext(), "获取验证码失败, 再试一次", Toast.LENGTH_SHORT).show();
    }
}
