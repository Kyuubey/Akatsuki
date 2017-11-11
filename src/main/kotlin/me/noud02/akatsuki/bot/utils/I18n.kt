/*
 *  Copyright (c) 2017 Noud Kerver
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

package me.noud02.akatsuki.bot.utils

import java.util.*

object I18n {
    fun parse(str: String, values: Map<String, Any>): String {
        val regex = "\\{\\{([^}]+)}}".toRegex()
        var new = str

        while (regex.containsMatchIn(new)) {
            val match = regex.find(new)?.groupValues

            if (values.contains(match?.get(1)) && match != null)
                new = new.replace(match[0], values[match[1]].toString())
        }

        return new
    }

    fun permission(lang: ResourceBundle, perm: String): String {
        val new = perm.replace("_", "").toCharArray()

        new[0] = new[0].toString().decapitalize().toCharArray()[0]

        return lang.getString(new.joinToString(""))
    }
}