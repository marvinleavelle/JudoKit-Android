package com.judopay.secure3d;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.judopay.BuildConfig;
import com.judopay.payment.ThreeDSecureInfo;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * A view that displays a 3D-Secure web page for performing additional security checks when validating
 * the payment method used by a customer. This WebView displays the page and then listens for when
 * the redirect URL is reached to obtain the payment data needed to finish the transaction.
 */
public class ThreeDSecureWebView extends WebView implements JsonParsingJavaScriptInterface.JsonListener {

    private static final String JS_NAMESPACE = "JudoPay";
    private static final String REDIRECT_URL = "https://pay.judopay.com/Android/Parse3DS";

    private ThreeDSecureListener threeDSecureListener;
    private String receiptId;

    private ThreeDSecureWebViewClient webViewClient;

    public ThreeDSecureWebView(Context context) {
        super(context);
        initialise();
    }

    public ThreeDSecureWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise();
    }

    public ThreeDSecureWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise();
    }

    @TargetApi(LOLLIPOP)
    public ThreeDSecureWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialise();
    }

    public ThreeDSecureWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
        initialise();
    }

    @SuppressLint("AddJavascriptInterface")
    private void initialise() {
        configureSettings();

        if (BuildConfig.DEBUG && SDK_INT >= KITKAT) {
            setWebContentsDebuggingEnabled(true);
        }

        addJavascriptInterface(new JsonParsingJavaScriptInterface(this), JS_NAMESPACE);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureSettings() {
        WebSettings settings = getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
    }

    /**
     *
     * @param acsUrl URL to request in the WebView for displaying the 3D-Secure page to the user
     * @param md parameter submitted to the acsUrl
     * @param paReq parameter submitted to the acsUrl
     * @param receiptId the receipt ID of the transaction
     * @throws IOException
     */
    public void authorize(final String acsUrl, final String md, final String paReq, String receiptId) throws IOException {
        List<NameValuePair> params = new LinkedList<>();

        params.add(new BasicNameValuePair("MD", md));
        params.add(new BasicNameValuePair("TermUrl", REDIRECT_URL));
        params.add(new BasicNameValuePair("PaReq", paReq));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new UrlEncodedFormEntity(params, HTTP.UTF_8).writeTo(bos);

        this.receiptId = receiptId;

        this.webViewClient = new ThreeDSecureWebViewClient(acsUrl, REDIRECT_URL, JS_NAMESPACE, threeDSecureListener);
        setWebViewClient(webViewClient);

        postUrl(acsUrl, bos.toByteArray());
    }

    /**
     * @param threeDSecureListener listener that will be notified with authorization events
     */
    public void setThreeDSecureListener(ThreeDSecureListener threeDSecureListener) {
        this.threeDSecureListener = threeDSecureListener;
    }

    /**
     * @param json data returned from the redirected page containing JSON with the 3D-Secure result
     */
    @Override
    public void onJsonReceived(String json) {
        Gson gson = new Gson();

        ThreeDSecureInfo threeDSecureResult = gson.fromJson(json, ThreeDSecureInfo.class);

        threeDSecureListener.onAuthorizationCompleted(threeDSecureResult, receiptId);
    }

    public void setResultPageListener(ThreeDSecureResultPageListener resultPageListener) {
        this.webViewClient.setResultPageListener(resultPageListener);
    }

}