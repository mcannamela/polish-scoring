package com.ultimatepolish.throwstats;

public class CountVisitor extends IndicatorNodeVisitor {

	@Override
	public void update(IndicatorNode node) {
		if (node==null){
			throw new RuntimeException("got a null node!");
		}
		node.increment();
	}

	@Override
	public void prepareLeaf(IndicatorNode leafNode) {

	}

	@Override
	public void prepare(IndicatorNode node) {
		
	}

}
