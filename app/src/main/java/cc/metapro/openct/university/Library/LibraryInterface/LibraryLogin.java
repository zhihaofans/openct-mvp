package cc.metapro.openct.university.Library.LibraryInterface;

import java.io.IOException;
import java.util.Map;

/**
 * Created by jeffrey on 11/23/16.
 */

public interface LibraryLogin {

    String login(Map<String, String> strings);

    void getVCODE(String path) throws IOException;
}
