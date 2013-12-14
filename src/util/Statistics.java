package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;

public class Statistics<V, E> {

	//average degree
    public double avg_degree(Graph<V, E> G){
    	return (double)(G.edgeSet().size()*2)/G.vertexSet().size();
    }
    
    //average clustering coefficient
    public double avg_clustering(Graph<V, E> G){
    	Map<V, Double> r = new HashMap<V, Double>();

        for (V n : G.vertexSet()) {

            List<V> nbr = Graphs.neighborListOf(G, n);
            int nbr_n = nbr.size();
            int sum_e_jk = 0;

            if (nbr.size() < 2) {
                r.put(n, 0.0);
                continue;
            }

            for (int i = 0; i < nbr_n; i++) {
                for (int j = i + 1; j < nbr_n; j++) {
                    if (G.containsEdge(nbr.get(i), nbr.get(j)))
                        sum_e_jk++;
                }
            }
            double C_i = (2.0 * sum_e_jk) / (nbr_n * (nbr_n - 1.0));
            r.put(n, C_i);
        }
        double clustering = 0;
        for (double c: r.values())
        	clustering += c;
        return clustering/r.size();
    }
    
    // largest connected component
    public Graph<V,E> largestConnectedComponent(Graph<V,E> G){
    	ConnectivityInspector ci = new ConnectivityInspector((UndirectedGraph) G);
    	List<Set> ConnectedSubgraphs = ci.connectedSets();
    	Set largestCC = null;
    	int size = 0;
    	for (Set s : ConnectedSubgraphs){
    		if (s.size() > size){
    			size = s.size();
    			largestCC = s;
    		}    			
    	}
    	Set<V> nodelist = new HashSet(G.vertexSet());
    	for (V n : nodelist) {
    		if (!largestCC.contains(n))
    				G.removeVertex(n);
    	}
    	return G;   	
    }  
    
    /*
     * This method computes the assortativity coefficient. Further details:
     * M. E. J. Newman "Assortative Mixing in Networks", 2002. Eq. (4)
    */
    public double assortativityCoefficient(Graph<V,E> G){
//    	System.out.println("Computing the assortativity coefficient ...");
    	Set<E> edges = G.edgeSet();
    	int degreek1 = 0;
    	int degreek2 = 0;
    	double r = 0; //assortativity coefficient
    	double sumk1k2mult = 0.0; //Sum{degreek1*degreek2}
    	double sumk1k2add = 0.0; //Sum{degreek1+degreek2}
    	double sumk1k2squareadd = 0.0; //Sum{degreek1^2+degreek2^2}
    	double M = edges.size();

    	for(E edge : edges){
    		degreek1 = ((UndirectedGraph) G).degreeOf(G.getEdgeSource(edge));
    		degreek2 = ((UndirectedGraph) G).degreeOf(G.getEdgeTarget(edge));
    		sumk1k2mult = sumk1k2mult + (degreek1 * degreek2);
    		sumk1k2add = sumk1k2add + degreek1 + degreek2;
    		sumk1k2squareadd = sumk1k2squareadd + (degreek1 * degreek1) + (degreek2 * degreek2);
    	}
    	double a = (1/M * sumk1k2mult) - (Math.pow(1/M * 0.5 * sumk1k2add,2));
    	double b = (1/M * 0.5 * sumk1k2squareadd) - (Math.pow(1/M * 0.5 * sumk1k2add,2));
    	r = a/b;
    	//System.out.println("Assortativity coefficient: " + r);
    	return r;
    }
    
    public double avg_shorted_path(Graph<V,E> G){
    	double l = 0.0;
    	int count = 0;
    	List<V> nodelist = new ArrayList<V>();
    	for ( V n: G.vertexSet())
    		nodelist.add(n);
    	for (int i = 0; i< nodelist.size(); i++){
    		for (int j = i+1; j<nodelist.size(); j++){
    			V m = nodelist.get(i);
    			V n = nodelist.get(j);
    			l += DijkstraShortestPath.findPathBetween(G, m, n).size();
    			count +=1;
    		}
    	}    	
    	return l/count;
    }
}
