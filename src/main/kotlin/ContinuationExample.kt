import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

/**
 * File Read 를 하는 함수
 */
fun a() {
    println("File Reading....")
}

fun b() {
    println("Read Another file")
}

fun c() {
    println("Read Another file")
}

fun callback() {
    println("File read Done")
}

class ContinuationExample(override val context: CoroutineContext = EmptyCoroutineContext) : Continuation<Unit> {
    override fun resumeWith(result: Result<Unit>) {
        callback()
    }

}

fun main() {
    val mainCoroutine = MainContinuationImpl()
    mainCoroutine.resumeWith(Result.success(Unit))
}

class MainContinuationImpl(
    var label: Int = 0,
    override val context: CoroutineContext = EmptyCoroutineContext
): Continuation<Unit> {


    override fun resumeWith(result: Result<Unit>) {
        when(label) {
            0 -> {
                label = 1
                a()
                callback() // a.resumeWith()
                this.resumeWith(Result.success(Unit))
            }
            1 -> {
                label = 2
                b()
                this.resumeWith(Result.success(Unit))
            }
            2 -> {
                c()
                return
            }
        }
    }
}