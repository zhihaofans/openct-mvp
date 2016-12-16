package cc.metapro.openct.university.Library;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.BookInfo;
import cc.metapro.openct.data.BorrowInfo;
import cc.metapro.openct.libsearch.LibSearchPresenter;
import cc.metapro.openct.university.University;
import cc.metapro.openct.utils.Constants;

/**
 * Created by jeffrey on 11/23/16.
 */

public abstract class UniversityLibrary {

    protected University.LibraryInfo mLibraryInfo;

    // Strings related to login
    protected String mLoginURL, mCaptchaURL, mUserCenterURL, mLoginReferer;

    // Strings related to search
    protected String mSearchRefer, mSearchURL;

    @Nullable
    protected abstract String login(@NonNull Map<String, String> loginMap);

    public abstract void getCODE(@NonNull String path);

    @Nullable
    public abstract List<BookInfo> search(@NonNull Map<String, String> searchMap);

    @Nullable
    public abstract List<BookInfo> getBooksInPageAt(@NonNull Map<String,String> searchMap);

    @Nullable
    public abstract List<BorrowInfo> getBorrowInfo(@NonNull Map<String, String> loginMap);

    protected abstract String typeTrans(String cnType);

    @Nullable
    protected BookInfo getBookInfo(Element entry) {
        String libMainURL = mLibraryInfo.mLiburl + "opac/";
        Elements els_title = entry.children().select("h3");
        String tmp = els_title.text();
        String title = els_title.select("a").text();
        String href = libMainURL + els_title.select("a").attr("href");

        if (Strings.isNullOrEmpty(title)) return null;

        title = title.split("\\.")[1];
        String[] tmps = tmp.split(" ");
        String content = tmps[tmps.length - 1];
        Elements els_body = entry.children().select("p");
        String author = els_body.text();
        els_body = els_body.select("span");
        String remains = els_body.text();
        author = author.substring(author.indexOf(remains) + remains.length());
        return new BookInfo(title, author, content, remains, href);
    }

    @Nullable
    protected BorrowInfo getBorrowInfo(Element entryTR) {
        try {
            Elements entry = entryTR.select("td");
            String title = entry.get(mLibraryInfo.mBorrowTableInfo.mTitleIndex).text().split("/")[0];
            String author = entry.get(mLibraryInfo.mBorrowTableInfo.mTitleIndex).text().split("/")[1];
            return new BorrowInfo
                    (
                            entry.get(mLibraryInfo.mBorrowTableInfo.mBarcodeIndex).text(),
                            title,
                            author,
                            "",
                            entry.get(mLibraryInfo.mBorrowTableInfo.mBorrowDateIndex).text(),
                            entry.get(mLibraryInfo.mBorrowTableInfo.mDueDateIndexIndex).text()
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String getType(Map<String, String> searchMap) {
        return typeTrans(searchMap.get(LibSearchPresenter.TYPE));
    }

    @Nullable
    protected String getSearchContent(Map<String, String> searchMap) {
        try {
            return URLEncoder.encode(searchMap.get(LibSearchPresenter.CONTENT), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    protected String getPageIndex(Map<String, String> searchMap) {
        try {
            return searchMap.get(LibSearchPresenter.PAGE_INDEX);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    protected String getUsername(Map<String, String> loginMap) {
        try {
            return URLEncoder.encode(loginMap.get(Constants.USERNAME_KEY), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    protected String getPassword(Map<String, String> loginMap) {
        try {
            return URLEncoder.encode(loginMap.get(Constants.PASSWORD_KEY), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    protected String getCODE(Map<String, String> loginMap) {
        try {
            return URLEncoder.encode(loginMap.get(Constants.CAPTCHA_KEY), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class BorrowTableInfo {
        public int
                mBarcodeIndex,
                mTitleIndex,
                mBorrowDateIndex,
                mDueDateIndexIndex;
        public String mTableID;
    }
}
