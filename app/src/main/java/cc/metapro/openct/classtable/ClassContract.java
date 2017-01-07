package cc.metapro.openct.classtable;

import java.util.List;

import cc.metapro.openct.BaseView;
import cc.metapro.openct.LoginPresenter;
import cc.metapro.openct.data.ClassInfo;

interface ClassContract {
    interface View extends BaseView<Presenter> {

        void updateClasses(List<ClassInfo> classes, int week);

    }

    interface Presenter extends LoginPresenter {

        void loadLocalClasses();

        void removeClassInfo(ClassInfo info);

        void storeClasses();

        void exportCLasses();
    }
}
