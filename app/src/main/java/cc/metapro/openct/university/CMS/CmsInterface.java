package cc.metapro.openct.university.CMS;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;

/**
 * Created by jeffrey on 16/12/6.
 */

public interface CmsInterface {

    void getCAPTCHA(String path) throws IOException;

    List<ClassInfo> getClassInfos(Map<String, String> loginMap);

    List<GradeInfo> getGradeInfos(Map<String, String> loginMap);

}
