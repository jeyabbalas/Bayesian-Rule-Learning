package org.probe.rls.util.visualize.brevity.formatter;

import java.util.List;

import org.probe.rls.util.visualize.brevity.NodeStatistics;

public class DisplayNode {

	public DisplayNode(String identifier, EdgeInfo edgeInfo, TargetInfo targetInfo, 
			NodeStatistics stats, List<DisplayNode> children) {
		this.identifier = identifier;
		
		this.name = edgeInfo.getName();
		this.parentNode = edgeInfo.getParentNode();
		this.edge = edgeInfo.getEdge();
		this.rule = edgeInfo.getRule();
		
		this.targetName = targetInfo.getTargetName();
		this.targetValue = targetInfo.getTargetValue();
		this.targetColor = targetInfo.getTargetColor();
		
		if(stats != null) {
			this.posteriorProbability = stats.getPosteriorProbability();
			this.TP = stats.getTruePositives();
			this.FP = stats.getFalsePositives();
		}
		
		this.children = children;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParentNode() {
		return parentNode;
	}

	public void setParentNode(String parentNode) {
		this.parentNode = parentNode;
	}

	public String getEdge() {
		return edge;
	}

	public void setEdge(String edge) {
		this.edge = edge;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public String getTargetValue() {
		return targetValue;
	}

	public void setTargetValue(String targetValue) {
		this.targetValue = targetValue;
	}

	public String getTargetColor() {
		return targetColor;
	}

	public void setTargetColor(String targetColor) {
		this.targetColor = targetColor;
	}

	public double getPosteriorProbability() {
		return posteriorProbability;
	}

	public void setPosteriorProbability(double posteriorProbability) {
		this.posteriorProbability = posteriorProbability;
	}

	public int getTP() {
		return TP;
	}

	public void setTP(int tP) {
		TP = tP;
	}

	public int getFP() {
		return FP;
	}

	public void setFP(int fP) {
		FP = fP;
	}

	public List<DisplayNode> getChildren() {
		return children;
	}

	public void setChildren(List<DisplayNode> children) {
		this.children = children;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}


	private String identifier;
	
	private String name;
	private String parentNode;
	private String edge;
	private String rule;
	
	private String targetName;
	private String targetValue;
	private String targetColor;
	
	private double posteriorProbability;
	private int TP;
	private int FP;
	
	private List<DisplayNode> children;
}
