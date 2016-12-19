package cc.metapro.openct.gradelist;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import cc.metapro.openct.R;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.ActivityUtils;

public class GradeActivity extends AppCompatActivity {

    private final static String pdMessage = "正在加载成绩";
    private GradeContract.Presenter mPresenter;
    private AlertDialog mCAPTCHADialog;
    private GradeFragment mGradeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade);

        Toolbar toolbar = (Toolbar) findViewById(R.id.grade_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        // add fragment
        FragmentManager fm = getSupportFragmentManager();
        mGradeFragment =
                (GradeFragment) fm.findFragmentById(R.id.grade_info_container);

        if (mGradeFragment == null) {
            mGradeFragment = GradeFragment.newInstance();
            ActivityUtils.addFragmentToActivity(fm, mGradeFragment, R.id.grade_info_container);
        }

        // floating action button (for grade refresh)
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_refresh);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Loader.cmsNeedCAPTCHA()) {
                    mCAPTCHADialog.show();
                    mPresenter.loadCAPTCHA();
                } else {
                    ActivityUtils.getProgressDialog(GradeActivity.this, null, pdMessage).show();
                    mPresenter.loadOnlineGradeInfos(GradeActivity.this, "");
                }
            }
        });

        mPresenter = new GradePresenter(mGradeFragment, getCacheDir().getPath());

        setCAPTCHADialog();
    }

    private void setCAPTCHADialog() {
        final ActivityUtils.CaptchaDialogHelper captchaDialogHelper = new ActivityUtils.CaptchaDialogHelper() {
            @Override
            public void loadCAPTCHA() {
                mPresenter.loadCAPTCHA();
            }

            @Override
            public void showOnCodeEmpty() {
                Toast.makeText(GradeActivity.this, "请输入验证码", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void loadOnlineInfo() {
                ActivityUtils.getProgressDialog(GradeActivity.this, null, pdMessage).show();
                mPresenter.loadOnlineGradeInfos(GradeActivity.this, getCode());
            }
        };

        mCAPTCHADialog = ActivityUtils.getCAPTCHADialog(this, captchaDialogHelper, "刷新");
        mGradeFragment.setCAPTCHADialogHelper(captchaDialogHelper);
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
        } else if (id == R.id.cet_query) {
            mGradeFragment.showCETQueryDialog();
        }
        return super.onOptionsItemSelected(item);
    }
}
