package cc.metapro.openct.university.Library.ConcreteLibrary;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.BookInfo;
import cc.metapro.openct.data.BorrowInfo;
import cc.metapro.openct.university.Library.UniversityLibrary;
import cc.metapro.openct.university.University;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.OkCurl;

/**
 * Created by jeffrey on 11/23/16.
 */

public class OPAC extends UniversityLibrary {

    private String queryTmp;

    public OPAC(University.LibraryInfo libraryInfo) {
        mLibraryInfo = libraryInfo;
        if (!mLibraryInfo.mLiburl.endsWith("/")) mLibraryInfo.mLiburl += "/";

        mSearchURL = mLibraryInfo.mLiburl + "opac/" + "openlink.php?";
        mSearchRefer = mLibraryInfo.mLiburl + "opac/" + "search.php";

        mCaptchaURL = mLibraryInfo.mLiburl + "reader/captcha.php";
        mLoginURL = mLibraryInfo.mLiburl + "reader/redr_verify.php";
        mLoginReferer = mLibraryInfo.mLiburl + "reader/login.php";
        mUserCenterURL = mLibraryInfo.mLiburl + "reader/redr_info.php";
    }

    @Nullable
    @Override
    public String login(@NonNull Map<String, String> loginMap) {
        if (loginMap.size() == 3) {
            try {
                Map<String, String> headers = new HashMap<>(1);
                headers.put("Referer", mLoginReferer);
                String loginPostContent =
                        "number=" + getUsername(loginMap) +
                                "&passwd=" + getPassword(loginMap) +
                                "&captcha=" + getCODE(loginMap) +
                                "&select=cert_no&returnUrl=";
                return OkCurl.curlSynPOST(mLoginURL, headers, Constants.POST_CONTENT_TYPE_FORM_URLENCODED, loginPostContent).body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public void getCODE(@NonNull String path) {
        try {
            Map<String, String> headers = new HashMap<>(1);
            headers.put("Referer", mLoginReferer);
            OkCurl.curlSynGET(mCaptchaURL, headers, path);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    @Nullable
    @Override
    public List<BookInfo> search(@NonNull Map<String, String> searchMap) {
        try {
            String query = "strSearchType=" + getType(searchMap) +
                            "&match_flag=forward&historyCount=1&" +
                            "strText=" + getSearchContent(searchMap) +
                            "&doctype=ALL&displaypg=10&showmode=list&sort=CATA_DATE&orderby=desc&location=ALL";

            queryTmp = mSearchURL + query;
            Map<String, String> map = new HashMap<>(1);
            map.put("Referer", mSearchRefer);
            String resultPage = OkCurl.curlSynGET(queryTmp, map, null).body().string();

            return Strings.isNullOrEmpty(resultPage) ? null : parseBook(resultPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<BookInfo> getBooksInPageAt(@NonNull Map<String, String> searchMap) {
        try {
            Map<String, String> map = new HashMap<>(1);
            map.put("Referer", queryTmp);
            String url = mSearchURL +
                    "location=ALL&" +
                    getType(searchMap) + "=" + getSearchContent(searchMap) +
                    "&doctype=ALL&lang_code=ALL&match_flag=forward&displaypg=10&showmode=list&orderby=DESC&sort=CATA_DATE&onlylendable=no&count=677&with_ebook=&" +
                    "page=" + getPageIndex(searchMap);

            String reslutPage = OkCurl.curlSynGET(url, map, null).body().string();

            return Strings.isNullOrEmpty(reslutPage) ? null : parseBook(reslutPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    @Override
    public List<BorrowInfo> getBorrowInfo(@NonNull Map<String, String> loginMap) {
        try {
            String userPage = login(loginMap);

            // login fail
            if (Strings.isNullOrEmpty(userPage)) return null;

            // login success
            Map<String, String> headers = new HashMap<>(1);
            headers.put("Referer", mUserCenterURL);
            String libBorrowInfoURL =  mLibraryInfo.mLiburl + "reader/book_lst.php";
            String borrowPage = OkCurl.curlSynGET(libBorrowInfoURL, headers, null).body().string();

            return Strings.isNullOrEmpty(borrowPage) ? null : parseBorrow(borrowPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

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

    private List<BookInfo> parseBook(@NonNull String resultPage) {
        List<BookInfo> bookInfos = new ArrayList<>();
        Document document = Jsoup.parse(resultPage);
        Elements elements = document.select("li").attr("class", "book_list_info");
        for (Element entry : elements) {
            BookInfo b = getBookInfo(entry);
            if (b != null) {
                bookInfos.add(b);
            }
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
                    BorrowInfo info = getBorrowInfo(tr);
                    if (info != null) {
                        list.add(info);
                    }
                }
                return list;
            }
        }
        return null;
    }

}
