package com.ultimatepolish.throwstats;

public abstract class BaseWalkingVisitor extends IndicatorNodeVisitor {

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

}
