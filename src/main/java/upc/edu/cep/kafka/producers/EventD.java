package upc.edu.cep.kafka.producers;

/**
 * Created by osboxes on 03/05/17.
 */
public class EventD {
    private String a;
    private long b;

    public EventD(String a, long b) {
        this.a = a;
        this.b = b;
    }

    public EventD() {
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public long getB() {
        return b;
    }

    public void setB(long b) {
        this.b = b;
    }
}
