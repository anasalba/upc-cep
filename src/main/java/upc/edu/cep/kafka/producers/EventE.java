package upc.edu.cep.kafka.producers;

/**
 * Created by osboxes on 03/05/17.
 */
public class EventE {
    private String a;
    private int b;

    public EventE(String a, int b) {
        this.a = a;
        this.b = b;
    }

    public EventE() {
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }
}
