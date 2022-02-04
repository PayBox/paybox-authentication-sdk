package money.paybox.authentication_sdk.api

internal interface ApiListener {
    fun onAuth(result: ResponseData) {}
    fun onCheck(result: ResponseData) {}
}