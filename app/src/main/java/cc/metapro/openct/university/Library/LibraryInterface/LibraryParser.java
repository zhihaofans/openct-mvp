package cc.metapro.openct.university.Library.LibraryInterface;

import java.util.List;

import cc.metapro.openct.data.BookInfo;
import cc.metapro.openct.data.BorrowInfo;

/**
 * Created by jeffrey on 11/23/16.
 */

public interface LibraryParser {
    List<BookInfo> parseBook(String searchResultPage);

    String getUserCenterPage(String userPage);

    List<BorrowInfo> parseBorrow(String borrowPage);
}
