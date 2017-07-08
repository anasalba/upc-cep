package upc.edu.cep.loadbalancing;

import upc.edu.cep.RDF_Model.Operators.*;
import upc.edu.cep.RDF_Model.Rule;
import upc.edu.cep.RDF_Model.action.Action;
import upc.edu.cep.RDF_Model.condition.*;
import upc.edu.cep.RDF_Model.event.*;
import upc.edu.cep.RDF_Model.window.Window;
import upc.edu.cep.RDF_Model.window.WindowType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by osboxes on 04/07/17.
 */
public class testKmeans {

    public static List<EventSchema> eventSchemas = new ArrayList<>();
    public static List<Rule> rules = new ArrayList<>();

    public static void main(String[] astrArgs) {
        /**
         * The code commented out here is just an example of how to use
         * the provided functions and constructors.
         *
         */

        List<EventSchema> eventSchemas;
        List<Rule> rules;

        schemas = generateEvents(10);
        rules = generateRules(schemas,20);


        List<Server> servers= new ArrayList<>();
        Server server1 = new Server();
        server1.setName("s1");
        Server server2 = new Server();
        server2.setName("s2");
        Server server3 = new Server();
        server3.setName("s3");
        servers.add(server1);
        servers.add(server2);
        servers.add(server3);

        createRule();
        KMeans KM = new KMeans(schemas, rules, servers);
        KM.clustering(10); // 2 clusters, maximum 10 iterations
        KM.printResults();


//        ArrayList<String> adasd = new ArrayList<>(5);
//        adasd.add(0,"sdf");
//        adasd.add(4,"sdf");

    }


    private static Map<String,Stream> schemas;

    private static Map<String,Stream>  generateEvents(int number) {
        Map<String,Stream> schemas = new HashMap<>();
        for (int i = 0; i < number; i++)
        {

            EventSchema schema = new EventSchema();
            schema.setIRI("e" +i );
            schema.setEventName("e" +i);

            Stream stream = new Stream(schema,0);
            stream.setOrder(i);
            schemas.put(schema.getIRI(),stream);
        }
        return schemas;
    }

    private static List<Rule> generateRules(Map<String,Stream> schemas, int number)
    {
        List<Rule> rules = new ArrayList<>();

        for (int i = 0; i < number; i++)
        {

            Sequence sequence = new Sequence();
            TemporalPattern sequenceEvent = new TemporalPattern();
            sequenceEvent.setTemporalOperator(sequence);
            for (Stream eventSchema : schemas.values())
            {
                if(Math.random()>0.6)
                {
                    Event event = new Event();
                    event.setEventSchema(eventSchema.getSchema());
                    event.setIRI(eventSchema.getSchema().getIRI()+"e");
                    sequenceEvent.addEvents(event);
                }
            }

            Rule rule = new Rule();
            rule.setIRI("r" +i );
            rule.setCEPElement(sequenceEvent);
            rules.add(rule);
        }
        return rules;
    }

