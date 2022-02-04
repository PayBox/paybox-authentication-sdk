package money.paybox.authentication_sdk.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.AttributeSet
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import money.paybox.authentication_sdk.EventListener
import org.json.JSONObject
import android.webkit.PermissionRequest

import android.webkit.WebChromeClient
import money.paybox.authentication_sdk.api.Constants
import money.paybox.authentication_sdk.api.IdentificationHelper
import java.lang.Exception


class AuthView : FrameLayout {

    private var listenerActivity: Activity? = null
    private lateinit var webView: WebView

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(
        context,
        attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        initWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView = WebView(context)
        addView(webView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowFileAccess = true

        webView.webChromeClient = object : WebChromeClient() {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionRequest(request: PermissionRequest) {
                if(request.origin.toString() == Constants.SERVER_URL) {
                    request.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
                } else {
                    request.deny()
                }
            }
        }

        webView.webViewClient = object: WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                (listenerActivity as EventListener).onLoadStarted()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webView.loadUrl(
                    "javascript:(function() {" +
                            "parent.addEventListener ('message', function(event) {" +
                            " Android.receiveMessage(JSON.stringify(event.data));});" +
                            "})()"
                )

                getListener()?.onLoadFinished()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return false
            }
        }

    }

    private fun getListener(): EventListener? {
        return if (listenerActivity is EventListener) listenerActivity as EventListener else null
    }

    fun addListener(listener: Activity) {
        listenerActivity = listener
    }

    @SuppressLint("AddJavascriptInterface")
    fun loadUrl(token: String, url: String) {
        val arr = url.split("token=")
        if (arr.size < 2) {
            getListener()?.onError("Invalid URL")
            return
        }

        webView.addJavascriptInterface(JsObject(context, getListener(), token), "Android")

        webView.loadUrl(url)
    }

    internal class JsObject(private val context: Context, private val listener: EventListener?, private val token: String) {
        @JavascriptInterface
        fun receiveMessage(result: String): Boolean {
            try {
                val helper = IdentificationHelper(context, token)
                listener?.onAuth(helper.process(JSONObject(result).getJSONObject("data")))
            } catch (e: Exception) {
                listener?.onError(e.message.toString())
            }

            return true
        }
    }
}