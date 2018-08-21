package data.model

data class Take(
        var id: Int = 0,
        val filePath: String,
        val sort: Int,
        var played: Boolean
)