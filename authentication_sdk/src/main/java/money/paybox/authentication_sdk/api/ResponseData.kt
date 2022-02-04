package money.paybox.authentication_sdk.api

internal data class ResponseData(
    val code: Int,
    val response: String,
    val url: String,
    val error: Boolean = code!=200
)