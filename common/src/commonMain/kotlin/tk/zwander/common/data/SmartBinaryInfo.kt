package tk.zwander.common.data

data class SmartBinaryInfo(
    val index: Int?,
    val sequence: Int,
    val modelName: String,
    val displayName: String?,
    val swVersion: String,
    val displayVersion: String?,
    val directVersion: String?,
    val localCode: String,
    val buyerCode: String?,
    val nature: Int?,
    val status: Int?,
    val exists: Int?,
    val osName: String?,
    val platform: String?,
    val openDate: String?,
    val sharing: Int?,
    val category: String?,
    val open: Int?,
)
