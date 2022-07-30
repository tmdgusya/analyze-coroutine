package cps_guide

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
) {
    fun execute(): T {
        // 일단 Return 가능
        control.release()

        // 메소드를 실행
        val result = method.invoke()

        return result
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
    val mainThread = MainThread()

    val executionTime = measureTimeMillis {
        while (mainThread.isCanNotReadNextLine()) {

            Thread {
                NonBlockingFunction(mainThread.giveOwnerShip()) {
                    println("Do some work...")
                    Thread.sleep(500)
                }.execute()
            }.start()
        }

        mainThread.goNextLine()
    }

    println("Execution Time : $executionTime")
}