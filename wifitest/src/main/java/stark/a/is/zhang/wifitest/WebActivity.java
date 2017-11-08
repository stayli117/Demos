package stark.a.is.zhang.wifitest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class WebActivity extends AppCompatActivity {
    private WebView webView;

    private String url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net);
        webView = (WebView) findViewById(R.id.web);
        url = "http://www.baidu.com";
        initWebViewSettings(url);

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initWebViewSettings(String url) {

        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        WebSettings settings = webView.getSettings();
        // js
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);//设置js可以直接打开窗


        // 开启自适应
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        // 文件
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(true); //启用或禁止WebView访问文件数据
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        settings.setAllowContentAccess(true);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // localStorage
        settings.setDomStorageEnabled(true);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        settings.setAppCachePath(appCachePath);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);

        webView.setWebChromeClient(new WebChromeClientSubClass());//作界面渲梁效果，js弹框或者进度加载
        webView.setWebViewClient(new WebClient());


        //点击后退按钮,让WebView后退一页(也可以覆写Activity的onKeyDown方法)
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {  //表示按返回键时的操作
                        webView.goBack();   //后退

                        //webview.goForward();//前进
                        return true;    //已处理
                    }
                }
                return false;
            }
        });
        Log.e(TAG, "initWebViewSettings: " + url);
        webView.loadUrl(url);


    }

    private class WebChromeClientSubClass extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);

        }

        @Override
        public void onConsoleMessage(String message, int lineNumber, String sourceID) {
            Log.e("console2", message + "(" + sourceID + ":" + lineNumber + ")");
            super.onConsoleMessage(message, lineNumber, sourceID);
            TextView tv = getTv(message);


        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.e("console1", "[" + consoleMessage.messageLevel() + "] " + consoleMessage.message() + "(" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ")");
            TextView tv = getTv(System.currentTimeMillis() + consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Toast.makeText(WebActivity.this, "onJsAlert -> " + message, Toast.LENGTH_SHORT).show();
            return super.onJsAlert(view, url, message, result);
        }

        /**
         * 处理confirm弹出框
         */
        @Override
        public boolean onJsConfirm(WebView view, String url, String message,
                                   JsResult result) {
            Toast.makeText(WebActivity.this, "onJsConfirm -> " + message, Toast.LENGTH_SHORT).show();
            return super.onJsConfirm(view, url, message, result);
        }

        /**
         * 处理prompt弹出框
         */
        @Override
        public boolean onJsPrompt(WebView view, String url, String message,
                                  String defaultValue, JsPromptResult result) {
//            Toast.makeText(WebActivity.this, "onJsPrompt -> " + message, Toast.LENGTH_SHORT).show();
            return super.onJsPrompt(view, url, message, message, result);
        }
    }


    private static final String TAG = "WebActivity";

    private class WebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e(TAG, "shouldOverrideUrlLoading:--->  " + url);
            if (url.equals("hoho://people:8080")) {
                Uri uri = Uri.parse("hoho://people:8080");
                Intent intent = new Intent("android.safetydemo.schemeurl.activity");
                intent.setData(uri);
                startActivity(intent);
            }
            return false;

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //网页加载开始
//            super.onPageStarted(view, url, favicon);
            Log.e(TAG, "onPageStarted: " + url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            //网页加载失败
//            super.onReceivedError(view, request, error);
            Log.e(TAG, "onReceivedError: " + error);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            //网页加载完成
//            super.onPageFinished(view, url);
            Log.e(TAG, "onPageFinished: " + url);
//            Toast.makeText(WebActivity.this, "" + mLlContent.getChildCount(), Toast.LENGTH_SHORT).show();
//            Toast.makeText(WebActivity.this, "" + url, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        webView.stopLoading();
        webView.removeAllViews();
        webView.destroy();
        webView = null;
        super.onDestroy();
    }


    public TextView getTv(String msg) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams);
        textView.setGravity(Gravity.CENTER);
        textView.setText(msg);
        textView.setPadding(20, 0, 20, 0);
        textView.setTextSize(18);
        return textView;
    }

    @Override
    public void onBackPressed() {


        super.onBackPressed();
    }
}
