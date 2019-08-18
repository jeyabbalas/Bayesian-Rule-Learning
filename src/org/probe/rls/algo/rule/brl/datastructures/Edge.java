package org.probe.rls.algo.rule.brl.datastructures;


public class Edge {
	
	public Edge(Node parent, Node child, String value) {
		this.parent = parent;
		this.child = child;
		this.value = value;
	}

	public Node getParent() {
		return parent;
	}
	
	public void setParent(Node parent) {
		this.parent = parent;
	}
	
	public Node getChild() {
		return child;
	}
	
	public void setChild(Node child) {
		this.child = child;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(parent.toString());
		sb.append("=").append(getValue()).append(")");
		
		return sb.toString();
	}
	
	
	private Node parent;
	private Node child;
	private String value;
}
