package tk.zwander.common.data

data class AuthHeader(
    val magic: Int,
    val alignment: Int,
    val block1: AuthHeaderBlock,
    val block2: AuthHeaderBlock,
    val block3: AuthHeaderBlock,
    val block4: AuthHeaderBlock,
    val block5: AuthHeaderBlock,
    val block6: AuthHeaderBlock,
)

data class AuthHeaderBlock(
    val offset: Int,
    val size: Int,
)
