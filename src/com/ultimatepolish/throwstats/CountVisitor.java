package com.ultimatepolish.throwstats;

public class CountVisitor extends IndicatorNodeVisitor {

	@Override
	public void update(IndicatorNode node) {
		node.increment();
	}

	@Override
	public void prepareLeaf(IndicatorNode leafNode) {

	}

	@Override
	public void prepare(IndicatorNode node) {
		
	}

}
