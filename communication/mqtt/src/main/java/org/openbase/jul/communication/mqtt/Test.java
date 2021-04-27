package org.openbase.jul.communication.mqtt;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Test {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Mqtt5AsyncClient client = MqttClient.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost("broker.hivemq.com")
                .useMqttVersion5()
                .buildAsync();

        try {
            client.connect().get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException | ExecutionException e) {
            System.out.println("Connection failed");
            e.printStackTrace();
        }

        final String topic = "/asdkjaflasjdhalsk/test";

        RPCServer rpcServer = new RPCServer(client, topic);
        rpcServer.activate();
        RPCRemote rpcRemote = new RPCRemote(client, topic);

        System.out.println("Call method");
        rpcRemote.callMethod("hello there");

        System.out.println("Wait...");
        Thread.sleep(3000);
        System.out.println("Finished");

        rpcServer.deactivate();
    }
}
