package models;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import util.ExportGraph;
import util.Statistics;

public class AIM {
	
	static int N = 0;				// number of nodes	(param)
	static int E = 0;				// number of edges  (param)
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
		
	public static void main(String [ ] args) throws IOException
	{		    	
	    System.out.println("Enter parameters: N E alpha(attractive) beta(introduce):");
	    try {
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    	String paras[] = reader.readLine().split(" ");
	    	N = Integer.parseInt(paras[0]);
	    	E = Integer.parseInt(paras[1]);
	    	alpha = Float.parseFloat(paras[2]);
	    	beta =  Float.parseFloat(paras[3]);
	    }
	    catch (IOException e){
	      System.out.println("Error reading from user");
	    }
	    
		long start = System.currentTimeMillis();
		
		int simulations = 10; 
		for (int t = 1; t <= simulations; t++){
			
		//Initialization, generate an empty network of N nodes
	    AIM dynamic = new AIM();
	    G = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	    for (int i=0; i< N; i++)
	    	G.addVertex(i);
	    String para = "AIM_"+Integer.toString(t);
	    String folder = "files/"+"Networks"+"/";
	    File f=new File(folder);
	    if(f.exists()==false){
	    	f.mkdirs();
	    }
	    	    			
	    // distribution of attractiveness
    	ArrayList<Double> attractiveness = new ArrayList<Double>();
    	attractiveness = dynamic.uniform(N); 
    	for (int i = 0 ; i< N; i++){
    		if (attractiveness.get(i) > alpha){
    			attractiveness.set(i, (double) 0);
    		}
    	}    	
	    // distribution of prob. of introducing friends
    	ArrayList<Double> introduce = new ArrayList<Double>();
    	introduce = dynamic.uniform(N); 
    	for (int i = 0 ; i< N; i++){
    		if (introduce.get(i) < beta)
    			introduce.set(i, (double) 1);
    		else introduce.set(i, (double) 0);
    	}
	    
    	int edgecount = G.edgeSet().size();
    	while (edgecount < E){
    		// choose a random pair
    		int u = random.nextInt(N);	//person 1
    		int v = random.nextInt(N);	//person 2
    		if (u == v)
    			continue;
    		
    		if (random.nextDouble()<attractiveness.get(v)){  	//check if person 1 nominates person 2 as a friend
    			if (!G.containsEdge(u, v))
    				G.setEdgeWeight((DefaultWeightedEdge)G.addEdge(u,v), 1);
    			if (random.nextDouble()<introduce.get(u)){		//check if person 1 introduces person 2 to friends
    				List<Integer> neighbors = Graphs.neighborListOf(G, u); 	//get person 1s neighbors
    				if (neighbors.size()>0){
    					for (int ne: neighbors){
    						if (ne == v) continue;
    						if (random.nextDouble() < attractiveness.get(v))
    							if (!G.containsEdge(ne, v))
    								G.setEdgeWeight((DefaultWeightedEdge)G.addEdge(ne,v), 1);
    					}
    				}
    			}   			
    		}
    		edgecount = G.edgeSet().size();
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
	}
		long elapsedTimeMillis = System.currentTimeMillis()-start;
    	float elapsedTimeHour = elapsedTimeMillis/(60*60*1000F);
    	System.out.print("Elapsed time: ");
    	System.out.print(elapsedTimeHour);
    	System.out.println(" hours.");
	}
}
