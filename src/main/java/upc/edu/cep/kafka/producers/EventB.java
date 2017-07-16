package upc.edu.cep.kafka.producers;

/**
 * Created by osboxes on 03/05/17.
 */
public class EventB {
    private String a;
    private String b;
    private double c;

    public EventB(String mylog, String yourlog, double c) {
        this.a = mylog;
        this.b = yourlog;
        this.c = c;
    }

    public EventB() {
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public double getC() {
        return c;
    }

    public void setC(double c) {
        this.c = c;
    }
}
