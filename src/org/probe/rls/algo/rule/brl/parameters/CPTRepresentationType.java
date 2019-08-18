package org.probe.rls.algo.rule.brl.parameters;

public enum CPTRepresentationType {
	Global(0),
	Local_C(1),
	Local_B(2),
	Local_M(3),
	Local_CB(4),
	Local_CM(5),
	Local_BM(6),
	Local_CBM(7);
	
	CPTRepresentationType(int index){
		this.index = index;
	}
	
	public int getIndex(){
		return index;
	}
	
	private final int index;
}
