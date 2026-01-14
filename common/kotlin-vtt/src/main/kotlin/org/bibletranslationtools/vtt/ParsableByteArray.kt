package org.bibletranslationtools.vtt

import com.google.common.primitives.Chars
import com.google.common.primitives.UnsignedBytes
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Wraps a byte array, providing a set of methods for parsing data from it. Numerical values are
 * parsed with the assumption that their constituent bytes are in big endian order.
 */
class ParsableByteArray {
    /**
     * Returns the underlying array.
     *
     *
     * Changes to this array are reflected in the results of the `read...()` methods.
     *
     *
     * This reference must be assumed to become invalid when [.reset] or [ ][.ensureCapacity] are called (because the array might get reallocated).
     */
    var data: ByteArray
        private set
    private var position = 0

    // TODO(internal b/147657250): Enforce this limit on all read methods.
    private var limit = 0

    /** Creates a new instance that initially has no backing data.  */
    constructor() {
        data = byteArrayOf()
    }

    /**
     * Creates a new instance with `limit` bytes and sets the limit.
     *
     * @param limit The limit to set.
     */
    constructor(limit: Int) {
        this.data = ByteArray(limit)
        this.limit = limit
    }

    /**
     * Creates a new instance wrapping `data`, and sets the limit to `data.length`.
     *
     * @param data The array to wrap.
     */
    constructor(data: ByteArray) {
        this.data = data
        limit = data.size
    }

    /**
     * Creates a new instance that wraps an existing array.
     *
     * @param data The data to wrap.
     * @param limit The limit to set.
     */
    constructor(data: ByteArray, limit: Int) {
        this.data = data
        this.limit = limit
    }

    /**
     * Resets the position to zero and the limit to the specified value. This might replace or wipe
     * the [underlying array][.getData], potentially invalidating any local references.
     *
     * @param limit The limit to set.
     */
    fun reset(limit: Int) {
        reset(if (capacity() < limit) ByteArray(limit) else data, limit)
    }

    /**
     * Updates the instance to wrap `data`, and resets the position to zero.
     *
     * @param data The array to wrap.
     * @param limit The limit to set.
     */
    /**
     * Updates the instance to wrap `data`, and resets the position to zero and the limit to
     * `data.length`.
     *
     * @param data The array to wrap.
     */
    @JvmOverloads
    fun reset(data: ByteArray, limit: Int = data.size) {
        this.data = data
        this.limit = limit
        position = 0
    }

    /**
     * Ensures the backing array is at least `requiredCapacity` long.
     *
     *
     * [position][.getPosition], [limit][.limit], and all data in the underlying
     * array (including that beyond [.limit]) are preserved.
     *
     *
     * This might replace or wipe the [underlying array][.getData], potentially invalidating
     * any local references.
     */
    fun ensureCapacity(requiredCapacity: Int) {
        if (requiredCapacity > capacity()) {
            data = data.copyOf(requiredCapacity)
        }
    }

    /** Returns the number of bytes yet to be read.  */
    fun bytesLeft(): Int {
        return limit - position
    }

    /** Returns the limit.  */
    fun limit(): Int {
        return limit
    }

    /**
     * Sets the limit.
     *
     * @param limit The limit to set.
     */
    fun setLimit(limit: Int) {
        check(limit >= 0 && limit <= data.size)
        this.limit = limit
    }

    /** Returns the current offset in the array, in bytes.  */
    fun getPosition(): Int {
        return position
    }

    /**
     * Sets the reading offset in the array.
     *
     * @param position Byte offset in the array from which to read.
     * @throws IllegalArgumentException Thrown if the new position is neither in nor at the end of the
     * array.
     */
    fun setPosition(position: Int) {
        // It is fine for position to be at the end of the array.
        check(position >= 0 && position <= limit)
        this.position = position
    }

    /** Returns the capacity of the array, which may be larger than the limit.  */
    fun capacity(): Int {
        return data.size
    }

