package cc.metapro.openct.data;

import android.graphics.drawable.Drawable;

/**
 * Created by jeffrey on 11/29/16.
 */

public final class BookInfo {

    public String mTitle, mAuthor, mContent, mStoreInfo, mLink;

    public BookInfo(String title, String author, String content,
                    String storeInfo, String link) {
        mTitle = title;
        mAuthor = author;
        mContent = content;
        mStoreInfo = storeInfo;
        mLink = link;
    }

    @Override
    public String toString() {
        return "Book: " + mTitle + ", written by " + mAuthor;
    }
}
