package org.reactivecommons.async.helpers;

import org.reactivecommons.async.rabbit.RabbitMessage;
import org.reactivecommons.async.commons.communications.Message;

import static org.reactivecommons.async.commons.Headers.*;

public class TestStubs {

    public static Message mockMessage() {
        Message.Properties properties = new RabbitMessage.RabbitMessageProperties();
        properties.getHeaders().put(REPLY_ID, "reply");
        properties.getHeaders().put(CORRELATION_ID, "correlation");
        properties.getHeaders().put(SERVED_QUERY_ID, "my-query");
        return new RabbitMessage("{\"id\":\"id\",\"name\":\"name\",\"date\":\"2020-10-22T17:03:26.062Z\"}".getBytes()
                , properties);
    }
}
