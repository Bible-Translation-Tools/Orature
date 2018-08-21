package data.model

data class Language(
        var id: Int = 0,
        val slug: String,
        val name: String,
        val isGateway: Boolean,
        val anglicizedName: String
)