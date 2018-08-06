package api.model
import api.Door43Language
import data.model.Language

object Door43Mapper {

    // maps the local door43Language data class to the common language data class
    fun mapToLanguage(door43Language: Door43Language) =
            Language(
                    0,
                    door43Language.lc,
                    door43Language.ln,
                    door43Language.gw,
                    door43Language.ang
            )
}