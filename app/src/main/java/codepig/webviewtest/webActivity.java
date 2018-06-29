package codepig.webviewtest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Set;

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
    private Button testBtn,testBtn2;

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

        testBtn=(Button) findViewById(R.id.testBtn);
        testBtn2=(Button) findViewById(R.id.testBtn2);
        testBtn.setOnClickListener(clickBtn);
        testBtn2.setOnClickListener(clickBtn);
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
        mWeb.addJavascriptInterface(new InJavaScriptLocalObj(), "local_obj");//js代码映射
        _userAgent =intent.getStringExtra("userAgent");
        if (!_userAgent.equals("jsTest")){
            testBtn.setVisibility(View.GONE);
        }
        settings.setUserAgentString(_userAgent);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWeb.setWebContentsDebuggingEnabled(true);
        }

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

                        //alert事件
                        @Override
                        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                            AlertDialog.Builder b = new AlertDialog.Builder(webActivity.this);
                            b.setTitle("Alert");
                            b.setMessage(message);
                            b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            });
                            b.setCancelable(false);
                            b.create().show();
                            return true;
                        }
                    }
                );
                mWeb.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        view.loadUrl("javascript:window.local_obj.showSource('<head>'+" + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        // 根据协议的参数，判断是否是所需要的url
                        // 一般根据scheme（协议格式） & authority（协议名）判断（前两个参数）
                        //假定传入进来的 url = "js://callApp?arg1=0&arg2=1"（同时也是约定好的需要拦截的,其中的js和callApp就是用来判断的名称）
                        Uri uri = Uri.parse(url);
                        // 如果url的协议 = 预先约定的 js 协议
                        // 就解析往下解析参数
                        if ( uri.getScheme().equals("js")) {
                            // 如果 authority  = 预先约定协议里的 callApp，即代表都符合约定的协议
                            // 所以拦截url,下面JS开始调用Android需要的方法
                            if (uri.getAuthority().equals("callApp")) {
                                Toast.makeText(webActivity.this, "js调用了Android的方法", Toast.LENGTH_SHORT).show();
                                // 可以在协议上带有参数并传递到Android上
                                Set<String> collection = uri.getQueryParameterNames();
                                for (String key : collection) {
                                    String value = uri.getQueryParameter(key);
                                    Log.d("LOGCAT", key + ":" + value);
                                }
                            }
                            return true;
                        }
                        return super.shouldOverrideUrlLoading(view, url);
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
    final class InJavaScriptLocalObj extends Object{
        @JavascriptInterface
        public void showSource(String html) {
            getHtmlContent(html);
        }

        // 被JS调用的方法必须加入@JavascriptInterface注解
        @JavascriptInterface
        public int callApp(String msg){
            Toast.makeText(webActivity.this, msg, Toast.LENGTH_SHORT).show();
            return 0;
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

    View.OnClickListener clickBtn = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()){
                case R.id.testBtn:
//                    有可能是个耗时操作，所以异步
                    mWeb.post(new Runnable() {
                        @Override
                        public void run() {
                            // 注意调用的JS方法名要对应上
                            // 调用javascript的callJS()方法
                            // 调用必须在onPageFinished后
                            mWeb.loadUrl("javascript:callJS()");
                        }
                    });
                    break;
                case R.id.testBtn2:
//                    该方法的执行不会使页面刷新，而（loadUrl ）的执行则会。
//                    Android 4.4 (19)后才可使用
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        mWeb.evaluateJavascript("javascript:callJS()", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                //此处为 js 返回的结果
                                Toast.makeText(webActivity.this, "返回值为：" + value, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    break;
            }
        }
    };
}
