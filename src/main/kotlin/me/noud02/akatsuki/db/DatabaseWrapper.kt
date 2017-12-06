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

package me.noud02.akatsuki.db

import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.entities.CoroutineDispatcher
import me.noud02.akatsuki.schema.Guilds
import me.noud02.akatsuki.schema.Users
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

data class DBGuild(
        val id: String,
        val name: String,
        val lang: String,
        val prefixes: List<String>,
        val forceLang: Boolean
)

data class DBUser(
        val id: String,
        val username: String,
        val discriminator: String,
        val lang: String
)

object DatabaseWrapper {
    private val pool: ExecutorService by lazy {
        Executors.newCachedThreadPool {
            Thread(it, "Akatsuki-Database-Pool-Thread").apply {
                isDaemon = true
            }
        }
    }
    private val coroutineDispatcher by lazy {
        CoroutineDispatcher(pool)
    }

    suspend fun getGuild(guild: Guild) = getGuild(guild.id)

    suspend fun getGuild(id: String): DBGuild {
        return asyncTransaction(pool) {
            val selection = Guilds.select {
                Guilds.id.eq(id)
            }

            if (selection.empty())
                throw Exception("Guild not found")
            else {
                val guild = selection.first()

                return@asyncTransaction DBGuild(
                        guild[Guilds.id],
                        guild[Guilds.name],
                        guild[Guilds.lang],
                        guild[Guilds.prefixes].toList(),
                        guild[Guilds.forceLang]
                )
            }
        }.await()
    }

    suspend fun newGuild(guild: Guild) {
        asyncTransaction(pool) {
            val selection = Guilds.select {
                Guilds.id.eq(guild.id)
            }

            if (selection.empty())
                Guilds.insert {
                    it[id] = guild.id
                    it[lang] = "en_US"
                    it[forceLang] = false
                    it[prefixes] = arrayOf()
                    it[name] = guild.name
                }
        }.await()
    }

    suspend fun getUser(user: User) = getUser(user.id)

    suspend fun getUser(member: Member) = getUser(member.user.id)

    suspend fun getUser(id: String): DBUser {
        return asyncTransaction(pool) {
            val selection = Users.select {
                Users.id.eq(id)
            }

            if (selection.empty())
                throw Exception("User not found")
            else {
                val user = selection.first()

                return@asyncTransaction DBUser(
                        user[Users.id],
                        user[Users.username],
                        user[Users.discriminator],
                        user[Users.lang]
                )
            }
        }.await()
    }

    suspend fun newUser(member: Member) = newUser(member.user)

    suspend fun newUser(user: User) {
        asyncTransaction(pool) {
            val selection = Users.select {
                Users.id.eq(user.id)
            }

            if (selection.empty())
                Users.insert {
                    it[id] = user.id
                    it[username] = user.name
                    it[discriminator] = user.discriminator
                    it[lang] = "en_US"
                }
        }.await()
    }

    suspend fun getGuildSafe(guild: Guild): DBGuild {
        try {
            getGuild(guild)
        } catch (e: Exception) {
            newGuild(guild)
        } finally {
            return getGuild(guild)
        }
    }

    suspend fun getUserSafe(member: Member) = getUserSafe(member.user)

    suspend fun getUserSafe(user: User): DBUser {
        try {
            getUser(user)
        } catch (e: Exception) {
            newUser(user)
        } finally {
            return getUser(user)
        }
    }
}