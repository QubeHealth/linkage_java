package com.linkage.utility.sqs;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.linkage.LinkageConfiguration;

public class QueueConnection {

    private static QueueConnection instance;
    public static Connection connection;

    public QueueConnection() {

    }

    public QueueConnection(LinkageConfiguration configuration) {
        // Configure RabbitMQ connection
        ConnectionFactory rabbitConnectionFactory = new ConnectionFactory();
        rabbitConnectionFactory.setUsername(configuration.getRabbitMqConfig().getUserName());
        rabbitConnectionFactory.setPassword(configuration.getRabbitMqConfig().getPassword());
        rabbitConnectionFactory.setVirtualHost(configuration.getRabbitMqConfig().getVirtualHost());
        rabbitConnectionFactory.setHost(configuration.getRabbitMqConfig().getHostName());
        rabbitConnectionFactory.setPort(configuration.getRabbitMqConfig().getPortNumber());
        Producer.initialize(configuration);

        try {
            // Create RabbitMQ connection
            connection = rabbitConnectionFactory.newConnection();
        } catch (Exception e) {
            e.printStackTrace(); // Handle connection creation failure
        }
    }

    public static synchronized QueueConnection getInstance() {
        if (instance == null) {
            instance = new QueueConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

}