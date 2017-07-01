package upc.edu.cep.flume.sinks;

import com.espertech.esper.client.*;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventHelper;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import upc.edu.cep.events.LogEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**

 */
public class CEPSink2 extends AbstractSink implements Configurable {

    private static final Logger logger = LoggerFactory.getLogger(CEPSink2.class);

    private EPServiceProvider epService;

    @Override
    public synchronized void start() {
        super.start();

        // Configuration
        Configuration config = new Configuration();
        config.addEventType("com.edu.cep.CEPElements.LogEvent", LogEvent.class.getName());
         epService = EPServiceProviderManager.getDefaultProvider(config);


        // Creating a Statement
        String expression = "select count(log) from com.edu.cep.CEPElements.LogEvent.win:time(2 sec)"; //time_batch
        EPStatement statement = epService.getEPAdministrator().createEPL(expression);


        // Adding a Listener
        MyListener listener = new MyListener();
        statement.addListener(listener);
    }

    @Override
    public void configure(Context context) {
//test
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

                Map<String,String> headers = event.getHeaders();

                String line = EventHelper.dumpEvent(event);
                logger.debug(line);

                byte[] body = event.getBody();
                String data = new String(body);
                /*System.out.println(data);
                System.out.println(headers.get("hostname"));
                System.out.println(Long.parseLong(headers.get("timestamp")));*/
                // Sending CEPElements
                LogEvent cepEvent = new LogEvent();
                cepEvent.setLog(data);
                if(headers != null) {
                    cepEvent.setHostname(headers.get("hostname"));
                    cepEvent.setTimestamp(Long.parseLong(headers.get("timestamp")));
                }

                epService.getEPRuntime().sendEvent(cepEvent);

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

    /**
     * Here are Biz codes.
     */
    public class MyListener implements UpdateListener {

        public void update(EventBean[] newEvents, EventBean[] oldEvents) {
            try {
                if (newEvents == null) {

                    return;
                }
                EventBean event = newEvents[0];
                System.out.println("Count: " + getChannel().getName() + "   " + event.get("count(log)") + "**************");
                try {
                    Files.write(Paths.get("/home/osboxes/upc-cep/cep2.txt"), ("Count: "+ event.get("count(log)") + "**************").getBytes(), StandardOpenOption.APPEND);
                }catch (IOException e) {
                    //exception handling left as an exercise for the reader
                }
                //logger.info("Count: "+ event.get("count(log)") + "**************");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}