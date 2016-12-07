package cc.metapro.openct.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.gradelist.GradeActivity;
import cc.metapro.openct.libborrowinfo.LibBorrowActivity;
import cc.metapro.openct.libsearch.LibSearchActivity;
import cc.metapro.openct.preference.SettingsActivity;
import cc.metapro.openct.utils.Constants;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private boolean mExitState;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case Constants.RESULT_FAIL:
                    Toast.makeText(MainActivity.this, "加载学校信息失败", Toast.LENGTH_LONG).show();
                    break;
            }
            return false;
        }
    });

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_class_table) {
            Intent intent = new Intent(this, ClassActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_grade_info) {
            Intent intent = new Intent(this, GradeActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_lib_search) {
            Intent intent = new Intent(this, LibSearchActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_lib_borrow_info) {
            Intent intent = new Intent(this, LibBorrowActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        new Loader(null, new Loader.CallBack() {
            @Override
            public void onResultOk(Object results) {

            }

            @Override
            public void onResultFail() {
                Message message = new Message();
                message.what = Constants.RESULT_FAIL;
                mHandler.sendMessage(message);
            }
        }).loadUniversity(this);
        super.onResume();
    }
}
