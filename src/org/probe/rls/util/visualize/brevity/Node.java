package org.probe.rls.util.visualize.brevity;

import java.util.LinkedList;
import java.util.List;

import org.probe.rls.util.RuleFormatter;

public class Node {

	public Node(String identifier, Edge parent, String label, String rule, NodeStatistics nodeStatistics) {
		this.setIdentifier(identifier);
		
		this.setParent(parent);
		
		this.setLabel(label);
		this.setRule(rule);
		this.setNodeStatistics(nodeStatistics);
		
		this.children = new LinkedList<Edge>();
		
		this.computeVariablesInPath();
	}
	
	public void setIdentifier(String identifierStr) {
		this.identifer = identifierStr;
	}
	
	public String getIdentifier() {
		return this.identifer;
	}
	
	public static Node createRootNode() {
		return new Node("ROOT", null, "ROOT", "", null);
	}
	
	public boolean nodeContainsChildWithLabel(String nodeLabel) {
		List<Edge> nodeChildren = this.getChildren();
		
		for(Edge child: nodeChildren) {
			if(child.getTo().getLabel().equalsIgnoreCase(nodeLabel)) {
				return true;
			}
		}
		
		return false;
	}
	
	public Node getChildWithLabel(String nodeLabel) {
		List<Edge> nodeChildren = this.getChildren();
		
		for(Edge child: nodeChildren) {
			if(child.getTo().getLabel().equalsIgnoreCase(nodeLabel)) {
				return child.getTo();
			}
		}
		
		return null;
	}
	
	private void computeVariablesInPath() {
		if(parent != null) {
			List<String> vars = new LinkedList<String>(parent.getFrom().getVariablesInPath());
			String variableInLiteral = RuleFormatter.removeAllWhiteSpace(RuleFormatter.removeParentheses(label).split("=")[0]);
			vars.add(variableInLiteral);
			variablesInPath = vars;
		} else {
			variablesInPath = new LinkedList<String>();
		}
	}
	
	public List<String> getVariablesInPath() {
		return this.variablesInPath;
	}
	
	public void addChild(Edge childEdge) {
		children.add(childEdge);
	}
	
	public Edge getParent() {
		return parent;
	}

	public void setParent(Edge parent) {
		this.parent = parent;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public NodeStatistics getNodeStatistics() {
		return nodeStatistics;
	}

	public void setNodeStatistics(NodeStatistics nodeStatistics) {
		this.nodeStatistics = nodeStatistics;
	}

	public List<Edge> getChildren() {
		return children;
	}

	public void setChildren(List<Edge> children) {
		this.children = children;
	}

	public void setVariablesInPath(List<String> variablesInPath) {
		this.variablesInPath = variablesInPath;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Label = "+this.label).append("\n");
		sb.append("Rule string = "+this.rule).append("\n");
		sb.append(nodeStatistics.toString());
		
		return sb.toString();
	}
	

	private String identifer;

	private transient Edge parent;
	
	private String label;
	private String rule;
	private transient List<String> variablesInPath;
	private NodeStatistics nodeStatistics;
	
	private List<Edge> children;
}
