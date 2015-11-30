package com.judopay.secure3d;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static android.view.View.INVISIBLE;

class ThreeDSecureWebViewClient extends WebViewClient {

    private final String acsUrl;
    private final String postbackUrl;
    private final String javaScriptNamespace;
    private final ThreeDSecureListener threeDSecureListener;

    private ThreeDSecureResultPageListener resultPageListener;

    public ThreeDSecureWebViewClient(String acsUrl, String postbackUrl, String javaScriptNamespace, ThreeDSecureListener threeDSecureListener) {
        this.acsUrl = acsUrl;
        this.postbackUrl = postbackUrl;
        this.javaScriptNamespace = javaScriptNamespace;
        this.threeDSecureListener = threeDSecureListener;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (url.equals(acsUrl)) {
            threeDSecureListener.onAuthorizationWebPageLoaded();
        } else if (url.equals(postbackUrl)) {
            view.loadUrl(String.format("javascript:window.%s.parseJsonFromHtml(document.getElementsByTagName('html')[0].innerHTML);", javaScriptNamespace));
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);

        if (url.equals(postbackUrl)) {
            if (resultPageListener != null) {
                resultPageListener.onPageStarted();
            }
            view.setVisibility(INVISIBLE);
        }
    }

    public void setResultPageListener(ThreeDSecureResultPageListener resultPageListener) {
        this.resultPageListener = resultPageListener;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);

        if (!failingUrl.startsWith(postbackUrl)) {
            threeDSecureListener.onAuthorizationWebPageLoadingError(errorCode, description, failingUrl);
        }
    }

}