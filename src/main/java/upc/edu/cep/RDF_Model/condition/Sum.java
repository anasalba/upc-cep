package upc.edu.cep.RDF_Model.condition;

import upc.edu.cep.Interpreter.InterpreterContext;
import upc.edu.cep.Interpreter.InterpreterException;
import upc.edu.cep.RDF_Model.event.Attribute;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by osboxes on 25/05/17.
 */
public class Sum extends FunctionOperand {

    public Sum(String functionIRI, String parameterIRI, Attribute attribute) {
        this.setFunctionName("sum");
        FunctionParameter parameter = new FunctionParameter(attribute, 0, parameterIRI);
        List<FunctionParameter> parameterSingleton = Collections.singletonList(parameter);

        this.setParameters(parameterSingleton);
        this.setFunctionMethod(null);
        this.setFunctionURL("");
        this.setIRI(functionIRI);
        this.setOperandType(OperandType.having);
    }

    @Override
    public String interpret(InterpreterContext context) throws InterpreterException {
        switch (context) {
            case ESPER: {
                return "sum(" + this.getParameters().get(0).interpret(context) + ")";
            }
            default: {
                return "sum(" + this.getParameters().get(0).interpret(context) + ")";
            }
        }
    }

    @Override
    public Map<String, String> interpretToMap(InterpreterContext context) throws InterpreterException {
        Map<String, String> map = new HashMap<>();
        switch (context) {
            case ESPER: {

                map.put("sum", "sum(" + this.getParameters().get(0).interpret(context) + ")");
                return map;
            }
            default: {

                map.put("sum", "sum(" + this.getParameters().get(0).interpret(context) + ")");
                return map;
            }
        }
    }
}
