package enums

enum class Regex(val formatter: String) {
    CUSTOMER_NAME("^[A-Za-z]+(?: [A-Za-z]+)*"),
    PHONE_NUMBER("^(0[1-9][0-9]{8})")
}