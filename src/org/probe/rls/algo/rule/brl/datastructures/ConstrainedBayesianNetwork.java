package org.probe.rls.algo.rule.brl.datastructures;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.probe.rls.data.DataAttribute;
import org.probe.rls.data.DataModel;
import org.probe.rls.algo.rule.brl.BRLSearchParameters;
import org.probe.rls.algo.rule.brl.util.BayesianScore;

/**
 * A constrained Bayesian network only has one target node and a set of its parents.
 * 
 * @author jeya
 *
 */
public class ConstrainedBayesianNetwork implements Cloneable, Comparator<ConstrainedBayesianNetwork>, Comparable<ConstrainedBayesianNetwork> {
	
	public ConstrainedBayesianNetwork(DataModel dataModel, BRLSearchParameters params) {
		this.setDataModel(dataModel);
		this.setParams(params);
		
		this.initGlobalStructure();
		this.initLocalStructure();
		
		this.setNetworkPriorProbability(0.0);
	}
	
	public ConstrainedBayesianNetwork(ConstrainedBayesianNetwork cbn) {
		this.setDataModel(cbn.getDataModel());
		this.setParams(cbn.getParams());
		
		this.initGlobalStructure(cbn);
		this.initLocalStructure(cbn);
		
		this.setNetworkPriorProbability(cbn.getNetworkPriorProbability());
		this.setNetworkPosteriorProbability(cbn.getNetworkPosteriorProbability());
	}
	
	public static ConstrainedBayesianNetwork createRandomModel(DataModel dataModel, BRLSearchParameters params) {
		ConstrainedBayesianNetwork randModel = new ConstrainedBayesianNetwork(dataModel, params);
		randModel.setRandomModel(true);
		return randModel;
	}
	
	private void initGlobalStructure() {
		//For BREVity: LinkedHashSet to remember the order of insertion. 
		//Earlier added variables are more important than later ones.
		this.classParents = new LinkedHashSet<DataAttribute>();
	}
	
	private void initGlobalStructure(ConstrainedBayesianNetwork cbn) {
		this.classParents = new LinkedHashSet<DataAttribute>(cbn.getClassParents());
	}
	
	private void initLocalStructure() {
		this.classLocalStructure = new DecisionGraph(dataModel, dataModel.getClassAttribute());
	}
	
	private void initLocalStructure(ConstrainedBayesianNetwork cbn) {
		this.classLocalStructure = cbn.getClassLocalStructure().deepCopy();
	}

	public DataModel getDataModel() {
		return dataModel;
	}

