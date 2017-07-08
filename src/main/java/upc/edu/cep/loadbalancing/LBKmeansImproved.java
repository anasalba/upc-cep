package upc.edu.cep.loadbalancing;

import com.google.common.primitives.Doubles;
import com.mongodb.*;
import upc.edu.cep.RDF_Model.Rule;
import upc.edu.cep.RDF_Model.event.*;

import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by osboxes on 04/07/17.
 */
public class LBKmeansImproved {

    private List<Rule> rules;
    private Map<String, Stream> streams;
    private List<Server> servers;
    private KMeans KM;
    private int maxIterations;

    private String mongoConnectionString;
    private int mongoPort;
    private String mongoDatabase;
    private String mongoCollection;
    private List<String> sources;

    public LBKmeansImproved(List<Rule> rules,
                            List<EventSchema> schemas,
                            List<Server> servers,
                            int maxIterations,
                            List<String> sources,
                            int mongoPort,
                            String mongoConnectionString,
                            String mongoDatabase,
                            String mongoCollection) {
        this.rules = rules;
        this.streams = new HashMap<>();
        this.servers = servers;
        this.sources = sources;
        this.mongoConnectionString = mongoConnectionString;
        this.mongoCollection = mongoCollection;
        this.mongoDatabase = mongoDatabase;
        this.mongoPort = mongoPort;


        int counter = 0;
        for (EventSchema eventSchema : schemas) {
            Stream stream = new Stream(eventSchema, 0);
            stream.setOrder(counter++);
            streams.put(eventSchema.getIRI(), stream);
        }

        KM = new KMeans(streams, rules, servers);
        KM.clustering(maxIterations); // 2 clusters, maximum 10 iterations


        for (int i = 0; i < KM.getMyData().size(); i++) {
            servers.get(KM.getMyData().get(i).getLabel()).getRules().add(rules.get(i).getIRI());
        }
    }

    public void addRule(Rule rule) {
        updateStatistics();
        rules.add(rule);
        RulePoint rulePoint = new RulePoint(rule);
        double[] thisRule = new double[KM.get_ndims()];
        Queue<CEPElement> CEPElementQueue = new ArrayDeque<>();
        CEPElementQueue.add(rule.getCEPElement());
        while (!CEPElementQueue.isEmpty()) {
            CEPElement CEPElement = CEPElementQueue.poll();
            if (CEPElement.getClass().equals(Event.class)) {
                thisRule[streams.get(((Event) CEPElement).getEventSchema().getIRI()).getOrder()] = 1.0d;
            } else if (!CEPElement.getClass().equals(TimeEvent.class)) {
                for (CEPElement e : ((Pattern) CEPElement).getCEPElements()) {
                    CEPElementQueue.add(e);
                }
            }
        }
        rulePoint.setPoint(new ArrayList<>(Doubles.asList(thisRule)));


        double[][] centroids = KM.getCentroids();
        for (int i=0; i< servers.size();i++)
        {
            for (Stream stream: streams.values())
            {
                centroids[i][stream.getOrder()]=centroids[i][stream.getOrder()]<1?0:stream.getCurrentUsage();
            }
        }


        double[] newPoint = Doubles.toArray(rulePoint.getPoint());
        for (Stream stream: streams.values())
        {
            newPoint[stream.getOrder()]=newPoint[stream.getOrder()]<1?0:stream.getCurrentUsage();
        }

        double mindist = KM.dist(newPoint, centroids[0])*(servers.get(0).getCurrentUsage()/servers.get(0).getCapacity());
        int label = 0;
        for (int i = 1; i < servers.size(); i++) {
            double t = KM.dist(newPoint, centroids[i])*servers.get(i).getCurrentUsage()/servers.get(i).getCapacity();
            if (mindist > t) {
                mindist = t;
                label = i;
            }
        }

        rulePoint.setLabel(label);

        KM.getMyData().add(KM.getMyData().size(), rulePoint);
    }

    public void updateStatistics() {
        try {
            /**** Connect to MongoDB ****/
            // Since 2.10.0, uses MongoClient
            MongoClient mongo = new MongoClient(this.mongoConnectionString, this.mongoPort);

            /**** Get database ****/
            // if database doesn't exists, MongoDB will create it for you
            DB db = mongo.getDB(this.mongoDatabase);

            /**** Get collection / table from 'testdb' ****/
            // if collection doesn't exists, MongoDB will create it for you
            DBCollection table = db.getCollection(this.mongoCollection);


            for (String stream : streams.keySet()) {
                streams.get(stream).setCurrentUsage(0);
            }

            for (Server server : servers) {
                server.setCurrentUsage(0);
            }
            for (String source : sources) {
                BasicDBObject regexQuery = new BasicDBObject();
                regexQuery.put("_id",
                        new BasicDBObject("$regex", source + ".*")
                                .append("$options", "i"));


                DBCursor curr = table.find(regexQuery).sort(new BasicDBObject("_id", -1))
                        .limit(1);

                if (curr.hasNext()) {
                    DBObject dbObject = curr.next();

                    for (String stream : streams.keySet()) {
                        streams.get(stream).setCurrentUsage(streams.get(stream).getCurrentUsage() + Integer.parseInt(dbObject.get(stream).toString()));
                    }

                    for (Server server : servers) {
                        server.setCurrentUsage(server.getCurrentUsage() + Integer.parseInt(dbObject.get(server.getName()).toString()));
                    }
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }


    public void addServer(Server server) {
        this.servers.add(server);
    }

    public void addEventSchema(EventSchema schema) {
        Stream stream = new Stream(schema, 0);
        this.streams.put(schema.getIRI(), stream);
    }

    public void reExecute() {
        KM.clustering(this.maxIterations);
    }

    public List<Rule> getRules() {
        return rules;
    }

    public Map<String, Stream> getStreams() {
        return streams;
    }

    public List<Server> getServers() {
        return servers;
    }

    public KMeans getKM() {
        return KM;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }
}
