package cc.metapro.openct.university;


import cc.metapro.openct.university.CMS.ClassTableInfo;
import cc.metapro.openct.university.CMS.GradeTableInfo;

/**
 * Created by jeffrey on 11/24/16.
 */

public class CMSInfo {
    public boolean mNeedCAPTCHA;

    public String
            mCmsSys, mCmsurl, mCharset,
            mUsernameBoxName,
            mPasswordBoxName,
            mRadioButtonName,
            mRadioOptionText,
            mCaptchaBoxName,
            mOtherBoxNameAndValues;

    public ClassTableInfo mClassTableInfo;
    public GradeTableInfo mGradeTableInfo;

}
