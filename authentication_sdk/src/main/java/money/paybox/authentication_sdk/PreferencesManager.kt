package money.paybox.authentication_sdk

import android.content.Context

internal class PreferencesManager(val context: Context) {
    private val shared = context.getSharedPreferences("auth_shared_preferences", Context.MODE_PRIVATE)

    fun setID(id: Int) {
        val editor = shared.edit()
        editor.putInt("ID", id)
        editor.apply()
    }

    fun getID(): Int {
        return shared.getInt("ID", 0)
    }
}