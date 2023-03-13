package org.wycliffeassociates.otter.common.domain.project

import java.util.regex.Pattern

val takeFilenamePattern: Pattern = run {
    val chapter = """_c(\d+)"""
    val verse = """(?:_v(\d+))?"""
    val sort = """(?:_s(\d+))?"""
    val type = """(?:_([A-Za-z]+))?"""
    val take = """_t(\d+)"""
    val extensionDelim = """\."""
    Pattern.compile(chapter + verse + sort + type + take + extensionDelim)
}