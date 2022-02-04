package money.paybox.authentication_sdk.api

import java.util.*

internal data class RequestData(
    val params: HashMap<String, Any>,
    val method: RequestMethod,
    val url: String,
    val accessType: String
)

internal enum class RequestMethod {
    GET, POST
}
enum class Language {
    ru, en, kz, de
}