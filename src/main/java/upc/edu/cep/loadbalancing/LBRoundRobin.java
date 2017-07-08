package upc.edu.cep.loadbalancing;

import upc.edu.cep.RDF_Model.Rule;
import upc.edu.cep.RDF_Model.event.EventSchema;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by osboxes on 04/07/17.
 */
public class LBRoundRobin {

    List<Rule> rules;
    List<EventSchema> schemas;
    List<Server> servers;

    public LBRoundRobin(List<Server> servers) {
        this.servers = servers;
        rules = new ArrayList<>();
        schemas = new ArrayList<>();
    }

    public void addRule(Rule rule)
    {
        int min=Integer.MAX_VALUE;
        int index=-1;
        for (int i=0;i<servers.size();i++)
        {
            if (servers.get(i).getRules().size()<min)
            {
                min = servers.get(i).getRules().size();
                index = i;
            }
        }
        rules.add(rule);
        servers.get(index).getRules().add(rule.getIRI());
    }
}
