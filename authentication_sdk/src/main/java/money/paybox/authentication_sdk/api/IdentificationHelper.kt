package money.paybox.authentication_sdk.api

import android.content.Context
import money.paybox.authentication_sdk.PreferencesManager
import money.paybox.authentication_sdk.model.AuthResult
import org.json.JSONObject
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal class IdentificationHelper(private val context: Context, private val token: String) {
    fun process(obj: JSONObject): AuthResult {
        val result: AuthResult

        val originalHash = obj.getString("hash")
        val identification = obj.getJSONObject("identification")

        if (originalHash == getHash(identification)) {
            val id = identification.getInt("id")
            val pref = PreferencesManager(context)
            pref.setID(id)

            result = if (identification.getString("state") == "error") {
                AuthResult(
                    AuthResult.Status.ERROR, id,
                    AuthResult.Error(
                        identification.getInt("failure_code"),
                        identification.getString("failure_description")
                    )
                )
            } else {
                AuthResult(
                    AuthResult.Status.valueOf(identification.getString("state")),
                    id, null
                )
            }

        } else {
            result = AuthResult(
                AuthResult.Status.ERROR, identification.getInt("id"),
                AuthResult.Error(-1, "Result is corrupted")
            )
        }

        return result
    }

    private fun getHash(identification: JSONObject): String {
        val bytes = identification.toString().toByteArray(Charsets.UTF_8)
        val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)

        val sha256Hmac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(token.toByteArray(Charsets.UTF_8), "HmacSHA256")
        sha256Hmac.init(secretKey)

        val resultBytes = sha256Hmac.doFinal(base64.toByteArray())
        return getHexString(resultBytes, resultBytes.size)
    }

    private fun getHexString(data: ByteArray?, len: Int): String {
        if (data != null) {
            val str = StringBuffer(len)
            for (i in 0 until len) {
                var digit = Integer.toHexString(data[i].toInt() and 0x00ff)
                if (digit.length == 1) {
                    digit = "0$digit"
                }
                digit = digit.uppercase(Locale.US)
                str.append(digit)
            }
            return str.toString().lowercase(Locale.getDefault())
        }
        return ""
    }
}