package upc.edu.cep.loadbalancing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by osboxes on 04/07/17.
 */
public class Server {

    private String name;
    private String hostname;
    private int port;
    private Set<String> rules;
    private boolean changed;
    private List<Double> point;

    private double capacity;
    private double currentUsage;

    public Server(String name, Set<String> rules) {
        this.name = name;
        this.rules = rules;
    }

    public Server() {
        rules = new HashSet<>();
        point = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getRules() {
        return rules;
    }

    public void setRules(Set<String> rules) {
        this.rules = rules;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public List<Double> getPoint() {
        return point;
    }

    public void setPoint(List<Double> point) {
        this.point = point;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getCurrentUsage() {
        return currentUsage;
    }

    public void setCurrentUsage(double currentUsage) {
        this.currentUsage = currentUsage;
    }
}
