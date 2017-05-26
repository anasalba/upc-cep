package upc.edu.cep.RDF_Model.Operators;


import upc.edu.cep.Interpreter.InterpreterContext;
import upc.edu.cep.Interpreter.InterpreterException;

import java.util.Map;

/**
 * Created by osboxes on 18/04/17.
 */
public class ComparasionOperator extends Operator {

    private ComparasionOperatorEnum operator;

    public ComparasionOperator(ComparasionOperatorEnum operator) {
        super();
        this.operator = operator;
    }

    public ComparasionOperator(ComparasionOperatorEnum operator, String IRI) {
        super(IRI);
        this.operator = operator;
    }

    public ComparasionOperatorEnum getOperator() {
        return operator;
    }

    public void setOperator(ComparasionOperatorEnum operator) {
        this.operator = operator;
    }

    @Override
    public String interpret(InterpreterContext context) throws InterpreterException {
        switch (context) {
            case ESPER: {
                switch (operator) {
                    case EQ: {
                        return "=";
                    }
                    case NE: {
                        return "!=";
                    }
                    case GE: {
                        return ">=";
                    }
                    case GT: {
                        return ">";
                    }
                    case LE: {
                        return "<=";
                    }
                    case LT: {
                        return "<";
                    }
                }
            }
            default: {
                switch (operator) {
                    case EQ: {
                        return "=";
                    }
                    case NE: {
                        return "!=";
                    }
                    case GE: {
                        return ">=";
                    }
                    case GT: {
                        return ">";
                    }
                    case LE: {
                        return "<=";
                    }
                    case LT: {
                        return "<";
                    }
                }
            }
        }
        throw new InterpreterException("not supported");
    }

    @Override
    public Map<String, String> interpretToMap(InterpreterContext context) throws InterpreterException {
        throw new InterpreterException("not supported");
    }
}






