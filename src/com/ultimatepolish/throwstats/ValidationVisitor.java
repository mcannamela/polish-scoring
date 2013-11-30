package com.ultimatepolish.throwstats;

public class ValidationVisitor extends BaseWalkingVisitor {

	@Override
	public void update(IndicatorNode node) {
		if (!node.isComplete()){
			throw new RuntimeException("Tree is incomplete");
		}
	}

	@Override
	public void prepareLeaf(IndicatorNode leafNode) {}

	@Override
	public void prepare(IndicatorNode node) {}

}
