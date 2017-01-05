package cc.metapro.openct.homepage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import cc.metapro.openct.R;
import cc.metapro.openct.classtable.ClassActivity;
import cc.metapro.openct.customviews.InitDiaolgHelper;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.gradelist.GradeActivity;
import cc.metapro.openct.libborrow.LibBorrowActivity;
import cc.metapro.openct.libsearch.LibSearchActivity;
import cc.metapro.openct.preference.SettingsActivity;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private boolean mExitState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mExitState = false;
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean inited = preferences.getBoolean(Constants.PREF_INITED, false);
        if (!inited) {
            new InitDiaolgHelper(this).getInitDialog().show();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constants.PREF_INITED, true);
            editor.apply();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (mExitState) {
                finish();
            } else {
                Toast.makeText(this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
                mExitState = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mExitState = false;
                    }
                }, 2000);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.nav_class_table:
                intent = new Intent(this, ClassActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_grade_info:
                intent = new Intent(this, GradeActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_lib_search:
                intent = new Intent(this, LibSearchActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_lib_borrow_info:
                intent = new Intent(this, LibBorrowActivity.class);
                startActivity(intent);
                break;
//            case R.id.nav_empty_room:
//                intent = new Intent(this, RoomActivity.class);
//                startActivity(intent);
//                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        Observable
                .create(new ObservableOnSubscribe() {
                    @Override
                    public void subscribe(ObservableEmitter e) throws Exception {
                        Loader.loadUniversity(MainActivity.this);
                        ActivityUtils.encryptionCheck(MainActivity.this);
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(MainActivity.this, "FATAL: 加载学校信息失败", Toast.LENGTH_LONG).show();
                    }
                })
                .subscribe();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
//        DBManger.closeDB();
        super.onDestroy();
    }
}
