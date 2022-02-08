package money.paybox.authentication_sdk

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import money.paybox.authentication_sdk.api.*
import money.paybox.authentication_sdk.ui.AuthView
import org.json.JSONObject
import java.util.HashMap

@Suppress("unused")
class PayBoxAuth {

    private var activity: Activity? = null
    private var fragment: Fragment? = null
    private var authView: AuthView? = null
    private var slug: String
    private var secretKey: String
    private lateinit var token: String
    private var language: Language

    private val apiListener = object : ApiListener {
        override fun onAuth(result: ResponseData) {
            if (result.code == 200) {
                val obj = JSONObject(result.response)
                getActivity()?.runOnUiThread {
                    getListener()?.let { authView?.addListener(getListener()!!) }
                    authView?.loadUrl(token, obj.getString("url"))
                }
            } else {
                getActivity()?.runOnUiThread {
                    getListener()?.onLoadFinished()
                    getListener()?.onError(result.response)
                }
            }
        }

        override fun onCheck(result: ResponseData) {
            getActivity()?.runOnUiThread {
                getListener()?.onLoadFinished()
            }

            if (result.code == 200) {
                val obj = JSONObject(result.response)
                getActivity()?.runOnUiThread {
                    val helper = IdentificationHelper(getActivity()!!, token)
                    getListener()?.onCheck(helper.process(obj))
                }
            } else {
                getActivity()?.runOnUiThread {
                    getListener()?.onError(result.response)
                }
            }
        }
    }

    private var api: APIHandler

    private constructor(activity: Activity, authView: AuthView?, slug: String, secretKey: String, token: String, language: Language) {
        this.activity = activity
        this.authView = authView
        this.slug = slug
        this.secretKey = secretKey
        this.token = token
        this.language = language

        api = APIHandler(slug, secretKey, language, apiListener)
    }

    private constructor(fragment: Fragment, authView: AuthView?, slug: String, secretKey: String, token: String, language: Language) {
        this.fragment = fragment
        this.authView = authView
        this.slug = slug
        this.secretKey = secretKey
        this.token = token
        this.language = language

        api = APIHandler(slug, secretKey, language, apiListener)
    }

    private constructor(builder: Builder) /*: this(builder.activity, builder.authView, builder.slug, builder.secretKey, builder.token, builder.language)*/ {
        this.activity = builder.activity
        this.fragment = builder.fragment
        this.authView = builder.authView
        this.slug = builder.slug
        this.secretKey = builder.secretKey
        this.token = builder.token
        this.language = builder.language

        api = APIHandler(slug, secretKey, language, apiListener)
    }


    private fun runInspection(authViewRequired: Boolean): Boolean {
        if(getActivity() == null) {
            return true
        }

        if(authViewRequired) {
            if(authView == null) {
                getListener()?.onError("AuthView is required")
                return true
            }

            if(ContextCompat.checkSelfPermission(getActivity()!!, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                getListener()?.onError("Camera permission not granted")
                return true
            }
        }

        if(slug.isEmpty()) {
            getListener()?.onError("Slug key is required")
            return true
        }

        if(secretKey.isEmpty()) {
            getListener()?.onError("Secret key is required")
            return true
        }

        if(token.isEmpty()) {
            getListener()?.onError("Token is required")
            return true
        }

        return false
    }

    /**
     * Method for authentication by camera
     * On result calls listener's method onAuth()
     * Requires API 21+
     *
     * @param phone format is 77771112233
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun auth(phone: String) {
        if(runInspection(true)) return

        getListener()?.onLoadStarted()
        api.auth(phone)
    }

    /**
     * Method for check status of last performed authentication
     * On result calls listener's method onCheck()
     */
    fun checkLast() {
        if(runInspection(false)) return

        val pref = PreferencesManager(getActivity()!!)

        val map = HashMap<String, Any>()
        map["id"] = pref.getID()

        if (map["id"] == 0) {
            getListener()?.onError("Status check for last operation is not available")
            return
        }

        getListener()?.onLoadStarted()
        api.check(map)
    }

    /**
     * Method for check authentication status by id
     * On result calls listener's method onCheck()
     *
     * @param id integer number of authentication id
     */
    fun checkStatusById(id: Int) {
        if(runInspection(false)) return
        val map = HashMap<String, Any>()
        map["id"] = id

        getListener()?.onLoadStarted()
        api.check(map)
    }

    /**
     * Method for check status of last performed authentication
     * On result calls listener's method onCheck()
     *
     * @param phone String format is 77771112233
     */
    fun checkStatusByPhone(phone: String) {
        if(runInspection(false)) return
        val map = HashMap<String, Any>()
        map["phone"] = phone

        getListener()?.onLoadStarted()
        api.check(map)
    }

    private fun getActivity() : Activity? {
        if (activity != null ) {
            return activity!!
        }

        if (fragment != null) {
            return fragment!!.activity
        }

        return null
    }

    private fun getListener(): EventListener? {
        if (activity != null && activity is EventListener) {
            return activity as EventListener
        }

        if(fragment != null && fragment is EventListener) {
            return fragment as EventListener
        }

        return null
    }

    class Builder {
        var activity: Activity? = null
        var fragment: Fragment? = null

        constructor(activity: Activity) {
            this.activity = activity
        }

        constructor(fragment: Fragment) {
            this.fragment = fragment
        }

        var authView: AuthView? = null
            private set

        var slug: String = ""
            private set

        var secretKey: String = ""
            private set

        var token: String = ""
            private set

        var language: Language = Language.ru
            private set

        fun setAuthView(view: AuthView) = apply { this.authView = view }

        fun setSlug(slug: String) = apply { this.slug = slug }

        fun setSecretKey(secretKey: String) = apply { this.secretKey = secretKey }

        fun setToken(token: String) = apply { this.token = token }

        fun setLanguage(language: Language) = apply { this.language = language }

        fun build() = PayBoxAuth(this)
    }
}