    /**
     * Moves the reading offset by `bytes`.
     *
     * @param bytes The number of bytes to skip.
     * @throws IllegalArgumentException Thrown if the new position is neither in nor at the end of the
     * array.
     */
    fun skipBytes(bytes: Int) {
        setPosition(position + bytes)
    }

    /**
     * Reads the next `length` bytes into `bitArray`, and resets the position of `bitArray` to zero.
     *
     * @param bitArray The [ParsableBitArray] into which the bytes should be read.
     * @param length The number of bytes to write.
     */
//    fun readBytes(bitArray: ParsableBitArray, length: Int) {
//        readBytes(bitArray.data, 0, length)
//        bitArray.setPosition(0)
//    }

    /**
     * Reads the next `length` bytes into `buffer` at `offset`.
     *
     * @see System.arraycopy
     * @param buffer The array into which the read data should be written.
     * @param offset The offset in `buffer` at which the read data should be written.
     * @param length The number of bytes to read.
     */
    fun readBytes(buffer: ByteArray?, offset: Int, length: Int) {
        System.arraycopy(data, position, buffer, offset, length)
        position += length
    }

    /**
     * Reads the next `length` bytes into `buffer`.
     *
     * @see ByteBuffer.put
     * @param buffer The [ByteBuffer] into which the read data should be written.
     * @param length The number of bytes to read.
     */
    fun readBytes(buffer: ByteBuffer, length: Int) {
        buffer.put(data, position, length)
        position += length
    }

    /** Peeks at the next byte as an unsigned value.  */
    fun peekUnsignedByte(): Int {
        return (data[position].toInt() and 0xFF)
    }

    /**
     * Peeks at the next char.
     *
     *
     * Equivalent to passing [Charsets.UTF_16] or [Charsets.UTF_16BE] to [ ][.peekChar].
     */
    fun peekChar(): Char {
        return ((data[position].toInt() and 0xFF) shl 8 or (data[position + 1].toInt() and 0xFF)).toChar()
    }

    /**
     * Peeks at the next char (as decoded by `charset`)
     *
     * @throws IllegalArgumentException if charset is not supported. Only US_ASCII, UTF-8, UTF-16,
     * UTF-16BE, and UTF-16LE are supported.
     */
    fun peekChar(charset: Charset): Char {
        check(SUPPORTED_CHARSETS_FOR_READLINE.contains(charset))
        return (peekCharacterAndSize(charset) shr java.lang.Short.SIZE).toChar()
    }

    /** Reads the next byte as an unsigned value.  */
    fun readUnsignedByte(): Int {
        return (data[position++].toInt() and 0xFF)
    }

    /** Reads the next two bytes as an unsigned value.  */
    fun readUnsignedShort(): Int {
        return (data[position++].toInt() and 0xFF) shl 8 or (data[position++].toInt() and 0xFF)
    }

    /** Reads the next two bytes as an unsigned value.  */
    fun readLittleEndianUnsignedShort(): Int {
        return (data[position++].toInt() and 0xFF) or ((data[position++].toInt() and 0xFF) shl 8)
    }

    /** Reads the next two bytes as a signed value.  */
    fun readShort(): Short {
        return ((data[position++].toInt() and 0xFF) shl 8 or (data[position++].toInt() and 0xFF)).toShort()
    }

    /** Reads the next two bytes as a signed value.  */
    fun readLittleEndianShort(): Short {
        return ((data[position++].toInt() and 0xFF) or ((data[position++].toInt() and 0xFF) shl 8)).toShort()
    }

    /** Reads the next three bytes as an unsigned value.  */
    fun readUnsignedInt24(): Int {
        return (data[position++].toInt() and 0xFF) shl 16 or ((data[position++].toInt() and 0xFF) shl 8
                ) or (data[position++].toInt() and 0xFF)
    }

