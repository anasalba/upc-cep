package upc.edu.cep.kafka.producers;

/**
 * Created by osboxes on 04/04/17.
 */

import com.google.common.io.Resources;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
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
import java.util.Properties;


//  /home/osboxes/apache-flume-1.7.0-bin/bin/flume-ng agent -name remote_agent -c /home/osboxes/apache-flume-1.7.0-bin/conf -f /home/osboxes/apache-flume-1.7.0-bin/conf/5-4-2017-jsoneventtest.properties


public class JSONProducer {
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
        String userSchema = "{\"type\":\"record\"," +
                "\"name\":\"myrecord\"," +
                "\"fields\":[{\"name\":\"mylog\",\"type\":\"string\"},{\"name\":\"yourlog\",\"type\":\"string\"}]}";
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(userSchema);
        int i = 0;
        while (true) {

            GenericRecord avroRecord = new GenericData.Record(schema);
            avroRecord.put("mylog", "v1");
            avroRecord.put("yourlog", "value2");
            //  byte[] bytes = recordInjection.apply(avroRecord);

            EventA eventA = new EventA();
            eventA.setA("v1");
            eventA.setB("v2");

            //  ProducerRecord<String, byte[]> record = new ProducerRecord<>("mytopic", bytes);
            //ProducerRecord record = new ProducerRecord<String, byte[]>("logcep", key, datumToByteArray(schema, avroRecord));
            ProducerRecord record = new ProducerRecord<String, byte[]>("logcep", key, objectMapper.writeValueAsBytes(eventA));


            try {
                producer.send(record);
            } catch (SerializationException e) {
                // may need to do something with it
                e.printStackTrace();
            }

            Thread.sleep(10000000);

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
