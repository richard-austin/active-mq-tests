package org.example;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) throws Exception {
        thread(new HelloWorldProducer(), false);
        // thread(new HelloWorldProducer(), false);
        thread(new HelloWorldConsumer(), false);
        Thread.sleep(1000);
        // thread(new HelloWorldConsumer(), false);
        // hread(new HelloWorldProducer(), false);
        // thread(new HelloWorldConsumer(), false);
        // hread(new HelloWorldProducer(), false);
        Thread.sleep(1000);
        // thread(new HelloWorldConsumer(), false);
        // hread(new HelloWorldProducer(), false);
        // thread(new HelloWorldConsumer(), false);
        // thread(new HelloWorldConsumer(), false);
        // hread(new HelloWorldProducer(), false);
        // hread(new HelloWorldProducer(), false);
        Thread.sleep(1000);
        // hread(new HelloWorldProducer(), false);
        // thread(new HelloWorldConsumer(), false);
        // thread(new HelloWorldConsumer(), false);
        // hread(new HelloWorldProducer(), false);
        // thread(new HelloWorldConsumer(), false);
        // hread(new HelloWorldProducer(), false);
        // thread(new HelloWorldConsumer(), false);
        // hread(new HelloWorldProducer(), false);
        // thread(new HelloWorldConsumer(), false);
        // thread(new HelloWorldConsumer(), false);
        // hread(new HelloWorldProducer(), false);
    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }


    public static class HelloWorldProducer implements Runnable {
        Destination replys;
        public void run() {
            try {
                // Create a ConnectionFactory
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();

                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create the destination (Topic or Queue)
                Destination destination = session.createQueue("TEST");
                replys = session.createTemporaryQueue();


                // Create a MessageProducer from the Session to the Topic or Queue
                MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

                // Create a messages
                String text = "Hello world! From: " + Thread.currentThread().getName() + " : " + this.hashCode();
                TextMessage message = session.createTextMessage(text);
                message.setJMSReplyTo(replys);
                message.setJMSCorrelationID("correlationid");
                message.setIntProperty("token", 1234567);
                // Tell the producer to send the message
                System.out.println("Sent message: "+ message.hashCode() + " : " + Thread.currentThread().getName());
                producer.send(message);
                MessageConsumer consumer = session.createConsumer(replys);
                Message response = consumer.receive(1000);
                int newToken = response.getIntProperty("token");
                String ci = response.getJMSCorrelationID();
                String txtResponse = ((TextMessage)response).getText();
                // Clean up
                session.close();
                connection.close();
            }
            catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }
    }

    public static class HelloWorldConsumer implements Runnable, ExceptionListener {
        public void run() {
            try {

                // Create a ConnectionFactory
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();

                connection.setExceptionListener(this);

                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create the destination (Topic or Queue)
                Destination destination = session.createQueue("TEST");

                // Create a MessageConsumer from the Session to the Topic or Queue
                MessageConsumer consumer = session.createConsumer(destination);

                // Wait for a message
                Message message = consumer.receive(1000);
                int token =  message.getIntProperty("token");
                String correlationId = message.getJMSCorrelationID();
                Destination replyTo = message.getJMSReplyTo();
                MessageProducer producer = session.createProducer(null);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                String msg = "OK Then" + Thread.currentThread().getName() + " : " + this.hashCode();
                TextMessage responseMessage = session.createTextMessage(msg);
                responseMessage.setJMSCorrelationID(correlationId);
                responseMessage.setIntProperty("token", token+1);
                producer.send(replyTo, responseMessage);
                if (message instanceof TextMessage textMessage) {
                    String text = textMessage.getText();
                    System.out.println("Received: " + text);
                } else if(message != null){
                    System.out.println("Received: " + message);
                    String sp = message.getStringProperty("SP");
                    byte[] data = message.getBody(byte[].class);
                    byte[] x  = data;
                }

                consumer.close();
                session.close();
                connection.close();
            } catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }

        public synchronized void onException(JMSException ex) {
            System.out.println("JMS Exception occured.  Shutting down client.");
        }
    }
}
