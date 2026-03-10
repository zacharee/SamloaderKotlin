package tk.zwander.common.util

interface RandomAccessStream {
    operator fun get(pos: Long): UByte
    @OptIn(ExperimentalUnsignedTypes::class)
    operator fun get(pos: Long, len: Int): UByteArray
}
