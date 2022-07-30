# CPS Style

## 들어가기에 앞서

이 글에서는 이를 이해하기 위해 알아야 할 기초 개념들을 내가 알고 있는 CS 지식으로 설명하려고 한다. 
여담이지만, 이 글의 깊이가 사실 내가 컴퓨터를 이해하고 있는 깊이와 비슷하다고 생각해도 좋다.. 그래서 잘못된 부분이 있으면 피드백해주면 정말 고마울 것 같다.

틀린 내용이 있을 수도 있습니다. (틀린 내용이 있다면 댓글로 알려주세요~! FeedBack 은 언제든지 환영입니다.)

## CPS Style?

CPS Style 은 suspend 의 개념을 익히기 위해서 반드시 알아야 하는 개념 중 하나라고 생각한다. 
일단 CPS 의 정의는 무엇일까? WikiPedia 의 설명으로는 아래와 같이 적혀있다.

> In functional programming, continuation-passing style (CPS) is a style of programming in which control is passed explicitly in the form of a continuation

간단하게 해석해 보면, **함수형 프로그래밍에서 Continuation 형태로 제어권을 명시적으로 넘기는 것**을 뜻한다. 
이렇게 개념적으로 정의해 둔것도 사실 엄청나게 추상적이여서 이해하기 힘들다고 생각한다. 
일단 CPS 를 이해하기 위해서는 우리의 코드가 어떻게 제어권을 관리하고 동작하는지에 대해서 약간은 아는 것이 중요하다고 생각한다.
아래의 그림을 한번 보자.

## Interpreter?

우리가 제어권(Control) 에 관한 개념을 이해하기 위해서는 내 생각에는 **일반적으로 컴퓨터는 코드를 한줄한줄 실행한다는 개념**을 이해하는게 좋다고 생각한다. 
(하지만 자신이 작성한 한줄의 코드가 컴퓨터에서 한줄이 아닌경우가 대다수지만.. 일단은 이해를 위해 한줄이라고 생각하자.)
Java 혹은 JVM Language 를 공부했고, JVM 에 관한 내용을 읽어봤다면 어떻게 자신의 코드가 실행되는지 대략적으로 알고 있을 거라고 생각한다.
내가 작성한 **"xx.java" 는 Compile 되면 "xx.class" 형태로 변하고 JVM 이 이를 한줄한줄 읽으며 Machine Language 로 번역**된다.
즉, Java 가 OS 에 Dependency 를 가지지 않고 실행할 수 있다고 얘기할 수 있는 이유도, Class 파일로 변경하면 JVM 은 각각 운영체제에 맞게 JRE 를 이용하면 되기 때문이다. 

일단 여기서 중요한건 **한줄한줄 실행**된다는게 상당히 중요하다. 왜 이 내용이 중요하냐면, 누군가는 한줄한줄 읽어야 한다는 뜻이기 때문이다. 
아래의 코드 예시를 한번 보자. 보면 간단하게 파일을 읽고 출력하는 예시 중 하나이다.

```kotlin
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

    fileReader.forEachLine {// 5
        println("Current Code Reader Thread : ${Thread.currentThread().name} .. Looping")
        result.append(it) // in loop... 5
    }

    println("Current Code Reader Thread : ${Thread.currentThread().name} .. Succeed LineNumber Five")

    fileReader.close() // 6

    println("Current Code Reader Thread : ${Thread.currentThread().name} .. Succeed LineNumber Six")

    println(result.toString()) // 7

    println("Current Code Reader Thread : ${Thread.currentThread().name} .. Succeed LineNumber Seven")
}
```

위 코드를 보면 어떤 Thread 가 Code 를 실행시키고 있는지 확인하는 코드가 포함되고 있다. 이를 실행시키면 아래와 같은 실행결과가 나오게 된다.

```shell
Current Code Reader Thread : main
Current Code Reader Thread : main .. Succeed LineNumber One
Current Code Reader Thread : main .. Succeed LineNumber Two
Current Code Reader Thread : main .. Succeed LineNumber Three
Current Code Reader Thread : main .. Succeed LineNumber Four
Current Code Reader Thread : main .. Looping
Current Code Reader Thread : main .. Looping
Current Code Reader Thread : main .. Looping
Current Code Reader Thread : main .. Looping
Current Code Reader Thread : main .. Looping
Current Code Reader Thread : main .. Looping
Current Code Reader Thread : main .. Looping
Current Code Reader Thread : main .. Succeed LineNumber Five
Current Code Reader Thread : main .. Succeed LineNumber Six
"Hello World""Hello World""Hello World""Hello World"
Current Code Reader Thread : main .. Succeed LineNumber Seven

Process finished with exit code 0
```

