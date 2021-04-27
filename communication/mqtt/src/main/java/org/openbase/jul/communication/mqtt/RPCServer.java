package org.openbase.jul.communication.mqtt;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class RPCServer {

    private final Mqtt5AsyncClient mqttClient;
    private final String topic;

    public RPCServer(final Mqtt5AsyncClient client, final String topic) {
        this.mqttClient = client;
        this.topic = topic + "/rpc";
    }

    public void activate() {
        this.mqttClient.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .callback(new InternalSubscriber(this.mqttClient, this.topic))
                .send();
    }

    public void deactivate() {
        this.mqttClient.unsubscribeWith()
                .topicFilter(topic)
                .send();
    }
}

class InternalSubscriber implements Consumer<Mqtt5Publish> {

    private final Mqtt5AsyncClient mqttClient;
    private String topic;

    public InternalSubscriber(final Mqtt5AsyncClient mqttClient, final String topic) {
        this.mqttClient = mqttClient;
        this.topic = topic;
    }

    @Override
    public void accept(Mqtt5Publish mqtt5Publish) {
        ResponseType.Response.Builder responseBuilder = ResponseType.Response.newBuilder();
        try {
            RequestType.Request request = RequestType.Request.parseFrom(mqtt5Publish.getPayloadAsBytes());

            this.topic += "/" + request.getId();

            responseBuilder.setStatus(ResponseType.Response.Status.ACKNOWLEDGED);
            responseBuilder.setId(request.getId());
            mqttClient.publishWith()
                    .topic(this.topic)
                    .qos(MqttQos.EXACTLY_ONCE)
                    .payload(responseBuilder.build().toByteArray())
                    .send(); //TODO call get?

            //TODO open thread to send PROGRESSING message periodically

            try {
                //TODO call method as defined in request.getMethodName();
                request.getMethodName();
                responseBuilder.setStatus(ResponseType.Response.Status.FINISHED);
                responseBuilder.setResult(ByteString.copyFrom(request.getMethodName().getBytes(StandardCharsets.UTF_8)));
            } catch (Exception ex) {
                responseBuilder.setStatus(ResponseType.Response.Status.FINISHED);
                responseBuilder.setError(ex.getMessage()); //TODO: build message accordingly from stackstrace...
            }

            mqttClient.publishWith()
                    .topic(this.topic)
                    .qos(MqttQos.EXACTLY_ONCE)
                    .payload(responseBuilder.build().toByteArray())
                    .send(); //TODO call get?
        } catch (InvalidProtocolBufferException e) {
            // We cannot inform the client here because we do not know
            // on which topic the client listens, so just log the error
            //TODO: log err
        }
    }
}

interface Callback<R extends Message> {

    R call();
}
