package upc.edu.cep.kafka.producers;

/**
 * Created by osboxes on 03/05/17.
 */
public class Event1 {
    private String mylog;
    private String yourlog;
    private int fifi;

    public Event1(String mylog, String yourlog) {
        this.mylog = mylog;
        this.yourlog = yourlog;
    }

    public Event1() {
    }

    public String getMylog() {
        return mylog;
    }

    public void setMylog(String mylog) {
        this.mylog = mylog;
    }

    public String getYourlog() {
        return yourlog;
    }

    public void setYourlog(String yourlog) {
        this.yourlog = yourlog;
    }

    public int getFifi() {
        return fifi;
    }

    public void setFifi(int fifi) {
        this.fifi = fifi;
    }
}
