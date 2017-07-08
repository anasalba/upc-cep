package upc.edu.cep.flume.sinks;

import com.mongodb.*;

import java.net.UnknownHostException;

public class monSinkExample {

    public static void main(String[] args) throws Exception {
        try {

            /**** Connect to MongoDB ****/
            // Since 2.10.0, uses MongoClient
            MongoClient mongo = new MongoClient("server1", 27017);

            /**** Get database ****/
            // if database doesn't exists, MongoDB will create it for you
            DB db = mongo.getDB("statistics");


            /**** Get collection / table from 'testdb' ****/
            // if collection doesn't exists, MongoDB will create it for you
            DBCollection table = db.getCollection("cep");

            BasicDBObject searchQuery = new BasicDBObject();

            DBCursor cursor = table.find(searchQuery);

            while (cursor.hasNext()) {
                System.out.println(cursor.next());
            }


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        }

    }

}
