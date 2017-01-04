package cc.metapro.openct.gradelist;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cc.metapro.openct.R;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.RecyclerViewHelper;

public class GradeFragment extends Fragment implements GradeContract.View {

    @BindView(R.id.grade_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.fab_refresh)
    FloatingActionButton fab;
    private Context mContext;
    private AlertDialog mCaptchaDialog;
    private Unbinder mUnbinder;
    private GradeAdapter mGradeAdapter;
    private GradeContract.Presenter mPresenter;
    private TextView mCAPTCHA;
    private AlertDialog.Builder ab;

    @OnClick(R.id.fab_refresh)
    public void refresh() {
        if (Loader.cmsNeedCAPTCHA()) {
            mCaptchaDialog.show();
            mPresenter.loadCAPTCHA();
        } else {
            ActivityUtils.getProgressDialog(mContext, null, R.string.loading_grade_infos).show();
            mPresenter.loadRemoteGrades("");
        }
    }

    @Override
    public View
    onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grade, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mContext = getContext();

        final ActivityUtils.CaptchaDialogHelper captchaDialogHelper =
                new ActivityUtils.CaptchaDialogHelper() {
                    @Override
                    public void loadCAPTCHA() {
                        mPresenter.loadCAPTCHA();
                    }

                    @Override
                    public void showOnCodeEmpty() {
                        Toast.makeText(mContext, R.string.need_captcha, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void loadOnlineInfo() {
                        ActivityUtils.getProgressDialog(mContext, null, R.string.loading_grade_infos).show();
                        mPresenter.loadRemoteGrades(getCode());
                    }
                };

        mCaptchaDialog = ActivityUtils.getCAPTCHADialog(mContext, captchaDialogHelper, "刷新");
        mCAPTCHA = captchaDialogHelper.getCAPTCHATextView();

        mGradeAdapter = new GradeAdapter(mContext);
        RecyclerViewHelper.setRecyclerView(mContext, mRecyclerView, mGradeAdapter);
        return view;
    }

    @Override
    public void onResume() {
        mPresenter.start();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mPresenter.storeGrades();
        mUnbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void onLoadGrades(List<GradeInfo> infos) {
        mGradeAdapter.updateGradeInfos(infos);
        mGradeAdapter.notifyDataSetChanged();
        ActivityUtils.dismissProgressDialog();
    }

    @Override
    public void onCaptchaPicLoaded(Drawable captcha) {
        mCAPTCHA.setText("");
        mCAPTCHA.setBackground(captcha);
    }

    @Override
    public void showCETDialog() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_cet_query, null);
        final EditText num = (EditText) view.findViewById(R.id.cet_cert_num);
        final EditText name = (EditText) view.findViewById(R.id.cet_cert_name);

        ab = new AlertDialog.Builder(mContext);
        ab.setPositiveButton("查询", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String n = num.getText().toString();
                String na = name.getText().toString();
                if (!Strings.isNullOrEmpty(n) && !Strings.isNullOrEmpty(na)) {
                    Map<String, String> queryMap = new HashMap<>(2);
                    queryMap.put(Constants.CET_NUM_KEY, n);
                    queryMap.put(Constants.CET_NAME_KEY, na);
                    mPresenter.loadCETGrade(queryMap);
                    ActivityUtils.getProgressDialog(mContext, null, R.string.loading_cet_grade).show();
                }
            }
        });
        ab.setTitle("CET 成绩查询");
        ab.setNegativeButton("取消", null);
        ab.setCancelable(false);
        ab.setView(view);
        ab.show();
    }

    @Override
    public void onLoadCETGrade(Map<String, String> resultMap) {
        String name = resultMap.get(Constants.CET_NAME_KEY);
        String school = resultMap.get(Constants.CET_SCHOOL_KEY);
        String type = resultMap.get(Constants.CET_TYPE_KEY);
        String num = resultMap.get(Constants.CET_NUM_KEY);
        String time = resultMap.get(Constants.CET_TIME_KEY);
        String grade = resultMap.get(Constants.CET_GRADE_KEY);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_cet_result, null);
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.cet_result_layout);
        if (!Strings.isNullOrEmpty(name)) {
            TextView textView = new TextView(mContext);
            textView.setText("姓名: " + name);
            textView.setTextSize(15);
            textView.setPadding(10, 10, 10, 10);
            layout.addView(textView);
        }

        if (!Strings.isNullOrEmpty(school)) {
            TextView textView = new TextView(mContext);
            textView.setText("学校: " + school);
            textView.setTextSize(15);
            textView.setPadding(10, 10, 10, 10);
            layout.addView(textView);
        }

        if (!Strings.isNullOrEmpty(type)) {
            TextView textView = new TextView(mContext);
            textView.setText("CET类型: " + type);
            textView.setTextSize(15);
            textView.setPadding(10, 10, 10, 10);
            layout.addView(textView);
        }

        if (!Strings.isNullOrEmpty(num)) {
            TextView textView = new TextView(mContext);
            textView.setText("准考证号: " + num);
            textView.setTextSize(15);
            textView.setPadding(10, 10, 10, 10);
            layout.addView(textView);
        }

        if (!Strings.isNullOrEmpty(time)) {
            TextView textView = new TextView(mContext);
            textView.setText("考试时间: " + time);
            textView.setTextSize(15);
            textView.setPadding(10, 10, 10, 10);
            layout.addView(textView);
        }

        if (!Strings.isNullOrEmpty(grade)) {
            TextView textView = new TextView(mContext);
            textView.setText("成绩: " + grade);
            textView.setTextSize(15);
            textView.setPadding(10, 10, 10, 10);
            layout.addView(textView);
        }

        ab.setPositiveButton("好的", null);
        ab.setNegativeButton("", null);
        ab.setView(view);
        ab.show();
        ActivityUtils.dismissProgressDialog();
    }

    @Override
    public void setPresenter(GradeContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
