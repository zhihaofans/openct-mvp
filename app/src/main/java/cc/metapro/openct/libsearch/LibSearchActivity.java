package cc.metapro.openct.libsearch;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;

import cc.metapro.openct.R;
import cc.metapro.openct.utils.ActivityUtils;

public class LibSearchActivity extends AppCompatActivity {

    private LibSearchPresnter mLibSearchPresnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lib_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        AppCompatEditText editText = (AppCompatEditText) toolbar.findViewById(R.id.lib_search_content_edittext);

        AppCompatSpinner spinner = (AppCompatSpinner) toolbar.findViewById(R.id.type_spinner);
        FragmentManager fm = getSupportFragmentManager();
        SearchResultFragment resultFragment =
                (SearchResultFragment) fm.findFragmentById(R.id.search_result_fragment_continer);

        if (resultFragment == null) {
            resultFragment = SearchResultFragment.newInstance();
            ActivityUtils.addFragmentToActivity(fm, resultFragment, R.id.search_result_fragment_continer);
        }
        resultFragment.setViews(spinner, editText);

        mLibSearchPresnter = new LibSearchPresnter(resultFragment);
    }
}
