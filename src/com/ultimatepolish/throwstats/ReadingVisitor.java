package com.ultimatepolish.throwstats;

import java.util.ArrayList;

public class ReadingVisitor extends BaseWalkingVisitor {
	ArrayList<ArrayList<String>> lineages = new ArrayList<ArrayList<String>>();
	ArrayList<Double> conditionalFractions = new ArrayList<Double>();
	ArrayList<Double> absoluteFractions = new ArrayList<Double>();
	ArrayList<Integer> counts = new ArrayList<Integer>();
	
	@Override
	public void update(IndicatorNode node) {
		lineages.add(node.lineage());
		conditionalFractions.add(node.fraction());
		absoluteFractions.add(node.absoluteFraction());
		counts.add(node.count);
	}

	@Override
	public void prepareLeaf(IndicatorNode leafNode) {}

	@Override
	public void prepare(IndicatorNode node) {}
	
	

}