	public void setDataModel(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	public BRLSearchParameters getParams() {
		return params;
	}

	public void setParams(BRLSearchParameters params) {
		this.params = params;
	}

	public Set<DataAttribute> getClassParents() {
		return classParents;
	}

	public void setClassParents(Set<DataAttribute> classParents) {
		this.classParents = classParents;
	}

	public DecisionGraph getClassLocalStructure() {
		return classLocalStructure;
	}

	public void setClassLocalStructure(DecisionGraph classLocalStructure) {
		this.classLocalStructure = classLocalStructure;
	}

	public double getNetworkPosteriorProbability() {
		return lnNetworkPosteriorProbability;
	}

	public void setNetworkPosteriorProbability(double lnNetworkPosteriorProbability) {
		this.lnNetworkPosteriorProbability = lnNetworkPosteriorProbability;
	}

	public double getNetworkPriorProbability() {
		return lnNetworkPriorProbability;
	}

	public void setNetworkPriorProbability(double lnNetworkPriorProbability) {
		this.lnNetworkPriorProbability = lnNetworkPriorProbability;
		this.computeNetworkPosteriorProbability();
	}

	public double getModelWeight() {
		return modelWeight;
	}

	public void setModelWeight(double modelWeight) {
		this.modelWeight = modelWeight;
	}

	public void addParent(DataAttribute attribute) {
		classParents.add(attribute);
	}
	
	public void addParents(List<DataAttribute> attributes) {
		for(DataAttribute attribute: attributes) {
			this.addParent(attribute);
		}
	}
	
	public boolean applyCompleteSplitOperatorOnAllLeavesOfLocalStructure(DataAttribute attribute) {
		boolean success = false;
		
		for(int leafID = 0; leafID < classLocalStructure.getNumLeaves();) {
			LeafNode selectedLeaf = classLocalStructure.getLeafNodes().get(leafID);
			//if any of the split was useful, we accept the model.
			boolean thisSuccess =  classLocalStructure.applyCompleteSplitOperator(selectedLeaf, attribute);
			leafID += attribute.getAttributeValues().size();
			success = success || thisSuccess;
		}
		
		if(success) {
			classParents.add(attribute);
			computeNetworkPosteriorProbability();
		}
		
		return success;
	}
	
	public boolean forceApplyCompleteSplitOperatorOnAllLeavesOfLocalStructure(DataAttribute attribute) {
		
		for(int leafID = 0; leafID < classLocalStructure.getNumLeaves();) {
			LeafNode selectedLeaf = classLocalStructure.getLeafNodes().get(leafID);
			//if any of the split was useful, we accept the model.
			classLocalStructure.applyCompleteSplitOperator(selectedLeaf, attribute);
			leafID += attribute.getAttributeValues().size();
		}
		
		classParents.add(attribute);
		computeNetworkPosteriorProbability();
		
		return true;
	}
	
	public boolean applyCompleteSplitOperatorOnLocalStructure(int leafID, DataAttribute attribute) {
		LeafNode selectedLeaf = classLocalStructure.getLeafNodes().get(leafID);
		boolean success = classLocalStructure.applyCompleteSplitOperator(selectedLeaf, attribute);
		
		if(success) {
			classParents.add(attribute);
			computeNetworkPosteriorProbability();
		}
		
		return success;
	}
	
	public boolean applyBinarySplitOperatorOnLocalStructure(int leafID, DataAttribute attribute, 
			List<String> attributeValues1, List<String> attributeValues2) {
		LeafNode selectedLeaf = classLocalStructure.getLeafNodes().get(leafID);
		boolean success = classLocalStructure.applyBinarySplitOperator(selectedLeaf, attribute, 
				attributeValues1, attributeValues2);
		
		if(success) {
			classParents.add(attribute);
			computeNetworkPosteriorProbability();
		}
		
		return success;
	}
	
	public boolean applyMergeOperatorOnLocalStructure(int leaf1, int leaf2) {
		LeafNode v1 = classLocalStructure.getLeafNodes().get(leaf1);
		LeafNode v2 = classLocalStructure.getLeafNodes().get(leaf2);
		
		boolean success = classLocalStructure.applyMergeOperator(v1, v2);
		
		if(success) {
			computeNetworkPosteriorProbability();
		}
		
		return success;
	}
	
	public boolean isRandomModel() {
		return isRandomModel;
	}

	public void setRandomModel(boolean isRandomModel) {
		this.isRandomModel = isRandomModel;
	}

	public ConstrainedBayesianNetwork copyByValue() {
		return new ConstrainedBayesianNetwork(this);
	}
	
	@Override
	public int compareTo(ConstrainedBayesianNetwork cbn) {
		if(this.getNetworkPosteriorProbability() > cbn.getNetworkPosteriorProbability()) {
			return 1;
		} else if(this.getNetworkPosteriorProbability() < cbn.getNetworkPosteriorProbability()) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	public int compare(ConstrainedBayesianNetwork cbn1, ConstrainedBayesianNetwork cbn2) {
		if(cbn1.getNetworkPosteriorProbability() > cbn2.getNetworkPosteriorProbability()) {
			return 1;
		} else if(cbn1.getNetworkPosteriorProbability() < cbn2.getNetworkPosteriorProbability()) {
			return -1;
		} else {
			return 0;
		}
	}
	
	private double computeNetworkPosteriorProbability() {
		//Global penalty
		if(this.params.isUseComplexityPenaltyPrior()) {
			// complexity penalty P(G) = kappa^#parents
			double structurePrior = Math.log(Math.pow(this.params.getKappa(), classParents.size()));
			if(params.isUseInformativePrior()) {
				structurePrior = structurePrior + Math.log(params.getInformativePrior().computeStructurePrior(classParents));
				//TODO
			}
			this.lnNetworkPriorProbability = structurePrior;
			
		} else {
			this.lnNetworkPriorProbability = 0.0;
		}
		
		
		lnNetworkPosteriorProbability = this.getNetworkPriorProbability();
		List<LeafNode> leaves = classLocalStructure.getLeafNodes();
		
		for(LeafNode leaf: leaves) {
			List<String> classValues = dataModel.getClassAttribute().getAttributeValues();
			double[] classCounts = new double[classValues.size()];
			double[] alpha = new double[classValues.size()];
			double alpha_0 = 1.0;
			
			for(int t=0; t<classValues.size(); t++) {
				String classValue = classValues.get(t);
				
				double cost = 1.0;
				if(params.doCostSensitiveSearch()) {
					cost = params.getMisclassificationCost().get(classValue);
				}
				
				if(params.doWeightedLearning()) {
					classCounts[t] = cost*getWeightedLabelCountsFromLeaf(leaf, classValue);
				} else {
					classCounts[t] = cost*leaf.getLabelCounts(classValue);
				}
				alpha[t] = alpha_0/(leaves.size()*classValues.size());
			}
			
			leaf.updateParameterPosteriorDistribution(
					computeParameterPosteriorDistribution(classCounts, alpha));
			lnNetworkPosteriorProbability += BayesianScore.lnBDeuScoreForLeaf(classCounts, alpha);
		}
		
		return lnNetworkPosteriorProbability;
	}
	
	private double getWeightedLabelCountsFromLeaf(LeafNode leaf, String classValue) {
		double weightedLabelCounts = 0.0;
		List<Integer> indices = leaf.getAttributeValueIndices().get(classValue);
		
		for(int index: indices) {
			weightedLabelCounts += dataModel.getInstanceWeight(index);
		}
		
		return weightedLabelCounts;
	}

	private Map<String, Double> computeParameterPosteriorDistribution(double[] classCounts, double[] alpha) {
		Map<String, Double> parameterPosterior = new TreeMap<String, Double>();
		
		double[] classPosterior = BayesianScore.bdeuParameterPosteriorForLeaf(classCounts, alpha);
		List<String> classValues = dataModel.getClassAttribute().getAttributeValues();
		
		for(int t=0; t<classValues.size(); t++) {
			parameterPosterior.put(classValues.get(t), classPosterior[t]);
		}
		
		return parameterPosterior;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(classLocalStructure.toString());
		sb.append("Bayesian Score (natural logarithm): "+lnNetworkPosteriorProbability);
		sb.append("\nVariables used: ");
		List<DataAttribute> attributesUsed = new LinkedList<DataAttribute>(classParents);
		for(int i=0; i<attributesUsed.size(); i++) {
			if(i>0)
				sb.append(", ");
			sb.append(attributesUsed.get(i).getAttributeName());
		}
		if(attributesUsed.size()>0)
			sb.append(".");
		sb.append("\n");
		
		return sb.toString();
	}
	
	private boolean isRandomModel;

	private DataModel dataModel;
	private BRLSearchParameters params;
	private Set<DataAttribute> classParents;
	private DecisionGraph classLocalStructure;
	private double lnNetworkPosteriorProbability;
	private double lnNetworkPriorProbability;
	private double modelWeight;
}
