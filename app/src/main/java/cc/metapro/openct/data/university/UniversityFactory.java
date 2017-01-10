package cc.metapro.openct.data.university;

/*
 *  Copyright 2015 2017 metapro.cc Jeffctor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.HTMLUtils.Form;
import cc.metapro.openct.utils.HTMLUtils.FormHandler;
import cc.metapro.openct.utils.HTMLUtils.FormUtils;
import okhttp3.ResponseBody;


public abstract class UniversityFactory {

    private static final Pattern LOGIN_SUCCESS = Pattern.compile("(当前借阅)|(个人信息)");

    UniversityInfo.LibraryInfo mLibraryInfo;

    UniversityInfo.CMSInfo mCMSInfo;

    LibraryFactory.BorrowTableInfo mBorrowTableInfo;

    CmsFactory.ClassTableInfo mClassTableInfo;

    CmsFactory.GradeTableInfo mGradeTableInfo;

    String dynPart;
    UniversityService mService;
    private boolean gotDynPart;

    @Nullable
    String login(@NonNull Map<String, String> loginMap) throws Exception {
        getDynPart();

        String loginPageHtml = mService.getPage(getLoginURL(), null).execute().body();

        FormHandler handler = new FormHandler(loginPageHtml, getLoginURL());
        Form form = handler.getForm(0);

        if (form == null) {
            throw new Exception("学校服务器好像除了点问题~\n要不等下再试试?");
        }

        Map<String, String> res = FormUtils.getLoginFiledMap(form, loginMap, true);
        String action = res.get(Constants.ACTION);
        res.remove(Constants.ACTION);

        String userCenter = mService.login(action, getLoginRefer(), res).execute().body();
        if (LOGIN_SUCCESS.matcher(userCenter).find()) {
            return userCenter;
        } else {
            throw new Exception("登录失败, 请检查您的用户名和密码\n(以及验证码)");
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
        }
    }

    public void getCAPTCHA(@NonNull String path) throws IOException {
        getDynPart();
        ResponseBody body = mService.getCAPTCHA(getCaptchaURL()).execute().body();
        StoreHelper.storeBytes(path, body.byteStream());
    }

    protected abstract String getCaptchaURL();

    protected abstract String getLoginURL();

    protected abstract String getLoginRefer();

    protected abstract void resetURLFactory();
}
