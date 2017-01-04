package cc.metapro.openct.libborrowinfo;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.utils.ActivityUtils;

public class LibBorrowActivity extends AppCompatActivity {

    @BindView(R.id.lib_borrow_toolbar)
    Toolbar mToolbar;

    private LibBorrowContract.Presenter mPresenter;

    private LibBorrowFragment mLibBorrowFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lib_borrow);

        ButterKnife.bind(this);

        // set toolbar
        mToolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_filter));
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // add fragment
        FragmentManager fm = getSupportFragmentManager();
        mLibBorrowFragment =
                (LibBorrowFragment) fm.findFragmentById(R.id.lib_borrow_container);

        if (mLibBorrowFragment == null) {
            mLibBorrowFragment = new LibBorrowFragment();
            ActivityUtils.addFragmentToActivity(fm, mLibBorrowFragment, R.id.lib_borrow_container);
        }

        mPresenter = new LibBorrowPresenter(mLibBorrowFragment, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_all_borrow_info:
                mLibBorrowFragment.onLoadBorrows(mPresenter.getBorrows());
                break;
            case R.id.show_due_borrow_info:
                mLibBorrowFragment.showDue(mPresenter.getBorrows());
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