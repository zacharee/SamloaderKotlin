package tk.zwander.common.data.exception

class VersionMismatchException(message: String) : VersionException(message)
class VersionCheckException(message: String) : VersionException(message)

abstract class VersionException(message: String) : Exception(message)
