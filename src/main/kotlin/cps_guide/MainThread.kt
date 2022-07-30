package cps_guide

import java.lang.AssertionError
import java.lang.IllegalArgumentException
import kotlin.system.measureTimeMillis

/**
 * 기본적으로 코드를 실행하는 Thread 제어권과 몇번째 코드 라인을 읽었는지를 기록하고 있다.
 */
class MainThread(
    private var currentCodeLine: Int = 0,
    private val control: Control = Control()
) {

    fun hasNotControl(): Boolean {
        return this.control.isBlockingStatus()
    }

    fun isCanNotReadNextLine(): Boolean {
        return this.hasNotControl()
    }

    fun hasControl(): Boolean {
        return !this.control.isBlockingStatus()
    }

    fun goNextLine() {
        if (hasNotControl()) {
            throw IllegalAccessException("you have not ownership")
        }
        currentCodeLine++
    }

    fun giveOwnerShip(): Control {
        // 소유권을 넘겨주며 Blocking 을 건다.
        this.control.block()
        return this.control
    }
}

class Function<T>(
    private val control: Control,
    private val method: () -> T,
) {
    fun execute(): T {
        // 제어권을 잡기위해 Blocking 한다.
        control.block()

        // 메소드를 실행
        val result = method.invoke()

        // 메소드의 결과가 모두 실행 됬다면 goReturn 을 통해 Control 의 상태도 True 로 바꿔줌.
        control.release()

        return result
    }
}

class NonBlockingFunction<T>(
    private val control: Control,
    private val method: () -> T,
    private val callback: (T) -> Promise<T>
) {
    fun execute(): T {
        // 일단 Return 가능
        control.release()

        // 메소드를 실행
        val result = method.invoke()

        return result
    }
}


class Promise<T>(
    private var worker: Thread? = null,
    private var result: T? = null,
    private var isError: Boolean? = true,
    private var errorHandler: ((e: Exception) -> T)? = null,
    private val originFunction: () -> T,
) {

    init {
        if (result != null) throw AssertionError("Result is must be null")

        worker = Thread {
            result = try {
                originFunction.invoke()
            } catch (e: java.lang.Exception) {
                isError = true
                errorHandler?.let { it(e) } ?: throw e
            }
        }

        worker?.start()
    }

    fun then(callback: (T) -> Any): Promise<T> {
        worker?.join()
        callback(result!!)
        return this
    }

    fun onError(handler: (e: Exception) -> T): Promise<T> {
        this.errorHandler = handler
        return this
    }
}

class Control(
    private var isBlock: Boolean = true
) {

    fun isBlockingStatus(): Boolean {
        return this.isBlock
    }

    fun release() {
        this.isBlock = false
    }

    fun block() {
        this.isBlock = true
    }

}

fun main() {

   val executionTime = measureTimeMillis {
       val promise1 = Promise<Int> {
           Thread.sleep(1000)
           return@Promise 1
       }

       val promise2 = Promise<Int> {
           Thread.sleep(2000)
           throw IllegalArgumentException("TEST Exception!")
           return@Promise 2
       }.onError { err ->
           println(err.message)
           return@onError -1
       }

       promise1.then {
           println(it)
       }

       promise2.then {
           println(it)
       }
   }

    println("Execution Time : $executionTime")
}