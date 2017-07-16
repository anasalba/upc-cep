package upc.edu.cep.kafka.producers;

/**
 * Created by osboxes on 04/04/17.
 */

import com.google.common.io.Resources;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


//  /home/osboxes/apache-flume-1.7.0-bin/bin/flume-ng agent -name remote_agent -c /home/osboxes/apache-flume-1.7.0-bin/conf -f /home/osboxes/apache-flume-1.7.0-bin/conf/5-4-2017-jsoneventtest.properties


public class multipleTest {
    //hi
    public static void main(String[] args) throws Exception {
        KafkaProducer<String, String> producer;
        try (InputStream props = Resources.getResource("producer.props").openStream()) {
            Properties properties = new Properties();
            properties.load(props);
            properties.put("partitioner.class", "upc.edu.cep.kafka.partitioners.SimplePartitioner");
            producer = new KafkaProducer<>(properties);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String key = "key1";

        try {
            Files.write(Paths.get("/home/osboxes/upc-cep/testsend.txt"), ("\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(Paths.get("/home/osboxes/upc-cep/testfinishsend.txt"), ("\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(Paths.get("/home/osboxes/upc-cep/testdone.txt"), ("\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
        }

        Random rand = new Random(System.currentTimeMillis());
        Random r = new Random(System.currentTimeMillis());

        EventA eventA = new EventA();
        EventB eventB = new EventB();
        EventC eventC = new EventC();
        EventD eventD = new EventD();
        EventE eventE = new EventE();
        ProducerRecord record;

        for (int itr = 0; itr < 1; itr++) {
            try {
                Files.write(Paths.get("/home/osboxes/upc-cep/testsend.txt"), (System.currentTimeMillis() + "\n").getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
            }
            Long i = 0l;
            long limit = 1000000;
            long limitless = limit - 1000;
            while (limit > i++) {

                int c = rand.nextInt(100);
                if (c < 50) {
                    eventD.setA("a");
                    eventD.setB(r.nextLong()%20);
                    if (i == limitless) {
                        eventD.setA("final");
                        eventD.setB(5);
                    }
                    record = new ProducerRecord<String, byte[]>("dstream", i.toString(), objectMapper.writeValueAsBytes(eventD));
                } else if (c < 65) {
                    eventA.setA("a");
                    eventA.setB("b");
                    eventA.setC(r.nextInt(6));
                    if (i == limitless) {
                        eventA.setA("final");
                        eventA.setC(1);
                    }
                    record = new ProducerRecord<String, byte[]>("astream", i.toString(), objectMapper.writeValueAsBytes(eventA));
                } else if (c < 80) {
                    eventB.setA("a");
                    eventB.setB("b");
                    eventB.setC(r.nextDouble()*15);
                    if (i == limitless) {
                        eventB.setA("final");
                        eventB.setC(1);
                    }
                    record = new ProducerRecord<String, byte[]>("bstream", i.toString(), objectMapper.writeValueAsBytes(eventB));
                } else if (c < 95) {
                    eventC.setA("a");
                    eventC.setB(r.nextInt(8));
                    if (i == limitless) {
                        eventC.setA("final");
                        eventC.setB(1);
                    }
                    record = new ProducerRecord<String, byte[]>("cstream", i.toString(), objectMapper.writeValueAsBytes(eventC));
                } else {
                    eventE.setA("a");
                    eventE.setB(r.nextInt(12));
                    if (i == limitless) {
                        eventE.setA("final");
                        eventE.setB(1);
                    }
                    record = new ProducerRecord<String, byte[]>("estream", i.toString(), objectMapper.writeValueAsBytes(eventE));
                }


                try {
                    producer.send(record);
                } catch (SerializationException e) {
                    // may need to do something with it
                    e.printStackTrace();
                }
                //Thread.sleep(0,01);
                //TimeUnit.MICROSECONDS.sleep(100);
                //busyWaitNanos(1000);

            }
            try {
                Files.write(Paths.get("/home/osboxes/upc-cep/testfinishsend.txt"), (System.currentTimeMillis() + "\n").getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
            }


        }
        //producer.close();
    }

    public static void busyWaitNanos(long nanos) {
        long waitUntil = System.nanoTime() + (nanos);
        while (waitUntil > System.nanoTime()) {
            ;
        }
    }

    public static byte[] datumToByteArray(Schema schema, GenericRecord datum) throws IOException {
        GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<GenericRecord>(schema);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            Encoder e = EncoderFactory.get().binaryEncoder(os, null);
            writer.write(datum, e);
            e.flush();
            byte[] byteData = os.toByteArray();
            return byteData;
        } finally {
            os.close();
        }
    }

}
