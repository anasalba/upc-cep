package upc.edu.cep.loadbalancing;

import upc.edu.cep.RDF_Model.Rule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by osboxes on 04/07/17.
 */
public class RulePoint {
    Rule rule;
    int label;
    List<Double> point;

    public RulePoint(Rule rule) {
        this.rule = rule;
        this.point = new ArrayList<>();
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public List<Double> getPoint() {
        return point;
    }

    public void setPoint(List<Double> point) {
        this.point = point;
    }
}
