package cc.metapro.openct.classtable;

import android.app.ProgressDialog;
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
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.Constants;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

public class ClassActivity extends AppCompatActivity implements ClassContract.View {

    private ClassContract.Presenter mPresenter;

    private List<View> mViewList;

    private TodayClassAdapter mTodayClassAdapter;

    private AppCompatTextView mCaptchaTextView;

    private ViewPager mViewPager;

    private AlertDialog mAlertDialog;

    private Toolbar mToolbar;

    private ViewGroup mWeekSeq, mWeekContent, mSemSeq, mSemContent;

    private int height, width;

    private int colorIndex;

    private int classLength = Loader.getClassLength();

    private int dailyClasses = Loader.getDailyClasses();

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("正在加载课程表");

        mToolbar = (Toolbar) findViewById(R.id.class_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        width = (int) Math.round(metrics.widthPixels * (2.0 / 15.0));
        height = (int) Math.round(metrics.heightPixels * (1.0 / 15.0));

        initViewPager();

        mPresenter = new ClassPresenter(this, this, getCacheDir().getPath());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.captcha_diaolg, null);
        mCaptchaTextView = (AppCompatTextView) view.findViewById(R.id.captcha_image);
        final AppCompatEditText editText = (AppCompatEditText) view.findViewById(R.id.captcha_edit_text);
        mCaptchaTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.loadCAPTCHA();
            }
        });
        builder.setPositiveButton("更新课表", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String s = editText.getText().toString();
                if (Strings.isNullOrEmpty(s)) {
                    showOnCodeEmpty();
                } else {
                    mProgressDialog.show();
                    mPresenter.loadOnlineClassInfos(ClassActivity.this, s);
                }
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_GO || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String code = editText.getText().toString();
                    if (Strings.isNullOrEmpty(code)) {
                        showOnCodeEmpty();
                    } else {
                        mProgressDialog.show();
                        mPresenter.loadOnlineClassInfos(ClassActivity.this, code);
                        mAlertDialog.dismiss();
                    }
                    return true;
                }
                return false;
            }
        });
        builder.setView(view);
        mAlertDialog = builder.create();
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
        strip.setTabIndicatorColor(Color.parseColor("#FF4081"));
        LayoutInflater layoutInflater = getLayoutInflater();

        View td = layoutInflater.inflate(R.layout.class_today, null);
        RecyclerView todayRecyclerView = (RecyclerView) td.findViewById(R.id.class_today_recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        todayRecyclerView.setLayoutManager(manager);
        mTodayClassAdapter = new TodayClassAdapter(this);
        todayRecyclerView.setAdapter(new AlphaInAnimationAdapter(mTodayClassAdapter));
        SlideInLeftAnimator animator = new SlideInLeftAnimator();
        animator.setInterpolator(new OvershootInterpolator());
        todayRecyclerView.setItemAnimator(animator);
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
            AppCompatTextView textView = new AppCompatTextView(this);
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
                ClassInfo c = infos.get(j * 7 + i);
                if (c == null) {
                    continue;
                }

                int x = i * width;
                int y = j * height * classLength;
                if (onlyOneWeek) {
                    ClassInfo sub = c.getSubClassInfo();
                    if (c.hasClass(thisWeek)) {
                        addClassTextView(content, c, x, y);
                    } else if (c.hasSubClass()) {
                        while (sub != null) {
                            if (sub.hasClass(thisWeek)) {
                                addClassTextView(content, sub, x, y);
                            }
                            sub = sub.getSubClassInfo();
                        }
                    }
                } else {
                    addClassTextView(content, c, x, y);
                }
            }
        }
    }

    private void addClassTextView(ViewGroup content, final ClassInfo classInfo, int x, int y) {
        final TextView classInfoView = new TextView(this);
        classInfoView.setText(classInfo.toString());
        int h = classInfo.getClassLength() * height;
        if (h < 0) h = height;
        classInfoView.setMinHeight(h);
        classInfoView.setMaxHeight(h);

        classInfoView.setMaxWidth(width);
        classInfoView.setMinWidth(width);

        classInfoView.setTextSize(12);
        classInfoView.setPadding(10, 10, 10, 10);
        classInfoView.setX(x);
        classInfoView.setY(y);
        classInfoView.setGravity(Gravity.TOP);
        classInfoView.setClickable(true);
        classInfoView.setBackgroundColor(Constants.getColor(colorIndex));

        classInfoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder a = new AlertDialog.Builder(ClassActivity.this);
                a.setMessage(classInfo.toFullString());
                a.setCancelable(true);
                a.show();
                a.setCancelable(true);
            }
        });
        // exclude empty classInfo
        if (classInfo.isEmpty()) classInfoView.setVisibility(View.INVISIBLE);
        content.addView(classInfoView);
    }

    @Override
    public void updateClassInfos(List<ClassInfo> infos, int week) {
        mToolbar.setSubtitle("第 " + week + " 周");
        showCurrentSem(infos);
        showSelectedWeek(infos, week);
        showToday(infos, week);
    }

    @Override
    public void showCurrentSem(List<ClassInfo> infos) {
        addSeqViews(mSemSeq);
        addContentView(mSemContent, infos, false, -1);
    }

    @Override
    public void showToday(List<ClassInfo> infos, int week) {
        mTodayClassAdapter.setNewTodayClassInfos(infos, week);
        mTodayClassAdapter.notifyDataSetChanged();
    }

    @Override
    public void showSelectedWeek(List<ClassInfo> infos, int week) {
        addSeqViews(mWeekSeq);
        addContentView(mWeekContent, infos, true, week);
    }

    @Override
    public void showOnCAPTCHALoaded(Drawable captcha) {
        mCaptchaTextView.setBackgroundDrawable(captcha);
        mCaptchaTextView.setText("");
    }

    @Override
    public void showOnCAPTCHAFail() {
        Snackbar.make(mViewPager, "加载验证码失败, 再试一次看看", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showOnResultOk() {
        mProgressDialog.dismiss();
        if (mTodayClassAdapter.hasClassToday()) {
            int count = mTodayClassAdapter.getItemCount();
            Snackbar.make(mViewPager, "今天有 " + count + " 节课", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(mViewPager, "今天没有课, 啦啦啦~", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showOnResultFail() {
        mProgressDialog.dismiss();
        Snackbar.make(mViewPager, "当前还没有课程信息~", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showOnCodeEmpty() {
        Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setPresenter(ClassContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
