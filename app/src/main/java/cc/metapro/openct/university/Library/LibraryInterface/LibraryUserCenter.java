package cc.metapro.openct.university.Library.LibraryInterface;


import cc.metapro.openct.data.BorrowInfo;

/**
 * Created by jeffrey on 11/23/16.
 */

public interface LibraryUserCenter {

    String getBorrowPage(String userPage);

    String moreTime(BorrowInfo borrowInfo);

}
