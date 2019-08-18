package org.probe.rls.algo.rule.brl.datastructures;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.probe.rls.data.DataAttribute;
import org.probe.rls.data.DataModel;


/**
 * Decision Graph to represent the Conditional Probability Table.
 * 
 * @author jeya
 */
public class DecisionGraph {

	public DecisionGraph(DataModel dataModel, DataAttribute leafNodeAttribute) {
		this.dataModel = dataModel;
		initLeafNode(leafNodeAttribute);
	}
	
	public DecisionGraph(DecisionGraph decisionGraph) {
		this.setDataModel(decisionGraph.getDataModel());
		this.setLeafNodes(copyLeafNodes(decisionGraph.getLeafNodes()));
	}
	
	private void initLeafNode(DataAttribute leafNodeAttribute) {
		this.leaves = new LinkedList<LeafNode>();
		
		LeafNode leafNode = new LeafNode(leafNodeAttribute, new LinkedList<Edge>(), 
				dataModel.getAttributeLabelIndices(leafNodeAttribute));
		leafNode.setIsRoot(true);
		this.leaves.add(leafNode);
	}
	
	private List<LeafNode> copyLeafNodes(List<LeafNode> leafNodesList) {
		Set<Node> graphNodes = new HashSet<Node>();
		
		for(LeafNode leaf: leafNodesList) {
			if(leaf.getParents().size()>0) {
				graphNodes.addAll(getNodesInPath(leaf));
			}
		}
		
		List<Node> graphNodesList = new LinkedList<Node>(graphNodes);
		List<Node> copiedGraphNodesList = copyGraphNodesList(graphNodesList);
		List<LeafNode> copiedLeafNodesList = copyLeafNodesList(leafNodesList);
		
		//connecting leaves to parents
		for(int i=0; i<leafNodesList.size(); i++) {
			LeafNode originalLeaf = leafNodesList.get(i);
			LeafNode copiedLeaf = copiedLeafNodesList.get(i);
			
			for(Edge parentEdge: originalLeaf.getParents()) {
				int nodeIndex = graphNodesList.indexOf(parentEdge.getParent());
				
				Edge parentEdgeCopy = new Edge(copiedGraphNodesList.get(nodeIndex), 
						copiedLeaf, parentEdge.getValue());
				copiedGraphNodesList.get(nodeIndex).addChild(parentEdgeCopy);
				copiedLeaf.addParent(parentEdgeCopy);
			}
			
			copiedLeaf.setAttributesInPath(new HashSet<DataAttribute>(originalLeaf.getAttributesInPath()));
		}
		
		
		//connecting nodes
		for(int i=0; i<graphNodesList.size(); i++) {
			Node originalNode = graphNodesList.get(i);
			Node copiedNode = copiedGraphNodesList.get(i);
			
			for(Edge parentEdge: originalNode.getParents()) {
				int nodeIndex = graphNodesList.indexOf(parentEdge.getParent());
				
				Edge parentEdgeCopy = new Edge(copiedGraphNodesList.get(nodeIndex), 
						copiedNode, parentEdge.getValue());
				copiedGraphNodesList.get(nodeIndex).addChild(parentEdgeCopy);
				copiedNode.addParent(parentEdgeCopy);
			}
		}
		
		return copiedLeafNodesList;
	}
	
	private Set<Node> getNodesInPath(LeafNode leaf) {
		Set<Node> nodesInPath = new HashSet<Node>();
		
		for(Edge parentEdge: leaf.getParents()) {
			Node parentNode = parentEdge.getParent();
			nodesInPath.add(parentNode);
			if(parentNode.getParents().size() > 0) {
				nodesInPath.addAll(getNodesInPath(parentNode));
			}
		}
		
		return nodesInPath;
	}
	
	private Set<Node> getNodesInPath(Node node) {
		Set<Node> nodesInPath = new HashSet<Node>();
		for(Edge parentEdge: node.getParents()) {
			Node parentNode = parentEdge.getParent();
			nodesInPath.add(parentNode);
			if(parentNode.getParents().size() > 0) {
				nodesInPath.addAll(getNodesInPath(parentNode));
			}
		}
		
		return nodesInPath;
	}
	
