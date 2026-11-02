package com.das.cleanlink;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.graphics.Color;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.net.Uri;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {
    private WebView webView;

    private static final Set<String> BLOCKED_DOMAINS = new HashSet<>(Arrays.asList(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adservice.google.com",
        "adnxs.com",
        " taboola.com".trim(),
        "outbrain.com",
        "広告.com",
        "ads.yahoo.com",
        "adcolony.com",
        "unityads.unity3d.com",
        "appsflyer.com"
    ));

    private boolean isBlocked(String url) {
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            if (host == null) return false;
            host = host.toLowerCase();

            for (String domain : BLOCKED_DOMAINS) {
                if (host.equals(domain) || host.endsWith("." + domain)) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private WebResourceResponse emptyResponse() {
        return new WebResourceResponse(
            "text/plain",
            "utf-8",
            204,
            "No Content",
            null,
            new ByteArrayInputStream(new byte[0])
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(Color.rgb(8, 11, 20));
        window.setNavigationBarColor(Color.rgb(8, 11, 20));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(8, 11, 20));

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(
                    WebView view, WebResourceRequest request) {
                if (isBlocked(request.getUrl().toString())) {
                    return emptyResponse();
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (isBlocked(url)) return true;
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(
                    WebView view, boolean isDialog,
                    boolean isUserGesture, android.os.Message resultMsg) {
                return false;
            }
        });

        setContentView(webView, new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
