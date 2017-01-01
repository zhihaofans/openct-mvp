package cc.metapro.openct.university;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.HTMLUtils.Form;
import cc.metapro.openct.utils.HTMLUtils.FormHandler;
import cc.metapro.openct.utils.HTMLUtils.FormUtils;
import okhttp3.ResponseBody;

/**
 * Created by jeffrey on 17/1/1.
 */

public abstract class UniversityFactory {

    private static final Pattern LOGIN_SUCCESS = Pattern.compile("(当前借阅)|(个人信息)");

    protected UniversityInfo.LibraryInfo mLibraryInfo;

    protected UniversityInfo.CMSInfo mCMSInfo;

    protected LibraryFactory.BorrowTableInfo mBorrowTableInfo;

    protected CmsFactory.ClassTableInfo mClassTableInfo;

    protected CmsFactory.GradeTableInfo mGradeTableInfo;

    protected String dynPart;
    protected UniversityService mService;
    private boolean gotDynPart;

    @Nullable
    protected String login(@NonNull Map<String, String> loginMap) throws IOException, LoginException {
        getDynPart();

        String loginPageHtml = mService.getPage(getLoginURL(), null).execute().body();

        FormHandler handler = new FormHandler(loginPageHtml, getLoginURL());
        Form form = handler.getForm(0);

        if (form == null) return null;

        Map<String, String> res = FormUtils.getLoginFiledMap(form, loginMap, true);
        String action = res.get(Constants.ACTION);
        res.remove(Constants.ACTION);

        String userCenter = mService.login(action, getLoginReferer(), res).execute().body();
        if (LOGIN_SUCCESS.matcher(userCenter).find()) {
            return userCenter;
        } else {
            throw new LoginException("login fail");
        }
    }

    private void getDynPart() {
        if (mCMSInfo != null) {
            if (mCMSInfo.mDynLoginURL && !gotDynPart) {
                try {
                    String dynURL;
                    URL url = new URL(mCMSInfo.mCmsURL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setInstanceFollowRedirects(false);
                    if (conn.getResponseCode() == 302) {
                        dynURL = conn.getHeaderField("Location");
                        if (!Strings.isNullOrEmpty(dynURL)) {
                            Pattern pattern = Pattern.compile("\\(.*\\)+");
                            Matcher m = pattern.matcher(dynURL);
                            if (m.find()) {
                                dynPart = m.group();

                            }
                        }
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!Strings.isNullOrEmpty(dynPart)) {
                    resetURLFactory();
                    gotDynPart = true;
                }
            }
        } else if (mLibraryInfo != null) {

        }
    }

    public void getCAPTCHA(@NonNull String path) throws IOException {
        getDynPart();

        ResponseBody body = mService.getCAPTCHA(getCaptchaURL()).execute().body();
        StoreHelper.storeBytes(path, body.byteStream());
    }

    protected abstract String getCaptchaURL();

    protected abstract String getLoginURL();

    protected abstract String getLoginReferer();

    protected abstract void resetURLFactory();
}
