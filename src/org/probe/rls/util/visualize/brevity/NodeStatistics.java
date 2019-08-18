package org.probe.rls.util.visualize.brevity;

public class NodeStatistics {

	public NodeStatistics(String identifier, double posteriorProbability, int truePositives, int falsePositives) {
		this.setIdentifier(identifier);
		
		this.setPosteriorProbability(posteriorProbability);
		this.setTruePositives(truePositives);
		this.setFalsePositives(falsePositives);
	}
	
	public void setIdentifier(String identifierStr) {
		this.identifer = identifierStr;
	}
	
	public String getIdentifier() {
		return this.identifer;
	}
	
	public double getPosteriorProbability() {
		return posteriorProbability;
	}

	public void setPosteriorProbability(double posteriorProbability) {
		this.posteriorProbability = posteriorProbability;
	}

	public int getTruePositives() {
		return truePositives;
	}

	public void setTruePositives(int truePositives) {
		this.truePositives = truePositives;
	}

	public int getFalsePositives() {
		return falsePositives;
	}

	public void setFalsePositives(int falsePositives) {
		this.falsePositives = falsePositives;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Posterior probability = "+this.posteriorProbability).append("\n");
		sb.append("TP = "+this.truePositives).append("\n");
		sb.append("FP = "+this.falsePositives).append("\n");
		
		return sb.toString();
	}
	
	
	private String identifer;
	
	private double posteriorProbability;
	private int truePositives;
	private int falsePositives;
}
