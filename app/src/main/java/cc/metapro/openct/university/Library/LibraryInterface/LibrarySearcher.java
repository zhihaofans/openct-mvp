package cc.metapro.openct.university.Library.LibraryInterface;

import java.util.Map;

/**
 * Created by jeffrey on 11/23/16.
 */

public interface LibrarySearcher {
    String search(String query);

    String getPageAt(Map<String, String> kvs, int i);

    String getQuery(Map<String, String> kvs);
}
