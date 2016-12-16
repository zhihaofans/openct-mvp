package cc.metapro.openct.university;

import cc.metapro.openct.university.CMS.AbstractCMS;
import cc.metapro.openct.university.Library.UniversityLibrary;

public class University {
    public CMSInfo mCMSInfo;
    public LibraryInfo mLibraryInfo;


    public static class LibraryInfo{
        public String mLibSys, mLiburl, mCharset;

        public boolean mNeedCAPTCHA;

        public UniversityLibrary.BorrowTableInfo mBorrowTableInfo;
    }

    public static class CMSInfo{
        public boolean mNeedCAPTCHA;

        public String
                mCmsSys, mCmsurl, mCharset,
                mUsernameBoxName,
                mPasswordBoxName,
                mRadioButtonName,
                mRadioOptionText,
                mCaptchaBoxName,
                mOtherBoxNameAndValues;

        public AbstractCMS.ClassTableInfo mClassTableInfo;
        public AbstractCMS.GradeTableInfo mGradeTableInfo;
    }
}