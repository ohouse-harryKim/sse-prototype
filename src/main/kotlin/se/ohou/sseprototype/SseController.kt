package se.ohou.sseprototype

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

@RestController
@CrossOrigin(origins = ["*"])
class SseController(
    private val processor: MessageProcessor
) {

    @GetMapping("/sse/{id}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun sse(@PathVariable id: String): Flux<String> {
        val userKey = UUID.randomUUID().toString()
        return processor.listen(id, userKey)
    }
}

@Component
class MessageProcessor {

    // manage listeners
    private val listenersMap: MutableMap<String, MutableMap<String, Consumer<String>>> = ConcurrentHashMap()

    fun register(id: String, userKey: String, consumer: Consumer<String>) {

        listenersMap.computeIfAbsent(id) { ConcurrentHashMap() }
            .apply {
                putIfAbsent(userKey, consumer)
            }

        // todo. pub register event to redis

        process(id, userKey)
    }

    fun remove(id: String, userKey: String) {
        listenersMap[id]?.apply {
            remove(userKey)
            if (this.isEmpty()) {
                listenersMap.remove(id)
            }
        }

        // todo. pub remove event to redis

        process(id, userKey)
    }


    fun process(id: String, userKey: String) {

        // todo. sub

        val count = listenersMap[id]?.size.toString()
        listenersMap[id]?.let {
            it.values.forEach { consumer ->
                consumer.accept(count)
            }
        }
    }

    // todo. sub event from redis

    fun listen(id: String, userKey: String): Flux<String> {
        return Flux.create<String?> {
            register(id, userKey, it::next)
        }.doFinally {
            remove(id, userKey)
        }
    }
}