    /** Reads the next three bytes as a signed value.  */
    fun readInt24(): Int {
        return ((data[position++].toInt() and 0xFF) shl 24) shr 8 or ((data[position++].toInt() and 0xFF) shl 8
                ) or (data[position++].toInt() and 0xFF)
    }

    /** Reads the next three bytes as a signed value in little endian order.  */
    fun readLittleEndianInt24(): Int {
        return ((data[position++].toInt() and 0xFF)
                or ((data[position++].toInt() and 0xFF) shl 8
                ) or ((data[position++].toInt() and 0xFF) shl 16))
    }

    /** Reads the next three bytes as an unsigned value in little endian order.  */
    fun readLittleEndianUnsignedInt24(): Int {
        return ((data[position++].toInt() and 0xFF)
                or ((data[position++].toInt() and 0xFF) shl 8
                ) or ((data[position++].toInt() and 0xFF) shl 16))
    }

    /** Reads the next four bytes as an unsigned value.  */
    fun readUnsignedInt(): Long {
        return (data[position++].toLong() and 0xFFL) shl 24 or ((data[position++].toLong() and 0xFFL) shl 16
                ) or ((data[position++].toLong() and 0xFFL) shl 8
                ) or (data[position++].toLong() and 0xFFL)
    }

    /** Reads the next four bytes as an unsigned value in little endian order.  */
    fun readLittleEndianUnsignedInt(): Long {
        return ((data[position++].toLong() and 0xFFL)
                or ((data[position++].toLong() and 0xFFL) shl 8
                ) or ((data[position++].toLong() and 0xFFL) shl 16
                ) or ((data[position++].toLong() and 0xFFL) shl 24))
    }

    /** Reads the next four bytes as a signed value  */
    fun readInt(): Int {
        return (data[position++].toInt() and 0xFF) shl 24 or ((data[position++].toInt() and 0xFF) shl 16
                ) or ((data[position++].toInt() and 0xFF) shl 8
                ) or (data[position++].toInt() and 0xFF)
    }

    /** Reads the next four bytes as a signed value in little endian order.  */
    fun readLittleEndianInt(): Int {
        return ((data[position++].toInt() and 0xFF)
                or ((data[position++].toInt() and 0xFF) shl 8
                ) or ((data[position++].toInt() and 0xFF) shl 16
                ) or ((data[position++].toInt() and 0xFF) shl 24))
    }

    /** Reads the next eight bytes as a signed value.  */
    fun readLong(): Long {
        return (data[position++].toLong() and 0xFFL) shl 56 or ((data[position++].toLong() and 0xFFL) shl 48
                ) or ((data[position++].toLong() and 0xFFL) shl 40
                ) or ((data[position++].toLong() and 0xFFL) shl 32
                ) or ((data[position++].toLong() and 0xFFL) shl 24
                ) or ((data[position++].toLong() and 0xFFL) shl 16
                ) or ((data[position++].toLong() and 0xFFL) shl 8
                ) or (data[position++].toLong() and 0xFFL)
    }

    /** Reads the next eight bytes as a signed value in little endian order.  */
    fun readLittleEndianLong(): Long {
        return ((data[position++].toLong() and 0xFFL)
                or ((data[position++].toLong() and 0xFFL) shl 8
                ) or ((data[position++].toLong() and 0xFFL) shl 16
                ) or ((data[position++].toLong() and 0xFFL) shl 24
                ) or ((data[position++].toLong() and 0xFFL) shl 32
                ) or ((data[position++].toLong() and 0xFFL) shl 40
                ) or ((data[position++].toLong() and 0xFFL) shl 48
                ) or ((data[position++].toLong() and 0xFFL) shl 56))
    }

    /** Reads the next four bytes, returning the integer portion of the fixed point 16.16 integer.  */
    fun readUnsignedFixedPoint1616(): Int {
        val result = (data[position++].toInt() and 0xFF) shl 8 or (data[position++].toInt() and 0xFF)
        position += 2 // Skip the non-integer portion.
        return result
    }

