package money.paybox.authentication_sdk.model

class AuthResult(val status: Status, val id: Int, val error: Error?) {

    class Error(val code: Int, val message: String)

    enum class Status(status: String) {
        NEW("new"),
        APPROVED("approved"),
        PROCESS("process"),
        VERIFIED("verified"),
        IDENTIFIED("identified"),
        ERROR("error")
    }
}