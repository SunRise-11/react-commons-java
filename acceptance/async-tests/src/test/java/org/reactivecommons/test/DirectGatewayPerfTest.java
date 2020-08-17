package org.reactivecommons.test;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivecommons.api.domain.Command;
import org.reactivecommons.async.api.DirectAsyncGateway;
import org.reactivecommons.async.api.HandlerRegistry;
import org.reactivecommons.async.impl.RabbitDirectAsyncGateway;
import org.reactivecommons.async.impl.config.annotations.EnableDirectAsyncGateway;
import org.reactivecommons.async.impl.config.annotations.EnableMessageListeners;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static reactor.core.publisher.Flux.range;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DirectGatewayPerfTest {

    private static final String COMMAND_NAME = "app.command.test";
    private static final int messageCount = 40000;
    private static final Semaphore semaphore = new Semaphore(0);

    @Autowired
    private RabbitDirectAsyncGateway gateway;

    @Value("${spring.application.name}")
    private String appName;


    @Test
    public void shouldSendInOptimalTime() throws InterruptedException {
        final Flux<Command<DummyMessage>> messages = createMessages(messageCount);
        final Flux<Void> target = messages.flatMap(dummyMessageCommand ->
            gateway.sendCommand(dummyMessageCommand, appName)
            .doOnSuccess(aVoid -> semaphore.release()));

        final long init = System.currentTimeMillis();
        target.subscribe();
        semaphore.acquire(messageCount);
        final long end = System.currentTimeMillis();

        assertMessageThroughput(end - init, messageCount, 200);
    }

    @Test
    public void shouldSendBatchInOptimalTime4Channels() throws InterruptedException {
        shouldSendBatchInOptimalTimeNChannels(4);
    }

    @Test
    public void shouldSendBatchInOptimalTime2Channels() throws InterruptedException {
        shouldSendBatchInOptimalTimeNChannels(2);
    }

    @Test
    public void shouldSendBatchInOptimalTime1Channel() throws InterruptedException {
        shouldSendBatchInOptimalTimeNChannels(1);
    }

    private void shouldSendBatchInOptimalTimeNChannels(int channels) throws InterruptedException {
        List<Mono<Void>> subs = new ArrayList<>(channels);
        for (int i = 0; i < channels; ++i) {
            final Flux<Command<DummyMessage>> messages = createMessages(messageCount/channels);
            final Mono<Void> target = gateway.sendCommands(messages, appName).then().doOnSuccess(_v -> semaphore.release());
            subs.add(target);
        }

        final long init = System.currentTimeMillis();
        subs.forEach(Mono::subscribe);
        System.out.println("Wait for publish");
        semaphore.acquire(channels);
        final long end = System.currentTimeMillis();

        final long total = end - init;
        assertMessageThroughput(total, messageCount, 230);
    }

    private void assertMessageThroughput(long total, long messageCount, int reqMicrosPerMessage) {
        final double microsPerMessage =  ((total+0.0)/messageCount)*1000;
        System.out.println("Message count: " + messageCount);
        System.out.println("Total Execution Time: " + total + "ms");
        System.out.println("Microseconds per message: " + microsPerMessage + "us");
        System.out.println("Throughput: " + Math.round(messageCount/(total/1000.0)) + " Msg/Seg");
        Assertions.assertThat(microsPerMessage).isLessThan(reqMicrosPerMessage);
    }

    private Flux<Command<DummyMessage>> createMessages(int count) {
        final List<Command<DummyMessage>> commands = IntStream.range(0, count).mapToObj(value -> new Command<>(COMMAND_NAME, UUID.randomUUID().toString(), new DummyMessage())).collect(Collectors.toList());
        return Flux.fromIterable(commands);
    }

    @SpringBootApplication
    @EnableDirectAsyncGateway
    static class App{
        public static void main(String[] args) {
            SpringApplication.run(App.class, args);
        }

    }
}