여기서 Main Thread 가 코드를 한줄한줄 읽으며 실행하고 있음을 알 수 있다. 우리는 이 결과값을 보면서 Main Thread 의 의무가 무엇인지를 간략하게 생각해 볼 수 있다. 
1. **일단 코드를 위에서 부터 아래로 순서대로 실행**
2. **모든 코드의 결과를 기다려서 결과값을 반환받기 위해 기다림**
3. **Main Thread 의 종료는 곧 프로그램의 종료**

여기서 사고를 조금 더 확장하기 위해 위의 예시 코드 중 일부를 들고 와 보았다.

```kotlin
    fileReader.forEachLine {// 5
        println("Current Code Reader Thread : ${Thread.currentThread().name} .. Looping")
        result.append(it) // in loop... 5
    }
```

위는 예시 코드 중 파일의 데이터를 읽어오는 부분인데, 만약 여기에 10GB Text 가 적혀있다면 어떻게 될까? 아마도 **Main Thread 는 엄청난 시간을 파일을 읽는데 소비해야 할 것이다. 
즉, Main Thread 는 해당 File 을 읽어오는 시간 동안 Blocking** 되게 된다. 이것이 일반적으로 말하는 프로그래밍에서의 Blocking 개념이다. 다만, 예시 코드 중 File 객체를 생성하고, 
if 문을 검증하고의 부분들도 엄연히 말하면 Blocking 코드지만, 대부분 **통용되는 Blocking 이라는 단어는 Thread 가 조금 오래 Blocking 되는 경우를 보통은 Blocking 된다고 말하는 것 같다.**


## NonBlocking

그럼 NonBlocking 은 Blocking 이 되지 않는건가? 그게 가능한건가? 라는 생각을 하게 될 수 있다. 일단 **NonBlocking 의 개념을 Blocking 이 없다 라고 생각하지말고,
Blocking 시간을 최소화 한다..** 라고 생각하는게 나는 좋다고 생각하는데, 일단 그렇게 생각하고 이 글을 읽어주면 될 것 같다. 그러면 우리는 이제 앞으로 NonBlocking 을
Blocking 시간을 최소화 한다 라는 뜻으로 이야기 할 것이다. (이에 관한 설명은 밑에 예시에서 아주 자세하게 나온다.)

## Controll

여기서 제어권(Control) 이라는 개념을 간단하게 설명하면 좋을 것 같은데, 제어권이란 간단히 설명하자면 코드를 실행하는 흐름을
누가 제어하느냐 라고 생각하면 편하다고 생각한다. 쉽게 설명하자면, Main Thread 는 기본적으로 제어권을 가지고 있다. 이를 한번 코드로 작성해보겠다.

```kotlin
class MainThread(
    private var currentCodeLine: Int = 0,
    private val control: Control = Control()
) {

    fun isCanNotReadNextLine(): Boolean {
        return this.hasNotControl()
    }

    fun hasNotControl(): Boolean {
        return this.control.isBlockingStatus()
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
```

Main Thread 는 기본적으로 제어권을 소유하고 있다. 코드를 약간 설명하자면 Contol(제어권) 의 isBlock 은 제어권이 현재 다른 실행 흐름에 의해 Blocking 
되어 있는지를 확인하는 Flag Value 이다. 이를 쉽게 얘기하기 위해 Main Thread 의 `goNextLine()` 메소드를 보면, **control(제어권) 없이는 다음 라인의 
코드를 읽는 것이 불가능 함**을 알 수 있다. 여기서 이해를 조금 더 쉽게 하기 위해 함수 객체인 Function Class 를 추가해 볼 것이다.

```kotlin
class Function<T>(
    private val control: Control,
    private val method: () -> T,
) {
    fun execute(): T {
        // 메소드를 실행
        val result = method.invoke()

        // 메소드의 결과가 모두 실행 됬다면 goReturn 을 통해 Control 의 상태도 True 로 바꿔줌.
        control.release()

        return result
    }
}
```

**함수는 생성될때 MainThread 로 부터 Control(제어권) 을 넘겨 받고, 실행될때 결과를 위한 과정들을 전부 마친 뒤에야 제어권을 release** 해준다. 
즉, 현재로서는 **해당 함수의 실행(결과값을 계산하는 행위) 가 끝나기 전까지 Main Thread 는 제어권이 존재하지 않아 Blocking** 이 되게 된다. 
그럼 이를 응용해서 우리가 실제로 코드를 실행시키는 것 처럼 한번 코드를 작성해보자.

