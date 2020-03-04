package com.judopay.ui.cardverification

import android.graphics.Bitmap
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient


class CardVerificationWebViewClient(
    private val javaScriptNamespace: String?,
    private val redirectUrl: String?,
    private val onPageStarted: (() -> Unit)?,
    private val onPageLoaded: () -> Unit
) : WebViewClient() {

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        setViewport(view)
        if (url == redirectUrl) {
            view.loadUrl(
                String.format(
                    "javascript:window.%s.parseJsonFromHtml(document.documentElement.innerHTML);",
                    javaScriptNamespace
                )
            )
        } else {
            onPageLoaded.invoke()
        }
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (url == redirectUrl) {
            onPageStarted?.invoke()
            view.visibility = View.INVISIBLE
        }
    }

    private fun setViewport(view: WebView) {
        view.setInitialScale(1)
        view.loadUrl(
            "javascript:(function() {" +
                    "var meta = document.createElement('meta');" +
                    "meta.setAttribute('name','viewport');" +
                    "meta.setAttribute('content','width=device-width, initial-scale=1.0');" +
                    "var head = document.getElementsByTagName('head')[0];" +
                    "head.appendChild(meta);" +
                    "})()"
        )
    }
}