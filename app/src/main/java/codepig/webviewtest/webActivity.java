package codepig.webviewtest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * webView页面
 * Created by QZD on 2016/7/13.
 */
public class webActivity extends Activity{
    private Context context;
    private WebView mWeb;
    private Intent intent;
    private String _url;
    private String pageTitle;
    private String _userAgent="boosjapp";
    private String pageDescription="";
    private ValueCallback mUploadMessage;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 1) {
            if (null == mUploadMessage) return;
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.web_l);
        intent = getIntent();

        mWeb=(WebView) findViewById(R.id.mWeb);
        mWeb.removeJavascriptInterface("searchBoxJavaBredge_");//禁止远程代码执行
        WebSettings settings = mWeb.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setSupportMultipleWindows(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);//不使用缓存
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
//        settings.setSupportZoom(true);          //支持缩放
        settings.setBuiltInZoomControls(false);  //不启用内置缩放装置
        settings.setJavaScriptEnabled(true);    //启用JS脚本
        mWeb.addJavascriptInterface(new InJavaScriptLocalObj(), "local_obj");
        _userAgent =intent.getStringExtra("userAgent");
        settings.setUserAgentString(_userAgent);

        try{
            _url =intent.getStringExtra("webUrl");
            if(!_url.equals("")) {
                mWeb.setWebChromeClient(new WebChromeClient() {
                                            @Override
                                            public void onReceivedTitle(WebView view, String title) {
                                                super.onReceivedTitle(view, title);
                                                try {
                                                    pageTitle=title;
                                                } catch (Exception e) {
                                                }
                                            }
                                        }
                );
                mWeb.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        view.loadUrl("javascript:window.local_obj.showSource('<head>'+" + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                    }
                });
                mWeb.loadUrl(_url);
            }
        }catch (Exception e){
        }
    }

    /**
     * js接口
     */
    final class InJavaScriptLocalObj {
        @JavascriptInterface
        public void showSource(String html) {
            getHtmlContent(html);
        }
    }

    /**
     * 获取内容
     * @param html
     */
    private void getHtmlContent(final String html){
//        Log.d("LOGCAT","网页内容:"+html);
        Document document = Jsoup.parse(html);
        //通过类名获取到一组Elements，获取一组中第一个element并设置其html
//                Elements elements = document.getElementsByClass("loadDesc");
//                elements.get(0).html("<p>test</p>");

        //通过ID获取到element并设置其src属性
//                Element element = document.getElementById("imageView");
//                element.attr("src","file:///test/dragon.jpg");

        pageDescription=document.select("meta[name=description]").get(0).attr("content");
        Log.d("LOGCAT","description:"+pageDescription);
    }
}
