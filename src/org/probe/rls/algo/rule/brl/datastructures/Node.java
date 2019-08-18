package org.probe.rls.algo.rule.brl.datastructures;

import java.util.List;

import org.probe.rls.data.DataAttribute;

public class Node {
	
	public Node(DataAttribute attribute, List<Edge> parents, List<Edge> children) {
		this.attribute = attribute;
		this.parents = parents;
		this.children = children;
		this.isRoot = false;
		this.isLeaf = false;
	}

	public List<Edge> getParents() {
		return parents;
	}
	
	public void addParent(Edge parent) {
		parent.setChild(this);
		this.parents.add(parent);
	}
	
	public void addParents(List<Edge> newParents) {
		for(Edge parent: newParents) {
			this.addParent(parent);
		}
	}
	
	public List<Edge> getChildren() {
		return children;
	}
	
	public void addChild(Edge child) {
		child.setParent(this);
		this.children.add(child);
	}
	
	public DataAttribute getDataAttribute() {
		return attribute;
	}
	
	public void setIsRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}
	
	public boolean isRoot() {
		return this.isRoot;
	}
	
	public boolean isLeaf() {
		return isLeaf;
	}

	public void setIsLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if(parents.size()>1) 
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
		if(parents.size()>1) 
			sb.append(")");
		
		sb.append("(").append(attribute.getAttributeName());
		
		return sb.toString();
	}
	

	protected boolean isRoot;
	protected boolean isLeaf;
	
	protected DataAttribute attribute;
	protected List<Edge> parents;
	protected List<Edge> children;
}
