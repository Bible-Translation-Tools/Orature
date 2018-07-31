package data.model

data class Chunk(
        var id: Int = 0,
        val start: Int,
        val end: Int,
        val sort: Int,
        var recorded: Boolean,
        var edited: Boolean,
        var completed: Boolean
)