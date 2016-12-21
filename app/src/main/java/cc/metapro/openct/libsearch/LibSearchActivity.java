package cc.metapro.openct.libsearch;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import cc.metapro.openct.R;
import cc.metapro.openct.utils.ActivityUtils;

public class LibSearchActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab_search)
    FloatingActionButton mFabSearch;
    @BindView(R.id.lib_search_content_edittext)
    EditText mEditText;
    @BindView(R.id.type_spinner)
    Spinner mSpinner;
    private LibSearchPresenter mLibSearchPresnter;

    @OnClick(R.id.fab_search)
    public void fabSearch() {
        mLibSearchPresnter.search();
    }

    @OnEditorAction(R.id.lib_search_content_edittext)
    public boolean editorSearch(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_SEARCH || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            mLibSearchPresnter.search();
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lib_search);

        ButterKnife.bind(this);

        // set mToolbar
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        FragmentManager fm = getSupportFragmentManager();
        SearchResultFragment resultFragment =
                (SearchResultFragment) fm.findFragmentById(R.id.search_result_fragment_continer);

        if (resultFragment == null) {
            resultFragment = new SearchResultFragment();
            ActivityUtils.addFragmentToActivity(fm, resultFragment, R.id.search_result_fragment_continer);
        }

        mLibSearchPresnter = new LibSearchPresenter(resultFragment, mSpinner, mEditText);
    }
}
