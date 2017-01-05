package cc.metapro.openct.gradelist;

import java.util.List;
import java.util.Map;

import cc.metapro.openct.BaseView;
import cc.metapro.openct.LoginPresenter;
import cc.metapro.openct.data.GradeInfo;

interface GradeContract {

    interface View extends BaseView<Presenter> {

        void onLoadGrades(List<GradeInfo> infos);

        void showCETDialog();

        void onLoadCETGrade(Map<String, String> resultMap);

    }

    interface Presenter extends LoginPresenter {

        void loadLocalGrades();

        void loadCETGrade(Map<String, String> queryMap);

        void storeGrades();

        void clearGrades();
    }
}
