package models;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import util.ExportGraph;
import util.RandomProb;
import util.Statistics;

public class CFCM {
	
	static int N = 0;				// number of nodes	(param)
	static float p_d = 0.001f;  	// node deletion prob.
	static float delta = 0.5f;		// reinforcement of tie strength
	static int steps = 10000000; 		// number of time steps
	static float p_r = 0f; 			// random link prob. (param)
	static float p_delta = 0f; 		// introduce new link prob. (param)
	static int w_0 = 1; 			//  initial tie strength
	static SimpleWeightedGraph<Integer, DefaultWeightedEdge> G; 
	static Random random = new Random();
		
	public void twoStepRW(int v)
	{
		// two-step weighted random walk
		List<Integer> neighbors = Graphs.neighborListOf(G, v);
		if (neighbors.size()<=0)
			return;
		// friend
		HashMap<Integer, Double> node_strength = new HashMap<Integer,Double>();
		for(int neighbor : neighbors)
		{
			node_strength.put(neighbor, G.getEdgeWeight(G.getEdge(neighbor,v)));
		}		
		RandomProb rp = new RandomProb<Integer>();
    	Integer f = (Integer) rp.randomPro(node_strength);
    	if (f == null)
    		return;
    	
    	// friend of friend
    	List<Integer> fofs = Graphs.neighborListOf(G, f);
    	node_strength = new HashMap<Integer,Double>();
    	Integer fof;
    	if (fofs.size()<=1) //no other friends
    		return;
    	else{
    		for(int i : fofs)
    		{
    			if (i==v)				//self-avoiding random walk
    				continue;
    			node_strength.put(i, G.getEdgeWeight(G.getEdge(i,f)));
    		}		
        	fof = (Integer) (rp.randomPro(node_strength));
        	if (fof == null)
        		return;
    	}
    	
    	// edge weights
    	DefaultWeightedEdge vf = G.getEdge(v,f);
    	DefaultWeightedEdge ffof = G.getEdge(f, fof);
    	if (vf == null || ffof == null){
    		System.out.print("Error");
    		return;
    	}
    	G.setEdgeWeight(vf, G.getEdgeWeight(vf)+delta);
    	G.setEdgeWeight(ffof, G.getEdgeWeight(ffof)+delta);
    	if(G.containsEdge(fof, v)){
    		DefaultWeightedEdge fofv = G.getEdge(fof, v);
    		G.setEdgeWeight(fofv, G.getEdgeWeight(fofv)+delta);
    	}    		
    	else{ 
    		float prob = random.nextFloat();
    		if (prob < p_delta)				// prob. of adding a new link
    			G.setEdgeWeight((DefaultWeightedEdge)G.addEdge(fof,v), (double)w_0);
    	}    	
    	return;
	}
	
	public void globalattach(int v)
	{
		int u = random.nextInt(N);	
		while (u==v)
			u = random.nextInt(N);
		if (G.containsEdge(v, u)){
			DefaultWeightedEdge vu = G.getEdge(v, u);
			G.setEdgeWeight(vu,G.getEdgeWeight(vu)+w_0);			
		}			
		else
			G.setEdgeWeight((DefaultWeightedEdge)G.addEdge(v,u), w_0);
	}
		
	public void localattach(int v)
	{
		// two-step weighted random walk
		twoStepRW(v);
	}
		
	public static void main(String [ ] args) throws IOException
	{		    	
	    System.out.println("Enter parameters: N p_delta(prob. of introduce new link) p_r(random global link[0.001]) steps:");
	    try {
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    	String paras[] = reader.readLine().split(" ");
	    	N = Integer.parseInt(paras[0]);
	    	p_delta = Float.parseFloat(paras[1]);
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
	    CFCM dynamic = new CFCM();
	    G = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	    for (int i=0; i< N; i++)
	    	G.addVertex(i);
	    String para = "CFCM_"+Integer.toString(t);
	    String folder = "files/"+"Networks"+"/";
	    File f=new File(folder);
	    if(f.exists()==false){
	    	f.mkdirs();
	    }
	    	    			
	    for(int s=0; s<steps; s++){
	    	int v = random.nextInt(N);
	    	//LA process
	    	dynamic.localattach(v);   	
	    	//GA process
				// no edges, create one random link with a unit weight w=1
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
