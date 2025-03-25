package com.sleeptracker.wrapper;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SamsungHealthBridge.WebViewCallback {
    private WebView webView;
    private SamsungHealthBridge healthBridge;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        setupWebView();
        
        healthBridge = new SamsungHealthBridge(this, this);
        webView.addJavascriptInterface(healthBridge, "SamsungHealthBridge");
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        
        // Load the web application
        webView.loadUrl(BuildConfig.WEB_APP_URL);
    }

    @Override
    public void onSuccess(String functionName, String result) {
        runOnUiThread(() -> {
            String script = String.format(
                "window.dispatchEvent(new CustomEvent('SamsungHealthBridge_%s_success', { detail: %s }));",
                functionName, result
            );
            webView.evaluateJavascript(script, null);
        });
    }

    @Override
    public void onError(String functionName, String error) {
        runOnUiThread(() -> {
            String script = String.format(
                "window.dispatchEvent(new CustomEvent('SamsungHealthBridge_%s_error', { detail: '%s' }));",
                functionName, error.replace("'", "\\'")
            );
            webView.evaluateJavascript(script, null);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (healthBridge != null) {
            healthBridge.disconnect();
        }
    }
} 