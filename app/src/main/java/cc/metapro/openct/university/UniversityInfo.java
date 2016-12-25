package cc.metapro.openct.university;

public class UniversityInfo {

    public CMSInfo mCMSInfo;
    public LibraryInfo mLibraryInfo;

    public static class LibraryInfo {
        public String mLibSys, mLibURL, mCharset, mLoginRadioOptionText;

        public boolean mNeedCAPTCHA;

        public LibraryFactory.BorrowTableInfo mBorrowTableInfo;
    }

    public static class CMSInfo {
        public boolean mNeedCAPTCHA, mDynLoginURL;

        public String mCmsSys, mCmsURL;

        public CmsFactory.ClassTableInfo mClassTableInfo;
        public CmsFactory.GradeTableInfo mGradeTableInfo;
    }
}