## Just some tests on ActiveMQ
* Copy the the files in the *Put these in home dir* directory to home directory
* Edit the path to these files in the App.java as appropriate.
* Install ActiveMQ message broker 
* Add the following line to the **\<transportConnectors\>** section in ${active_mq_dir}/conf/activemq.xml
```xml
<transportConnector name="ssl" uri="ssl://0.0.0.0:61617?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
```
* Add the following to the **\<brokers\>** section in ${active_mq_dir}/conf/activemq.xml

```xml
 <sslContext>
     <sslContext keyStore="file:/home/richard/broker.ks" 
                 keyStorePassword="password"/>
 </sslContext>
```