    /**
     * Reads a Synchsafe integer.
     *
     *
     * Synchsafe integers keep the highest bit of every byte zeroed. A 32 bit synchsafe integer can
     * store 28 bits of information.
     *
     * @return The parsed value.
     */
    fun readSynchSafeInt(): Int {
        val b1 = readUnsignedByte()
        val b2 = readUnsignedByte()
        val b3 = readUnsignedByte()
        val b4 = readUnsignedByte()
        return (b1 shl 21) or (b2 shl 14) or (b3 shl 7) or b4
    }

    /**
     * Reads the next four bytes as an unsigned integer into an integer, if the top bit is a zero.
     *
     * @throws IllegalStateException Thrown if the top bit of the input data is set.
     */
    fun readUnsignedIntToInt(): Int {
        val result = readInt()
        check(result >= 0) { "Top bit not zero: $result" }
        return result
    }

    /**
     * Reads the next four bytes as a little endian unsigned integer into an integer, if the top bit
     * is a zero.
     *
     * @throws IllegalStateException Thrown if the top bit of the input data is set.
     */
    fun readLittleEndianUnsignedIntToInt(): Int {
        val result = readLittleEndianInt()
        check(result >= 0) { "Top bit not zero: $result" }
        return result
    }

    /**
     * Reads the next eight bytes as an unsigned long into a long, if the top bit is a zero.
     *
     * @throws IllegalStateException Thrown if the top bit of the input data is set.
     */
    fun readUnsignedLongToLong(): Long {
        val result = readLong()
        check(result >= 0) { "Top bit not zero: $result" }
        return result
    }

    /** Reads the next four bytes as a 32-bit floating point value.  */
    fun readFloat(): Float {
        return java.lang.Float.intBitsToFloat(readInt())
    }

    /** Reads the next eight bytes as a 64-bit floating point value.  */
    fun readDouble(): Double {
        return java.lang.Double.longBitsToDouble(readLong())
    }

    /**
     * Reads the next `length` bytes as characters in the specified [Charset].
     *
     * @param length The number of bytes to read.
     * @param charset The character set of the encoded characters.
     * @return The string encoded by the bytes in the specified character set.
     */
    /**
     * Reads the next `length` bytes as UTF-8 characters.
     *
     * @param length The number of bytes to read.
     * @return The string encoded by the bytes.
     */
    @JvmOverloads
    fun readString(length: Int, charset: Charset = StandardCharsets.UTF_8): String {
        val result = String(data, position, length, charset)
        position += length
        return result
    }

    /**
     * Reads the next `length` bytes as UTF-8 characters. A terminating NUL byte is discarded,
     * if present.
     *
     * @param length The number of bytes to read.
     * @return The string, not including any terminating NUL byte.
     */
//    fun readNullTerminatedString(length: Int): String {
//        if (length == 0) {
//            return ""
//        }
//        var stringLength = length
//        val lastIndex = position + length - 1
//        if (lastIndex < limit && data[lastIndex].toInt() == 0) {
//            stringLength--
//        }
//        val result: String = Util.fromUtf8Bytes(data, position, stringLength)
//        position += length
//        return result
//    }

    /**
     * Reads up to the next NUL byte (or the limit) as UTF-8 characters.
     *
     * @return The string not including any terminating NUL byte, or null if the end of the data has
     * already been reached.
     */
    fun readNullTerminatedString(): String? {
        return readDelimiterTerminatedString('\u0000')
    }

    /**
     * Reads up to the next delimiter byte (or the limit) as UTF-8 characters.
     *
     * @return The string not including any terminating delimiter byte, or null if the end of the data
     * has already been reached.
     */
    fun readDelimiterTerminatedString(delimiter: Char): String? {
        if (bytesLeft() == 0) {
            return null
        }
        var stringLimit = position
        while (stringLimit < limit && data[stringLimit] != delimiter.code.toByte()) {
            stringLimit++
        }
        val string: String = fromUtf8Bytes(data, position, stringLimit - position)
        position = stringLimit
        if (position < limit) {
            position++
        }
        return string
    }

