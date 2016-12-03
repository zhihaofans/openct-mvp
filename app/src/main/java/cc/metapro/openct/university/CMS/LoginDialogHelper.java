//package cc.metapro.openct.university.CMS;
//
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.SharedPreferences;
//import android.graphics.drawable.Drawable;
//import android.preference.PreferenceManager;
//import android.support.v7.app.AlertDialog;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.CheckBox;
//import android.widget.CompoundButton;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.victor.loading.rotate.RotateLoading;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static android.support.v7.appcompat.R.styleable.AlertDialog;
//
//
///**
// * Created by jeffrey on 11/20/16.
// */
//
//public class LoginDialogHelper {
//
//    private AlertDialog.Builder ab;
//    private Context mContext;
//    private EditText usernameText, passwordText, captchaText;
//    private TextView captchaImage;
//    private boolean isChecked;
//    private String username, password;
//    private ViewGroup weekGroup;
//    private RotateLoading captchaProgress;
//    private SharedPreferences sp;
//    private Spinner weekSpinner, schoolSpinner;
//    private CheckBox rememberMe;
//
//    public LoginDialogHelper(Context context) {
//        mContext = context;
//        commonOperation();
//    }
//
//    public AlertDialog getClassTableDialog() {
//        cmsCommonOperation();
//        setWeekSpinner();
//        setClassTableLoginButton();
//        setClassVcodeView();
//        ab.setTitle("设置");
//        return ab.create();
//    }
//
//    public AlertDialog getGradeTableDialog() {
//        cmsCommonOperation();
//        setGradeLoginButton();
//        setGradeVcodeView();
//        weekGroup.setVisibility(View.GONE);
//        ab.setTitle("登录");
//        return ab.create();
//    }
//
//    public AlertDialog getLibraryLoginDialog() {
//        ab.setTitle("登录");
//        isChecked = sp.getBoolean(REMEMBER_MY_LIB_INFO_PREF, false);
//        if (isChecked) {
//            username = university.getLibUsername();
//            password = university.getLibPassword();
//            usernameText.setText(username);
//            passwordText.setText(password);
//        }
//        weekGroup.setVisibility(View.GONE);
//        setLibLoginButton();
//        setLibOkButton();
//        setLibVcodeView();
//        setLibSchoolSpinner();
//        setRemember();
//        return ab.create();
//    }
//
//    private void commonOperation() {
//        sp = PreferenceManager.getDefaultSharedPreferences(mContext);
//
//        ab = new AlertDialog.Builder(mContext);
//        LayoutInflater inflater = LayoutInflater.from(mContext);
//        View view = inflater.inflate(R.layout.login_dialog, null);
//
//        weekSpinner = (Spinner) view.findViewById(R.id.login_dialog_week_selection);
//        schoolSpinner = (Spinner) view.findViewById(R.id.login_dialog_school_selection);
//        weekGroup = (LinearLayout) view.findViewById(R.id.login_dialog_week_selection_group);
//
//        usernameText = (EditText) view.findViewById(R.id.login_dialog_username);
//        passwordText = (EditText) view.findViewById(R.id.login_dialog_password);
//
//        captchaText = (EditText) view.findViewById(R.id.login_dialog_vcode);
//        captchaImage = (TextView) view.findViewById(R.id.login_dialog_vcode_image);
//        captchaProgress = (RotateLoading) view.findViewById(R.id.rotateloading);
//
//        rememberMe = (CheckBox) view.findViewById(R.id.login_dialog_rememberpass);
//
//        ab.setNegativeButton("取消", null);
//        ab.setView(view);
//    }
//
//    private void cmsCommonOperation() {
//        isChecked = sp.getBoolean(REMEMBER_MY_CMS_INFO_PREF, false);
//        if (isChecked) {
//            username = university.getCmsUsername();
//            password = university.getCmsPassword();
//            usernameText.setText(username);
//            passwordText.setText(password);
//        }
//        setCmsSchoolSpinner();
//        setCmsOkButton();
//        setRemember();
//    }
//
//    private void setClassTableLoginButton() {
//        ab.setNeutralButton("获取课表", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                username = usernameText.getText().toString();
//                password = passwordText.getText().toString();
//                if (isChecked) {
//                    whenCmsChecked();
//                } else {
//                    whenCmsUnChecked();
//                }
//                if (!Constants.isNetworkConnected(mContext)) {
//                    Toast.makeText(mContext, "当前没有网络, 无法获取课表", Toast.LENGTH_LONG).show();
//                    return;
//                }
//                String vcode = captchaText.getText().toString();
//                if (university.cmsNeedCAPTCHA()) {
//                    if ("".equals(vcode)) {
//                        Toast.makeText(mContext, "请输入验证码", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                }
//                university.setCmsStuUserPass(username, password);
//                university.loadClassTableFromCms(mContext, vcode);
//            }
//        });
//    }
//
//    private void setGradeLoginButton() {
//        ab.setNeutralButton(R.string.get_grades, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                username = usernameText.getText().toString();
//                password = passwordText.getText().toString();
//                if (isChecked) {
//                    whenCmsChecked();
//                } else {
//                    whenCmsUnChecked();
//                }
//                if (!Constants.isNetworkConnected(mContext)) {
//                    Toast.makeText(mContext, "当前没有网络, 无法获取课表", Toast.LENGTH_LONG).show();
//                    return;
//                }
//                String vcode = captchaText.getText().toString();
//                if (university.cmsNeedCAPTCHA()) {
//                    if ("".equals(vcode)) {
//                        Toast.makeText(mContext, "请输入验证码", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                }
//                university.setCmsStuUserPass(username, password);
//                university.loadGradeInfoFromCms(mContext, vcode);
//            }
//        });
//    }
//
//    private void setLibLoginButton() {
//        ab.setNeutralButton("现在更新", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                username = usernameText.getText().toString();
//                password = passwordText.getText().toString();
//                if (isChecked) {
//                    whenLibChecked();
//                } else {
//                    whenLibUnChecked();
//                }
//                if (!Constants.isNetworkConnected(mContext)) {
//                    Toast.makeText(mContext, "先打开网络再获取吧", Toast.LENGTH_LONG).show();
//                    return;
//                }
//                String vcode = captchaText.getText().toString();
//                if (university.libNeedCAPTCHA()) {
//                    if ("".equals(vcode)) {
//                        Toast.makeText(mContext, "请输入验证码", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                }
//                university.setLibStuUserPass(username, password);
//                university.loadBorrowInfoFromLib(mContext, vcode);
//            }
//        });
//    }
//
//    private void setLibOkButton() {
//        ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                if (isChecked) {
//                    whenLibChecked();
//                } else {
//                    whenLibUnChecked();
//                }
//            }
//        });
//    }
//
//    private void setCmsOkButton() {
//        ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                if (isChecked) {
//                    whenCmsChecked();
//                } else {
//                    whenCmsUnChecked();
//                }
//                ClassTableActivity.sendMessage(Constants.UPDATE_CLASS_TABLE_VIEWS);
//            }
//        });
//    }
//
//    private void whenLibChecked() {
//        String u = usernameText.getText().toString();
//        String p = passwordText.getText().toString();
//        university.setLibStuUserPass(u, p);
//        university.storeStuInfo(mContext);
//        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
//        editor.putBoolean(REMEMBER_MY_LIB_INFO_PREF, true);
//        editor.apply();
//    }
//
//    private void whenLibUnChecked() {
//        university.clearStuLibInfo();
//        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
//        editor.putBoolean(REMEMBER_MY_LIB_INFO_PREF, false);
//        editor.apply();
//    }
//
//    private void whenCmsChecked() {
//        String u = usernameText.getText().toString();
//        String p = passwordText.getText().toString();
//        university.setCmsStuUserPass(u, p);
//        university.storeStuInfo(mContext);
//        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
//        editor.putBoolean(REMEMBER_MY_CMS_INFO_PREF, true);
//        editor.apply();
//    }
//
//    private void whenCmsUnChecked() {
//        university.clearStuCMSInfo();
//        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
//        editor.putBoolean(REMEMBER_MY_CMS_INFO_PREF, false);
//        editor.apply();
//    }
//
//    private void setWeekSpinner() {
//        List<String> spinnserList = new ArrayList<>();
//        for (int i = 1; i < Constants.weekLimit; i++) spinnserList.add("第" + i + "周");
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, spinnserList);
//        weekSpinner.setAdapter(arrayAdapter);
//        weekSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                String tmp = adapterView.getSelectedItem().toString();
//                int weekSeq = RE.getInt(tmp).get(0);
//                university.setCurrentWeek(weekSeq);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
//        weekSpinner.setSelection(university.getCurrentWeek() - 1);
//    }
//
//    private void setLibSchoolSpinner() {
//        schoolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                String tmp = adapterView.getSelectedItem().toString();
//                tmp = RE.getEnString(tmp);
//                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
//                editor.putString(SCHOOL_PREF, tmp);
//                editor.apply();
//                String schoolJson = JSONHelper.getSchoolJson(mContext, tmp);
//                university = Constants.getGson().fromJson(schoolJson, University.class);
//                toggleVCODE(university.libNeedCAPTCHA());
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
//        String[] strings = mContext.getResources().getStringArray(R.array.school_names);
//        for (int i = 0; i < strings.length; i++) {
//            String tmp = RE.getEnString(strings[i]);
//            if (university.getSchoolName().equalsIgnoreCase(tmp)) {
//                schoolSpinner.setSelection(i, true);
//                schoolSpinner.setSelected(true);
//                break;
//            }
//        }
//        if (!schoolSpinner.isSelected()) {
//            schoolSpinner.setSelection(0, true);
//        }
//    }
//
//    private void setCmsSchoolSpinner() {
//        schoolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                String tmp = adapterView.getSelectedItem().toString();
//                tmp = RE.getEnString(tmp);
//                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
//                editor.putString(SCHOOL_PREF, tmp);
//                editor.apply();
//                String schoolJson = JSONHelper.getSchoolJson(mContext, tmp);
//                university = Constants.getGson().fromJson(schoolJson, University.class);
//                toggleVCODE(university.cmsNeedCAPTCHA());
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
//        String[] strings = mContext.getResources().getStringArray(R.array.school_names);
//        for (int i = 0; i < strings.length; i++) {
//            String tmp = RE.getEnString(strings[i]);
//            if (university.getSchoolName().equalsIgnoreCase(tmp)) {
//                schoolSpinner.setSelection(i, true);
//                schoolSpinner.setSelected(true);
//                break;
//            }
//        }
//        if (!schoolSpinner.isSelected()) {
//            schoolSpinner.setSelection(0, true);
//        }
//    }
//
//    private void setRemember() {
//        rememberMe.setChecked(false);
//        if (isChecked) {
//            rememberMe.setChecked(true);
//            if (!"".equals(username)) {
//                usernameText.setText(username);
//                passwordText.setText(password);
//            }
//        }
//        rememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                isChecked = b;
//            }
//        });
//    }
//
//    private void setGradeVcodeView() {
//        captchaText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if (b) {
//                    if (Constants.isNetworkConnected(mContext)) {
//                        if (!captchaProgress.isStart()) {
//                            captchaProgress.start();
//                            String path = mContext.getCacheDir() + CAPTCHA_FILENAME;
//                            university.getVCODEbyGradeTable(path);
//                        }
//                    } else {
//                        Toast.makeText(mContext, "当前没有网络, 无法获取验证码", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        });
//    }
//
//    private void setLibVcodeView() {
//        captchaText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if (b) {
//                    if (Constants.isNetworkConnected(mContext)) {
//                        if (!captchaProgress.isStart()) {
//                            captchaProgress.start();
//                            String path = mContext.getCacheDir() + CAPTCHA_FILENAME;
//                            university.getVCODEbyLibrary(path);
//                        }
//                    } else {
//                        Toast.makeText(mContext, "当前没有网络, 无法获取验证码", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        });
//    }
//
//    private void setClassVcodeView() {
//        captchaText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if (b) {
//                    if (Constants.isNetworkConnected(mContext)) {
//                        if (!captchaProgress.isStart()) {
//                            captchaProgress.start();
//                            String path = mContext.getCacheDir() + CAPTCHA_FILENAME;
//                            university.getVCODEbyClassTable(path);
//                        }
//                    } else {
//                        Toast.makeText(mContext, "当前没有网络, 无法获取验证码", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        });
//    }
//
//    private void toggleVCODE(boolean show) {
//        if (show) {
//            captchaImage.setVisibility(View.VISIBLE);
//            captchaText.setVisibility(View.VISIBLE);
//        } else {
//            captchaImage.setVisibility(View.GONE);
//            captchaText.setVisibility(View.GONE);
//        }
//    }
//
//    public void setVCODE(Drawable drawable) {
//        captchaImage.setBackground(drawable);
//        captchaImage.setVisibility(View.VISIBLE);
//    }
//
//    public void dismissRotate() {
//        captchaProgress.stop();
//    }
//
//}