```kotlin
fun main() {
    val mainThread = MainThread()

    val executionTime = measureTimeMillis {
        while (mainThread.isCanReadNextLine()) {

            Thread {
                Function(mainThread.giveOwnerShip()) {
                    println("Do some work...")
                    Thread.sleep(500)
                }.execute()
            }.start()
        }

        mainThread.goNextLine()
    }

    println("Execution Time : $executionTime")
}
```

이 코드를 설명하기 전에 코틀린을 모르는 사람이 이글을 읽고 있을 수도 있으므로, `measureTimeMills` 함수는 간단하게 함수의 실행시간을 측정하는 함수이다. 
일단 코드를 보면 while 문이 돌아가는 조건은 `isCanNotReadNextLine()` 일때이다. **즉, 우리의 코드로 이해해 보면, Main Thread 가 하나의 라인을 읽는데 
해당 함수가 끝나기 전까지는 다음 라인을 읽을 수 없다는 것**이다. 그럼 이 함수의 실행시간은 얼마나 걸리게 될까? 결과를 보면 아래와 같다.

```shell
Do some work...
Execution Time : 519
```

이제 우리는 이 NonBlocking 함수를 대략적으로 구현해 볼 것이다. 그때도 말했지만, 우리가 말하는 NonBlocking 은 Main Thread 의 Blocking Time 을 
최소화 하는 것이라고 했다. 그렇다면 코드로 어떻게 표현하면 될까?

```kotlin
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
```

일단은 간단하게 위처럼 표현하면 될 것이다. 함수의 결과값을 계산하기 전부터 제어권을 release 해버린다. **그렇게 되면 while 문을 탈출하게 될 것이고, Main Thread 
의 Blocking 시간은 최소화 될 것**이다.

```kotlin
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
```

위 처럼 코드를 실행하게 됬을때 나오는 결과값은 아래와 같다

```shell
Do some work...
Execution Time : 3
```

즉, **MainThread 는 Function 의 결과를 기다리지 않는다, 기다리지 않는다 라는 건 결국 Blocking 하지 않는 다는 것**이다. 즉, 이것이 NonBlocking 이다. 
여기까지 이해했다면 이제 NonBlocking 이 왜 Blocking 시간을 최소화 하는 것이라고 이야기 하는지 이해했을 것이라고 생각한다. 그리고 **왜 제어권(Control) 을 
곧 바로 넘기는 방법이 NonBlocking 을 하는 방법** 인지 이해했을 것이라고 생각한다.

## 문제 발생

이제 여기서 한가지 문제가 발생하는데, **위와 같은 코드는 다음 라인의 코드를 빠르게 실행 시킬 수는 있지만 결과 값을 받을 수 없다는 단점이 존재**한다. 
따라서 이를 위해 몇가지 방법론이 대두하게 되는데 그 중 하나가 CPS Style 이다. 위에 사전적 정의를 다시 들고와 보겠다.

> **함수형 프로그래밍에서 Continuation 형태로 제어권을 명시적으로 넘기는 것**

그럼 우리는 Continuation 이 뭔지 생각해 볼 필요가 있다. **우리가 생각해 봤을때, 제어권을 명시적으로 넘기는 녀석은 Control Class 
임을 알 수 있다. 즉, Control 이 우리의 Continuation** 이다. 근데 우리는 단순히 코드의 제어권을 넘기는 것 뿐만 아니라, 이제는 
기존과 똑같이 Blocking 시간을 최소화 하면서 함수의 결과값도 받아보고 싶다고 해보자. 그렇다면 어떻게 해야 할까? 

JavaScript 에서는 이를 보통 Callback Function 으로 처리하기도 한다. 어떻게 Callback 으로 이를 구현할 수 있는지는 한번 코드로 
알아보자.

```kotlin
class Promise<T>(
    private var worker: Thread? = null,
    private var result: T? = null,
    private val originFunction: () -> T,
) {

    init {
        if (result != null) throw AssertionError("Result is must be null")

        worker = Thread {
            result = originFunction.invoke()
        }

        worker?.start()
    }

    fun then(callback: (T) -> Any): Promise<T> {
        worker?.join()
        callback(result!!)
        return this
    }
}
```

Promise 코드를 보면 **내가 처음에 비동기로 돌리고 싶은 함수(originalFunction)** 를 넣어주게 되면 이를 다른 Thread 를 통해 비동기로 돌려준다. 
다만 이제 결과값을 then 을 통해 받을 수 있는데, then 의 경우에는 함수의 결과값(T) 를 통해 하고자 하는 callback Function 을 받는다. 
이를 통해 아래 코드와 같이 async-nonblocking 로 코드를 실행하는 것이 가능하다.

```kotlin
fun main() {

   val executionTime = measureTimeMillis {
       val promise1 = Promise<Int> {
           Thread.sleep(1000)
           return@Promise 1
       }

       val promise2 = Promise<Int> {
           Thread.sleep(2000)
           return@Promise 2
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
```

