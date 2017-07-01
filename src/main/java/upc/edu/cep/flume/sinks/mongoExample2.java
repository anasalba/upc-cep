package upc.edu.cep.flume.sinks;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

import java.net.UnknownHostException;

public class mongoExample2 {

    public static void main(String[] args) throws Exception {
        try {

            /**** Connect to MongoDB ****/
            // Since 2.10.0, uses MongoClient
            MongoClient mongo = new MongoClient("localhost", 27017);

            /**** Get database ****/
            // if database doesn't exists, MongoDB will create it for you
            DB db = mongo.getDB("testdb");

            db.dropDatabase();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        }

    }

}