    /**
     * Reads a line of text in `charset`.
     *
     *
     * A line is considered to be terminated by any one of a carriage return ('\r'), a line feed
     * ('\n'), or a carriage return followed immediately by a line feed ('\r\n'). This method discards
     * leading UTF byte order marks (BOM), if present.
     *
     *
     * The [position][.getPosition] is advanced to start of the next line (i.e. any
     * line terminators are skipped).
     *
     * @param charset The charset used to interpret the bytes as a [String].
     * @return The line not including any line-termination characters, or null if the end of the data
     * has already been reached.
     * @throws IllegalArgumentException if charset is not supported. Only US_ASCII, UTF-8, UTF-16,
     * UTF-16BE, and UTF-16LE are supported.
     */
    /**
     * Reads a line of text in UTF-8.
     *
     *
     * Equivalent to passing [Charsets.UTF_8] to [.readLine].
     */
    @JvmOverloads
    fun readLine(charset: Charset = StandardCharsets.UTF_8): String? {
        check(SUPPORTED_CHARSETS_FOR_READLINE.contains(charset))
        if (bytesLeft() == 0) {
            return null
        }
        if (charset != StandardCharsets.US_ASCII) {
            val unused = readUtfCharsetFromBom() // Skip BOM if present
        }
        val lineLimit = findNextLineTerminator(charset)
        val line = readString(lineLimit - position, charset)
        if (position == limit) {
            return line
        }
        skipLineTerminator(charset)
        return line
    }

    /**
     * Reads a long value encoded by UTF-8 encoding
     *
     * @throws NumberFormatException if there is a problem with decoding
     * @return Decoded long value
     */
    fun readUtf8EncodedLong(): Long {
        var length = 0
        var value = data[position].toLong()
        // find the high most 0 bit
        for (j in 7 downTo 0) {
            if ((value and (1 shl j).toLong()) == 0L) {
                if (j < 6) {
                    value = value and ((1 shl j) - 1).toLong()
                    length = 7 - j
                } else if (j == 7) {
                    length = 1
                }
                break
            }
        }
        if (length == 0) {
            throw NumberFormatException("Invalid UTF-8 sequence first byte: $value")
        }
        for (i in 1 until length) {
            val x = data[position + i].toInt()
            if ((x and 0xC0) != 0x80) { // if the high most 0 bit not 7th
                throw NumberFormatException("Invalid UTF-8 sequence continuation byte: $value")
            }
            value = (value shl 6) or (x and 0x3F).toLong()
        }
        position += length
        return value
    }

    /**
     * Reads a UTF byte order mark (BOM) and returns the UTF [Charset] it represents. Returns
     * `null` without advancing [position][.getPosition] if no BOM is found.
     */
    fun readUtfCharsetFromBom(): Charset? {
        if (bytesLeft() >= 3 && data[position] == 0xEF.toByte() && data[position + 1] == 0xBB.toByte() && data[position + 2] == 0xBF.toByte()) {
            position += 3
            return StandardCharsets.UTF_8
        } else if (bytesLeft() >= 2) {
            if (data[position] == 0xFE.toByte() && data[position + 1] == 0xFF.toByte()) {
                position += 2
                return StandardCharsets.UTF_16BE
            } else if (data[position] == 0xFF.toByte() && data[position + 1] == 0xFE.toByte()) {
                position += 2
                return StandardCharsets.UTF_16LE
            }
        }
        return null
    }

