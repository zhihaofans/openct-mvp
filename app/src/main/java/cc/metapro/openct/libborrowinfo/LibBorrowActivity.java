package cc.metapro.openct.libborrowinfo;

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

public class LibBorrowActivity extends AppCompatActivity {

    private LibBorrowContract.Presenter mPresenter;

    private AlertDialog mCAPTCHADialog;

    private LibBorrowFragment mLibBorrowFragment;

    public static ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lib_borrow);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("正在加载借阅信息");

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.lib_borrow_toolbar);
        toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_filter));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        FragmentManager fm = getSupportFragmentManager();
        mLibBorrowFragment =
                (LibBorrowFragment) fm.findFragmentById(R.id.lib_borrow_container);

        if (mLibBorrowFragment == null) {
            mLibBorrowFragment = LibBorrowFragment.newInstance();
            ActivityUtils.addFragmentToActivity(fm, mLibBorrowFragment, R.id.lib_borrow_container);
        }

        mPresenter = new LibBorrowPresenter(mLibBorrowFragment, getCacheDir().getPath());

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
                        mLibBorrowFragment.showOnCodeEmpty();
                        mCAPTCHADialog.dismiss();
                    } else {
                        mProgressDialog.show();
                        mPresenter.loadOnlineBorrowInfos(LibBorrowActivity.this, code);
                    }
                    return true;
                }
                return false;
            }
        });

        mLibBorrowFragment.setCAPTCHA(textView);
        FloatingActionButton refreshFab = (FloatingActionButton) findViewById(R.id.fab_refresh);
        refreshFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Loader.libNeedCAPTCHA()) {
                    mCAPTCHADialog.show();
                    mPresenter.loadCAPTCHA();
                } else {
                    mProgressDialog.show();
                    mPresenter.loadOnlineBorrowInfos(LibBorrowActivity.this, "");
                }
            }
        });
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setPositiveButton("刷新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String code = editText.getText().toString();
                if (Strings.isNullOrEmpty(code)) {
                    mLibBorrowFragment.showOnCodeEmpty();
                } else {
                    mProgressDialog.show();
                    mPresenter.loadOnlineBorrowInfos(LibBorrowActivity.this, code);
                }
            }
        });
        ab.setView(view);
        mCAPTCHADialog = ab.create();
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