package upc.edu.cep.loadbalancing;

import upc.edu.cep.RDF_Model.event.EventSchema;

/**
 * Created by osboxes on 06/07/17.
 */
public class Stream {
    private EventSchema schema;
    private double currentUsage;
    private int order;

    public Stream(EventSchema schema, double currentUsage) {
        this.schema = schema;
        this.currentUsage = currentUsage;
    }

    public EventSchema getSchema() {
        return schema;
    }

    public void setSchema(EventSchema schema) {
        this.schema = schema;
    }

    public double getCurrentUsage() {
        return currentUsage;
    }

    public void setCurrentUsage(double currentUsage) {
        this.currentUsage = currentUsage;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
