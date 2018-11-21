package us.sofka.commons.reactive.async;

import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import us.sofka.commons.reactive.async.api.AsyncQuery;
import us.sofka.commons.reactive.async.api.Command;
import us.sofka.commons.reactive.async.api.DirectAsyncGateway;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static reactor.core.publisher.Mono.fromCallable;

public class RabbitDirectAsyncGateway implements DirectAsyncGateway {

    private final ObjectMapper mapper = new ObjectMapper();

    private final MessageConfig.BrokerConfig config;
    private final MessageConfig.ReactiveReplyRouter router;
    private final ReactiveMessageSender sender;


    public RabbitDirectAsyncGateway(MessageConfig.BrokerConfig config, MessageConfig.ReactiveReplyRouter router, ReactiveMessageSender sender) {
        this.config = config;
        this.router = router;
        this.sender = sender;
    }

    @Override
    public <T> Mono<Void> sendCommand(Command<T> command, String targetName) {
        return sender.sendWithConfirm(command, targetName, Collections.emptyMap());
    }

    @Override
    public <T, R> Mono<R> requestReply(AsyncQuery<T> query, String targetName, Class<R> type) {
        final String correlationID = UUID.randomUUID().toString().replaceAll("-", "");

        final Mono<R> replyHolder = router.register(correlationID).timeout(Duration.ofSeconds(15)).flatMap(s ->
                fromCallable(() -> String.class.equals(type) ? type.cast(s) : mapper.readValue(s, type)));

        Map<String, Object> headers = new HashMap<>();
        headers.put("x-reply_id", config.getRoutingKey());
        headers.put("x-command-id", query.getResource());
        headers.put("x-correlation-id", correlationID);

        return sender.sendWithConfirm(query, targetName, headers).then(replyHolder);
    }

}
