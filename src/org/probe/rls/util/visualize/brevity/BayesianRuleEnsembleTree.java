package org.probe.rls.util.visualize.brevity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.probe.rls.models.rulemodel.Rule;
import org.probe.rls.models.rulemodel.RuleLiteral;
import org.probe.rls.models.rulemodel.RuleModel;
import org.probe.rls.models.rulemodel.ensemble.EnsembleRuleModel;
import org.probe.rls.util.RuleFormatter;

public class BayesianRuleEnsembleTree {

	public BayesianRuleEnsembleTree(EnsembleRuleModel eRM) {
		this.eRM = eRM;
		constructTree();
	}
	
	private void constructTree() {
		this.root = Node.createRootNode();
		
		List<RuleModel> ruleModels = eRM.getListOfRuleModels();
		
		for(RuleModel ruleModel: ruleModels) {
			List<Rule> rules = ruleModel.getRules();
			for(Rule rule: rules) {
				createChainFromRule(root, rule, ruleModel);
			}//for each rule
		}//for each model
	}
	
	private void createChainFromRule(Node chainStart, Rule rule, RuleModel ruleModel) {
		List<RuleLiteral> literals = sortLiteralsByImportance(rule.getLHS().getLiteralsUsed(), ruleModel);
		Node pointer = chainStart;
		
		
		for(int litID = 0; litID < literals.size(); litID++) {
			if(litID == (literals.size() - 1)) {//last node in chain
				String label = literals.get(litID).toString();
				String identifier = literals.get(litID).getIdentifier();
				
				if(pointer.nodeContainsChildWithLabel(label)) {
					if(pointer.getChildWithLabel(label).getNodeStatistics() == null) {
						pointer = pointer.getChildWithLabel(label);
						createEndNodeFromRule(pointer, identifier, label, rule);
					}
					break;
				}
				
				createEndNodeFromRule(pointer, identifier, label, rule);
			} else {//internal node
				String label = literals.get(litID).toString();
				String identifier = literals.get(litID).getIdentifier();
				
				if(pointer.nodeContainsChildWithLabel(label)) {
					pointer = pointer.getChildWithLabel(label);
					continue;
				}
				
				pointer = createInternalNodeFromRule(pointer, identifier, label);
			}
		}
		
	}
	
	private Node createInternalNodeFromRule(Node pointer, String identifier, String label) {
		String ruleString = "";
		NodeStatistics chainStats = null;
		
		Edge childEdge = new Edge(pointer, 0.0, null);
		Node child = new Node(identifier, childEdge, label, ruleString, chainStats);
		childEdge.setWeight(computeEdgeWeight(child));
		childEdge.setTo(child);
		
		pointer.addChild(childEdge);
		
		return child;
	}
	
	private void createEndNodeFromRule(Node pointer, String identifier, String label, Rule rule) {
		String ruleString = rule.getLHS().toString()+" -> "+rule.getRHS().toString();
		
		int tp = rule.getRuleStatistics().getTruePositives();
		int fp = rule.getRuleStatistics().getFalsePositives();
		double postProb = rule.getProbabilities().get(rule.getRHSValue());
		NodeStatistics chainStats = new NodeStatistics(rule.getRuleStatistics().getIdentifier(), postProb, tp, fp);
		
		Edge childEdge = new Edge(pointer, 0.0, null);
		Node child = new Node(identifier, childEdge, label, ruleString, chainStats);
		childEdge.setWeight(computeEdgeWeight(child));
		childEdge.setTo(child);
		
		pointer.addChild(childEdge);
	}
	
	private List<RuleLiteral> sortLiteralsByImportance(List<RuleLiteral> literals, RuleModel ruleModel) {
		List<RuleLiteral> sortedLiterals = new LinkedList<RuleLiteral>();
		
		for(String attribute: ruleModel.getAttributesUsed()) {
			for(RuleLiteral literal: literals) {
				String literalStr = RuleFormatter.removeAllWhiteSpace(RuleFormatter.removeParentheses(literal.toString()));
				
				if(mapLiteralToIdentifier.containsKey(literalStr)) {
					String uniqueIdentifier = mapLiteralToIdentifier.get(literalStr);
					literal.setIdentifier(uniqueIdentifier);
				} else {
					mapLiteralToIdentifier.put(literalStr, literal.getIdentifier());
				}
				
				
				if(literalStr.split("=")[0].equalsIgnoreCase(attribute)) {
					sortedLiterals.add(literal);
				}
			}
		}
		
		return sortedLiterals;
	}
	
	private double computeEdgeWeight(Node node) {
		List<String> vars = node.getVariablesInPath();
		
		return computeVariableSetImportance(vars);
	}
	
	private double computeVariableSetImportance(List<String> variableSet) {
		List<RuleModel> ruleModels = new LinkedList<RuleModel>(eRM.getListOfRuleModels());
		List<RuleModel> ruleModelsSubset = null;
		
		List<Double> modelWeights = new LinkedList<Double>(eRM.getModelWeights());
		List<Double> modelWeightsSubset = null;
		
		for(String variable: variableSet) {
			ruleModelsSubset = new LinkedList<RuleModel>();
			modelWeightsSubset = new LinkedList<Double>();
			
			for(int h=0; h<ruleModels.size(); h++) {
				if(ruleModels.get(h).containsField(variable)) {
					ruleModelsSubset.add(ruleModels.get(h));
					modelWeightsSubset.add(modelWeights.get(h));
				}
			}
			
			ruleModels = ruleModelsSubset;
			modelWeights = modelWeightsSubset;
		}
		
		
		double variableSetImportance = 0.0;
		
		for(int h=0; h<ruleModels.size(); h++) {
			variableSetImportance += modelWeights.get(h);
		}
		
		
		return variableSetImportance;
	}
	
	public Node getRoot() {
		return root;
	}
	
	
	private transient Map<String, String> mapLiteralToIdentifier = new HashMap<String, String>();
	
	private Node root;
	private transient EnsembleRuleModel eRM; 
}
