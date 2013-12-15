package com.ultimatepolish.throwstats;

import java.util.ArrayList;
import java.util.Iterator;

public class ReadingVisitor extends BaseWalkingVisitor 
	implements Iterator<String>, Iterable<String>{
	ArrayList<ArrayList<String>> lineages = new ArrayList<ArrayList<String>>();
	ArrayList<Double> conditionalFractions = new ArrayList<Double>();
	ArrayList<Double> absoluteFractions = new ArrayList<Double>();
	ArrayList<Double> conditionalFractionStdDev = new ArrayList<Double>();
	ArrayList<Double> absoluteFractionStdDev = new ArrayList<Double>();
	ArrayList<Integer> counts = new ArrayList<Integer>();
	
	int iterator_index = 0;
	@Override
	public void update(IndicatorNode node) {
		lineages.add(node.lineage());
		conditionalFractions.add(node.conditionalFraction());
		absoluteFractions.add(node.absoluteFraction());
		conditionalFractionStdDev.add(node.conditionalFractionStandardDev());
		absoluteFractionStdDev.add(node.absoluteFractionStandardDev());
		counts.add(node.count);
	}

	@Override
	public void prepareLeaf(IndicatorNode leafNode) {}

	@Override
	public void prepare(IndicatorNode node) {}

	@Override
	public Iterator<String> iterator() {
		//I am my own iterator.
		iterator_index = 0;
		return this;
	}

	@Override
	public boolean hasNext() {
		return iterator_index<size();
	}

	@Override
	public String next() {
		
		String line = render_line(iterator_index);
		iterator_index++;
		return line;
		
	}

	@Override
	public void remove() {
		throw new RuntimeException("Removing elements isn't allowed, artard!");
		
	}

	public int size(){
		return counts.size();
	}
	
	String render_line(int idx){
		String s = "";
		String lineage = "";
		
		for (String lineage_element : lineages.get(idx)) {
			lineage+= lineage_element;
			lineage+= "->";
		}
		
		s = lineage+
				"  "+counts.get(idx)
				+",   "+String.format("%.1f", 100*conditionalFractions.get(idx))
				+"+/-"+String.format("%.1f", 100*conditionalFractionStdDev.get(idx))+"%"
				+",   "+String.format("%.2f", 100*absoluteFractions.get(idx))
				+"+/-"+String.format("%.2f", 100*absoluteFractionStdDev.get(idx))+"%";
		
		return s;
	}
	
	

}
