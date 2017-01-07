package cc.metapro.openct;

import android.widget.TextView;

public interface LoginPresenter extends BasePresenter {

    void loadCaptcha(final TextView view);

    void loadOnline(final String code);

}
