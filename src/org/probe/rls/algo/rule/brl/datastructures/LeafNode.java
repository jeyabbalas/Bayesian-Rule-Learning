package org.probe.rls.algo.rule.brl.datastructures;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.probe.rls.data.DataAttribute;

public class LeafNode extends Node {
	
	public LeafNode(DataAttribute attribute, List<Edge> parents, 
			Map<String, List<Integer>> attributeValueIndices) {
		super(attribute, parents, new LinkedList<Edge>());
		
		this.isLeaf = true;
		this.attributesInPath = new HashSet<DataAttribute>();
		this.setAttributeValueIndices(attributeValueIndices);
	}
	
	public Set<DataAttribute> getAttributesInPath() {
		return this.attributesInPath;
	}
	
	public void setAttributesInPath(Set<DataAttribute> attributesInPath) {
		this.attributesInPath = attributesInPath;
	}
	
	public void setAttributeValueIndices(Map<String, List<Integer>> attributeValueIndices) {
		this.attributeValueIndices = attributeValueIndices;
	}
	
	public Map<String, List<Integer>> getAttributeValueIndices() {
		return attributeValueIndices;
	}
	
	public boolean containsPathWithAttribute(DataAttribute attribute) {
		if(attributesInPath.contains(attribute)) 
			return true;
		
		return false;
	}
	
	@Override
	public void addParent(Edge parent) {
		parent.setChild(this);
		this.parents.add(parent);
		attributesInPath.add(parent.getParent().getDataAttribute());
	}
	
	@Override
	public void addParents(List<Edge> parents) {
		for(Edge parent: parents) {
			this.addParent(parent);
		}
	}
	
	// N_{ijk} for BDeu score.
	public double getLabelCounts(String attributeValue) {
		return (double) attributeValueIndices.get(attributeValue).size();
	}
	
	public void updateParameterPosteriorDistribution(Map<String, Double> parameterPosteriorDistribution) {
		this.parameterPosteriorDistribution = parameterPosteriorDistribution;
	}
	
	public Map<String, Double> getParameterPosteriorDistribution() {
		return this.parameterPosteriorDistribution;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("(");
		for(int i=0; i<parents.size(); i++) {
			
			if(i > 0) 
				sb.append(",");
			
			if(parents.size()>1)
				sb.append("(");
			
			sb.append(parents.get(i).toString());
			
			if(parents.size()>1)
				sb.append(")");
		}
		sb.append(") -> ");
		
		sb.append("[");
		boolean first = true;
		for(String attributeValue: attributeValueIndices.keySet()) {
			if(!first) {
				sb.append(", ");
			}
			sb.append("(").append(attribute.getAttributeName()).append("=").append(attributeValue).append(")");
			sb.append(":").append(attributeValueIndices.get(attributeValue).size());
			first = false;
		}
		sb.append("]\n");
		
		return sb.toString();
	}

	private Map<String, List<Integer>> attributeValueIndices;
	private Set<DataAttribute> attributesInPath;
	private Map<String, Double> parameterPosteriorDistribution;
}
