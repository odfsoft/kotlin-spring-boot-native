package com.devexplained.nativespringboot

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.web.bind.annotation.*
import java.sql.Connection


@RestController
class AvengerController(val jdbcTemplate: JdbcTemplate) {

    @GetMapping("/avengers")
    fun getAvengers(): List<Avenger> =
        jdbcTemplate.query(
            "SELECT id, name FROM avenger ORDER BY id"
        ) { rs, _ ->
            Avenger(
                rs.getInt("id"),
                rs.getString("name")
            )
        }

    @DeleteMapping("/avengers/{id}")
    fun deleteAvenger(@PathVariable("id") id: Int) {
        jdbcTemplate.update("DELETE FROM avenger WHERE id = ?", id)
    }

    @PostMapping("/avengers")
    fun postAvengers(@RequestBody avenger: Avenger): Avenger {
        val keyHolder: KeyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection: Connection ->
            val statement =
                connection.prepareStatement("INSERT INTO avenger(name) VALUES (?)", arrayOf("id"))
            statement.setString(1, avenger.name)
            statement
        }, keyHolder)
        avenger.copy(id = keyHolder.key!!.toInt())
        return avenger
    }

    data class Avenger(val id: Int, val name: String)

}
