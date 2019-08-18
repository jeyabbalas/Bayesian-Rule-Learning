package org.probe.rls.algo.rule.brl.parameters;

public enum EnsembleAggregationType {
	DefaultLinearCombination(0),
	SelectiveBayesianModelAveraging(1),
	SelectiveBayesianModelCombination(2);
	
	EnsembleAggregationType(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	
	private final int index;
}
