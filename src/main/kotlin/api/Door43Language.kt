package api

// to be mapped later to the common language data class
data class Door43Language(
        val pk: Int, // id
        val lc: String, // slug
        val ln: String, // name
        val gw: Boolean, // isGateway
        val ang: String // anglicizedName
)