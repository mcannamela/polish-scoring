package com.ultimatepolish.throwstats;

import android.util.Log;

import com.ultimatepolish.db.Throw;

public abstract class IndicatorNodeVisitor {
	public static String LOGTAG = "NODE_VISITOR";
	Throw t;
	int recursion_depth = 0;
	static int MAX_RECURSION_DEPTH = 10;
	boolean recursion_limit_reached = false;
	
	public void visit(IndicatorNode node){
		if (recursion_depth>=10){
			recursion_limit_reached = true;
			log("Recursion limit reached at node "+node.key);
		}
		if (recursion_limit_reached){
			log("Returning from node "+node.key+" due to excessive recursion");
			return;
		}
		update(node);
		if (node.isLeaf()){
			prepareLeaf(node);
			recursion_depth--;
//			log("Node '"+node.key+"' is a leaf, returning");
			return;
		}
		else{
			prepare(node);
			IndicatorNode child = node.indicate(t);
			recursion_depth++;
//			log("Entering child '"+child.key+"' at depth "+recursion_depth);
			visit(child);
		}
	}
	public abstract void update(IndicatorNode node);
	public abstract void prepareLeaf(IndicatorNode leafNode);
	public abstract void prepare(IndicatorNode node);
	
	public void setThrow(Throw t){
		this.t = t;
		recursion_depth = 0;
	}
	
	public void log(String msg) {
		Log.i(LOGTAG, msg);
	}
}
