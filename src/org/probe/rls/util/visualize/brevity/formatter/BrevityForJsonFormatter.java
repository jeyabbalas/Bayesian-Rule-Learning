package org.probe.rls.util.visualize.brevity.formatter;

import java.util.LinkedList;
import java.util.List;

import org.probe.rls.util.RuleFormatter;
import org.probe.rls.util.visualize.brevity.BayesianRuleEnsembleTree;
import org.probe.rls.util.visualize.brevity.Edge;
import org.probe.rls.util.visualize.brevity.Node;
import org.probe.rls.util.visualize.brevity.NodeStatistics;

public class BrevityForJsonFormatter {

	public BrevityForJsonFormatter(BayesianRuleEnsembleTree tree) {
		parseAndFormat(tree);
	}
	
	private void parseAndFormat(BayesianRuleEnsembleTree tree) {
		Node treeRoot = tree.getRoot();
		setRoot(parseNodeAndFormat(treeRoot));
	}
	
	private DisplayNode parseNodeAndFormat(Node node) {
		EdgeInfo edgeInfo = getEdgeInfo(node);
		TargetInfo targetInfo = getTargetInfo(node);
		NodeStatistics stats = node.getNodeStatistics();
		List<DisplayNode> children = new LinkedList<DisplayNode>();
		
		for(Edge childEdge: node.getChildren()) {
			Node child = childEdge.getTo();
			children.add(parseNodeAndFormat(child));
		}
		
		return new DisplayNode(node.getIdentifier(), edgeInfo, targetInfo, stats, children);
	}
	
	private EdgeInfo getEdgeInfo(Node node) {
		String name = node.getLabel();
		Edge parent = node.getParent();
		String rule = node.getRule();
		
		return new EdgeInfo(name, parent, rule);
	}
	
	private TargetInfo getTargetInfo(Node node) {
		String rule = node.getRule();
		
		if(rule.equalsIgnoreCase("")) {
			return new TargetInfo();
		} else {
			String rhs = rule.split("->")[1];
			String[] targetItems = RuleFormatter.formatRuleString(rhs).split("=");
			
			return new TargetInfo(targetItems[0], targetItems[1]);
		}
	}
	
	public DisplayNode getRoot() {
		return root;
	}

	public void setRoot(DisplayNode root) {
		this.root = root;
	}


	private DisplayNode root;
}
