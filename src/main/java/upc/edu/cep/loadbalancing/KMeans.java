package upc.edu.cep.loadbalancing;

import com.google.common.primitives.Doubles;
import upc.edu.cep.RDF_Model.Rule;
import upc.edu.cep.RDF_Model.event.*;

import java.util.*;

public class KMeans {
    // Data members

    private ArrayList<RulePoint> myData;

    //private int[] _label;  // generated cluster labels
    private int[] _withLabel; // if original labels exist, load them to _withLabel
    // by comparing _label and _withLabel, we can compute accuracy.
    // However, the accuracy function is not defined yet.
    private double[][] _centroids; // centroids: the center of clusters
    private List<Server> servers;
    private int _nrows, _ndims; // the number of rows and dimensions
    private int _numClusters; // the number of clusters;

    public int get_nrows() {
        return _nrows;
    }

    public int get_ndims() {
        return _ndims;
    }

//    public Map<String, Integer> getStreamsOrder() {
//        return streamsOrder;
//    }

    private static Map<String,Stream> streams;
    private static List<Rule> rules;

   // Map<String, Integer> streamsOrder;
    Map<String, Integer> rulessOrder;


    public KMeans(Map<String,Stream> streams, List<Rule> rules, List<Server> servers) {
        this.streams = streams;
        this.rules = rules;
        this.servers = servers;
        this._numClusters = servers.size();
        this._nrows = rules.size();

      //  streamsOrder = new HashMap<>();
        rulessOrder = new HashMap<>();


        this._ndims = streams.size();

        this.myData = new ArrayList<>(this._nrows);
//        for (int i = 0; i < this._nrows; i++) {
//            this.myData.add(i,new ArrayList<>(this._ndims));
//        }

        int counter = 0;
        for (Rule rule : rules) {
            rulessOrder.put(rule.getIRI(), counter);
            RulePoint rulePoint = new RulePoint(rule);
            double[] thisRule = new double[this._ndims];
            Queue<CEPElement> CEPElementQueue = new ArrayDeque<>();
            CEPElementQueue.add(rule.getCEPElement());
            while (!CEPElementQueue.isEmpty()) {
                CEPElement CEPElement = CEPElementQueue.poll();
                if (CEPElement.getClass().equals(Event.class)) {
                    thisRule[streams.get(((Event) CEPElement).getEventSchema().getIRI()).getOrder()] = 1.0d;
                } else if (!CEPElement.getClass().equals(TimeEvent.class)) {
                    for (CEPElement e : ((Pattern) CEPElement).getCEPElements()) {
                        CEPElementQueue.add(e);
                    }
                }
            }
            rulePoint.setPoint(new ArrayList<>(Doubles.asList(thisRule)));
            myData.add(counter++, rulePoint);
        }

        int sdsd = 4;
    }

    // Perform k-means clustering with the specified number of clusters and
    // Eucliden distance metric.
    // niter is the maximum number of iterations. If it is set to -1, the kmeans iteration is only terminated by the convergence condition.
    // centroids are the initial centroids. It is optional. If set to null, the initial centroids will be generated randomly.
    public void clustering(int niter) {

        ArrayList idx = new ArrayList();
        for (int i = 0; i < this._numClusters; i++) {
            int c;
            do {
                c = (int) (Math.random() * _nrows);
            } while (idx.contains(c)); // avoid duplicates
            idx.add(c);

            servers.get(i).setPoint(new ArrayList<>(myData.get(c).getPoint()));
        }
        System.out.println("selected random centroids");



        _centroids = new double[_nrows][];
        for (int i=0;i<servers.size();i++)
        {
            _centroids[i] = Doubles.toArray(servers.get(i).getPoint());
        }

        double[][] c1 = _centroids;
        double threshold = 0.001;
        int round = 0;

        while (true) {
            // update _centroids with the last round results
            _centroids = c1;

            //assign record to the closest centroid
            //_label = new int[_nrows];
            for (int i = 0; i < _nrows; i++) {

                myData.get(i).setLabel(closest(Doubles.toArray(myData.get(i).getPoint()),_centroids));
            }

            // recompute centroids based on the assignments
            c1 = updateCentroids();
            round++;
            if ((niter > 0 && round >= niter) || converge(_centroids, c1, threshold))
                break;
        }

        for (int i=0;i<servers.size();i++)
        {
            servers.get(i).setPoint(Doubles.asList(_centroids[i]));
        }

        System.out.println("Clustering converges at round " + round);
    }

    // find the closest centroid for the record v
    public int closest(double[] v, double[][] _centroids) {
        double mindist = dist(v, _centroids[0]);
        int label = 0;
        for (int i = 1; i < _numClusters; i++) {
            double t = dist(v, _centroids[i]);
            if (mindist > t) {
                mindist = t;
                label = i;
            }
        }
        return label;
    }

    // compute Euclidean distance between two vectors v1 and v2
    public double dist(double[] v1, double[] v2) {
        double sum = 0;
        for (int i = 0; i < _ndims; i++) {
            double d = v1[i] - v2[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    // according to the cluster labels, recompute the centroids
    // the centroid is updated by averaging its members in the cluster.
    // this only applies to Euclidean distance as the similarity measure.

    public double[][] updateCentroids() {
        // initialize centroids and set to 0
        double[][] newc = new double[_numClusters][]; //new centroids
        int[] counts = new int[_numClusters]; // sizes of the clusters

        // intialize
        for (int i = 0; i < _numClusters; i++) {
            counts[i] = 0;
            newc[i] = new double[_ndims];
            for (int j = 0; j < _ndims; j++)
                newc[i][j] = 0;
        }


        for (int i = 0; i < _nrows; i++) {
            int cn = myData.get(i).getLabel(); // the cluster membership id for record i
            for (int j = 0; j < _ndims; j++) {
                newc[cn][j] += myData.get(i).getPoint().get(j);//_data[i][j]; // update that centroid by adding the member data record
            }
            counts[cn]++;
        }

        // finally get the average
        for (int i = 0; i < _numClusters; i++) {
            for (int j = 0; j < _ndims; j++) {
                newc[i][j] /= counts[i];
            }
        }

        return newc;
    }

    // check convergence condition
    // max{dist(c1[i], c2[i]), i=1..numClusters < threshold
    public boolean converge(double[][] c1, double[][] c2, double threshold) {
        // c1 and c2 are two sets of centroids
        double maxv = 0;
        for (int i = 0; i < _numClusters; i++) {
            double d = dist(c1[i], c2[i]);
            if (maxv < d)
                maxv = d;
        }

        if (maxv < threshold)
            return true;
        else
            return false;

    }

    public double[][] getCentroids() {
        return _centroids;
    }

    public ArrayList<RulePoint> getMyData() {
        return myData;
    }

    public void setMyData(ArrayList<RulePoint> myData) {
        this.myData = myData;
    }

    public int nrows() {
        return _nrows;
    }

    public void printResults() {
        System.out.println("Label:");
        for (int i = 0; i < _nrows; i++)
            System.out.println(myData.get(i).getLabel());
        System.out.println("Centroids:");
        for (int i = 0; i < _numClusters; i++) {
            for (int j = 0; j < _ndims; j++)
                System.out.print(_centroids[i][j] + " ");
            System.out.println();
        }

    }


}