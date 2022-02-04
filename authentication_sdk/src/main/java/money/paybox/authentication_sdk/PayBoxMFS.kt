package money.paybox.authentication_sdk

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import money.paybox.authentication_sdk.api.*
import money.paybox.authentication_sdk.ui.AuthView
import org.json.JSONObject
import java.util.HashMap

@Suppress("unused")
class PayBoxMFS(private var activity: Activity,
                private var authView: AuthView?,
                private var slug: String,
                private var secretKey: String,
                private var token: String,
                language: Language) {

    private val apiListener = object : ApiListener {
        override fun onAuth(result: ResponseData) {
            if (result.code == 200) {
                val obj = JSONObject(result.response)
                activity.runOnUiThread {
                    getListener()?.let { authView?.addListener(activity) }
                    authView?.loadUrl(token, obj.getString("url"))
                }
            } else {
                activity.runOnUiThread {
                    getListener()?.onLoadFinished()
                    getListener()?.onError(result.response)
                }
            }
        }

        override fun onCheck(result: ResponseData) {
            activity.runOnUiThread {
                getListener()?.onLoadFinished()
            }

            if (result.code == 200) {
                val obj = JSONObject(result.response)
                activity.runOnUiThread {
                    val helper = IdentificationHelper(activity, token)
                    getListener()?.onCheck(helper.process(obj))
                }
            } else {
                activity.runOnUiThread {
                    getListener()?.onError(result.response)
                }
            }
        }
    }

    private val api: APIHandler = APIHandler(slug, secretKey, language, apiListener)

    private constructor(builder: Builder) :
            this(builder.activity, builder.authView, builder.slug, builder.secretKey, builder.token, builder.language)

    private fun runInspection(authViewRequired: Boolean): Boolean {
        if(authViewRequired) {
            if(authView == null) {
                getListener()?.onError("AuthView is required")
                return true
            }

            if(ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
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

        val pref = PreferencesManager(activity)

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

    private fun getListener(): EventListener? {
        return if (activity is EventListener) activity as EventListener else null
    }

    class Builder(val activity: Activity) {
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

        fun build() = PayBoxMFS(this)
    }
}