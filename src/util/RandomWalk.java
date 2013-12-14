package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import  util.RandomProb;

import org.jgrapht.*;
import org.jgrapht.graph.*;

public class RandomWalk {

	double p_w; // the walker moves with probability p_w 
	
	public RandomWalk(double p)
	{
		this.p_w = p;
	}
	
	public int stay_move()
	//0---stay, 1---move
	{
		double rand = Math.random();		 // rand [0,1)
		
		if (rand < this.p_w)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
	
	public Integer move(SimpleWeightedGraph G, Integer v)
	// from node v, move to a neighbor according to transition probability: tp = w/sum(w)
	{
		Integer f = null;
		List<Integer> neighbors = Graphs.neighborListOf(G, v);
		if (neighbors.size()<=0)
			return f;
		// friend
		HashMap<Integer, Double> node_strength = new HashMap<Integer,Double>();
		for(int neighbor : neighbors)
		{
			node_strength.put(neighbor, G.getEdgeWeight(G.getEdge(neighbor,v)));
		}		
		RandomProb rp = new RandomProb<Integer>();
    	f = (Integer) rp.randomPro(node_strength);
    	
    	return f;
	}
	
	public static void main(String [ ] args) throws IOException{
		RandomWalk rw = new RandomWalk(0.95);
		ArrayList<Integer> rw_path = new ArrayList<Integer>();  
		int simul = 10000;
		for (int i = 0 ; i<simul; i++){
			int status = rw.stay_move();
			int count = 0;
			while (status == 1)   // keep walking until status = 0
			{
				count ++;
				status = rw.stay_move();
			}
			rw_path.add(count);
		}
		PrintWriter out_file = new PrintWriter(new FileWriter("rw_pathlen_0.95"));			
		out_file.print(rw_path);
		out_file.flush();  
		out_file.close();		
	}	
}
