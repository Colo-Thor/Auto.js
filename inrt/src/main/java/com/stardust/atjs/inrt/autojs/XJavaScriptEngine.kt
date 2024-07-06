package com.stardust.atjs.inrt.autojs

import android.content.Context
import com.stardust.atjs.engine.LoopBasedJavaScriptEngine
import com.stardust.atjs.engine.encryption.ScriptEncryption
import com.stardust.atjs.script.EncryptedScriptFileHeader
import com.stardust.atjs.script.JavaScriptFileSource
import com.stardust.atjs.script.ScriptSource
import com.stardust.atjs.script.StringScriptSource
import com.stardust.pio.PFiles
import java.io.File
import java.security.GeneralSecurityException

class XJavaScriptEngine(context: Context) : LoopBasedJavaScriptEngine(context) {


    override fun execute(source: ScriptSource, callback: ExecuteCallback?) {
        if (source is JavaScriptFileSource) {
            try {
                if (execute(source.file)) {
                    return
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                return
            }
        }
        super.execute(source, callback)
    }

    private fun execute(file: File): Boolean {
        val bytes = PFiles.readBytes(file.path)
        if (!EncryptedScriptFileHeader.isValidFile(bytes)) {
            return false
        }
        try {
            super.execute(StringScriptSource(file.name, String(ScriptEncryption.decrypt(bytes, EncryptedScriptFileHeader.BLOCK_SIZE))))
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
        }
        return true
    }

}