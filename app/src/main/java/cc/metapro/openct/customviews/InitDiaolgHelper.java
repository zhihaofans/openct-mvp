package cc.metapro.openct.customviews;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import cc.metapro.openct.R;

/**
 * Created by jeffrey on 16/12/8.
 */

public class InitDiaolgHelper {

    private AlertDialog.Builder ab;

    private Context mContext;

    public InitDiaolgHelper(Context context) {
        mContext = context;
    }

    public AlertDialog getInitDialog() {
        ab = new AlertDialog.Builder(mContext);
        final View view = LayoutInflater.from(mContext).inflate(R.layout.info_init_dialog_layout, null);
        final AppCompatEditText cmsUsername = (AppCompatEditText) view.findViewById(R.id.info_init_cms_username);
        final AppCompatEditText cmsPassword = (AppCompatEditText) view.findViewById(R.id.info_init_cms_password);
        final AppCompatEditText libUsername = (AppCompatEditText) view.findViewById(R.id.info_init_lib_username);
        final AppCompatEditText libPassword = (AppCompatEditText) view.findViewById(R.id.info_init_lib_password);
        final AppCompatSpinner schoolSpinner = (AppCompatSpinner) view.findViewById(R.id.info_init_school);
        final AppCompatSpinner weekSpinner = (AppCompatSpinner) view.findViewById(R.id.info_init_week);

        ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String school = mContext.getResources().getStringArray(R.array.pref_school_values)[schoolSpinner.getSelectedItemPosition()];
                String week = mContext.getResources().getStringArray(R.array.pref_week_seq_values)[weekSpinner.getSelectedItemPosition()];
                SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = p.edit();
                editor.putString("pref_school_name", school);
                editor.putString("current_week_seq", week);
                editor.putString("pref_cms_user_name", cmsUsername.getText().toString());
                editor.putString("pref_cms_password", cmsPassword.getText().toString());
                editor.putString("pref_lib_user_name", libUsername.getText().toString());
                editor.putString("pref_lib_password", libPassword.getText().toString());
                editor.apply();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                alertDialog.setTitle("提示");
                alertDialog.setMessage("你还可以在 设置 -> 校园信息 中修改你的信息哦~");
                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton("好的, 我知道了", null);
                alertDialog.show();
            }
        });
        ab.setTitle("初始化个人信息");
        ab.setCancelable(false);
        ab.setView(view);
        return ab.create();
    }
}
