package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.transform.TransformerConfigurationException;

import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.xml.sax.SAXException;

public class ExportGraph {

	//v1	v2	  weight   for Gephi and networkx
	public void exportPajek(SimpleWeightedGraph<Integer, DefaultWeightedEdge> G, String filename, String folder) throws IOException
	{
		File f = new File(folder+filename+".net");
		FileWriter fstream = new FileWriter(f);
	    BufferedWriter out = new BufferedWriter(fstream);
		out.write("*Vertices "+G.vertexSet().size()+"\r\n");
		out.write("*Edges"+"\r\n");
		for (DefaultWeightedEdge edge: G.edgeSet())
		{
			Integer u = G.getEdgeSource(edge);
			Integer v = G.getEdgeTarget(edge);
	
			out.write(Integer.toString(u)+'\t'+Integer.toString(v)+'\t'+Double.toString(G.getEdgeWeight(edge)));
			out.newLine();
		}
		out.flush();
		out.close();		
	}
	
	public void exportWPairs(SimpleWeightedGraph<Integer, DefaultWeightedEdge> G, String filename, String folder) throws IOException
	{
		File f = new File(folder+filename+".dat");
		FileWriter fstream = new FileWriter(f);
	    BufferedWriter out = new BufferedWriter(fstream);
		for (DefaultWeightedEdge edge: G.edgeSet())
		{
			Integer u = G.getEdgeSource(edge);
			Integer v = G.getEdgeTarget(edge);
	
			out.write(Integer.toString(u)+'\t'+Integer.toString(v)+'\t'+Double.toString(G.getEdgeWeight(edge)));
			out.newLine();
		}
		out.flush();
		out.close();		
	}
	
	public void exportGraphML(SimpleWeightedGraph<Integer, DefaultWeightedEdge> G, String filename, String folder) throws IOException, TransformerConfigurationException, SAXException
	{
		GraphMLExporter gmlexport = new GraphMLExporter();
		FileWriter fw =  new FileWriter(folder+filename+".graphml");
		gmlexport.export(fw,G);
	}
}
