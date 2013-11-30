package com.ultimatepolish.throwstats;

public class ValidationVisitor extends IndicatorNodeVisitor {

	@Override
	public void visit(IndicatorNode node){
		update(node);
		if (node.isLeaf()){
			prepareLeaf(node);
			return;
		}
		else{
			prepare(node);
			for (IndicatorNode child : node.childMap.values()) {
				visit(child);
			}
		}
	}
	
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
