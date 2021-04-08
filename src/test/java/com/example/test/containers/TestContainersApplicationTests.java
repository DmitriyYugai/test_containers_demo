package com.example.test.containers;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@Testcontainers
class TestContainersApplicationTests {

    private final static String QUEUE_NAME = "hello";

    @Container
    private RabbitMQContainer rabbitmqContainer = new RabbitMQContainer("rabbitmq")
            .withExposedPorts(5672);
//            .withVhost("/")
//            .withUser("admin", "admin")
//            .withPermission("/", "admin", ".*", ".*", ".*");

    @Test
    void testContainersTest() throws IOException, TimeoutException, InterruptedException {
        String host = rabbitmqContainer.getHost();
        Integer port = rabbitmqContainer.getMappedPort(5672);

        String expected = "Hello World!";
        String[] result = new String[1];
        ConnectionFactory sendFactory = new ConnectionFactory();
        sendFactory.setHost(host);
        sendFactory.setPort(port);
        sendFactory.setUsername("guest");
        sendFactory.setPassword("guest");
        try (Connection connection = sendFactory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, expected.getBytes(StandardCharsets.UTF_8));

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                result[0] = new String(delivery.getBody(), "UTF-8");
            };
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
            });
        }

//        Thread.sleep(1000);
//
//        String[] result = new String[1];
//        ConnectionFactory recvFactory = new ConnectionFactory();
//        recvFactory.setHost(host);
//        recvFactory.setPort(port);
//        recvFactory.setUsername("guest");
//        recvFactory.setPassword("guest");
//        try (Connection connection = recvFactory.newConnection();
//        Channel channel = connection.createChannel()) {
//            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//                result[0] = new String(delivery.getBody(), "UTF-8");
//            };
//            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
//            });
//        }

        Assertions.assertEquals(expected, result[0]);
    }

}
