package com.ultimatepolish.throwstats;

import java.util.Set;

public class ValidationVisitor extends BaseWalkingVisitor {

	@Override
	public void update(IndicatorNode node) {
		if (!node.isComplete()){
			String msg = "Tree is incomplete:\nkeys: ";
			Set<String> childKeys = node.childKeys();
			for (String  key : childKeys) {
				msg+=key +", ";
			}
			msg+="\n children: ";
			for (String  key : node.getChildMapKeySet()) {
				msg+=key +", ";
			}
			throw new RuntimeException(msg);
		}
	}

	@Override
	public void prepareLeaf(IndicatorNode leafNode) {}

	@Override
	public void prepare(IndicatorNode node) {}

}
