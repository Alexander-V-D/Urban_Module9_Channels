import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

//Канал для первой задачи
val channel = Channel<Pair<Int, String>>()

//Канал для второй задачи
val newChannel = Channel<Pair<Int, String>>()

suspend fun main() = coroutineScope {
    println("Выберите задачу:\n1: Первая задача\n2: Вторая задача")
    when (readln()) {
        "1" -> runDecisionOfTask1()
        "2" -> runDecisionOfTask2()
    }
}

//Функция для задач 1 и 2
suspend fun getList(text: String) = coroutineScope {
    launch {
        getStringList(text).forEachIndexed { index, it ->
            launch {
                delay(10L)
                channel.send(Pair(index, it))
            }
        }
    }.join()
    channel.close()
}

//Функция для задач 1 и 2
fun getStringList(text: String): List<String> = text.split(" ")

//Функция для задачи 2
suspend fun modifiedList(text: String) = coroutineScope {
    launch {
        getStringList(text).forEachIndexed { index, it ->
            launch {
                val modified = it.first().uppercase() + it.drop(1)
                delay(10L)
                newChannel.send(Pair(index, modified))
            }
        }
    }.join()
    newChannel.close()
}

//Фунция для запуска решения для задачи 1
suspend fun runDecisionOfTask1() = coroutineScope {
    println("Решение для задачи 1:\n")
    val text = Storage().text
    var stringResult = ""
    val receiveList = mutableListOf<Pair<Int, String>>()
    val send = launch {
        getList(text)
    }
    val receive = launch {
        for (pair in channel) {
            receiveList.add(pair)
        }
        receiveList.sortBy { it.first }
        receiveList.forEach {
            stringResult += "${it.second} "
        }
        stringResult = stringResult.dropLast(1)
        println(stringResult)
        println("Проверка на равенство оригинала и полученной копии: ${stringResult == Storage().text}")
    }
    send.start()
    val time = measureTimeMillis {
        receive.join()
    }
    println("Затраченное время: $time мс\n")
}

//Фунция для запуска решения для задачи 2
suspend fun runDecisionOfTask2() = coroutineScope {
    println("Решение для задачи 2:\n")
    val time = measureTimeMillis {
        val text = Storage().text
        var stringResult = ""
        val receiveList = mutableListOf<Pair<Int, String>>()
        val uppercaseLetters = mutableListOf<Char>()
        val send = launch {
            modifiedList(text)
        }
        val receive = launch {
            for (pair in newChannel) {
                receiveList.add(pair)
            }
            receiveList.sortBy { it.first }
            receiveList.forEach {
                stringResult += "${it.second} "
            }
            stringResult = stringResult.dropLast(1)
        }
        send.start()
        receive.join()
        stringResult.forEach {
            if (it.isUpperCase()) uppercaseLetters.add(it)
        }
        println(uppercaseLetters)
    }
    println("Затраченное время: $time мс")
}