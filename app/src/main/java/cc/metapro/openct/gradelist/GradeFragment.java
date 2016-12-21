package cc.metapro.openct.gradelist;


import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cc.metapro.openct.R;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.RecyclerViewHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class GradeFragment extends Fragment implements GradeContract.View {

    @BindView(R.id.grade_recycler_view)
    RecyclerView mRecyclerView;

    private Unbinder mUnbinder;

    private GradeAdapter mGradeAdapter;

    private GradeContract.Presenter mPresenter;

    private TextView mCAPTCHA;

    private AlertDialog.Builder ab;

    public GradeFragment() {
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
        mUnbinder = ButterKnife.bind(this, view);

        mGradeAdapter = new GradeAdapter(getContext());
        RecyclerViewHelper.setRecyclerView(getContext(), mRecyclerView, mGradeAdapter);
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
        mUnbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void showAll(List<GradeInfo> infos) {
        mGradeAdapter.setNewGradeInfos(infos);
        mGradeAdapter.notifyDataSetChanged();
        ActivityUtils.dismissProgressDialog();
    }

    @Override
    public void showOnResultFail() {
        ActivityUtils.dismissProgressDialog();
        Snackbar.make(getView(), "没有成绩可以显示", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void setCAPTCHADialogHelper(ActivityUtils.CaptchaDialogHelper captchaDialogHelper) {
        mCAPTCHA = captchaDialogHelper.getCAPTCHATextView();
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
    public void showCETQueryDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.cet_query_dialog, null);
        final EditText num = (EditText) view.findViewById(R.id.cet_cert_num);
        final EditText name = (EditText) view.findViewById(R.id.cet_cert_name);

        ab = new AlertDialog.Builder(getContext());
        ab.setPositiveButton("查询", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String n = num.getText().toString();
                String na = name.getText().toString();
                if (!Strings.isNullOrEmpty(n) && !Strings.isNullOrEmpty(na)) {
                    Map<String, String> queryMap = new HashMap<>(2);
                    queryMap.put(Constants.CET_NUM_KEY, n);
                    queryMap.put(Constants.CET_NAME_KEY, na);
                    mPresenter.loadCETGradeInfos(queryMap);
                    ActivityUtils.getProgressDialog(getContext(), null, R.string.loading_cet_grade).show();
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
    public void showCETGrade(Map<String, String> resultMap) {
        String name = resultMap.get(Constants.CET_NAME_KEY);
        String school = resultMap.get(Constants.CET_SCHOOL_KEY);
        String type = resultMap.get(Constants.CET_TYPE_KEY);
        String num = resultMap.get(Constants.CET_NUM_KEY);
        String time = resultMap.get(Constants.CET_TIME_KEY);
        String grade = resultMap.get(Constants.CET_GRADE_KEY);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.cet_result_dialog, null);
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.cet_result_layout);
        if (!Strings.isNullOrEmpty(name)) {
            TextView textView = new TextView(getContext());
            textView.setText("姓名: " + name);
            textView.setTextSize(15);
            textView.setPadding(10, 10, 10, 10);
            layout.addView(textView);
        }

        if (!Strings.isNullOrEmpty(school)) {
            TextView textView = new TextView(getContext());
            textView.setText("学校: " + school);
            textView.setTextSize(15);
            textView.setPadding(10, 10, 10, 10);
            layout.addView(textView);
        }

        if (!Strings.isNullOrEmpty(type)) {
            TextView textView = new TextView(getContext());
            textView.setText("CET类型: " + type);
            textView.setTextSize(15);
            textView.setPadding(10, 10, 10, 10);
            layout.addView(textView);
        }

        if (!Strings.isNullOrEmpty(num)) {
            TextView textView = new TextView(getContext());
            textView.setText("准考证号: " + num);
            textView.setTextSize(15);
            textView.setPadding(10, 10, 10, 10);
            layout.addView(textView);
        }

        if (!Strings.isNullOrEmpty(time)) {
            TextView textView = new TextView(getContext());
            textView.setText("考试时间: " + time);
            textView.setTextSize(15);
            textView.setPadding(10, 10, 10, 10);
            layout.addView(textView);
        }

        if (!Strings.isNullOrEmpty(grade)) {
            TextView textView = new TextView(getContext());
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
    public void showOnCETGradeFail() {
        ActivityUtils.dismissProgressDialog();
        Toast.makeText(getContext(), "获取CET成绩失败, 请重试", Toast.LENGTH_SHORT).show();
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

    @Override
    public void setPresenter(GradeContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
