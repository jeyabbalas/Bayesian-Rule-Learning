package org.probe.rls.algo.rule.brl.parameters;

public enum HeuristicScoreType {
	BDeu(0);
	
	HeuristicScoreType(int index){
		this.index = index;
	}
	
	public int getIndex(){
		return index;
	}
	
	private final int index;
}
