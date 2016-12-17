package cc.metapro.openct.university;

import cc.metapro.openct.university.cms.AbstractCMS;
import cc.metapro.openct.university.library.AbstractLibrary;

public class UniversityInfo {

    public CMSInfo mCMSInfo;
    public LibraryInfo mLibraryInfo;

    public static class LibraryInfo {
        public String mLibSys, mLibURL, mCharset, mLoginRadioOptionText;

        public boolean mNeedCAPTCHA;

        public AbstractLibrary.BorrowTableInfo mBorrowTableInfo;
    }

    public static class CMSInfo {
        public boolean mNeedCAPTCHA, mDynLoginURL;

        public String mCmsSys, mCmsURL, mRadioOptionText;

        public AbstractCMS.ClassTableInfo mClassTableInfo;
        public AbstractCMS.GradeTableInfo mGradeTableInfo;
    }
}