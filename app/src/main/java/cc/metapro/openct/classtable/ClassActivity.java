package cc.metapro.openct.classtable;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.WindowDecorActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.RecyclerViewHelper;

public class ClassActivity extends AppCompatActivity implements ClassContract.View {

    private final static String pdMessage = "正在加载课程表";
    private ClassContract.Presenter mPresenter;
    private List<View> mViewList;
    private TodayClassAdapter mTodayClassAdapter;
    private ViewPager mViewPager;
    private AlertDialog mAlertDialog;
    private Toolbar mToolbar;
    private ViewGroup mWeekSeq, mWeekContent, mSemSeq, mSemContent;
    private int height, width;
    private int colorIndex;
    private int classLength = Loader.getClassLength();
    private int dailyClasses = Loader.getDailyClasses();
    private ActivityUtils.CaptchaDialogHelper mCaptchaDialogHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class);

        mToolbar = (Toolbar) findViewById(R.id.class_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        width = (int) Math.round(metrics.widthPixels * (2.0 / 15.0));
        height = (int) Math.round(metrics.heightPixels * (1.0 / 15.0));

        initViewPager();

        mPresenter = new ClassPresenter(this, this, getCacheDir().getPath());

        mCaptchaDialogHelper = new ActivityUtils.CaptchaDialogHelper() {
            @Override
            public void loadCAPTCHA() {
                mPresenter.loadCAPTCHA();
            }

            @Override
            public void showOnCodeEmpty() {
                Toast.makeText(ClassActivity.this, "请输入验证码", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void loadOnlineInfo() {
                ActivityUtils.getProgressDialog(ClassActivity.this, null, pdMessage).show();
                mPresenter.loadOnlineClassInfos(ClassActivity.this, getCode());
            }
        };
        mAlertDialog = ActivityUtils.getCAPTCHADialog(this, mCaptchaDialogHelper, "更新课表");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.class_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh_classes) {
            if (Loader.cmsNeedCAPTCHA()) {
                mPresenter.loadCAPTCHA();
                mAlertDialog.show();
            } else {
                ActivityUtils.getProgressDialog(ClassActivity.this, null, pdMessage).show();
                mPresenter.loadOnlineClassInfos(this, "");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.loadLocalClassInfos(this);
    }

    @Override
    protected void onDestroy() {
        mPresenter.storeClassInfos(this);
        super.onDestroy();
    }

    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.class_view_pager);
        mViewList = new ArrayList<>();
        PagerTabStrip strip = (PagerTabStrip) findViewById(R.id.class_view_pager_title);
        strip.setTextColor(Color.WHITE);
        strip.setTabIndicatorColor(getResources().getColor(R.color.colorAccent));
        LayoutInflater layoutInflater = getLayoutInflater();

        View td = layoutInflater.inflate(R.layout.class_today, null);
        RecyclerView todayRecyclerView = (RecyclerView) td.findViewById(R.id.class_today_recycler_view);
        mTodayClassAdapter = new TodayClassAdapter(this);
        RecyclerViewHelper.setRecyclerView(this, todayRecyclerView, mTodayClassAdapter);
        mViewList.add(td);

        View tw = layoutInflater.inflate(R.layout.class_current_week, null);
        mWeekSeq = (LinearLayout) tw.findViewById(R.id.class_seq);
        mWeekContent = (RelativeLayout) tw.findViewById(R.id.class_content);
        mViewList.add(tw);

        View ts = layoutInflater.inflate(R.layout.class_current_sem, null);
        mSemSeq = (LinearLayout) ts.findViewById(R.id.sem_class_seq);
        mSemContent = (RelativeLayout) ts.findViewById(R.id.sem_class_content);
        mViewList.add(ts);

        final List<String> titles = new ArrayList<>();
        titles.add("今日课表");
        titles.add("本周课表");
        titles.add("学期课表");
        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return titles.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mViewList.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(mViewList.get(position));
                return mViewList.get(position);
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return titles.get(position);
            }
        });
        mViewPager.setCurrentItem(0);
    }

    private void addSeqViews(ViewGroup index) {
        index.removeAllViews();
        for (int i = 1; i <= dailyClasses * classLength; i++) {
            TextView textView = new TextView(this);
            if (classLength == 2) {
                if (i % classLength == 0) continue;
                textView.setText("第\n" + i + "\n~\n" + (i + 1) + "\n节");
            } else {
                textView.setText("第\n" + i + "\n节");
            }
            textView.setGravity(Gravity.CENTER);
            textView.setMinHeight(height * classLength);
            textView.setMaxHeight(height * classLength);
            textView.setTextSize(10);
            index.addView(textView);
        }
    }

    private void addContentView(ViewGroup content, List<ClassInfo> infos, boolean onlyOneWeek, int thisWeek) {
        content.removeAllViews();
        if (infos.size() < 7 * dailyClasses) return;
        for (int i = 0; i < 7; i++) {
            colorIndex = i;
            if (colorIndex > Constants.colorString.length) {
                colorIndex /= 3;
            }
            for (int j = 0; j < dailyClasses; j++) {
                colorIndex++;
                if (colorIndex >= Constants.colorString.length) {
                    colorIndex = 0;
                }
                ClassInfo classInfo = infos.get(j * 7 + i);
                if (classInfo == null) {
                    continue;
                }

                int x = i * width;
                int y = j * height * classLength;
                if (onlyOneWeek) {
                    if (classInfo.hasClass(thisWeek)  && classInfo.isActive()) {
                        addClassInfoView(content, classInfo, x, y);
                    }
                    while (classInfo.hasSubClass()) {
                        classInfo = classInfo.getSubClassInfo();
                        if (classInfo.hasClass(thisWeek)  && classInfo.isActive()) {
                            addClassInfoView(content, classInfo, x, y);
                        }
                    }
                } else {
                    addClassInfoView(content, classInfo, x, y);
                }
            }
        }
    }

    private void addClassInfoView(ViewGroup content, final ClassInfo classInfo, int x, int y) {
        // exclude empty classInfo
        if (classInfo.isEmpty()) return;

        final CardView cardView = (CardView) LayoutInflater.from(this).inflate(R.layout.item_class_info, null);
        TextView classInfoView = (TextView) cardView.findViewById(R.id.class_name);
        classInfoView.setText(classInfo.getName() + "@" + classInfo.getPlace());

        int h = classInfo.getLength() * height;

        if (h < 0 || h >= classLength * dailyClasses * height)
            h = height * classLength;

        cardView.setX(x);
        cardView.setY(y);

        cardView.setCardBackgroundColor(Constants.getColor(colorIndex));

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder a = new AlertDialog.Builder(ClassActivity.this);
                a.setMessage(classInfo.toFullString());
                a.setCancelable(true);
                a.setPositiveButton("返回", null);
                a.setNeutralButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog.Builder b = new AlertDialog.Builder(ClassActivity.this);
                        b.setNegativeButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mPresenter.removeClassInfo(classInfo);
                            }
                        });
                        b.setPositiveButton("取消", null);
                        b.setTitle("警告");
                        b.setMessage("这节课将被删除!\n\nPS: 该操作仅对今日课表和本周课表有效");
                        b.show();
                    }
                });
                a.setTitle("课程信息");
                a.show();
            }
        });

        content.addView(cardView);

        ViewGroup.LayoutParams params = cardView.getLayoutParams();
        params.width = width;
        params.height = h;
    }

    @Override
    public void updateClassInfos(List<ClassInfo> infos, int week) {
        mToolbar.setSubtitle("第 " + week + " 周");
        showCurrentSem(infos);
        showSelectedWeek(infos, week);
        showToday(infos, week);
        ActivityUtils.dismissProgressDialog();

        if (mTodayClassAdapter.hasClassToday()) {
            int count = mTodayClassAdapter.getItemCount();
            Snackbar.make(mViewPager, "今天有 " + count + " 节课", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(mViewPager, "今天没有课, 啦啦啦~", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showCurrentSem(List<ClassInfo> infos) {
        addSeqViews(mSemSeq);
        addContentView(mSemContent, infos, false, -1);
    }

    private void showToday(List<ClassInfo> infos, int week) {
        mTodayClassAdapter.setNewTodayClassInfos(infos, week);
        mTodayClassAdapter.notifyDataSetChanged();
    }

    private void showSelectedWeek(List<ClassInfo> infos, int week) {
        addSeqViews(mWeekSeq);
        addContentView(mWeekContent, infos, true, week);
    }

    @Override
    public void onCAPTCHALoaded(Drawable captcha) {
        mCaptchaDialogHelper.getCAPTCHATextView().setBackgroundDrawable(captcha);
        mCaptchaDialogHelper.getCAPTCHATextView().setText("");
    }

    @Override
    public void showOnCAPTCHAFail() {
        Snackbar.make(mViewPager, R.string.captcha_fail, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showOnResultFail() {
        ActivityUtils.dismissProgressDialog();
        Snackbar.make(mViewPager, "没有课程信息可以显示", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showOnLoginFail() {
        ActivityUtils.dismissProgressDialog();
        Toast.makeText(this, R.string.login_fail, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showOnNetworkError() {
        ActivityUtils.dismissProgressDialog();
        Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showOnNetworkTimeout() {
        ActivityUtils.dismissProgressDialog();
        Toast.makeText(this, R.string.netowrk_timeout, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setPresenter(ClassContract.Presenter presenter) {
        mPresenter = presenter;
    }

}
