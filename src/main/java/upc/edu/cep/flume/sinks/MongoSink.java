package upc.edu.cep.flume.sinks;


import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventHelper;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**

 */
public class MongoSink extends AbstractSink implements Configurable {

    private static final Logger logger = LoggerFactory.getLogger(MongoSink.class);


    @Override
    public synchronized void start() {
        super.start();
    }

    @Override
    public void configure(Context context) {

    }


    @Override
    public synchronized void stop() {
        super.stop();
    }

    @Override
    public Status process() throws EventDeliveryException {

        Status status = Status.READY;
        Channel channel = getChannel();
        Transaction tx = null;
        try {
            tx = channel.getTransaction();
            tx.begin();

            Event event = channel.take();

            if (event != null) {

                Map<String, String> headers = event.getHeaders();

                for (String key : headers.keySet()) {
                    System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ Key: " + key + ", Header: " + headers.get(key));
                }

                String line = EventHelper.dumpEvent(event);

                logger.debug(line);
                String eventName = headers.get("EventName");
                byte[] body = event.getBody();
                System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ Body: " + new String(body));

            } else {
                status = Status.BACKOFF;
            }

            tx.commit();
        } catch (Exception e) {
            System.out.println(e.toString());
            logger.error("can't process CEPElements, drop it!", e);
            if (tx != null) {
                tx.commit();// commit to drop bad event, otherwise it will enter dead loop.
            }

            throw new EventDeliveryException(e);
        } finally {
            if (tx != null) {
                tx.close();
            }
        }
        return status;
    }
}