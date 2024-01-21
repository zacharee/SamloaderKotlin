package tk.zwander.common.util.jna.macos

import ca.weblite.objc.NSObject
import ca.weblite.objc.RuntimeUtils.sel
import ca.weblite.objc.annotations.Msg
import com.sun.jna.Native
import com.sun.jna.win32.StdCallLibrary
import korlibs.memory.dyn.osx.NSApplication
import korlibs.memory.dyn.osx.NSClass
import korlibs.memory.dyn.osx.alloc
import korlibs.memory.dyn.osx.msgSend
import korlibs.memory.dyn.osx.msgSendInt


interface AppKit : StdCallLibrary {
    companion object INSTANCE : AppKit by Native.load("AppKit", AppKit::class.java)

    class NSWindow private constructor(val handle: Long): NSObject("NSObject") {
        companion object {
            fun getWindow() = NSWindow(NSApplication.sharedApplication().msgSend("windows"))
        }

        init {
//            println("HANDLE $handle")
//            println("WINDOW COUNT ${handle.msgSend("count")}")

            val windowCount = handle.msgSendInt("count")

            for (i in 0 until windowCount) {
                val windowPtr = handle.msgSend("objectAtIndex:", i)

//                println("WPointer $windowPtr")
//                println(NSString(windowPtr.msgSend("className")).cString)
//                println(NSString(windowPtr.msgSend("title")).cString)
            }

            val windowPtr = handle.msgSend("objectAtIndex:", 1)
            val contentViewPtr = windowPtr.msgSend("contentView")
//            println("View ptr $contentViewPtr")
//            println(NSString(windowPtr.msgSend("fp_methodDescription")).cString)
            val contentFrame = contentViewPtr.msgSend("frame")

//            println("PAST EFFECTVIEW")

//            println(ObjectiveC.class_getName(contentFrame))

//            effectView.msgSend("frame:", contentFrame)

            dispatch_sync {
                val effectView = NSVisualEffectView.initWithFrame(contentFrame)
                effectView.msgSend("setMaterial:", 3)

                contentViewPtr.msgSend("removeFromSuperview")

                val mainView = NSView.initWithFrame(contentFrame)
//                mainView.msgSend("addSubview:", effectView.toLong())
                mainView.msgSend("addSubview:", contentViewPtr)

                windowPtr.msgSend("setContentView:", mainView.toLong())
//                contentViewPtr.msgSend("addSubview:", effectView.toLong())
            }
        }
    }

    class NSVisualEffectView(id: Long): korlibs.memory.dyn.osx.NSObject(id) {
        companion object : NSClass("NSVisualEffectView") {
            fun init(): NSVisualEffectView = NSVisualEffectView(OBJ_CLASS.alloc().msgSend("init"))
            fun initWithFrame(frame: Long): NSVisualEffectView = NSVisualEffectView(OBJ_CLASS.alloc().msgSend("initWithFrame:", frame))
        }
    }

    class NSView(id: Long) : korlibs.memory.dyn.osx.NSObject(id) {
        companion object : NSClass("NSView") {
            fun initWithFrame(frame: Long): NSView = NSView(OBJ_CLASS.alloc().msgSend("initWithFrame:", frame))
        }
    }
}

fun dispatch_sync(r: Runnable) {
    object : NSObject("NSObject") {
        @Msg(selector = "run", like = "NSObject.finalize")
        fun run() {
            r.run()
        }
    }.send("performSelectorOnMainThread:withObject:waitUntilDone:", sel("run"), null, true)
}
