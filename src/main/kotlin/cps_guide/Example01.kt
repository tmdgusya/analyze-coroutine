package cps_guide

import java.io.File
import java.io.FileReader
import java.lang.IllegalArgumentException

fun main() {

    println("Current Code Reader Thread : ${Thread.currentThread().name}")

    val file = File("test.txt") // 1

    println("Current Code Reader Thread : ${Thread.currentThread().name} .. Succeed LineNumber One")

    if (!file.exists()) { // 2
        throw IllegalArgumentException("No such file or directory") // 2 - No
    }

    println("Current Code Reader Thread : ${Thread.currentThread().name} .. Succeed LineNumber Two")

    val fileReader = FileReader(file).buffered() // 3

    println("Current Code Reader Thread : ${Thread.currentThread().name} .. Succeed LineNumber Three")

    val result = StringBuffer() // 4

    println("Current Code Reader Thread : ${Thread.currentThread().name} .. Succeed LineNumber Four")

    println("[FINISH] It'll be sending by email..")

    Thread {
        fileReader.forEachLine {// 5
            println("Current Code Reader Thread : ${Thread.currentThread().name} .. Looping")
            result.append(it) // in loop... 5
        }

        fileReader.close() // 6

        println("Current Code Reader Thread : ${Thread.currentThread().name} .. Succeed LineNumber Six")

        println(result.toString()) // 7

        println("Current Code Reader Thread : ${Thread.currentThread().name} .. Succeed LineNumber Seven")
    }.start()

    println("Current Code Reader Thread : ${Thread.currentThread().name} .. Succeed LineNumber Five")


}