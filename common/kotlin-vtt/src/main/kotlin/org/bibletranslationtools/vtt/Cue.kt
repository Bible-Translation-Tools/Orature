package org.bibletranslationtools.vtt

/*
* Copyright (C) 2016 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/** Contains information about a specific cue, including textual content and formatting data.  */ // This class shouldn't be sub-classed. If a subtitle format needs additional fields, either they
// should be generic enough to be added here, or the format-specific decoder should pass the
// information around in a sidecar object.
class Cue private constructor(
    text: CharSequence?,
    markup: List<CharSequence>? = listOf()
) {
    /**
     * The cue text, or null if this is an image cue. Note the [CharSequence] may be decorated
     * with styling spans.
     */
    var text: CharSequence? = text
        private set

    val content: List<CharSequence> = if (markup != null) arrayListOf(*markup.toTypedArray()) else listOf()

    class Builder {
        /**
         * Gets the cue text.
         *
         * @see Cue.text
         */
        var text: CharSequence?
            private set

        var markup: List<CharSequence> = arrayListOf()

        constructor() {
            text = null
        }

        private constructor(cue: Cue) {
            text = cue.text
        }

        /**
         * Sets the cue text.
         *
         *
         * Note that `text` may be decorated with styling spans.
         *
         * @see Cue.text
         */
        fun setText(text: CharSequence?): Builder {
            this.text = text
            return this
        }

        fun addMarkup(markup: CharSequence): Builder {
            (this.markup as MutableList<CharSequence>).add(markup)
            return this
        }

        /** Build the cue.  */
        fun build(): Cue {
            return Cue(text, markup)
        }
    }

}