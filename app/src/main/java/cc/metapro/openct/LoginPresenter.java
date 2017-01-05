package cc.metapro.openct;

import android.widget.TextView;

/**
 * Created by jeffrey on 17/1/5.
 */

public interface LoginPresenter extends BasePresenter {

    void loadCaptcha(final TextView view);

    void loadOnline(final String code);

}
