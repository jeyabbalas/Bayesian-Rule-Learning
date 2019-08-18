package org.probe.rls.algo.rule.brl.parameters;

public enum BRLSearchType {
	GreedyBestFirst(0),
	GreedyBestFirstWithComplexityPenalty(1),
	BeamWithComplexityPenalty(2),
	ParallelGreedyWithComplexityPenalty(3),
	Bagging(4),
	Boosting(5),
	Random(6);
	
	BRLSearchType(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	
	private final int index;
}
