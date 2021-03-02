package xyz.hcabnettek.dgs.framework.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DgsFrameworkExampleApplication

fun main(args: Array<String>) {
	runApplication<DgsFrameworkExampleApplication>(*args)
}
