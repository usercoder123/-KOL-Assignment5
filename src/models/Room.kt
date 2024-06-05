package models

data class Room (
    val name: String,
    val map: Array<Array<Int>>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Room

        if (name != other.name) return false
        if (!map.contentDeepEquals(other.map)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + map.contentDeepHashCode()
        return result
    }
}