package me.noud02.akatsuki.bot

import java.util.*

object i18n {
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