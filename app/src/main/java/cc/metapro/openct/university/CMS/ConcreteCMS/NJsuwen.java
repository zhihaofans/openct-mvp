package cc.metapro.openct.university.cms.concretecms;

import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.university.UniversityInfo.CMSInfo;
import cc.metapro.openct.university.cms.AbstractCMS;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.HTMLUtils.Utils;
import cc.metapro.openct.utils.OkCurl;

/**
 * Created by jeffrey on 16/12/6.
 */

public class NJsuwen extends AbstractCMS {

    public NJsuwen(CMSInfo cmsInfo) {
        super(cmsInfo);
    }

    @Nullable
    @Override
    public List<ClassInfo> getClassInfos(Map<String, String> loginMap) throws IOException, LoginException {
        String userCenter = login(loginMap);

        // login fail, no more actions
        if (Strings.isNullOrEmpty(userCenter)) return null;

        String tableAddr = mCMSInfo.mCmsURL + "public/kebiaoall.aspx";
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Referer", mCMSInfo.mCmsURL);
        String tablePage = OkCurl.curlSynGET(tableAddr, headers, null).body().string();

        if (Strings.isNullOrEmpty(tablePage)) return null;

        return generateClassInfos(tablePage.replaceAll("◇", Constants.BR_REPLACER));
    }

    @Nullable
    @Override
    public List<GradeInfo> getGradeInfos(Map<String, String> loginMap) throws IOException, LoginException {
        String userCenter = login(loginMap);

        if (Strings.isNullOrEmpty(userCenter)) return null;

        String tableAddr = mCMSInfo.mCmsURL + "student/chengji.aspx";
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Referer", mCMSInfo.mCmsURL);
        String tablePage = OkCurl.curlSynGET(tableAddr, headers, null).body().string();

        if (Strings.isNullOrEmpty(tablePage)) return null;

        return generateGradeInfos(Utils.replaceAllBrWith(tablePage, Constants.BR_REPLACER));
    }
}
