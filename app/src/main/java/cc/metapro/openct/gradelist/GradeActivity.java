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
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.ActivityUtils;

public class GradeActivity extends AppCompatActivity {

    @BindView(R.id.grade_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab_refresh)
    FloatingActionButton fab;
    private GradeContract.Presenter mPresenter;
    private AlertDialog mCAPTCHADialog;
    private GradeFragment mGradeFragment;

    @OnClick(R.id.fab_refresh)
    public void refresh() {
        if (Loader.cmsNeedCAPTCHA()) {
            mCAPTCHADialog.show();
            mPresenter.loadCAPTCHA();
        } else {
            ActivityUtils.getProgressDialog(this, null, R.string.loading_grade_infos).show();
            mPresenter.loadOnlineGradeInfos(this, "");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        // add fragment
        FragmentManager fm = getSupportFragmentManager();
        mGradeFragment =
                (GradeFragment) fm.findFragmentById(R.id.grade_info_container);

        if (mGradeFragment == null) {
            mGradeFragment = new GradeFragment();
            ActivityUtils.addFragmentToActivity(fm, mGradeFragment, R.id.grade_info_container);
        }

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
                Toast.makeText(GradeActivity.this, R.string.need_captcha, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void loadOnlineInfo() {
                ActivityUtils.getProgressDialog(GradeActivity.this, null, R.string.loading_grade_infos).show();
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
