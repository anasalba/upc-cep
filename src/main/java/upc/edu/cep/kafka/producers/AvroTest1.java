package upc.edu.cep.kafka.producers;

/**
 * Created by osboxes on 04/04/17.
 */

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.flume.source.avro.AvroFlumeEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AvroTest1 {

    private static SpecificDatumReader<AvroFlumeEvent> reader = new SpecificDatumReader<AvroFlumeEvent>(AvroFlumeEvent.class);
    private static BinaryDecoder decoder = null;


    public static void main(String[] args) throws Exception {

        Schema schema = makeSchema();
        GenericRecord avroRecord = new GenericData.Record(schema);
        avroRecord.put("mylog","v1");
        avroRecord.put("yourlog","v2");
        byte[] arr = datumToByteArray(schema,avroRecord);

        DatumReader<GenericRecord> reader = new SpecificDatumReader<GenericRecord>(schema);
        Decoder decoder = DecoderFactory.get().binaryDecoder(arr, null);
        GenericRecord payload2 = null;
        payload2 = reader.read(null, decoder);
        System.out.println("Message received : " + payload2.get("mylog"));
        System.out.println("Message received : " + payload2.get("yourlog"));

    }


    public static Schema makeSchema() {
        List<Schema.Field> fields = new ArrayList<Schema.Field>();
        fields.add(new Schema.Field("mylog", Schema.create(Schema.Type.STRING), null, null));
        fields.add(new Schema.Field("yourlog", Schema.create(Schema.Type.STRING), null, null));

        Schema schema = Schema.createRecord("EventA", null, "", false);
        schema.setFields(fields);

        return (schema);
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