	private List<Node> copyGraphNodesList(List<Node> nodesList) {
		List<Node> copiedList = new LinkedList<Node>();
		
		for(Node node: nodesList) {
			Node copiedNode = new Node(node.attribute, 
					new LinkedList<Edge>(), new LinkedList<Edge>());
			if(node.isLeaf) {
				copiedNode.setIsLeaf(true);
			}
			if(node.isRoot) {
				copiedNode.setIsRoot(true);
			}
			
			copiedList.add(copiedNode);
		}
		
		return copiedList;
	}
	
	private List<LeafNode> copyLeafNodesList(List<LeafNode> nodesList) {
		List<LeafNode> copiedList = new LinkedList<LeafNode>();
		
		for(LeafNode node: nodesList) {
			LeafNode copiedNode = new LeafNode(node.attribute, 
					new LinkedList<Edge>(), copyAttributeValueIndices(node));
			if(node.isLeaf) {
				copiedNode.setIsLeaf(true);
			}
			if(node.isRoot) {
				copiedNode.setIsRoot(true);
			}
			
			copiedList.add(copiedNode);
		}
		
		return copiedList;
	}
	
	private Map<String, List<Integer>> copyAttributeValueIndices(LeafNode leaf) {
		Map<String, List<Integer>> copiedAttributeValueIndices = new TreeMap<String, List<Integer>>();
		Map<String, List<Integer>> originalAttributeValueIndices = leaf.getAttributeValueIndices();
		
		for(String attributeValue: originalAttributeValueIndices.keySet()) {
			List<Integer> copiedIndices = 
					new LinkedList<Integer>(originalAttributeValueIndices.get(attributeValue));
			copiedAttributeValueIndices.put(attributeValue, copiedIndices);
		}
		
		return copiedAttributeValueIndices;
	}

