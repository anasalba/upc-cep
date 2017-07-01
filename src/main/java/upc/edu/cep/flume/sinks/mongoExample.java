package upc.edu.cep.flume.sinks;

import com.mongodb.*;

import java.net.UnknownHostException;

public class mongoExample {

    public static void main(String[] args) throws Exception {
        try {

            /**** Connect to MongoDB ****/
            // Since 2.10.0, uses MongoClient
            MongoClient mongo = new MongoClient("localhost", 27017);

            /**** Get database ****/
            // if database doesn't exists, MongoDB will create it for you
            DB db = mongo.getDB("testdb");

            BasicDBObject regexQuery = new BasicDBObject();
            regexQuery.put("_id",
                    new BasicDBObject("$regex", "shadi.*")
                            .append("$options", "i"));

            /**** Get collection / table from 'testdb' ****/
            // if collection doesn't exists, MongoDB will create it for you
            DBCollection table = db.getCollection("user");

            DBCursor curr = table.find(regexQuery).sort(new BasicDBObject("_id", -1))
                    .limit(1);

            if (curr.hasNext())
                System.out.println(curr.next());

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        }

    }

}
