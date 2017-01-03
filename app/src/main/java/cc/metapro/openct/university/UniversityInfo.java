package cc.metapro.openct.university;

import java.util.Map;

import cc.metapro.openct.data.source.DBHelper;
import cc.metapro.openct.data.source.StoreHelper;

public class UniversityInfo {

    public CMSInfo mCMSInfo;
    public LibraryInfo mLibraryInfo;

    public static class LibraryInfo {

        public String mLibSys, mLibURL;

        public boolean mNeedCAPTCHA;

        public LibraryFactory.BorrowTableInfo mBorrowTableInfo;

        @Override
        public String toString() {
            return StoreHelper.getJsonText(this);
        }
    }

    public static class CMSInfo {

        public boolean mNeedCAPTCHA, mDynLoginURL;

        public String mCmsSys, mCmsURL;

        public CmsFactory.ClassTableInfo mClassTableInfo;
        public CmsFactory.GradeTableInfo mGradeTableInfo;

        @Override
        public String toString() {
            return StoreHelper.getJsonText(this);
        }
    }

    public static class SchoolInfo {

        public String
                abbr, name,
                cmsSys, cmsURL,
                libSys, libURL;

        public boolean
                cmsDynURL, cmsCaptcha, cmsInnerAccess,
                libDynURL, libCaptcha, libInnerAccess;

        public SchoolInfo() {
        }

        public SchoolInfo(Map<String, String> stringMap, Map<String, Boolean> booleanMap) {
            abbr = stringMap.get(DBHelper.ABBR);
            name = stringMap.get(DBHelper.SCHOOL_NAME);
            cmsSys = stringMap.get(DBHelper.CMS_SYS);
            cmsURL = stringMap.get(DBHelper.CMS_URL);
            libSys = stringMap.get(DBHelper.LIB_SYS);
            libURL = stringMap.get(DBHelper.LIB_URL);

            cmsDynURL = booleanMap.get(DBHelper.CMS_DYN_URL);
            cmsCaptcha = booleanMap.get(DBHelper.CMS_CAPTCHA);
            cmsInnerAccess = booleanMap.get(DBHelper.CMS_INNER_ACCESS);
            libDynURL = booleanMap.get(DBHelper.LIB_DYN_URL);
            libCaptcha = booleanMap.get(DBHelper.LIB_CAPTCHA);
            libInnerAccess = booleanMap.get(DBHelper.LIB_INNER_ACCESS);
        }
    }
}