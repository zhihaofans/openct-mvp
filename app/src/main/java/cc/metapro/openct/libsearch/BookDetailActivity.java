package cc.metapro.openct.libsearch;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import cc.metapro.openct.R;

public class BookDetailActivity extends AppCompatActivity {

    private static String mTitle, mURL;

    public static void actionStart(Context context, String title, String url) {
        mTitle = title;
        mURL = url;

        Intent intent = new Intent(context, BookDetailActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.book_detail_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle(mTitle);

        WebView webView = (WebView) findViewById(R.id.book_detail_web);
        webView.loadUrl(mURL);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setUseWideViewPort(true);
    }
}
