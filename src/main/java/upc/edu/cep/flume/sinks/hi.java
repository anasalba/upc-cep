package upc.edu.cep.flume.sinks;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by osboxes on 12/05/17.
 */
public class hi {

    static List<String> rules = new ArrayList<>();
    private static EPServiceProvider epService;
    private static EPStatement statement;

    public static void main(String[] args) throws Exception {
//
//        String ruleNames = "r r2 r4 r       6";
//        String[] rules1 = ruleNames.split(" ");
//
//        for (String r:rules1)
//        {
//            r = r.trim();
//            if (!r.equals(""))
//            {
//                rules.add(r);
//            }
//        }

        System.out.println(rules.size());
//        Configuration config = new Configuration();
//        //config.addEventType("com.edu.cep.events.LogEvent",LogEvent.class.getName());
//        epService = EPServiceProviderManager.getDefaultProvider(config);
//
//        //epService.getEPAdministrator().;

    }
}
