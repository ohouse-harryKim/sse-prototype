package se.ohou.sseprototype

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SsePrototypeApplication

fun main(args: Array<String>) {
    runApplication<SsePrototypeApplication>(*args)
}
