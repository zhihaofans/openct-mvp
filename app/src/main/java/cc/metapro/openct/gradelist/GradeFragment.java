package cc.metapro.openct.gradelist;


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

import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.data.GradeInfo;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

public class GradeFragment extends Fragment implements GradeContract.View {

    private GradeAdapter mGradeAdapter;

    private GradeContract.Presenter mPresenter;

    private AppCompatTextView mCAPTCHA;

    public GradeFragment() {
        // Required empty public constructor
    }

    public static GradeFragment newInstance() {
        return new GradeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grade, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.grade_recycler_view);

        // set recycler manager
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

        // set recycler adapter
        mGradeAdapter = new GradeAdapter(getContext());
        recyclerView.setAdapter(new AlphaInAnimationAdapter(mGradeAdapter));

        SlideInLeftAnimator animator = new SlideInLeftAnimator();
        animator.setInterpolator(new OvershootInterpolator());
        recyclerView.setItemAnimator(animator);

        return view;
    }

    @Override
    public void onResume() {
        mPresenter.loadLocalGradeInfos(getContext());
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mPresenter.storeGradeInfos(getContext());
        super.onDestroy();
    }

    @Override
    public void showAll(List<GradeInfo> infos) {
        mGradeAdapter.setNewGradeInfos(infos);
        mGradeAdapter.notifyDataSetChanged();
    }

    @Override
    public void showOnResultFail() {
        Snackbar.make(getView(), "还没有这个学期的成绩", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showOnResultOk() {

    }

    @Override
    public void showOnCodeEmpty() {
        Toast.makeText(getContext(), "请输入验证码", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setCAPTCHATextView(AppCompatTextView textView) {
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

    @Override
    public void setPresenter(GradeContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
