package org.probe.rls.util.visualize.brevity;


public class Edge {

	public Edge(Node from, double weight, Node to) {
		this.setIdentifier();
		this.setFrom(from);
		this.setWeight(weight);
		this.setTo(to);
	}
	
	private void setIdentifier() {
		this.identifer = "Edge:"+(Edge.callCounter++);
	}
	
	public String getIdentifier() {
		return this.identifer;
	}
	
	public Node getFrom() {
		return from;
	}
	
	public void setFrom(Node from) {
		this.from = from;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public Node getTo() {
		return to;
	}
	
	public void setTo(Node to) {
		this.to = to;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("--(").append(weight).append(")-->");
		
		return sb.toString();
	}
	
	
	private String identifer;
	private static int callCounter = 1;

	private transient Node from;
	private double weight;
	private Node to;
}
