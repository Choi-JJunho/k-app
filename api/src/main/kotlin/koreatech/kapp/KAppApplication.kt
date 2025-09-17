package koreatech.kapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KAppApplication

fun main(args: Array<String>) {
    runApplication<KAppApplication>(*args)
}