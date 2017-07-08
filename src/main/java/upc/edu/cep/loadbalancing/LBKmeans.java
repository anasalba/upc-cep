package upc.edu.cep.loadbalancing;

import com.google.common.primitives.Doubles;
import upc.edu.cep.RDF_Model.Rule;
import upc.edu.cep.RDF_Model.event.*;

import java.util.*;

/**
 * Created by osboxes on 04/07/17.
 */
public class LBKmeans {

    private List<Rule> rules;
    private Map<String, Stream> streams;
    private List<Server> servers;
    private KMeans KM;
    private int maxIterations;

    public LBKmeans(List<Rule> rules, List<EventSchema> schemas, List<Server> servers,int maxIterations) {
        this.rules = rules;
        this.streams = new HashMap<>();
        this.servers = servers;

        int counter=0;
        for (EventSchema eventSchema : schemas) {
            Stream stream = new Stream(eventSchema,0);
            stream.setOrder(counter++);
            streams.put(eventSchema.getIRI(), stream);
        }


        KM = new KMeans(streams, rules, servers);
        KM.clustering(maxIterations); // 2 clusters, maximum 10 iterations


        for (int i=0;i<KM.getMyData().size();i++)
        {
            servers.get(KM.getMyData().get(i).getLabel()).getRules().add(rules.get(i).getIRI());
        }
    }

    public void addRule(Rule rule)
    {
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
        rulePoint.setLabel(KM.closest(Doubles.toArray(rulePoint.getPoint()),KM.getCentroids()));
        KM.getMyData().add(KM.getMyData().size(), rulePoint);
    }

    public void addServer(Server server)
    {
        this.servers.add(server);
    }

    public void addEventSchema(EventSchema schema)
    {
        Stream stream = new Stream(schema,0);
        this.streams.put(schema.getIRI(),stream);
    }

    public void reExecute()
    {
        KM.clustering(this.maxIterations);
    }

    public List<Rule> getRules() {
        return rules;
    }

    public Map<String,Stream> getStreams() {
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
