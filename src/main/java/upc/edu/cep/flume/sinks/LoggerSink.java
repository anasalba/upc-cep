package upc.edu.cep.flume.sinks;

import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventHelper;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerSink extends AbstractSink implements Configurable {

    private static final Logger logger = LoggerFactory.getLogger(LoggerSink.class);

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
                String line = EventHelper.dumpEvent(event);
                logger.debug(line);

                byte[] body = event.getBody();
                String data = new String(body);

                System.out.println(data);
                logger.debug(data);
                System.out.println(data);
            } else {
                status = Status.BACKOFF;
            }

            tx.commit();
        } catch (Exception e) {
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