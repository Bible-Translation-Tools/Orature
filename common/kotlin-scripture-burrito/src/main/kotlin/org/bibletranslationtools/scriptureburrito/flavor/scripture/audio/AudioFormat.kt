package org.bibletranslationtools.scriptureburrito.flavor.scripture.audio

import com.fasterxml.jackson.annotation.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "compression",
    "trackConfiguration",
    "bitRate",
    "bitDepth",
    "samplingRate",
    "timingDir"
)
class AudioFormat(
    @get:JsonProperty("compression")
    @set:JsonProperty("compression")
    @JsonProperty("compression")
    @JsonPropertyDescription("A textual string specified in one or multiple languages, indexed by IETF language tag.")
    var compression: Compression
) {

    @get:JsonProperty("trackConfiguration")
    @set:JsonProperty("trackConfiguration")
    @JsonProperty("trackConfiguration")
    @JsonPropertyDescription("A textual string specified in one or multiple languages, indexed by IETF language tag.")
    var trackConfiguration: TrackConfiguration? = null

    @get:JsonProperty("bitRate")
    @set:JsonProperty("bitRate")
    @JsonProperty("bitRate")
    @JsonPropertyDescription("A textual string specified in one or multiple languages, indexed by IETF language tag.")
    var bitRate: Int? = null

    @get:JsonProperty("bitDepth")
    @set:JsonProperty("bitDepth")
    @JsonProperty("bitDepth")
    @JsonPropertyDescription("A textual string specified in one or multiple languages, indexed by IETF language tag.")
    var bitDepth: Int? = null

    @get:JsonProperty("samplingRate")
    @set:JsonProperty("samplingRate")
    @JsonProperty("samplingRate")
    @JsonPropertyDescription("A textual string specified in one or multiple languages, indexed by IETF language tag.")
    var samplingRate: Int? = null

    @get:JsonProperty("timingDir")
    @set:JsonProperty("timingDir")
    @JsonProperty("timingDir")
    @JsonPropertyDescription("A textual string specified in one or multiple languages, indexed by IETF language tag.")
    var timingDir: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioFormat) return false

        if (compression != other.compression) return false
        if (trackConfiguration != other.trackConfiguration) return false
        if (bitRate != other.bitRate) return false
        if (bitDepth != other.bitDepth) return false
        if (samplingRate != other.samplingRate) return false
        if (timingDir != other.timingDir) return false

        return true
    }

    override fun hashCode(): Int {
        var result = compression.hashCode()
        result = 31 * result + (trackConfiguration?.hashCode() ?: 0)
        result = 31 * result + (bitRate ?: 0)
        result = 31 * result + (bitDepth ?: 0)
        result = 31 * result + (samplingRate ?: 0)
        result = 31 * result + (timingDir?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "AudioFormat(compression=$compression, trackConfiguration=$trackConfiguration, bitRate=$bitRate, bitDepth=$bitDepth, samplingRate=$samplingRate, timingDir=$timingDir)"
    }
}