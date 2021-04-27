package org.openbase.jul.communication.mqtt;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class RPCRemote {

    private final Mqtt5AsyncClient mqttClient;
    private final String topic;

    public RPCRemote(final Mqtt5AsyncClient client, final String topic) {
        this.mqttClient = client;
        this.topic = topic + "/rpc";
    }

    public void callMethod(final String methodName) throws ExecutionException, InterruptedException {
        final String id = UUID.randomUUID().toString();

        mqttClient.subscribeWith()
                .topicFilter(this.topic + "/" + id)
                .qos(MqttQos.EXACTLY_ONCE)
                .callback(mqtt5Publish -> {
                    ResponseType.Response response = null;
                    try {
                        response = ResponseType.Response.parseFrom(mqtt5Publish.getPayloadAsBytes());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Received response: " + response.getStatus().name());
                }).send().get();

        RequestType.Request.Builder requestBuilder = RequestType.Request.newBuilder();
        requestBuilder.setId(id);
        requestBuilder.setMethodName(methodName);
        //TODO set params
        mqttClient.publishWith()
                .topic(this.topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(requestBuilder.build().toByteArray())
                .send().get();

        //TODO handle methods correctly
        //TODO return Future related to the callback above
    }
}