    /**
     * Returns the index of the next occurrence of '\n' or '\r', or [.limit] if none is found.
     */
    private fun findNextLineTerminator(charset: Charset): Int {
        val stride = if (charset == StandardCharsets.UTF_8 || charset == StandardCharsets.US_ASCII) {
            1
        } else if (charset == StandardCharsets.UTF_16 || charset == StandardCharsets.UTF_16LE || charset == StandardCharsets.UTF_16BE) {
            2
        } else {
            throw IllegalArgumentException("Unsupported charset: $charset")
        }
        var i = position
        while (i < limit - (stride - 1)) {
            if ((charset == StandardCharsets.UTF_8 || charset == StandardCharsets.US_ASCII)
                && isLinebreak(data[i].toInt())
            ) {
                return i
            } else if ((charset == StandardCharsets.UTF_16 || charset == StandardCharsets.UTF_16BE)
                && (data[i].toInt() == 0x00
                        ) && isLinebreak(data[i + 1].toInt())
            ) {
                return i
            } else if (charset == StandardCharsets.UTF_16LE && data[i + 1].toInt() == 0x00 && isLinebreak(data[i].toInt())) {
                return i
            }
            i += stride
        }
        return limit
    }

    private fun skipLineTerminator(charset: Charset) {
        if (readCharacterIfInList(charset, CR_AND_LF) == '\r') {
            val unused = readCharacterIfInList(charset, LF)
        }
    }

    /**
     * Peeks at the character at [.position] (as decoded by `charset`), returns it and
     * advances [.position] past it if it's in `chars`, otherwise returns `0`
     * without advancing [.position]. Returns `0` if [.bytesLeft] doesn't allow
     * reading a whole character in `charset`.
     *
     *
     * Only supports characters in `chars` that occupy a single code unit (i.e. one byte for
     * UTF-8 and two bytes for UTF-16).
     */
    private fun readCharacterIfInList(charset: Charset, chars: CharArray): Char {
        val characterAndSize = peekCharacterAndSize(charset)

        if (characterAndSize != 0 && Chars.contains(chars, (characterAndSize shr java.lang.Short.SIZE).toChar())) {
            position += characterAndSize and 0xFFFF
            return (characterAndSize shr java.lang.Short.SIZE).toChar()
        } else {
            return '0'
        }
    }

    /**
     * Peeks at the character at [.position] (as decoded by `charset`), returns it and the
     * number of bytes the character takes up within the array packed into an int. First four bytes
     * are the character and the second four is the size in bytes it takes. Returns 0 if [ ][.bytesLeft] doesn't allow reading a whole character in `charset` or if the `charset` is not one of US_ASCII, UTF-8, UTF-16, UTF-16BE, or UTF-16LE.
     *
     *
     * Only supports characters that occupy a single code unit (i.e. one byte for UTF-8 and two
     * bytes for UTF-16).
     */
    private fun peekCharacterAndSize(charset: Charset): Int {
        val character: Byte
        val characterSize: Short
        if ((charset == StandardCharsets.UTF_8 || charset == StandardCharsets.US_ASCII) && bytesLeft() >= 1) {
            character = Chars.checkedCast(UnsignedBytes.toInt(data[position]).toLong()).code.toByte()
            characterSize = 1
        } else if ((charset == StandardCharsets.UTF_16 || charset == StandardCharsets.UTF_16BE)
            && bytesLeft() >= 2
        ) {
            character = Chars.fromBytes(data[position], data[position + 1]).code.toByte()
            characterSize = 2
        } else if (charset == StandardCharsets.UTF_16LE && bytesLeft() >= 2) {
            character = Chars.fromBytes(data[position + 1], data[position]).code.toByte()
            characterSize = 2
        } else {
            return 0
        }
        return (Chars.checkedCast(character.toLong()).toInt() shl java.lang.Short.SIZE) + characterSize
    }

    companion object {
        private val CR_AND_LF = charArrayOf('\r', '\n')
        private val LF = charArrayOf('\n')
        private val SUPPORTED_CHARSETS_FOR_READLINE: Set<Charset> = setOf(
            StandardCharsets.US_ASCII,
            StandardCharsets.UTF_8,
            StandardCharsets.UTF_16,
            StandardCharsets.UTF_16BE,
            StandardCharsets.UTF_16LE
        )
    }
}
