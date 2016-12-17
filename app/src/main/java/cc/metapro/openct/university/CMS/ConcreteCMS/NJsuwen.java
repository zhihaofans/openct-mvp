package cc.metapro.openct.university.cms.concretecms;

import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.university.UniversityInfo.CMSInfo;
import cc.metapro.openct.university.cms.AbstractCMS;
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
    public List<ClassInfo> getClassInfos(Map<String, String> loginMap) {
        try {
            String userCenter = login(loginMap);
            // login fail, no more actions
            if (Strings.isNullOrEmpty(userCenter)) return null;

            // login success, fetch class table
            String tableAddr = mCMSInfo.mCmsURL + "public/kebiaoall.aspx";
            Map<String, String> headers = new HashMap<>(1);
            headers.put("Referer", mCMSInfo.mCmsURL);
            String tablePage = OkCurl.curlSynGET(tableAddr, headers, null).body().string();

            // fetch class table page fail, no more action
            if (Strings.isNullOrEmpty(tablePage)) return null;

            tablePage = tablePage.replaceAll("◇", "&");
            Document doc = Jsoup.parse(tablePage);
            Elements tables = doc.select("table");
            Element targetTable = null;
            for (Element table : tables) {
                if (mCMSInfo.mClassTableInfo.mClassTableID.equals(table.attr("id"))) {
                    targetTable = table;
                    break;
                }
            }

            return targetTable == null ? null : generateClassInfos(targetTable);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    @Override
    public List<GradeInfo> getGradeInfos(Map<String, String> loginMap) {
        try {
            String userCenter = login(loginMap);

            if (Strings.isNullOrEmpty(userCenter)) return null;

            // login success, fetch class table
            String tableAddr = mCMSInfo.mCmsURL + "student/chengji.aspx";
            Map<String, String> headers = new HashMap<>(1);
            headers.put("Referer", mCMSInfo.mCmsURL);
            String tablePage = OkCurl.curlSynGET(tableAddr, headers, null).body().string();

            // fetch class table page fail, no more action
            if (Strings.isNullOrEmpty(tablePage)) return null;

            Document doc = Jsoup.parse(tablePage, mCMSInfo.mCmsURL);
            Elements tables = doc.select("table");
            Element targetTable = null;
            for (Element table : tables) {
                if (mCMSInfo.mGradeTableInfo.mGradeTableID.equals(table.attr("id"))) {
                    targetTable = table;
                    break;
                }
            }

            return targetTable == null ? null : generateGradeInfos(targetTable);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
