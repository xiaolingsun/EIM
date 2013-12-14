package models;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import util.ExportGraph;
import util.Statistics;

public class SSM {
	
	static int N = 0;				// number of nodes	(param)
	static float alpha = 0f; 		// param 	
	static float beta = 0f;			// param
	static SimpleWeightedGraph<Integer, DefaultWeightedEdge> G; 
	static Random random = new Random();
		
	// generate n numbers following uniform distribution
	public ArrayList<Double> uniform(int n){
		ArrayList<Double> list = new ArrayList<Double>();
		for (int i=0; i<n; i++){
			list.add(random.nextDouble());
		}
		return list;
	}
	
	// the probability of linking nodes based on their social distance
	public double linkprob(double distance){
		return 1/(1+Math.pow((distance/beta), alpha));
	}
	
	
	public static void main(String [ ] args) throws IOException
	{		    	
	    System.out.println("Enter parameters: N alpha beta:");
	    try {
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    	String paras[] = reader.readLine().split(" ");
	    	N = Integer.parseInt(paras[0]);
	    	alpha = Float.parseFloat(paras[1]);
	    	beta =  Float.parseFloat(paras[2]);
	    }
	    catch (IOException e){
	      System.out.println("Error reading from user");
	    }
	    
		long start = System.currentTimeMillis();
		
		int simulations = 10; 
		for (int t = 1; t <= simulations; t++){
		
		//Initialization, generate an empty network of N nodes
	    SSM dynamic = new SSM();
	    G = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	    for (int i=0; i< N; i++)
	    	G.addVertex(i);
	    String para = "SSM_"+Integer.toString(t);
	    String folder = "files/"+"Networks"+"/";
	    File f=new File(folder);
	    if(f.exists()==false){
	    	f.mkdirs();
	    }
	    	    			
	    // distribution of social space
    	ArrayList<Double> socialposition = new ArrayList<Double>();
    	socialposition = dynamic.uniform(N); 
	    	
    	for (int u = 0; u < N; u++){
    		for (int v = u+1; v < N; v++){
    			double distance = Math.abs(socialposition.get(u)-socialposition.get(v));
    			double prob = dynamic.linkprob(distance);
    			if (random.nextDouble()<prob){
    				G.setEdgeWeight((DefaultWeightedEdge)G.addEdge(u,v), 1);
    			}
    		}
    	}
	    // delete isolate nodes
	    ArrayList<Integer> nodelist = new ArrayList<Integer>();
	    for (int node: G.vertexSet())
	    	nodelist.add(node);
	    for (int node : nodelist){
	    	if (G.degreeOf(node) == 0)
	    		G.removeVertex(node);
	    }  
	    System.out.print("Nodes:");
    	System.out.println(G.vertexSet().size());
    	System.out.print("Edges:");
    	System.out.println(G.edgeSet().size());
    	
    	//get largest connected component
	    Statistics stat = new Statistics();
	    Graph G_LCC = stat.largestConnectedComponent(G);    
    	
    	ExportGraph export = new ExportGraph();    
	    export.exportWPairs((SimpleWeightedGraph<Integer, DefaultWeightedEdge>) G_LCC,para,folder);
	    System.out.print("Nodes in LCC:");
    	System.out.println(G_LCC.vertexSet().size());
    	System.out.print("Edges in LCC:");
    	System.out.println(G_LCC.edgeSet().size());
    	System.out.println("Avg_degree: " + Double.toString(stat.avg_degree(G_LCC)));
    	System.out.println("Avg_clustering: " + Double.toString(stat.avg_clustering(G_LCC)));
    	System.out.println("Degree assortativity: " + Double.toString(stat.assortativityCoefficient(G_LCC)));
    				    
		long elapsedTimeMillis = System.currentTimeMillis()-start;
    	float elapsedTimeHour = elapsedTimeMillis/(60*60*1000F);
    	System.out.print("Elapsed time: ");
    	System.out.print(elapsedTimeHour);
    	System.out.println(" hours.");
	}
	}
}
