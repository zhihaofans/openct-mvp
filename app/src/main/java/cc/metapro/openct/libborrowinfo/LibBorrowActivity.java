package cc.metapro.openct.libborrowinfo;

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

import cc.metapro.openct.R;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.ActivityUtils;

public class LibBorrowActivity extends AppCompatActivity {

    private final static String pdMessage = "正在加载借阅信息";
    private LibBorrowContract.Presenter mPresenter;
    private AlertDialog mCAPTCHADialog;
    private LibBorrowFragment mLibBorrowFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lib_borrow);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.lib_borrow_toolbar);
        toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_filter));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // floating action button (borrow info refresh)
        FloatingActionButton refreshFab = (FloatingActionButton) findViewById(R.id.fab_refresh);
        refreshFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Loader.libNeedCAPTCHA()) {
                    mCAPTCHADialog.show();
                    mPresenter.loadCAPTCHA();
                } else {
                    ActivityUtils.getProgressDialog(LibBorrowActivity.this, null, pdMessage).show();
                    mPresenter.loadOnlineBorrowInfos(LibBorrowActivity.this, "");
                }
            }
        });

        // add fragment
        FragmentManager fm = getSupportFragmentManager();
        mLibBorrowFragment =
                (LibBorrowFragment) fm.findFragmentById(R.id.lib_borrow_container);

        if (mLibBorrowFragment == null) {
            mLibBorrowFragment = LibBorrowFragment.newInstance();
            ActivityUtils.addFragmentToActivity(fm, mLibBorrowFragment, R.id.lib_borrow_container);
        }

        mPresenter = new LibBorrowPresenter(mLibBorrowFragment, getCacheDir().getPath());

        setCAPTCHADialog();
    }

    private void setCAPTCHADialog() {
        ActivityUtils.CaptchaDialogHelper captchaDialogHelper = new ActivityUtils.CaptchaDialogHelper() {
            @Override
            public void loadCAPTCHA() {
                mPresenter.loadCAPTCHA();
            }

            @Override
            public void showOnCodeEmpty() {
                mLibBorrowFragment.showOnCodeEmpty();
            }

            @Override
            public void loadOnlineInfo() {
                ActivityUtils.getProgressDialog(LibBorrowActivity.this, null, pdMessage).show();
                mPresenter.loadOnlineBorrowInfos(LibBorrowActivity.this, getCode());
            }
        };
        mCAPTCHADialog = ActivityUtils.getCAPTCHADialog(this, captchaDialogHelper, "刷新");
        mLibBorrowFragment.setCAPTCHADialog(captchaDialogHelper);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_all_borrow_info:
                mLibBorrowFragment.showAll(mPresenter.getBorrowInfos());
                break;
            case R.id.show_due_borrow_info:
                mLibBorrowFragment.showDue(mPresenter.getBorrowInfos());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.borrow_menu, menu);
        return true;
    }
}