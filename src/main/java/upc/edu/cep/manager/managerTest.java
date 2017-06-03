package upc.edu.cep.manager;

import upc.edu.cep.RDF_Model.Operators.*;
import upc.edu.cep.RDF_Model.Rule;
import upc.edu.cep.RDF_Model.action.Action;
import upc.edu.cep.RDF_Model.condition.*;
import upc.edu.cep.RDF_Model.event.*;
import upc.edu.cep.RDF_Model.window.Window;
import upc.edu.cep.RDF_Model.window.WindowType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by osboxes on 01/06/17.
 */
public class managerTest {
    private static List<AtomicEvent> atomicEvents = new ArrayList<>();
    private static List<Rule> rules = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        //System.out.println(FlumeChannel.Interpret("AAA", "CCC"));
        Manager manager = new Manager();
        createRule();
        System.out.println(manager.CreateConfiguration("agent", atomicEvents, rules, "localhost:2181", "json", false, "rule2"));


    }

    private static void createRule() {
//        select a.custId, sum(b.price)
//        from pattern [every a=ServiceOrder ->
//                b=ProductOrder where timer:within(1 min)].win:time(2 hour)
//        where a.name = 'Repair' and b.price>10 and b.custId = a.custId
//        group by a.custId
//        having sum(b.price) > 100

        //Event and Attributes
        Attribute custIda = new Attribute();
        custIda.setIRI("custIda");
        custIda.setName("custId");
        custIda.setAttributeType(AttributeType.TYPE_STRING);
        Attribute name = new Attribute();
        name.setIRI("name");
        name.setName("name");
        name.setAttributeType(AttributeType.TYPE_STRING);

        AtomicEvent serviceOrdera = new AtomicEvent();
        serviceOrdera.setTopicName("ServiceOrderTopic");
        serviceOrdera.setIRI("ServiceOrder");
        serviceOrdera.setEventName("ServiceOrder");
        serviceOrdera.addAttribute(custIda);
        serviceOrdera.addAttribute(name);

        atomicEvents.add(serviceOrdera);

        SimpleEvent serviceOrder = new SimpleEvent();
        serviceOrder.setIRI("ServiceOrder-simple");
        serviceOrder.setAtomicEvent(serviceOrdera);

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

        AtomicEvent productOrdera = new AtomicEvent();
        productOrdera.setTopicName("ProductOrderTopic");
        productOrdera.setIRI("ProductOrder");
        productOrdera.setEventName("ProductOrder");
        productOrdera.addAttribute(price);
        productOrdera.addAttribute(custIdb);

        atomicEvents.add(productOrdera);

        SimpleEvent productOrder = new SimpleEvent();
        productOrder.setIRI("ProductOrder-simple");
        productOrder.setAtomicEvent(productOrdera);

        price.setEvent(productOrdera);
        custIdb.setEvent(productOrdera);

        Sequence sequence = new Sequence();
        ComplexTemporalEvent sequenceEvent = new ComplexTemporalEvent();
        sequenceEvent.setTemporalOperator(sequence);
        sequenceEvent.addEvents(serviceOrder);
        sequenceEvent.addEvents(productOrder);

        Within within = new Within();
        within.setOffset(1);
        within.setTimeUnit(TimeUnit.minute);

        ComplexTemporalEvent withinEvent = new ComplexTemporalEvent();
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
        rule.setEvent(withinEvent);
        rule.setWindow(window);
        rule.setAction(action);
        rule.setCondition(allCondition);

        rules.add(rule);
    }
}
