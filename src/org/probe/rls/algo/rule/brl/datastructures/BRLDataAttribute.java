package org.probe.rls.algo.rule.brl.datastructures;

import java.util.Comparator;

import org.probe.rls.data.DataAttribute;

public class BRLDataAttribute implements Comparable<BRLDataAttribute>, Comparator<BRLDataAttribute> {

	public BRLDataAttribute(DataAttribute dataAttribute) {
		this.dataAttribute = dataAttribute;
		this.score = Double.NEGATIVE_INFINITY;
	}
	
	public DataAttribute getDataAttribute() {
		return dataAttribute;
	}
	
	public void setDataAttribute(DataAttribute dataAttribute) {
		this.dataAttribute = dataAttribute;
	}
	
	public double getScore() {
		return score;
	}
	
	public void setScore(double score) {
		this.score = score;
	}
	
	@Override
	public int compare(BRLDataAttribute attribute1, BRLDataAttribute attribute2) {
		if(attribute1.getScore() > attribute2.getScore()) {
			return 1;
		} else if(attribute1.getScore() < attribute2.getScore()) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	public int compareTo(BRLDataAttribute attribute) {
		if(this.getScore() > attribute.getScore()) {
			return 1;
		} else if(this.getScore() < attribute.getScore()) {
			return -1;
		} else {
			return 0;
		}
	}



	private DataAttribute dataAttribute;
	private double score;
	
}
