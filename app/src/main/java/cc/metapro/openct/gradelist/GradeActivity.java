package cc.metapro.openct.gradelist;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.common.base.Strings;

import cc.metapro.openct.R;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.ActivityUtils;

public class GradeActivity extends AppCompatActivity {

    private GradeContract.Presenter mPresenter;

    private AlertDialog mAlertDialog;

    private GradeFragment mGradeFragment;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade);

        Toolbar toolbar = (Toolbar) findViewById(R.id.grade_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("正在加载成绩");

        FragmentManager fm = getSupportFragmentManager();
        mGradeFragment =
                (GradeFragment) fm.findFragmentById(R.id.grade_info_container);

        if (mGradeFragment == null) {
            mGradeFragment = GradeFragment.newInstance();
            ActivityUtils.addFragmentToActivity(fm, mGradeFragment, R.id.grade_info_container);
        }

        View view = getLayoutInflater().inflate(R.layout.captcha_diaolg, null);
        final AppCompatTextView textView = (AppCompatTextView) view.findViewById(R.id.captcha_image);
        final AppCompatEditText editText = (AppCompatEditText) view.findViewById(R.id.captcha_edit_text);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.loadCAPTCHA();
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_GO || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String code = editText.getText().toString();
                    if (Strings.isNullOrEmpty(code)) {
                        mGradeFragment.showOnCodeEmpty();
                        mAlertDialog.dismiss();
                    } else {
                        mPresenter.loadOnlineGradeInfos(GradeActivity.this, code);
                    }
                    return true;
                }
                return false;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_refresh);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Loader.cmsNeedCAPTCHA()) {
                    mAlertDialog.show();
                    mPresenter.loadCAPTCHA();
                } else {
                    mProgressDialog.show();
                    mPresenter.loadOnlineGradeInfos(GradeActivity.this, "");
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("刷新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String code = editText.getText().toString();
                if (Strings.isNullOrEmpty(code)) {
                    mGradeFragment.showOnCodeEmpty();
                } else {
                    mProgressDialog.show();
                    mPresenter.loadOnlineGradeInfos(GradeActivity.this, code);
                }
            }
        });
        builder.setView(view);
        mAlertDialog = builder.create();
        mPresenter = new GradePresenter(mGradeFragment, getCacheDir().getPath());
        mGradeFragment.setOtherViews(textView, mProgressDialog);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.grade_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.grade_clear) {
            mGradeFragment.showAll(null);
            mPresenter.clearGradeInfos();
        }
        return super.onOptionsItemSelected(item);
    }
}
