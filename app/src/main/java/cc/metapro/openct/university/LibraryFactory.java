package cc.metapro.openct.university;


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
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import cc.metapro.openct.data.BookInfo;
import cc.metapro.openct.data.BorrowInfo;
import cc.metapro.openct.data.ServerService.ServiceGenerator;
import cc.metapro.openct.university.UniversityInfo;
import cc.metapro.openct.university.UniversityService;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.HTMLUtils.Form;
import cc.metapro.openct.utils.HTMLUtils.FormHandler;
import cc.metapro.openct.utils.HTMLUtils.FormUtils;
import cc.metapro.openct.utils.OkCurl;
import okhttp3.Request;

/**
 * Created by jeffrey on 11/23/16.
 */

public class LibraryFactory {

    private final static Pattern loginSuccessPattern = Pattern.compile("当前借阅");

    private final static Pattern nextPagePattern = Pattern.compile("(下一页)");
    private static String nextPageURL;
    protected UniversityInfo.LibraryInfo mLibraryInfo;
    // Strings related to login
    private String mLoginURL, mCaptchaURL, mUserCenterURL, mLoginReferer;

    // Strings related to search
    private String mSearchRefer, mSearchURL;

    private String libBorrowInfoURL;

    private String pageResult;

    public LibraryFactory(UniversityInfo.LibraryInfo libraryInfo) {
        mLibraryInfo = libraryInfo;
        mLibraryInfo = libraryInfo;
        if (!mLibraryInfo.mLibURL.endsWith("/")) mLibraryInfo.mLibURL += "/";

        switch (mLibraryInfo.mLibSys) {
            case "njhuiwen":
                mSearchURL = mLibraryInfo.mLibURL + "opac/" + "openlink.php?";
                mSearchRefer = mLibraryInfo.mLibURL + "opac/" + "search.php";
                mCaptchaURL = mLibraryInfo.mLibURL + "reader/captcha.php";
                mLoginURL = mLibraryInfo.mLibURL + "reader/redr_verify.php";
                mLoginReferer = mLibraryInfo.mLibURL + "reader/login.php";
                mUserCenterURL = mLibraryInfo.mLibURL + "reader/redr_info.php";
                libBorrowInfoURL = mLibraryInfo.mLibURL + "reader/book_lst.php";
                break;
        }
    }

    @Nullable
    private UniversityService login(@NonNull Map<String, String> loginMap) throws IOException, LoginException {
        UniversityService service = ServiceGenerator
                .createService(UniversityService.class, ServiceGenerator.HTML_CONVERTER);
        String loginPageHtml = service.getPage(mLoginURL, null).execute().body();
        FormHandler handler = new FormHandler(loginPageHtml, mLibraryInfo.mLibURL);
        Form form = handler.getForm(0);

        if (form == null) return null;

        Map<String, String> res = FormUtils.getLoginFiledMap(form, loginMap);
        String action = res.get(Constants.ACTION);
        res.remove(Constants.ACTION);
        String userCenter = service.login(action, action, res).execute().body();

        if (loginSuccessPattern.matcher(userCenter).find()) {
            pageResult = userCenter;
            return service;
        } else {
            throw new LoginException("login fail");
        }
    }

    public void getCAPTCHA(@NonNull String path) throws IOException {
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Referer", mLoginReferer);
        OkCurl.curlSynGET(mCaptchaURL, headers, path);
    }

    @Nullable
    public List<BookInfo> search(@NonNull Map<String, String> searchMap) throws IOException {
        nextPageURL = null;
        UniversityService service = ServiceGenerator.createService(UniversityService.class, ServiceGenerator.HTML_CONVERTER);
        String searchPage = service.getPage(mSearchRefer, null).execute().body();

        FormHandler handler = new FormHandler(searchPage, mLibraryInfo.mLibURL);
        Form form = handler.getForm(0);

        if (form == null) return null;
        searchMap.put(Constants.SEARCH_TYPE, typeTrans(searchMap.get(Constants.SEARCH_CONTENT)));
        Map<String, String> res = FormUtils.getLibSearchQueryMap(form, searchMap);

        String action = res.get(Constants.ACTION);
        res.remove(Constants.ACTION);
        String resultPage = service.searchLibrary(action, action, res).execute().body();

        findNextPageURL(resultPage);
        return Strings.isNullOrEmpty(resultPage) ? null : parseBook(resultPage);
    }

    @Nullable
    public List<BookInfo> getNextPage() throws IOException {
        UniversityService service = ServiceGenerator
                .createService(UniversityService.class, ServiceGenerator.HTML_CONVERTER);
        String resultPage = service.getPage(nextPageURL, null).execute().body();
        findNextPageURL(resultPage);
        return Strings.isNullOrEmpty(resultPage) ? null : parseBook(resultPage);
    }

    private void findNextPageURL(String resultPage) {
        Document result = Jsoup.parse(resultPage, mSearchURL);
        Elements links = result.select("a");
        for (Element a : links) {
            if (nextPagePattern.matcher(a.text()).find()) {
                nextPageURL = a.absUrl("href");
            }
        }
    }

    @Nullable
    public List<BorrowInfo> getBorrowInfo(@NonNull Map<String, String> loginMap)
            throws IOException, LoginException {
        UniversityService service = login(loginMap);
        String borrowPage = service
                .getPage(libBorrowInfoURL, mUserCenterURL).execute().body();
        return Strings.isNullOrEmpty(borrowPage) ? null : parseBorrow(borrowPage);
    }

    private String typeTrans(String cnType) {
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

    public static class BorrowTableInfo {
        public int
                mBarcodeIndex,
                mTitleIndex,
                mBorrowDateIndex,
                mDueDateIndexIndex;
        public String mTableID;
    }

}
