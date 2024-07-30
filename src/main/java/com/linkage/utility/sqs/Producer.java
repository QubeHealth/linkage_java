package com.linkage.utility.sqs;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.linkage.LinkageConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Producer {

    private static String env = "test";

    public static void initialize(LinkageConfiguration configuration) {
        env = configuration.getRabbitMqConfig().getEnv();
    }

    public static void addInQueue(String exchangeName, String routingKey, String message)
            throws IOException {
        try {
            Channel channel = QueueConnection.getInstance().getConnection().createChannel();

            final String exchange = env.concat("_").concat(exchangeName);

            channel.exchangeDeclare(exchange, "topic", true);

            // Publish message
            channel.basicPublish(exchange, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN,
                    message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Exchange  '" + exchange + "'");
            System.out.println(" [x] Route key  '" + routingKey + "'");
            System.out.println(" [x] Sent '" + message + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}