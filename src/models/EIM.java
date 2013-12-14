package models;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import util.ExportGraph;
import util.RandomProb;
import util.RandomWalk;
import util.Statistics;

public class EIM {
	
	static int N = 0;				// number of nodes
	static float p_d = 0.001f;  	// node deletion prob.
	static int steps = 10000000; 	// number of time steps
	static double p_w = 0; 			// random walk prob.
	static double p_r = 0; 			// random link prob.
	static SimpleWeightedGraph<Integer, DefaultWeightedEdge> G; 
	static Random random = new Random();
	static ArrayList<Double> socialposition = new ArrayList<Double>();
	
	// generate n numbers following uniform distribution
	public ArrayList<Double> uniform(int n){
		ArrayList<Double> list = new ArrayList<Double>();
		for (int i=0; i<n; i++){
			list.add(random.nextDouble());
		}
		return list;
	}
	
	// the probability of linking nodes based on their social distance
	public double linkprob(double distance, int pathlen){
		return (1/(1+distance))*1/(double)pathlen;
	}	
	
	//select a node according to degrees
	public Integer SelectNode_PA() throws IOException
	{
		HashMap<Integer, Double> node_degree = new HashMap<Integer,Double>();
		for(int n : G.vertexSet())
		{
			node_degree.put(n, (double)G.degreeOf(n));
		}
		
		RandomProb rp = new RandomProb<Integer>();
    	Integer selected_node = (Integer) (rp.randomPro(node_degree));
   	
    	return selected_node;
	}
	
	public void randomwalk(int v)
	{
		// weighted random walk
		RandomWalk rw = new RandomWalk(p_w);
		int status = rw.stay_move();
		ArrayList<Integer> walkpath = new ArrayList<Integer>(); 
		Integer u = v;
		walkpath.add(v);		
		while (status == 1)   // keep moving until status = 0
		{
			u = rw.move(G, u);
			if (u==null)		//no neighbors
				break; 	
			if (!walkpath.contains(u))
				walkpath.add(u);    			    				
			status = rw.stay_move();
		}
//		System.out.println("walking length: "+ Integer.toString(walkpath.size()));
		
		for (int i=0;i<walkpath.size();i++)
			for (int j=i+1;j<walkpath.size();j++){
				int m = walkpath.get(i);
				int n = walkpath.get(j);
				if (!G.containsEdge(m, n)){
					double distance = Math.abs(socialposition.get(m)-socialposition.get(n));
					double prob = linkprob(distance, (j-i));
					if (random.nextDouble()<prob){
						G.setEdgeWeight((DefaultWeightedEdge)G.addEdge(m,n), 1);
					}
				}
				else{
					DefaultWeightedEdge edge = G.getEdge(m,n);
		    		G.setEdgeWeight(edge, G.getEdgeWeight(edge)+1);
				}
			}  	
    	return;
	}
	
// global attachment -- with prob 1 	
	public void globalattach(int v) throws IOException
	{
		Integer u = random.nextInt(N);
		while (u==v)
			u = random.nextInt(N);
		if (!G.containsEdge(v, u)){
			G.setEdgeWeight((DefaultWeightedEdge)G.addEdge(v,u), 1);
		}	
		else{
			DefaultWeightedEdge edge = G.getEdge(v,u);
    		G.setEdgeWeight(edge, G.getEdgeWeight(edge)+1);
		}
	}
		
	public void localattach(int v)
	{
		// weighted random walk
		randomwalk(v);
	}
		
	public static void main(String [ ] args) throws IOException
	{		    	
	    System.out.println("Enter parameters: N p_w p_r steps:");
	    try {
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    	String paras[] = reader.readLine().split(" ");
	    	N = Integer.parseInt(paras[0]);
	    	p_w = Float.parseFloat(paras[1]);
	    	p_r =  Float.parseFloat(paras[2]);
	    	steps = Integer.parseInt(paras[3]);
	    }
	    catch (IOException e){
	      System.out.println("Error reading from user");
	    }
	    
		long start = System.currentTimeMillis();
		
		int simulations = 1; 
		for (int t = 1; t <= simulations; t++){
			
		//Initialization, generate an empty network of N nodes
	    EIM dynamic = new EIM();
	    G = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	    for (int i=0; i< N; i++)
	    	G.addVertex(i);
	    String para = "EIM_"+Integer.toString(t);
	    String folder = "files/"+"Networks"+"/";
	    File f=new File(folder);
	    if(f.exists()==false){
	    	f.mkdirs();
	    }
	    
	    // distribution of social space
    	socialposition = dynamic.uniform(N); 
	    	    			
	    for(int s=0; s<steps; s++){
	    	int v = random.nextInt(N);
	    	//LA process - weighted random walk + link with social distance
	    	dynamic.localattach(v);   	
	    	//GA process
				// no edges, create one random link 
			if (G.edgesOf(v).size()==0){
				dynamic.globalattach(v);
			}
			else{
				float prob = random.nextFloat();
				if (prob < p_r)
					dynamic.globalattach(v);
			} 	   
			//ND process
			int d = random.nextInt(N);
			if(G.edgesOf(d).size()>0){
				float prob = random.nextFloat();
				if (prob < p_d){
					Set<DefaultWeightedEdge> edges = new HashSet(G.edgesOf(d));
					G.removeAllEdges(edges); 
				}
			}
			if (s%100000 == 0){
				System.out.print("Steps:"+Integer.toString(s)+"	");
				System.out.print("Edges:");
		    	System.out.print(G.edgeSet().size()+"	");
		    	System.out.println("Avg_degree: " + Float.toString((float)G.edgeSet().size()*2/G.vertexSet().size()));
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
		}
		
		long elapsedTimeMillis = System.currentTimeMillis()-start;
    	float elapsedTimeHour = elapsedTimeMillis/(60*60*1000F);
    	System.out.print("Elapsed time: ");
    	System.out.print(elapsedTimeHour);
    	System.out.println(" hours.");
	}
}
