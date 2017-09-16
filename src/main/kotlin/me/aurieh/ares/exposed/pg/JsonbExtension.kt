/*
 * Copyright 2017 aurieh <me@aurieh.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.aurieh.ares.exposed.pg

import me.aurieh.ares.exposed.pg.op.JsonbArrayContainsOp
import me.aurieh.ares.exposed.pg.type.JsonbArrayColumnType
import me.aurieh.ares.exposed.pg.type.JsonbMapColumnType
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.QueryParameter
import org.jetbrains.exposed.sql.Table

// TODO(?): Refactor to take a JSONArray instead of Array
/**
 * JsonbArray column
 * @param[String] name Name of the column
 * @return[Column<Array<V>>]
 */
fun<V> Table.jsonbArray(name: String) = registerColumn<Array<V>>(name, JsonbArrayColumnType<V>())

/**
 * JsonbMap (or dict) column
 * @param[String] name Name of the column
 * @return[Column<Map<K, V>>]
 */
fun<K, V> Table.jsonbMap(name: String) = registerColumn<Map<K, V>>(name, JsonbMapColumnType<K, V>())
/**
 * Check if a jsonb array contains something
 * @param[T] t Item
 * @param[IColumnType] sqlType Type of the iteam
 * @return[JsonbArrayContainsOp]
 */
inline fun<reified T> ExpressionWithColumnType<Array<T>>.jsonbSingleArrayContains(
        t: T,
        sqlType /* TODO: Find a better way to do this */: IColumnType
) = JsonbArrayContainsOp(this, QueryParameter(t, sqlType))