	public boolean applyCompleteSplitOperator(LeafNode v, DataAttribute attribute) {
		if(cannotApplySplit(v, attribute)) return false;
		
		Node newParent = new Node(attribute, new LinkedList<Edge>(), new LinkedList<Edge>());
		newParent.addParents(v.getParents());
		
		List<LeafNode> newChildren = new LinkedList<LeafNode>();
		Map<String, Map<String, List<Integer>>> classDistributionOnAttribute = 
				splitClassDistributionOnAttribute(v.getAttributeValueIndices(), attribute);
		
		for(String attributeValue: classDistributionOnAttribute.keySet()) {
			LeafNode newChild = new LeafNode(v.getDataAttribute(), new LinkedList<Edge>(), 
					classDistributionOnAttribute.get(attributeValue));
			Edge parentToChildEdge = new Edge(newParent, newChild, attributeValue);
			connectNodeToLeaf(newParent, parentToChildEdge, newChild);
			
			newChildren.add(newChild);
		}
		
		if(this.leaves.size() == 1) { //first specialization
			newParent.setIsRoot(true);
		}
		updateLeaves(v, newChildren);
		
		if(isTrivialSplit(newChildren)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Trivial split has leaves where all but one leaf have 0 instances.
	 * These splits are incorrectly picked because they fractionally increase the Bayesian score.
	 * They are undesirable because these variables don't add anything to the prediction.
	 */
	private boolean isTrivialSplit(List<LeafNode> newLeaves) {
		int nNewLeaves = newLeaves.size();
		int nTrivialLeaves = 0;
		
		for(LeafNode newLeaf: newLeaves) {
			int sum = 0;
			Map<String, List<Integer>> classValueIndices = newLeaf.getAttributeValueIndices();
			
			for(String classValue: classValueIndices.keySet()) {
				sum += classValueIndices.get(classValue).size();
			}
			
			if(sum == 0) {
				nTrivialLeaves += 1;
			}
		}
		
		return ((nNewLeaves - nTrivialLeaves) == 1);
	}
	
	private boolean cannotApplySplit(LeafNode v, DataAttribute attribute) {
		boolean cannotApplySplit = false;
		if(v.containsPathWithAttribute(attribute)) cannotApplySplit = true;
		if(attribute.getAttributeValues().size() == 1) cannotApplySplit = true;
		
		return cannotApplySplit;
	}
	
	private Map<String, Map<String, List<Integer>>> splitClassDistributionOnAttribute(
			Map<String, List<Integer>> classDistribution, DataAttribute attribute) {
		Map<String, Map<String, List<Integer>>> classDistributionByAttributeValue = 
				new TreeMap<String, Map<String, List<Integer>>>();
		List<String> attributeValues = attribute.getAttributeValues();
		
		for(String attributeValue: attributeValues) {
			classDistributionByAttributeValue.put(attributeValue, new TreeMap<String, List<Integer>>());
		}
		
		for(String classValue: classDistribution.keySet()) {
			for(String attributeValue: attributeValues) {
				classDistributionByAttributeValue.get(attributeValue).put(classValue, new LinkedList<Integer>());
			}
			
			for(Integer index: classDistribution.get(classValue)) {
				String attributeValueAtIndex = 
						dataModel.getColumnItemsByIndex(attribute.getAttributeIndex()).get(index);
				classDistributionByAttributeValue.get(attributeValueAtIndex).get(classValue).add(index);
			}
		}
		
		return classDistributionByAttributeValue;
	}
	
	private void connectNodeToLeaf(Node parent, Edge edge, LeafNode child) {
		parent.addChild(edge);
		child.addParent(edge);
	}
	
	private void updateLeaves(LeafNode oldLeaf, List<LeafNode> newLeaves) {
		int currentLeafIndex = leaves.indexOf(oldLeaf);
		leaves.remove(currentLeafIndex);
		leaves.addAll(currentLeafIndex, newLeaves);
	}

	public boolean applyBinarySplitOperator(LeafNode v, DataAttribute attribute, 
			List<String> attributeValues1, List<String> attributeValues2) {
		if(cannotApplySplit(v, attribute) || attributeValueSetsOverlap(
				attribute, attributeValues1, attributeValues2)) return false;
		
		Node newParent = new Node(attribute, new LinkedList<Edge>(), new LinkedList<Edge>());
		newParent.addParents(v.getParents());
		
		List<LeafNode> newChildren = new LinkedList<LeafNode>();
		Map<String, Map<String, List<Integer>>> classDistributionOnAttribute = 
				splitClassDistributionOnAttributeValueSets(v.getAttributeValueIndices(), attribute, 
						attributeValues1, attributeValues2);
		
		// Child 1
		String child1Label = concatenateAttributeValues(attributeValues1);
		LeafNode newChild1 = new LeafNode(v.getDataAttribute(), new LinkedList<Edge>(), 
				classDistributionOnAttribute.get(child1Label));
		Edge parentToChild1Edge = new Edge(newParent, newChild1, child1Label);
		connectNodeToLeaf(newParent, parentToChild1Edge, newChild1);
		newChildren.add(newChild1);
		
		// Child 2
		String child2Label = concatenateAttributeValues(attributeValues2);
		LeafNode newChild2 = new LeafNode(v.getDataAttribute(), new LinkedList<Edge>(), 
				classDistributionOnAttribute.get(child2Label));
		Edge parentToChild2Edge = new Edge(newParent, newChild2, child2Label);
		connectNodeToLeaf(newParent, parentToChild2Edge, newChild2);
		newChildren.add(newChild2);
		
		if(this.leaves.size() == 1) { //first specialization
			newParent.setIsRoot(true);
		}
		updateLeaves(v, newChildren);
		
		if(isTrivialSplit(newChildren)) {
			return false;
		}
		
		return true;
	}
	
	private boolean attributeValueSetsOverlap(DataAttribute attribute, 
			List<String> attributeValues1, List<String> attributeValues2) {
		for(String attributeValue: attributeValues1) {
			if(attributeValues2.contains(attributeValue))
				return true;
		}
		
		return false;
	}
	
	private Map<String, Map<String, List<Integer>>> splitClassDistributionOnAttributeValueSets(
			Map<String, List<Integer>> classDistribution, DataAttribute attribute, 
			List<String> attributeValueSet1, List<String> attributeValueSet2) {
		
		Map<String, Map<String, List<Integer>>> classDistributionByAttributeValueSets = 
				new TreeMap<String, Map<String, List<Integer>>>();
		
		String attributeSet1Label = concatenateAttributeValues(attributeValueSet1);
		String attributeSet2Label = concatenateAttributeValues(attributeValueSet2);
		
		classDistributionByAttributeValueSets.put(attributeSet1Label, new TreeMap<String, List<Integer>>());
		classDistributionByAttributeValueSets.put(attributeSet2Label, new TreeMap<String, List<Integer>>());
		
		for(String classValue: classDistribution.keySet()) {
			classDistributionByAttributeValueSets.get(attributeSet1Label).put(classValue, new LinkedList<Integer>());
			classDistributionByAttributeValueSets.get(attributeSet2Label).put(classValue, new LinkedList<Integer>());
			
			for(Integer index: classDistribution.get(classValue)) {
				String attributeValueAtIndex = 
						dataModel.getColumnItemsByIndex(attribute.getAttributeIndex()).get(index);
				if(attributeValueSet1.contains(attributeValueAtIndex)) {
					classDistributionByAttributeValueSets.get(attributeSet1Label).get(classValue).add(index);
				} else if(attributeValueSet2.contains(attributeValueAtIndex)) {
					classDistributionByAttributeValueSets.get(attributeSet2Label).get(classValue).add(index);
				}
			}
		}
		
		return classDistributionByAttributeValueSets;
	}
	
	private String concatenateAttributeValues(List<String> attributeValueSet) {
		StringBuffer sb = new StringBuffer();
		
		boolean begin = true;
		for(String attributeValue: attributeValueSet) {
			if(!begin)
				sb.append(",");
			sb.append(attributeValue);
			begin = false;
		}
		
		return sb.toString();
	}

	public boolean applyMergeOperator(LeafNode v1, LeafNode v2) {
		if(leavesShareAParentNode(v1, v2)) return false;
		
		Map<String, List<Integer>> mergedClassDistribution = mergeAttributeDistributions(
				v1.getAttributeValueIndices(), v2.getAttributeValueIndices());
		LeafNode mergedLeaf = new LeafNode(v1.getDataAttribute(), new LinkedList<Edge>(), mergedClassDistribution);
		
		mergedLeaf.addParents(v1.getParents());
		mergedLeaf.addParents(v2.getParents());
		
		int leafIndex = leaves.indexOf(v1);
		leaves.remove(leaves.indexOf(v1));
		leaves.remove(leaves.indexOf(v2));
		leaves.add(leafIndex, mergedLeaf);
		
		return true;
	}
	
	private boolean leavesShareAParentNode(LeafNode v1, LeafNode v2) {
		boolean haveSharedParents = false;
		
		Set<Node> leaf1Parents = new HashSet<Node>();
		Set<Node> leaf2Parents = new HashSet<Node>();
		
		for(int i=0; i<v1.getParents().size(); i++) 
			leaf1Parents.add(v1.getParents().get(i).getParent());
		
		for(int i=0; i<v2.getParents().size(); i++) 
			leaf2Parents.add(v2.getParents().get(i).getParent());
		
		leaf1Parents.retainAll(leaf2Parents);
		
		if(leaf1Parents.size() > 0) {
			haveSharedParents = true;
		}
		
		return haveSharedParents;
	}
	
	private Map<String, List<Integer>> mergeAttributeDistributions(
			Map<String, List<Integer>> distribution1, Map<String, List<Integer>> distribution2) {
		
		Map<String, List<Integer>> mergedDistribution = new TreeMap<String, List<Integer>>();
		
		for(String attributeValue: distribution1.keySet()) {
			List<Integer> mergedIndices = new LinkedList<Integer>();
			mergedIndices.addAll(distribution1.get(attributeValue));
			mergedIndices.addAll(distribution2.get(attributeValue));
			
			mergedDistribution.put(attributeValue, mergedIndices);
		}
		
		return mergedDistribution;
	}
	
	public DataModel getDataModel() {
		return this.dataModel;
	}
	
	public void setDataModel(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	public List<LeafNode> getLeafNodes() {
		return leaves;
	}
	
	public int getNumLeaves() {
		return leaves.size();
	}
	
	public void setLeafNodes(List<LeafNode> leaves) {
		this.leaves = leaves;
	}
	
	public DecisionGraph deepCopy() {
		return new DecisionGraph(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int count = 1;
		
		for(LeafNode leaf: leaves) {
			sb.append(count++).append(".\t").append(leaf.toString());
		}
		
		return sb.toString();
	}


	private DataModel dataModel;
	private List<LeafNode> leaves;
}
