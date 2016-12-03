package cc.metapro.openct.university.CMS.CMSInterface;

import android.util.SparseArray;

import java.io.IOException;

/**
 * Created by jeffrey on 11/20/16.
 */

public interface CMSLogin {

    /**
     * Note: you should follow your own policy to generate content to post,
     * even if you are using provided OkCurl based on OkHttp3, you should be careful
     * <p>
     * a method like formPostContent in ConcretCMS/ZFsoft may be a helpful Example
     * leave it alone if you have your way
     */

    String formPostContent(SparseArray<String> values);

    /**
     * if you don't use OkCurl for login post (some condition, e.g. ZFsoft), you have to
     * set your own User Home Url
     * leave it empty if you use OkCurl to do it
     */
    void setUserHomeURL(String username);

    /**
     * @param content
     * @return
     * @throws IOException
     */
    String loginPost(String content) throws IOException;

    void prepareLoginURL() throws IOException;
}
