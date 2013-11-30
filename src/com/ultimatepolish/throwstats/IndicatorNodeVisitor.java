package com.ultimatepolish.throwstats;

import com.ultimatepolish.scorebookdb.Throw;

public abstract class IndicatorNodeVisitor {
	Throw t;
	public void visit(IndicatorNode node){
		update(node);
		if (node.isLeaf()){
			prepareLeaf(node);
			return;
		}
		else{
			prepare(node);
			IndicatorNode child = node.indicate(t);
			visit(child);
		}
	}
	public abstract void update(IndicatorNode node);
	public abstract void prepareLeaf(IndicatorNode leafNode);
	public abstract void prepare(IndicatorNode node);
}
