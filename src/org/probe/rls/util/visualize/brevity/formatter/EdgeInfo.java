package org.probe.rls.util.visualize.brevity.formatter;

import org.probe.rls.util.visualize.brevity.Edge;

public class EdgeInfo {

	public EdgeInfo(String name, Edge parentEdge, String rule) {
		this.name = name;
		
		if(parentEdge != null) {
			this.parentNode = parentEdge.getFrom().getLabel();
			this.edge = ""+parentEdge.getWeight();
		} else {
			this.parentNode = null;
			this.edge = null;
		}
		
		if(rule.equalsIgnoreCase("")) {
			this.rule = null;
		} else {
			this.rule = rule;
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getParentNode() {
		return parentNode;
	}
	
	public String getEdge() {
		return edge;
	}
	
	public String getRule() {
		return rule;
	}
	
	
	private String name;
	private String parentNode;
	private String edge;
	private String rule;
}
