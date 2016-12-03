package cc.metapro.openct.university;


import cc.metapro.openct.university.CMS.ClassTableInfo;
import cc.metapro.openct.university.CMS.GradeTableInfo;

/**
 * Created by jeffrey on 11/24/16.
 */

public class CMSInfo extends Object {
    public boolean
            needCAPTCHA;

    public String
            cmsSys, cmsURL, charset,
            usernameBoxName,
            passwordBoxName,
            radioButtonName,
            radioOptionText,
            captchaBoxName,
            otherBoxNameAndValues,
            dynLoginURLRegualrExp;

    public ClassTableInfo classTableInfo;
    public GradeTableInfo gradeTableInfo;

}
