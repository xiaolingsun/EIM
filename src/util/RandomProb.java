package util;

import java.util.*;

public class RandomProb<S> {
	
	S selected = null; 
	
	@SuppressWarnings("unchecked")
	public List<S> sortByValue(final Map<S,Double> m) 
	{
		List<S> keys = new ArrayList<S>();
        keys.addAll(m.keySet());
        Collections.sort(keys, new Comparator(){
            public int compare(Object o1, Object o2) {
                Object v1 = m.get(o1);
                Object v2 = m.get(o2);
                if (v1 == null) {
                    return (v2 == null) ? 0 : 1;
                }
                else if (v1 instanceof Comparable) {
                    return ((Comparable) v1).compareTo(v2);
                }
                else {
                    return 0;
                }
            }
        });
        return keys;
	}
	
	public S randomPro(Map<S, Double> tm)   
	{
		float r = (float) Math.random();
	    float cumulative = (float) 0.0;  
	    double sum_values = 0.0;
	    S key = null;

		for (Double value : tm.values())
			sum_values += value;
		if (sum_values <= 0.0)
			return null;
		for (Iterator<S> i = sortByValue(tm).iterator(); i.hasNext(); ) {
				key = i.next();
		        cumulative += (double)tm.get(key)/sum_values;
		        if (r <= cumulative)
		        {
		        	selected = key;
		        	break;
		        }                        
		} 
		return selected;
	}	
}
