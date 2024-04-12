package tk.zwander.common.exceptions

import tk.zwander.common.util.invoke
import tk.zwander.samloaderkotlin.resources.MR

class NoBinaryFileError(model: String, region: String) : Exception(MR.strings.noBinaryFile(model, region))
