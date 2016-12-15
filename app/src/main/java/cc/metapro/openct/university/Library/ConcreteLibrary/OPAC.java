package cc.metapro.openct.university.Library.ConcreteLibrary;


import com.google.common.base.Strings;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.BookInfo;
import cc.metapro.openct.data.BorrowInfo;
import cc.metapro.openct.libsearch.LibSearchPresnter;
import cc.metapro.openct.university.Library.UniversityLibrary;
import cc.metapro.openct.university.LibraryInfo;
import cc.metapro.openct.utils.OkCurl;

import static cc.metapro.openct.utils.Constants.CAPTCHA_KEY;
import static cc.metapro.openct.utils.Constants.PASSWORD_KEY;
import static cc.metapro.openct.utils.Constants.USERNAME_KEY;

/**
 * Created by jeffrey on 11/23/16.
 */

public class OPAC extends UniversityLibrary {
    private String
            libMainURL,
            libSearchRefer, libSearchURL,
            libLoginURL, libUserCenterURL,
            libLoginPostURL, libBorrowInfoURL, libCaptchaURL;

    private boolean hasFormed;

    private LibraryInfo mLibraryInfo;

    private String queryTmp;

    public OPAC(LibraryInfo libraryInfo) {
        mLibraryInfo = libraryInfo;
        formURLs();
    }

    @Override
    public String login(Map<String, String> loginMap) {
        if (loginMap.size() == 3) {
            try {
                Map<String, String> headers = new HashMap<>(1);
                headers.put("Referer", libLoginURL);
                String loginPostContent =
                        "number=" + URLEncoder.encode(loginMap.get(USERNAME_KEY), "utf-8") +
                                "&passwd=" + URLEncoder.encode(loginMap.get(PASSWORD_KEY), "utf-8") +
                                "&captcha=" + URLEncoder.encode(loginMap.get(CAPTCHA_KEY), "utf-8") +
                                "&select=cert_no&returnUrl=";
                return OkCurl.curlSynPOST(libLoginPostURL, headers, "application/x-www-form-urlencoded", loginPostContent).body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public void getVCODE(String path) throws IOException {
        try {
            Map<String, String> headers = new HashMap<>(1);
            headers.put("Referer", libLoginURL);
            OkCurl.curlSynGET(libCaptchaURL, headers, path);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    @Override
    public List<BookInfo> parseBook(String searchResultPage) {
        if (searchResultPage == null) return new ArrayList<>();
        List<BookInfo> bookInfos = new ArrayList<>();
        Document document = Jsoup.parse(searchResultPage);
        Elements elements = document.select("li").attr("class", "book_list_info");
        for (Element e : elements) {
            Elements els_title = e.children().select("h3");
            String tmp = els_title.text();
            String title = els_title.select("a").text();
            String href = libMainURL + els_title.select("a").attr("href");
            if (Strings.isNullOrEmpty(title)) continue;
            title = title.split("\\.")[1];
            String[] tmps = tmp.split(" ");
            String content = tmps[tmps.length - 1];
            Elements els_body = e.children().select("p");
            String author = els_body.text();
            els_body = els_body.select("span");
            String remains = els_body.text();
            author = author.substring(author.indexOf(remains) + remains.length());
            BookInfo b = new BookInfo(title, author, content, remains, href);
            bookInfos.add(b);
        }
        return bookInfos;
    }

    @Override
    public String getUserCenterPage(String userPage) {
        return null;
    }

    @Override
    public List<BorrowInfo> parseBorrow(String borrowPage) {
        if (borrowPage == null) return null;
        List<BorrowInfo> list = new ArrayList<>();
        Document doc = Jsoup.parse(borrowPage, mLibraryInfo.mCharset);
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

    @Override
    public String search(String query) {
        queryTmp = query;
        Map<String, String> map = new HashMap<>(1);
        map.put("Referer", libSearchRefer);
        try {
            return OkCurl.curlSynGET(query, map, null).body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getPageAt(Map<String, String> queryKVs, int i) {
        if (queryKVs == null) {
            return null;
        }
        try {
            String type = getType(queryKVs.get(LibSearchPresnter.TYPE));
            String content = URLEncoder.encode(queryKVs.get(LibSearchPresnter.CONTENT), "UTF-8");
            Map<String, String> map = new HashMap<>(1);
            map.put("Referer", queryTmp);
            String url = libSearchURL + "location=ALL&" + type + "=" + content +
                    "&doctype=ALL&lang_code=ALL&match_flag=forward&displaypg=10" +
                    "&showmode=list&orderby=DESC&sort=CATA_DATE&onlylendable=no" +
                    "&count=677&with_ebook=&page=" + i;
            return OkCurl.curlSynGET(url, map, null).body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getType(String cnName) {
        switch (cnName) {
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
    public String getQuery(Map<String, String> kvs) {
        try {
            String type = getType(kvs.get(LibSearchPresnter.TYPE));
            String content = URLEncoder.encode(kvs.get(LibSearchPresnter.CONTENT), "UTF-8");

            String qurey = "strSearchType=" + type + "&match_flag=forward&historyCount=1&strText=" +
                    content + "&doctype=ALL&displaypg=10&showmode=list&sort=CATA_DATE&orderby=desc&location=ALL";
            return libSearchURL + qurey;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void formURLs() {
        if (!hasFormed) {
            hasFormed = true;
            if (!mLibraryInfo.mLiburl.endsWith("/")) mLibraryInfo.mLiburl += "/";
            libMainURL = mLibraryInfo.mLiburl + "opac/";
            libSearchRefer = libMainURL + "search.php";
            libSearchURL = libMainURL + "openlink.php?";
            libLoginURL = mLibraryInfo.mLiburl + "reader/login.php";
            libUserCenterURL = mLibraryInfo.mLiburl + "reader/redr_info.php";
            libLoginPostURL = mLibraryInfo.mLiburl + "reader/redr_verify.php";
            libBorrowInfoURL = mLibraryInfo.mLiburl + "reader/book_lst.php";
            libCaptchaURL = mLibraryInfo.mLiburl + "reader/captcha.php";
        }
    }

    @Override
    public String getBorrowPage(String userPage) {
        if (userPage == null) return null;
        try {
            Map<String, String> headers = new HashMap<>(1);
            headers.put("Referer", libUserCenterURL);
            return OkCurl.curlSynGET(libBorrowInfoURL, headers, null).body().string();
        } catch (IOException io) {
            io.printStackTrace();
        }
        return null;
    }

    @Override
    public String moreTime(BorrowInfo borrowInfo) {
        return null;
    }

}
