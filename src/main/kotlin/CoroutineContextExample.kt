import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class User(
    val name: String,
    val age: Int
): AbstractCoroutineContextElement(User) {
    companion object Key: CoroutineContext.Key<User>
}

suspend fun test() = coroutineScope {
    launch(User(name = "roach", age = 10) + Dispatchers.IO) {
        val userAge = coroutineContext[User]?.age
        val userName = coroutineContext[User]?.name
    }
}