    private static void createRule() {
//        select a.custId, sum(b.price)
//        from pattern [every a=ServiceOrder ->
//                b=ProductOrder where timer:within(1 min)].win:time(2 hour)
//        where a.name = 'Repair' and b.price>10 and b.custId = a.custId
//        group by a.custId
//        having sum(b.price) > 100

        //CEPElement and Attributes
        Attribute custIda = new Attribute();
        custIda.setIRI("custIda");
        custIda.setName("custId");
        custIda.setAttributeType(AttributeType.TYPE_STRING);
        Attribute name = new Attribute();
        name.setIRI("name");
        name.setName("name");
        name.setAttributeType(AttributeType.TYPE_STRING);

        EventSchema serviceOrdera = new EventSchema();
        serviceOrdera.setTopicName("ServiceOrderTopic");
        serviceOrdera.setIRI("ServiceOrder");
        serviceOrdera.setEventName("ServiceOrder");
        serviceOrdera.addAttribute(custIda);
        serviceOrdera.addAttribute(name);

        eventSchemas.add(serviceOrdera);

        Event serviceOrder = new Event();
        serviceOrder.setIRI("ServiceOrder-simple");
        serviceOrder.setEventSchema(serviceOrdera);

        custIda.setEvent(serviceOrdera);
        name.setEvent(serviceOrdera);

        Attribute price = new Attribute();
        price.setAttributeType(AttributeType.TYPE_INTEGER);
        price.setIRI("price");
        price.setName("price");
        Attribute custIdb = new Attribute();
        custIdb.setAttributeType(AttributeType.TYPE_STRING);
        custIdb.setIRI("custIdb");
        custIdb.setName("custId");

        EventSchema productOrdera = new EventSchema();
        productOrdera.setTopicName("ProductOrderTopic");
        productOrdera.setIRI("ProductOrder");
        productOrdera.setEventName("ProductOrder");
        productOrdera.addAttribute(price);
        productOrdera.addAttribute(custIdb);

        eventSchemas.add(productOrdera);

        Event productOrder = new Event();
        productOrder.setIRI("ProductOrder-simple");
        productOrder.setEventSchema(productOrdera);

        price.setEvent(productOrdera);
        custIdb.setEvent(productOrdera);

        Sequence sequence = new Sequence();
        TemporalPattern sequenceEvent = new TemporalPattern();
        sequenceEvent.setTemporalOperator(sequence);
        sequenceEvent.addEvents(serviceOrder);
        sequenceEvent.addEvents(productOrder);

        Within within = new Within();
        within.setOffset(1);
        within.setTimeUnit(TimeUnit.minute);

        TemporalPattern withinEvent = new TemporalPattern();
        withinEvent.setTemporalOperator(within);
        withinEvent.addEvents(sequenceEvent);

        //Window
        Window window = new Window();
        window.setTimeUnit(TimeUnit.hour);
        window.setWindowType(WindowType.TUMBLING_WINDOW);
        window.setWithin(2);

        //Action
        Action action = new Action();
        FunctionParameter parameter = new FunctionParameter();
        parameter.setOperand(price);
        //action.set
        Sum sum = new Sum(parameter);
        action.addActionAttribute(sum);
        action.addActionAttribute(custIda);

        //Condition
        LiteralOperand literal1 = new LiteralOperand();
        literal1.setType(AttributeType.TYPE_STRING);
        literal1.setValue("Repair");

        LiteralOperand literal2 = new LiteralOperand();
        literal2.setType(AttributeType.TYPE_INTEGER);
        literal2.setValue("100");

        LiteralOperand literal3 = new LiteralOperand();
        literal3.setType(AttributeType.TYPE_INTEGER);
        literal3.setValue("10");

        FunctionParameter groupParameter1 = new FunctionParameter();
        groupParameter1.setOperand(custIda);
        GroupBy groupBy = new GroupBy(groupParameter1);

        ComplexPredicate allCondition = new ComplexPredicate();
        allCondition.setOperator(new LogicOperator(LogicOperatorEnum.Conjunction));

        SimpleClause c1 = new SimpleClause();
        c1.setOperand1(sum);
        c1.setOperator(new ComparasionOperator(ComparasionOperatorEnum.GT));
        c1.setOperand2(literal2);

        SimpleClause c2 = new SimpleClause();
        c2.setOperand1(groupBy);


        SimpleClause c3 = new SimpleClause();
        c3.setOperand1(name);
        c3.setOperator(new ComparasionOperator(ComparasionOperatorEnum.EQ));
        c3.setOperand2(literal1);
        serviceOrder.getFilters().add(c3);

        SimpleClause whereCondition = new SimpleClause();
        whereCondition.setOperand1(custIda);
        whereCondition.setOperator(new ComparasionOperator(ComparasionOperatorEnum.EQ));
        whereCondition.setOperand2(custIdb);

        SimpleClause c5 = new SimpleClause();
        c5.setOperand1(price);
        c5.setOperator(new ComparasionOperator(ComparasionOperatorEnum.GT));
        c5.setOperand2(literal3);
        productOrder.getFilters().add(c5);


        allCondition.getConditions().add(c1);
        allCondition.getConditions().add(c2);
        allCondition.getConditions().add(whereCondition);


        //Rule
        Rule rule = new Rule();
        rule.setIRI("rule1");
        rule.setCEPElement(withinEvent);
        rule.setWindow(window);
        rule.setAction(action);
        rule.setCondition(allCondition);

        Rule rule1 = new Rule();
        rule1.setIRI("rule2");
        rule1.setCEPElement(serviceOrder);
        rule1.setWindow(window);
        rule1.setAction(action);
        rule1.setCondition(allCondition);

        rules.add(rule);
        rules.add(rule1);
    }
}
