package org.example;

import org.apache.activemq.ActiveMQSslConnectionFactory;
//import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.command.*;

import javax.jms.*;
import javax.jms.Message;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) throws Exception {
        thread(new HelloWorldProducer(), false);
        thread(new HelloWorldConsumer(), false);


        // thread(new HelloWorldProducer(), false);

       // Thread.sleep(1000);
        // thread(new HelloWorldConsumer(), false);
        // hread(new HelloWorldProducer(), false);
        // thread(new HelloWorldConsumer(), false);
        // hread(new HelloWorldProducer(), false);
        // thread(new HelloWorldConsumer(), false);
        // hread(new HelloWorldProducer(), false);
        // thread(new HelloWorldConsumer(), false);
        // thread(new HelloWorldConsumer(), false);
        // hread(new HelloWorldProducer(), false);
        // hread(new HelloWorldProducer(), false);

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

    public static class AdvisoryMonitor implements MessageListener {
        Session session;
        Destination destination;
        AdvisoryMonitor(Session session, Destination destination) {
            this.session = session;
            this.destination = destination;
            setup();
        }
        Destination advisoryDestination;
        MessageConsumer consumer;
        private void setup() {
            try {
                advisoryDestination = AdvisorySupport.getConsumerAdvisoryTopic(destination);
                consumer = session.createConsumer(advisoryDestination);
                consumer.setMessageListener(this);
            }
            catch(JMSException ex) {
             System.out.println("Exception: "+ex.getMessage());
            }
        }

        public void onMessage(Message msg){
            if(msg instanceof ActiveMQMessage) {
                try {
                    ActiveMQMessage aMsg = (ActiveMQMessage)msg;
                    DataStructure dataStructure = aMsg.getDataStructure();
                    if(dataStructure instanceof ProducerInfo) {
                        Object obj = (ProducerInfo)dataStructure;
                    }
                    else if(dataStructure instanceof ConsumerInfo) {
                        ConsumerInfo cons = (ConsumerInfo) aMsg.getDataStructure();
                        System.out.println("ConsumerInfo Message received: consumerCount = " + aMsg.getIntProperty("consumerCount"));
                    }
                    else if(dataStructure instanceof RemoveInfo) {
                        RemoveInfo ri = (RemoveInfo) dataStructure;
                        System.out.println("RemoveInfo Message received: consumerCount = " + aMsg.getIntProperty("consumerCount"));
                    }
                }
                catch(Exception e) {
                  //  log.error("Failed to process message: " + msg);
                }
            }
            else
                System.out.println("Something else");
        }
    }

    public static class HelloWorldProducer implements Runnable,  MessageListener  {
        Destination replys;
        public void run() {
            try {
                // Create a ConnectionFactory
                ActiveMQSslConnectionFactory connectionFactory = new ActiveMQSslConnectionFactory("failover://ssl://localhost:61617?socket.verifyHostName=false");
                connectionFactory.setKeyStore("/home/richard/client.ks");
                connectionFactory.setKeyStoreKeyPassword("password");
                connectionFactory.setTrustStore("/home/richard/client.ts");
                connectionFactory.setTrustStorePassword("password");
                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();


                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                // Create the destination (Topic or Queue)
                Destination destination = session.createQueue("TEST");
                AdvisoryMonitor am = new AdvisoryMonitor(session, destination);
              //  Thread.sleep(1200000);
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
                consumer.setMessageListener(this);
               Thread.sleep(100);
                session.close();
                connection.close();
            }
            catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }

        @Override
        public void onMessage(Message response) {
            try {
                int newToken = response.getIntProperty("token");
                String ci = response.getJMSCorrelationID();
                String txtResponse = ((TextMessage) response).getText();
                String x = txtResponse;
                System.out.println("Received: newToken = "+newToken+": correlationID = "+ci+" txtReponse ="+txtResponse);
            }
            catch(JMSException ex) {
                System.out.println(ex.getMessage());
            }

        }
    }

    static Session session;
    static MessageConsumer consumer;
    static Connection connection;
    public static class HelloWorldConsumer implements Runnable, ExceptionListener, MessageListener {
        public void run() {
            try {

                // Create a ConnectionFactory
                ActiveMQSslConnectionFactory connectionFactory = new ActiveMQSslConnectionFactory("failover://ssl://localhost:61617?socket.verifyHostName=false");
                connectionFactory.setKeyStore("/home/richard/client.ks");
                connectionFactory.setKeyStoreKeyPassword("password");
                connectionFactory.setTrustStore("/home/richard/client.ts");
                connectionFactory.setTrustStorePassword("password");

                // Create a Connection
                connection = connectionFactory.createConnection();
                connection.start();

                connection.setExceptionListener(this);

                // Create a Session
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create the destination (Topic or Queue)
                Destination destination = session.createQueue("TEST");

                // Create a MessageConsumer from the Session to the Topic or Queue
                consumer = session.createConsumer(destination);
                consumer.setMessageListener(this);
                // Wait for a message
               // Message message = consumer.receive(1000);
            } catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }

        public synchronized void onException(JMSException ex) {
            System.out.println("JMS Exception occured.  Shutting down client.");
        }

        @Override
        public void onMessage(Message message) {
            try {
                int token = message.getIntProperty("token");
                String correlationId = message.getJMSCorrelationID();
                Destination replyTo = message.getJMSReplyTo();
                MessageProducer producer = session.createProducer(null);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                String msg = "OK Then: " + Thread.currentThread().getName() + " : " + this.hashCode();
                TextMessage responseMessage = session.createTextMessage(msg);
                responseMessage.setJMSCorrelationID(correlationId);
                responseMessage.setIntProperty("token", token + 1);
                producer.send(replyTo, responseMessage);
                if (message instanceof TextMessage textMessage) {
                    String text = textMessage.getText();
                    System.out.println("Received text message: " + text);
                } else if (message != null) {
                    System.out.println("Received: " + message);
                    byte[] data = message.getBody(byte[].class);
                    byte[] x = data;
                }


                consumer.close();
                //Thread.sleep(1000);
                session.close();
                connection.close();
            }
            catch(Exception ex) {
                System.out.println(ex.getMessage());
            }

        }
    }
}
