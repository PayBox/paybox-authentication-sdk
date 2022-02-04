package money.paybox.authentication_sdk

import money.paybox.authentication_sdk.model.AuthResult

interface EventListener {
    /**
     * The method reports that loading has started, where you can show progress bar for example
     */
    fun onLoadStarted() {}

    /**
     * The method reports that loading is complete, here you can hide progress bar for example
     */
    fun onLoadFinished() {}

    /**
     * Shows message if error occurred
     */
    fun onError(message: String) {}

    /**
     * @param result of authentication
     */
    fun onAuth(result: AuthResult) {}

    /**
     * @param result of check authentication status
     */
    fun onCheck(result: AuthResult) {}
}