실행시간은 몇초가 나오게 될까? 2초가 나오게 된다.

```shell
Execution Time : 2009
```

이 쯤 되면, 왜 JavaScript 에서 비동기 함수를 처리할때 Callback 지옥에 빠졌는지 알 수 있을 것이다. Callback 지옥의 예시를 
재밌는 코드로 보여주면 아래와 같이 적어볼 수 있을 것 이다.

```kotlin
fun main() {

   val executionTime = measureTimeMillis {
       val promise1 = Promise<Int> {
           Thread.sleep(1000)
           return@Promise 1
       }

       val promise2 = Promise<Int> {
           Thread.sleep(2000)
           return@Promise 2
       }

       promise1.then { promise1Result ->
           println(promise1Result)
           val innerPromise1 = Promise<String> {
               Thread.sleep(500)
               return@Promise "Hello, "
           }

           innerPromise1.then { innerPromise1Result1 ->
               val innerPromise2 = Promise<String> {
                   Thread.sleep(500)
                   return@Promise "Roach!! "
               }

               innerPromise2.then {innerPromise2Result ->
                   println(innerPromise1Result1 + innerPromise2Result)
               }
           }
       }

       promise2.then {
           println(it)
       }
   }

    println("Execution Time : $executionTime")
}
```

이 함수의 실행이 오래걸릴 것 같아 보이지만, 오래 걸리지 않는다. 왜냐면 NonBlocking 으로 진행되기 때문이다.

```shell
1
Hello, Roach!! 
2
Execution Time : 2025
```

## 에러 처리 (Exception Handling)

이렇게 NonBlocking 형태로 코드를 작성하다보면 Error 처리가 힘들어지게 된다. 그 이유는 내가 실행하고자 하는 코드가 
현재 Context 에서 실행되지 않기 때문이다. 말이 어려워서 그런데 코드로 한번보면 이해가 빠르다. 아래 코드를 보기전에 
자신이 생각했을때 promise2 의 function 에서 에러가 난다면 어떻게 Controll 해야 할까를 생각해보자.

```kotlin
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
       }

       promise1.then {
           println(it)
       }

       try {
           promise2.then {
               println(it)
           }
       } catch (e: Exception) {
           println(e.message)
       }
   }

    println("Execution Time : $executionTime")
}
```

만약 위와 같이 Exception Handling 을 할것이라고 생각했다면 생각처럼 Exception Handling 이 되지 않을 것이다. 이렇게 
함수를 실행시키면 결과는 아래와 같다.

```kotlin
1
Exception in thread "Thread-1" java.lang.IllegalArgumentException: TEST Exception!
	at cps_guide.MainThreadKt$main$executionTime$1$promise2$1.invoke(MainThread.kt:127)
	at cps_guide.MainThreadKt$main$executionTime$1$promise2$1.invoke(MainThread.kt:125)
	at cps_guide.Promise._init_$lambda-0(MainThread.kt:86)
	at java.base/java.lang.Thread.run(Thread.java:829)
null
Execution Time : 2011
```

그 이유는 Promise 안의 생성자에서 함수가 실행되기 때문이다. 보통 JavaScript 나 다른 언어에서는 이를 해결하기 위해 
Error 를 핸들링 할 방법을 넣게 된다. `onError((err) -> Unit)` 과 같은 방식으로 말이다. 우리도 이 방법을 사용할 수 있도록 개선해보자.

```kotlin
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
```

위와 같이 errorHandler 를 직접 함수쪽에 넣어서 활용할 수 있도록 했다. 이 코드를 활용하면 아래와 같은 방법으로 Handler 를 추가 할 수 있을 것이다.

```kotlin
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
```

실행하게 되면 결과는 아래와 같다

```shell
1
TEST Exception!
-1
Execution Time : 2010
```

## 글 쓰면서 

글을 읽는 사람의 수준에 따라서 이 글이 엄청 어렵게 읽힐 수도 있고, 누군가 에겐 정말 쉬운 내용을 가르치고 있는 것일 수 있다고 생각된다. 
개인적으로 다양한 언어에서 공통적으로 사용되는 Style 은 그렇게 발전될수 밖에 없는 역사가 있다고 생각하는데,
이번 글에서는 CPS 패턴에 대해서 내가 아는 한도에서 최대한 코드로 보여줘서 표현하려고 노력했다. 

**개인적으로 틀린 내용이 있을 거라고 생각되는데, 이 지식에 대해서 조금 더 잘 아시는 분이라면 피드백을 주시면 감사 할 것 같습니다 :)**
