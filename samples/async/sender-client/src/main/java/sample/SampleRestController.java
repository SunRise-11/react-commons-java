package sample;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.reactivecommons.async.api.AsyncQuery;
import org.reactivecommons.async.api.DirectAsyncGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class SampleRestController {

    @Autowired
    private DirectAsyncGateway directAsyncGateway;
    private final String queryName = "query1";
    private final String queryName2 = "query2";
    private final String target = "receiver";

    private final WebClient webClient = WebClient.builder().build();

    @PostMapping(path = "/sample", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<RespQuery1> sampleService(@RequestBody Call call) {
        AsyncQuery<?> query = new AsyncQuery<>(queryName, call);
        return directAsyncGateway.requestReply(query, target, RespQuery1.class);
    }

    @PostMapping(path = "/sample2", consumes = MediaType.APPLICATION_JSON_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public Mono<RespQuery1> sampleServiceDelegate(@RequestBody Call call) {
        AsyncQuery<?> query = new AsyncQuery<>(queryName2, call);
        return directAsyncGateway.requestReply(query, target, RespQuery1.class);
    }

    @PostMapping(path = "/sample_http", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<RespQuery1> sampleServiceHttp(@RequestBody Call call) {
        DummyQuery dummyQuery = new DummyQuery(queryName, call);
        final Mono<RespQuery1> response = webClient.post().uri("http://127.0.0.1:4004/sample_destination")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(dummyQuery)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(RespQuery1.class);
        return response;
    }

    @PostMapping(path = "/sample_destination", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<RespQuery1> sampleReceiver(@RequestBody DummyQuery query) {
        RespQuery1 respQuery1 = new RespQuery1("OK", query.getCall());
        return Mono.just(respQuery1);
    }


    @Data
    @AllArgsConstructor
    static class RespQuery1 {
        private String response;
        private Call request;
    }


    @Data
    @AllArgsConstructor
    static class Call {
        private String name;
        private String phone;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class DummyQuery{
        private String resource;
        private Call call;
    }
}
