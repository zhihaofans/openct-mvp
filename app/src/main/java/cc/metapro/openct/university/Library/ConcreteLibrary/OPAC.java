package cc.metapro.openct.university.library.concretelibrary;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.BookInfo;
import cc.metapro.openct.data.BorrowInfo;
import cc.metapro.openct.university.UniversityInfo;
import cc.metapro.openct.university.library.AbstractLibrary;
import cc.metapro.openct.utils.OkCurl;

/**
 * Created by jeffrey on 11/23/16.
 */

public class OPAC extends AbstractLibrary {

    /**
     * support OPAC v4.5+
     */

    public OPAC(UniversityInfo.LibraryInfo libraryInfo) {
        mLibraryInfo = libraryInfo;
        if (!mLibraryInfo.mLibURL.endsWith("/")) mLibraryInfo.mLibURL += "/";

        mSearchURL = mLibraryInfo.mLibURL + "opac/" + "openlink.php?";
        mSearchRefer = mLibraryInfo.mLibURL + "opac/" + "search.php";

        mCaptchaURL = mLibraryInfo.mLibURL + "reader/captcha.php";
        mLoginURL = mLibraryInfo.mLibURL + "reader/redr_verify.php";
        mLoginReferer = mLibraryInfo.mLibURL + "reader/login.php";
        mUserCenterURL = mLibraryInfo.mLibURL + "reader/redr_info.php";
    }

    @Nullable
    @Override
    public List<BorrowInfo> genBorrowInfo(@NonNull Map<String, String> loginMap) {
        try {
            String userPage = login(loginMap);

            // login fail
            if (Strings.isNullOrEmpty(userPage)) return null;

            // login success
            Map<String, String> headers = new HashMap<>(1);
            headers.put("Referer", mUserCenterURL);
            String libBorrowInfoURL = mLibraryInfo.mLibURL + "reader/book_lst.php";
            String borrowPage = OkCurl.curlSynGET(libBorrowInfoURL, headers, null).body().string();

            return Strings.isNullOrEmpty(borrowPage) ? null : parseBorrow(borrowPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    @Override
    protected String typeTrans(String cnType) {
        switch (cnType) {
            case "书名":
                return "title";
            case "作者":
                return "author";
            case "ISBN":
                return "isbn";
            case "出版社":
                return "publisher";
            case "丛书名":
                return "series";
            default:
                return "title";
        }
    }

    @Override
    protected List<BookInfo> parseBook(@NonNull String resultPage) {
        List<BookInfo> bookInfos = new ArrayList<>();
        Document document = Jsoup.parse(resultPage, mLibraryInfo.mLibURL + "opac/");
        Elements elements = document.select("li[class=book_list_info]");
        Elements tmp = document.select("div[class=list_books]");
        elements.addAll(tmp);
        for (Element entry : elements) {
            Elements els_title = entry.children().select("h3");
            String tmp_1 = els_title.text();
            String title = els_title.select("a").text();
            String href = els_title.select("a").get(0).absUrl("href");

            if (Strings.isNullOrEmpty(title)) return null;

            title = title.split("\\.")[1];
            String[] tmps = tmp_1.split(" ");
            String content = tmps[tmps.length - 1];
            Elements els_body = entry.children().select("p");
            String author = els_body.text();
            els_body = els_body.select("span");
            String remains = els_body.text();
            author = author.substring(author.indexOf(remains) + remains.length());
            BookInfo b = new BookInfo(title, author, content, remains, href);
            bookInfos.add(b);
        }
        return bookInfos;
    }

    private List<BorrowInfo> parseBorrow(@NonNull String resultPage) {
        List<BorrowInfo> list = new ArrayList<>();
        Document doc = Jsoup.parse(resultPage, mLibraryInfo.mCharset);
        Elements elements = doc.select("table");
        for (Element e : elements) {
            if (e.attr("class").equals(mLibraryInfo.mBorrowTableInfo.mTableID)) {
                Elements trs = e.select("tr");
                trs.remove(0);
                for (Element tr : trs) {
                    Elements entry = tr.select("td");
                    String title = entry.get(mLibraryInfo.mBorrowTableInfo.mTitleIndex).text().split("/")[0];
                    String author = entry.get(mLibraryInfo.mBorrowTableInfo.mTitleIndex).text().split("/")[1];
                    BorrowInfo info = new BorrowInfo(
                            entry.get(mLibraryInfo.mBorrowTableInfo.mBarcodeIndex).text(),
                            title, author, "",
                            entry.get(mLibraryInfo.mBorrowTableInfo.mBorrowDateIndex).text(),
                            entry.get(mLibraryInfo.mBorrowTableInfo.mDueDateIndexIndex).text());
                    list.add(info);
                }
                return list;
            }
        }
        return null;
    }
}
