/*
 *   Copyright (c) 2017-2018 Noud Kerver
 *
 *   Permission is hereby granted, free of charge, to any person
 *   obtaining a copy of this software and associated documentation
 *   files (the "Software"), to deal in the Software without
 *   restriction, including without limitation the rights to use,
 *   copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the
 *   Software is furnished to do so, subject to the following
 *   conditions:
 *
 *   The above copyright notice and this permission notice shall be
 *   included in all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *   OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *   HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *   WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *   FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *   OTHER DEALINGS IN THE SOFTWARE.
 */

package me.noud02.akatsuki.utils

import org.luaj.vm2.Globals
import org.luaj.vm2.LoadState
import org.luaj.vm2.LuaThread
import org.luaj.vm2.LuaValue
import org.luaj.vm2.compiler.LuaC
import org.luaj.vm2.lib.*
import org.luaj.vm2.lib.jse.JseBaseLib
import org.luaj.vm2.lib.jse.JseMathLib

object LuaSandbox {
    fun eval(script: String, vararg args: String) = eval(script, args.toList())

    fun eval(script: String, args: List<String>): String {
        val serverGlobals = Globals()
        serverGlobals.load(JseBaseLib())
        serverGlobals.load(PackageLib())
        serverGlobals.load(StringLib())
        serverGlobals.load(JseMathLib())

        LoadState.install(serverGlobals)
        LuaC.install(serverGlobals)

        val globals = Globals()
        globals.load(JseBaseLib())
        globals.load(PackageLib())
        globals.load(Bit32Lib())
        globals.load(TableLib())
        globals.load(StringLib())
        globals.load(JseMathLib())
        globals.load(DebugLib())
        val sethook = globals["debug"]["sethook"]
        globals["debug"] = LuaValue.NIL
        globals["print"] = LuaValue.NIL
        globals["args"] = LuaValue.listOf(args.map { LuaValue.valueOf(it) }.toTypedArray())

        val chunk = serverGlobals.load(script, "main", globals)
        val thread = LuaThread(globals, chunk)

        sethook.invoke(LuaValue.varargsOf(arrayOf(thread, object : ZeroArgFunction() {
            override fun call(): LuaValue {
                throw Exception("Script hit resource limit!")
            }
        }, LuaValue.EMPTYSTRING, LuaValue.valueOf(20))))

        val result = thread.resume(LuaValue.NIL)

        return result.arg(2).tojstring()
    }
}