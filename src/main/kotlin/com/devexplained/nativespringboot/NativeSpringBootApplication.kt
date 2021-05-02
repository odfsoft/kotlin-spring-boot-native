package com.devexplained.nativespringboot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NativeSpringBootApplication

fun main(args: Array<String>) {
	runApplication<NativeSpringBootApplication>(*args)
}
