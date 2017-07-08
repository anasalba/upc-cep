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
import java.util.Properties;


//  /home/osboxes/apache-flume-1.7.0-bin/bin/flume-ng agent -name remote_agent -c /home/osboxes/apache-flume-1.7.0-bin/conf -f /home/osboxes/apache-flume-1.7.0-bin/conf/5-4-2017-jsoneventtest.properties


public class ProducerTest {
    //hi
    public static void main(String[] args) throws Exception {
        KafkaProducer<String, String> producer;
        try (InputStream props = Resources.getResource("producer.props").openStream()) {
            Properties properties = new Properties();
            properties.load(props);
            producer = new KafkaProducer<>(properties);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String key = "key1";



        for (int iii = 0 ; iii<10;iii++) {
            try {
                Files.write(Paths.get("/home/osboxes/upc-cep/cep2.txt"), ("\n"+"Start Sending: " + System.currentTimeMillis()+"  , ").getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
            }
            long i = 0;
            long limit = 1000000;
            long limitless = limit - 1000;
            while (limit > i++) {


                Event1 event1 = new Event1();
                event1.setMylog("v1345");
                event1.setYourlog("v2");
                event1.setFifi(4);
                if (i == limitless) {
                    event1.setMylog("final");
                    event1.setFifi(4);
                }


                ProducerRecord record = new ProducerRecord<String, byte[]>("logcep2", key, objectMapper.writeValueAsBytes(event1));


                try {
                    producer.send(record);
                } catch (SerializationException e) {
                    // may need to do something with it
                    e.printStackTrace();
                }
                //Thread.sleep(100);

            }
            try {
                Files.write(Paths.get("/home/osboxes/upc-cep/cep2.txt"), ("End Sending: " + System.currentTimeMillis() + "  , ").getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
            }
        }
        //producer.close();
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
