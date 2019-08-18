package org.probe.rls.algo.rule.brl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;

import org.probe.rls.algo.rule.brl.BRLSearchParameters;
import org.probe.rls.algo.rule.brl.datastructures.BRLDataAttribute;
import org.probe.rls.algo.rule.brl.datastructures.ConstrainedBayesianNetwork;
import org.probe.rls.algo.rule.brl.datastructures.LeafNode;
import org.probe.rls.algo.rule.brl.parameters.BRLSearchType;
import org.probe.rls.algo.rule.brl.parameters.CPTRepresentationType;
import org.probe.rls.algo.rule.brl.util.BRLUtils;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.DefaultDataModel;
import org.probe.rls.data.fold.BootstrapFoldGenerator;
import org.probe.rls.data.fold.FoldGenerator;
import org.probe.rls.models.rulemodel.Rule;
import org.probe.rls.models.rulemodel.RuleModel;
import org.probe.rls.models.rulemodel.general.GeneralRule;
import org.probe.rls.util.queue.FixedSizePriorityQueue;
import org.probe.rls.data.DataAttribute;

public class BRLSearch {

	public BRLSearch(BRLSearchParameters params, DataModel dataModel) {
		this.params = params;
		this.dataModel = dataModel;
		setValidSpecializationOperators();
		//TODO: Check all data is discrete
	}

	private void setValidSpecializationOperators() {
		CPTRepresentationType cptType = params.getCptRepresentation();

		if(cptType.equals(CPTRepresentationType.Global)) {
			onlyGenerateCompleteTrees = true;
			doCompleteSplit = false;
			doBinarySplit = false;
			doMerge = false;
		} else if(cptType.equals(CPTRepresentationType.Local_C)) {
			onlyGenerateCompleteTrees = false;
			doCompleteSplit = true;
			doBinarySplit = false;
			doMerge = false;
		} else if(cptType.equals(CPTRepresentationType.Local_B)) {
			onlyGenerateCompleteTrees = false;
			doCompleteSplit = false;
			doBinarySplit = true;
			doMerge = false;
		} else if(cptType.equals(CPTRepresentationType.Local_M)) {
			onlyGenerateCompleteTrees = false;
			doCompleteSplit = false;
			doBinarySplit = false;
			doMerge = true;
		} else if(cptType.equals(CPTRepresentationType.Local_CB)) {
			onlyGenerateCompleteTrees = false;
			doCompleteSplit = true;
			doBinarySplit = true;
			doMerge = false;
		} else if(cptType.equals(CPTRepresentationType.Local_CM)) {
			onlyGenerateCompleteTrees = false;
			doCompleteSplit = true;
			doBinarySplit = false;
			doMerge = true;
		} else if(cptType.equals(CPTRepresentationType.Local_BM)) {
			onlyGenerateCompleteTrees = false;
			doCompleteSplit = false;
			doBinarySplit = true;
			doMerge = true;
		} else if(cptType.equals(CPTRepresentationType.Local_CBM)) {
			onlyGenerateCompleteTrees = false;
			doCompleteSplit = true;
			doBinarySplit = true;
			doMerge = true;
		} else { // assume Global as default
			onlyGenerateCompleteTrees = true;
			doCompleteSplit = false;
			doBinarySplit = false;
			doMerge = false;
		}
	}

	public ConstrainedBayesianNetwork runBRLSearch() {
		if(params.getSearchAlgorithm().equals(BRLSearchType.GreedyBestFirst)) {
			return this.runGreedyBestFirstSearch();
		} else if(params.getSearchAlgorithm().equals(BRLSearchType.GreedyBestFirstWithComplexityPenalty)) {
			return this.runGreedyBestFirstSearchWithComplexityPenalty();
		} else if(params.getSearchAlgorithm().equals(BRLSearchType.BeamWithComplexityPenalty)) {
			return this.runGreedyBeamSearchWithComplexityPenalty();
		} else if(params.getSearchAlgorithm().equals(BRLSearchType.ParallelGreedyWithComplexityPenalty)) {
			return this.runGreedyParallelSearchWithComplexityPenalty();
		}

		return this.runGreedyBestFirstSearch();
	}

	public List<ConstrainedBayesianNetwork> runEnsembleBRLSearch() throws Exception {
		if(params.getSearchAlgorithm().equals(BRLSearchType.Bagging)) {
			return this.runGreedyBaggingSearch();
		} else if(params.getSearchAlgorithm().equals(BRLSearchType.Boosting)) {
			return this.runGreedyBoostingSearch();
		} else if(params.getSearchAlgorithm().equals(BRLSearchType.Random)) {
			return this.runDeliberatelyBadBaggingSearch();
		}

		return this.runGreedyBaggingSearch();
	}


	public ConstrainedBayesianNetwork runGreedyBestFirstSearch() {
		PriorityQueue<ConstrainedBayesianNetwork> beam = initializeBeamAndSortAttributes(); //initialization

		ConstrainedBayesianNetwork bestModel = null;
		if(beam.size() > 0) {
			bestModel = beam.poll();
		}

		boolean iterationImprovedModel = true;

		while(!terminationCondition(bestModel, iterationImprovedModel)) {
			beam.clear();
			iterationImprovedModel = false;

			for(DataAttribute attribute: this.attributes) { //specialization

				if(bestModel.getClassParents().contains(attribute)) {
					continue;
				}
				if(this.onlyGenerateCompleteTrees) { //complete tree
					ConstrainedBayesianNetwork specModel = specializeWithCompleteTree(bestModel, attribute);
					if(specModel != null) beam.add(specModel);
				} else {
					if(this.doCompleteSplit) { //complete split operator
						ConstrainedBayesianNetwork specModel = specializeWithGreedyBestFirstCompleteSplit(bestModel, attribute);
						if(specModel != null) beam.add(specModel);
					}
					if(this.doBinarySplit) { //binary split operator
						if(!this.doCompleteSplit || attribute.getAttributeValues().size()>2) {
							ConstrainedBayesianNetwork specModel = specializeWithGreedyBestFirstBinarySplit(bestModel, attribute);
							if(specModel != null) beam.add(specModel);
						}
					}
					if(this.doMerge) { //merge operator
						ConstrainedBayesianNetwork specModel = specializeWithGreedyBestFirstMerge(bestModel);
						if(specModel != null) beam.add(specModel);
					}
				}
			}

			ConstrainedBayesianNetwork bestModelInIteration = beam.poll();
			if(bestModelInIteration == null) {
				break;
			}

			if(bestModel.getNetworkPosteriorProbability() < 
					bestModelInIteration.getNetworkPosteriorProbability()) {
				iterationImprovedModel = true;
				bestModel = bestModelInIteration; //selection
			}
		} //termination


		return bestModel;
	}


	/**
	 * Removes maxConj parameter.	
	 * @return
	 */
	public ConstrainedBayesianNetwork runGreedyBestFirstSearchWithComplexityPenalty() {
		PriorityQueue<ConstrainedBayesianNetwork> beam = initializeBeamAndSortAttributes(); //initialization

		ConstrainedBayesianNetwork bestModel = null;
		if(beam.size() > 0) {
			bestModel = beam.poll();
		}

		boolean iterationImprovedModel = true;

		while(iterationImprovedModel) {
			beam.clear();
			iterationImprovedModel = false;


			for(DataAttribute attribute: this.attributes) { //specialization
				if(bestModel.getClassParents().contains(attribute)) {
					continue;
				}
				if(this.onlyGenerateCompleteTrees) { //complete tree
					ConstrainedBayesianNetwork specModel = specializeWithCompleteTree(bestModel, attribute);
					if(specModel != null) beam.add(specModel);
				} else {
					if(this.doCompleteSplit) { //complete split operator
						ConstrainedBayesianNetwork specModel = specializeWithGreedyBestFirstCompleteSplit(bestModel, attribute);
						if(specModel != null) beam.add(specModel);
					}
					if(this.doBinarySplit) { //binary split operator
						if(!this.doCompleteSplit || attribute.getAttributeValues().size()>2) {
							ConstrainedBayesianNetwork specModel = specializeWithGreedyBestFirstBinarySplit(bestModel, attribute);
							if(specModel != null) beam.add(specModel);
						}
					}
					if(this.doMerge) { //merge operator
						ConstrainedBayesianNetwork specModel = specializeWithGreedyBestFirstMerge(bestModel);
						if(specModel != null) beam.add(specModel);
					}
				}
			}

			ConstrainedBayesianNetwork bestModelInIteration = beam.poll();
			if(bestModelInIteration == null) {
				break;
			}

			if(bestModel.getNetworkPosteriorProbability() < 
					bestModelInIteration.getNetworkPosteriorProbability()) {
				iterationImprovedModel = true;
				bestModel = bestModelInIteration; //selection
			}
		} //termination


		return bestModel;
	}
	
	public ConstrainedBayesianNetwork runGreedyParallelSearchWithComplexityPenalty() {
		PriorityQueue<ConstrainedBayesianNetwork> beam = initializeBeamAndSortAttributes(); //initialization
		List<ConstrainedBayesianNetwork> kBestModels = new ArrayList<ConstrainedBayesianNetwork>();
		
		
		PriorityQueue<ConstrainedBayesianNetwork> bestGreedyModels = new PriorityQueue<ConstrainedBayesianNetwork>();

		int totalModels = params.getBeamWidth();
		if(beam.size() < totalModels) totalModels = beam.size();
		
		for(int h=0; h<totalModels; h++) {
			kBestModels.add(beam.poll());
		}
		
		for(int h=0; h<totalModels; h++) { // Greedy BFS on totalModels best models.
			ConstrainedBayesianNetwork bestModel = kBestModels.get(h);
			
			boolean iterationImprovedModel = true;

			while(iterationImprovedModel) {
				beam.clear();
				iterationImprovedModel = false;


				for(DataAttribute attribute: this.attributes) { //specialization
					if(bestModel.getClassParents().contains(attribute)) {
						continue;
					}
					if(this.onlyGenerateCompleteTrees) { //complete tree
						ConstrainedBayesianNetwork specModel = specializeWithCompleteTree(bestModel, attribute);
						if(specModel != null) beam.add(specModel);
					} else {
						if(this.doCompleteSplit) { //complete split operator
							ConstrainedBayesianNetwork specModel = specializeWithGreedyBestFirstCompleteSplit(bestModel, attribute);
							if(specModel != null) beam.add(specModel);
						}
						if(this.doBinarySplit) { //binary split operator
							if(!this.doCompleteSplit || attribute.getAttributeValues().size()>2) {
								ConstrainedBayesianNetwork specModel = specializeWithGreedyBestFirstBinarySplit(bestModel, attribute);
								if(specModel != null) beam.add(specModel);
							}
						}
						if(this.doMerge) { //merge operator
							ConstrainedBayesianNetwork specModel = specializeWithGreedyBestFirstMerge(bestModel);
							if(specModel != null) beam.add(specModel);
						}
					}
				}

				ConstrainedBayesianNetwork bestModelInIteration = beam.poll();
				if(bestModelInIteration == null) {
					break;
				}

				if(bestModel.getNetworkPosteriorProbability() < 
						bestModelInIteration.getNetworkPosteriorProbability()) {
					iterationImprovedModel = true;
					bestModel = bestModelInIteration; //selection
				}
			} //termination
			
			bestGreedyModels.add(bestModel);
		}


		return bestGreedyModels.poll();
	}

	public ConstrainedBayesianNetwork runGreedyBeamSearchWithComplexityPenalty() {
		PriorityQueue<ConstrainedBayesianNetwork> beam = initializeBeamAndSortAttributes(); //initialization
		ConstrainedBayesianNetwork bestModel = beam.peek().copyByValue(); // best model so far

		while(!beam.isEmpty()) {
			LinkedList<ConstrainedBayesianNetwork> modelsToSpecialize = new LinkedList<ConstrainedBayesianNetwork>(beam);
			beam.clear();

			int totalModels = params.getBeamWidth();
			if(modelsToSpecialize.size() < totalModels) totalModels = modelsToSpecialize.size();
			for(int mod=0; mod<totalModels; mod++) { // specializing top k candidates
				ConstrainedBayesianNetwork currModel = modelsToSpecialize.removeFirst();
				PriorityQueue<ConstrainedBayesianNetwork> tempBeam = new PriorityQueue<ConstrainedBayesianNetwork>();

				for(DataAttribute attribute: this.attributes) { //specialization
					if(currModel.getClassParents().contains(attribute)) {
						continue;
					}
					if(this.onlyGenerateCompleteTrees) { //complete tree
						ConstrainedBayesianNetwork specModel = specializeWithCompleteTree(currModel, attribute);
						if(specModel != null) tempBeam.add(specModel);
					} else {
						if(this.doCompleteSplit) { //complete split operator
							ConstrainedBayesianNetwork specModel = specializeWithGreedyBestFirstCompleteSplit(currModel, attribute);
							if(specModel != null) tempBeam.add(specModel);
						}
						if(this.doBinarySplit) { //binary split operator
							if(!this.doCompleteSplit || attribute.getAttributeValues().size()>2) {
								ConstrainedBayesianNetwork specModel = specializeWithGreedyBestFirstBinarySplit(currModel, attribute);
								if(specModel != null) tempBeam.add(specModel);
							}
						}
						if(this.doMerge) { //merge operator
							ConstrainedBayesianNetwork specModel = specializeWithGreedyBestFirstMerge(currModel);
							if(specModel != null) tempBeam.add(specModel);
						}
					}
				} // end specializing operators

				// if specialization helped, save for next iteration
				while(!tempBeam.isEmpty()) {
					ConstrainedBayesianNetwork iModel = tempBeam.poll();
					if(currModel.getNetworkPosteriorProbability() < 
							iModel.getNetworkPosteriorProbability()) {
						beam.add(iModel); // for further specialization
					} else {
						tempBeam.clear();
						break;
					}
				}

			} // end specializing loop

			if(!beam.isEmpty()) { // best model so far
				if(bestModel.getNetworkPosteriorProbability() <
						beam.peek().getNetworkPosteriorProbability()) {
					bestModel = beam.peek().copyByValue();
				}
			}

		} // end while


		return bestModel;
	}

	public ConstrainedBayesianNetwork createBadModel() {
		initializeBeamAndSortAttributes(); //initialization
		
		ConstrainedBayesianNetwork badModel = new ConstrainedBayesianNetwork(dataModel, params);

		/**
		int i = 0;
		int numParents = 0;
		do {
			i++;
			DataAttribute attribute = this.attributes.get(this.attributes.size()-i);
			badModel.applyCompleteSplitOperatorOnAllLeavesOfLocalStructure(attribute);
			numParents = badModel.getClassParents().size();
		} while(numParents == 0);
		*/
		DataAttribute attribute = this.attributes.get(this.attributes.size()-1);
		badModel.forceApplyCompleteSplitOperatorOnAllLeavesOfLocalStructure(attribute);
		

		return badModel;
	}

	/*
	 * Singleton models are Bayesian networks with only one parent.
	 */
	private PriorityQueue<ConstrainedBayesianNetwork> initializeBeamAndSortAttributes() {
		this.attributes = new LinkedList<DataAttribute>();
		PriorityQueue<BRLDataAttribute> sortedBRLDataAttributes = 
				new PriorityQueue<BRLDataAttribute>((dataModel.getAttributes().size()-2), Collections.reverseOrder());
		PriorityQueue<ConstrainedBayesianNetwork> initialBeam = 
				new PriorityQueue<ConstrainedBayesianNetwork>(params.getBeamWidth(), Collections.reverseOrder());
		initialBeam.add(new ConstrainedBayesianNetwork(dataModel, params));

		for(DataAttribute attribute: dataModel.getAttributes()) {
			if(attribute.isClass() || attribute.isInstance()) {
				continue;
			}

			BRLDataAttribute brlAttribute = new BRLDataAttribute(attribute);

			ConstrainedBayesianNetwork bestSingletonModel = new ConstrainedBayesianNetwork(dataModel, params);

			if(this.onlyGenerateCompleteTrees) { //complete tree
				ConstrainedBayesianNetwork bestCompleteTree = 
						specializeWithCompleteTree(new ConstrainedBayesianNetwork(dataModel, params), attribute);

				if(bestCompleteTree == null) continue;
				if(bestCompleteTree.getNetworkPosteriorProbability() > brlAttribute.getScore()) {
					brlAttribute.setScore(bestCompleteTree.getNetworkPosteriorProbability());
					bestSingletonModel = bestCompleteTree;
				}
			} else {
				if(this.doCompleteSplit) { //complete split
					ConstrainedBayesianNetwork bestCompleteSplit = 
							specializeWithGreedyBestFirstCompleteSplit(new ConstrainedBayesianNetwork(dataModel, params), attribute);

					if(bestCompleteSplit == null) continue;
					if(bestCompleteSplit.getNetworkPosteriorProbability() > brlAttribute.getScore()) {
						brlAttribute.setScore(bestCompleteSplit.getNetworkPosteriorProbability());
						bestSingletonModel = bestCompleteSplit;
					}
				}
				if(this.doBinarySplit) { //binary split
					ConstrainedBayesianNetwork bestBinarySplit = 
							specializeWithGreedyBestFirstBinarySplit(new ConstrainedBayesianNetwork(dataModel, params), attribute);

					if(bestBinarySplit == null) continue;
					if(bestBinarySplit.getNetworkPosteriorProbability() > brlAttribute.getScore()) {
						brlAttribute.setScore(bestBinarySplit.getNetworkPosteriorProbability());
						bestSingletonModel = bestBinarySplit;
					}
				}
			}

			// initial beam
			initialBeam.add(bestSingletonModel);

			sortedBRLDataAttributes.add(brlAttribute);
		}

		for(BRLDataAttribute attribute: sortedBRLDataAttributes) {
			this.attributes.add(attribute.getDataAttribute());
		}

		return initialBeam;
	}

	private boolean terminationCondition(ConstrainedBayesianNetwork model, 
			boolean previousIterationImprovedScore) {
		boolean terminateSearch = false;

		if(model.getClassParents().size() >= params.getMaxParents()) terminateSearch = true;
		if(!previousIterationImprovedScore) terminateSearch = true;

		return terminateSearch;
	}

	private ConstrainedBayesianNetwork specializeWithCompleteTree(
			ConstrainedBayesianNetwork model, DataAttribute attribute) {

		ConstrainedBayesianNetwork completeTreeModel = model.copyByValue();

		//if even one of the split was non-trivial, we accept it overall.
		boolean success = completeTreeModel.applyCompleteSplitOperatorOnAllLeavesOfLocalStructure(attribute);
		if(success) {
			return completeTreeModel;
		}

		return null;
	}

	private ConstrainedBayesianNetwork specializeWithExhaustiveGreedyBestFirstCompleteSplit(
			ConstrainedBayesianNetwork model, DataAttribute attribute) {

		PriorityQueue<ConstrainedBayesianNetwork> specializedModels = 
				new PriorityQueue<ConstrainedBayesianNetwork>(params.getBeamWidth(), Collections.reverseOrder());
		ConstrainedBayesianNetwork bestCompleteSplitModel = model.copyByValue();

		int initialTotalLeaves = model.getClassLocalStructure().getNumLeaves();
		int numSplitsAttempted = 0;
		boolean iterationHelped = true;

		while((numSplitsAttempted < initialTotalLeaves) && iterationHelped) {
			iterationHelped = false;
			specializedModels.clear();

			for(int leafID = 0; leafID < bestCompleteSplitModel.getClassLocalStructure().getNumLeaves(); leafID++) {
				ConstrainedBayesianNetwork candidateModel = bestCompleteSplitModel.copyByValue();
				boolean success = candidateModel.applyCompleteSplitOperatorOnLocalStructure(leafID, attribute);
				if(success) specializedModels.add(candidateModel);
			}

			if((specializedModels.size() != 0) && 
					(specializedModels.peek().getNetworkPosteriorProbability() > bestCompleteSplitModel.getNetworkPosteriorProbability())) {
				iterationHelped = true;
				bestCompleteSplitModel = specializedModels.poll();
			}

			numSplitsAttempted++;
		}

		return bestCompleteSplitModel;
	}

	private ConstrainedBayesianNetwork specializeWithGreedyBestFirstCompleteSplit(
			ConstrainedBayesianNetwork model, DataAttribute attribute) {

		PriorityQueue<ConstrainedBayesianNetwork> specializedModels = 
				new PriorityQueue<ConstrainedBayesianNetwork>(params.getBeamWidth(), Collections.reverseOrder());
		ConstrainedBayesianNetwork bestCompleteSplitModel = model.copyByValue();

		specializedModels.add(model);

		for(int leafID = 0; leafID < bestCompleteSplitModel.getClassLocalStructure().getNumLeaves(); leafID++) {
			ConstrainedBayesianNetwork candidateModel = bestCompleteSplitModel.copyByValue();
			boolean success = candidateModel.applyCompleteSplitOperatorOnLocalStructure(leafID, attribute);
			if(success) specializedModels.add(candidateModel);
		}

		if((specializedModels.size() != 0) && 
				(specializedModels.peek().getNetworkPosteriorProbability() > bestCompleteSplitModel.getNetworkPosteriorProbability())) {
			bestCompleteSplitModel = specializedModels.poll();
		}

		return bestCompleteSplitModel;
	}

	private ConstrainedBayesianNetwork specializeWithExhaustiveGreedyBestFirstBinarySplit(
			ConstrainedBayesianNetwork model, DataAttribute attribute) {

		PriorityQueue<ConstrainedBayesianNetwork> specializedModels = 
				new PriorityQueue<ConstrainedBayesianNetwork>(params.getBeamWidth(), Collections.reverseOrder());
		ConstrainedBayesianNetwork bestBinarySplitModel = model.copyByValue();

		int initialTotalLeaves = model.getClassLocalStructure().getNumLeaves();
		int numSplitsAttempted = 0;
		boolean iterationHelped = true;

		while((numSplitsAttempted < initialTotalLeaves) && iterationHelped) {
			iterationHelped = false;
			specializedModels.clear();

			for(int leafID = 0; leafID < bestBinarySplitModel.getClassLocalStructure().getNumLeaves(); leafID++) {
				ConstrainedBayesianNetwork candidateModel = bestBinarySplitModel.copyByValue();
				boolean success = false;

				List<List<List<String>>> binaryCombinationsOfValues = null;
				if(attribute.isOrdinal()) {
					binaryCombinationsOfValues = BRLUtils.partitionIntoOrderedBinarySetCombinations(attribute.getAttributeValues());
				} else {
					binaryCombinationsOfValues = BRLUtils.partitionIntoAllBinarySetCombinations(attribute.getAttributeValues());
				}

				for(List<List<String>> combination: binaryCombinationsOfValues) {
					List<String> listA = combination.get(0);
					List<String> listB = combination.get(1);

					success = candidateModel.applyBinarySplitOperatorOnLocalStructure(leafID, attribute, listA, listB);
				}

				if(success) specializedModels.add(candidateModel);
			}

			if((specializedModels.size() != 0) &&
					(specializedModels.peek().getNetworkPosteriorProbability() > bestBinarySplitModel.getNetworkPosteriorProbability())) {
				bestBinarySplitModel = specializedModels.poll();
				iterationHelped = true;
				numSplitsAttempted++;
			}
		}

		return bestBinarySplitModel;
	}

	private ConstrainedBayesianNetwork specializeWithGreedyBestFirstBinarySplit(
			ConstrainedBayesianNetwork model, DataAttribute attribute) {

		PriorityQueue<ConstrainedBayesianNetwork> specializedModels = 
				new PriorityQueue<ConstrainedBayesianNetwork>(params.getBeamWidth(), Collections.reverseOrder());
		ConstrainedBayesianNetwork bestBinarySplitModel = model.copyByValue();


		for(int leafID = 0; leafID < bestBinarySplitModel.getClassLocalStructure().getNumLeaves(); leafID++) {
			ConstrainedBayesianNetwork candidateModel = bestBinarySplitModel.copyByValue();
			boolean success = false;

			List<List<List<String>>> binaryCombinationsOfValues = null;
			if(attribute.isOrdinal()) {
				binaryCombinationsOfValues = BRLUtils.partitionIntoOrderedBinarySetCombinations(attribute.getAttributeValues());
			} else {
				binaryCombinationsOfValues = BRLUtils.partitionIntoAllBinarySetCombinations(attribute.getAttributeValues());
			}

			for(List<List<String>> combination: binaryCombinationsOfValues) {
				List<String> listA = combination.get(0);
				List<String> listB = combination.get(1);

				success = candidateModel.applyBinarySplitOperatorOnLocalStructure(leafID, attribute, listA, listB);
			}

			if(success) specializedModels.add(candidateModel);
		}

		if((specializedModels.size() != 0) &&
				(specializedModels.peek().getNetworkPosteriorProbability() > bestBinarySplitModel.getNetworkPosteriorProbability())) {
			bestBinarySplitModel = specializedModels.poll();
		}

		return bestBinarySplitModel;
	}

	private ConstrainedBayesianNetwork specializeWithExhaustiveGreedyBestFirstMerge(ConstrainedBayesianNetwork model) {

		PriorityQueue<ConstrainedBayesianNetwork> specializedModels = 
				new PriorityQueue<ConstrainedBayesianNetwork>(params.getBeamWidth(), Collections.reverseOrder());
		ConstrainedBayesianNetwork bestMergedModel = model.copyByValue();

		boolean iterationHelped = true;

		while(iterationHelped) {
			iterationHelped = false;
			for(int iLeaf = 0; iLeaf < (bestMergedModel.getClassLocalStructure().getNumLeaves()-1); iLeaf++) {
				for(int jLeaf = (iLeaf+1); jLeaf < bestMergedModel.getClassLocalStructure().getNumLeaves(); jLeaf++) {
					ConstrainedBayesianNetwork candidateModel = bestMergedModel.copyByValue();
					boolean success = candidateModel.applyMergeOperatorOnLocalStructure(iLeaf, jLeaf);
					if(success) specializedModels.add(candidateModel);
				}
			}

			if((specializedModels.size() != 0) && 
					(specializedModels.peek().getNetworkPosteriorProbability() > bestMergedModel.getNetworkPosteriorProbability())) {
				bestMergedModel = specializedModels.poll();
				iterationHelped = true;
			}
		}

		return bestMergedModel;
	}

	private ConstrainedBayesianNetwork specializeWithGreedyBestFirstMerge(ConstrainedBayesianNetwork model) {

		PriorityQueue<ConstrainedBayesianNetwork> specializedModels = 
				new PriorityQueue<ConstrainedBayesianNetwork>(params.getBeamWidth(), Collections.reverseOrder());
		ConstrainedBayesianNetwork bestMergedModel = model.copyByValue();


		for(int iLeaf = 0; iLeaf < (bestMergedModel.getClassLocalStructure().getNumLeaves()-1); iLeaf++) {
			for(int jLeaf = (iLeaf+1); jLeaf < bestMergedModel.getClassLocalStructure().getNumLeaves(); jLeaf++) {
				ConstrainedBayesianNetwork candidateModel = bestMergedModel.copyByValue();
				boolean success = candidateModel.applyMergeOperatorOnLocalStructure(iLeaf, jLeaf);
				if(success) specializedModels.add(candidateModel);
			}
		}

		if((specializedModels.size() != 0) && 
				(specializedModels.peek().getNetworkPosteriorProbability() > bestMergedModel.getNetworkPosteriorProbability())) {
			bestMergedModel = specializedModels.poll();
		}

		return bestMergedModel;
	}

	private FoldGenerator generateBootstrapSamples(int numBaseModels) throws Exception {
		FoldGenerator foldGenerator = new BootstrapFoldGenerator(numBaseModels);
		//((BootstrapFoldGenerator) foldGenerator).setSeed(1);
		foldGenerator.generateFolds(dataModel);

		return foldGenerator;
	}

	public List<ConstrainedBayesianNetwork> runDeliberatelyBadBaggingSearch() throws Exception {
		List<ConstrainedBayesianNetwork> baseModels = new ArrayList<ConstrainedBayesianNetwork>();

		for(int h=0; h<params.getNumBaseModels(); h++) {

			BRLSearch hBrlSearch = new BRLSearch(params, dataModel);
			ConstrainedBayesianNetwork hCbn = null;
			if(h==0) {
				hCbn = hBrlSearch.runGreedyBestFirstSearchWithComplexityPenalty();
			} else {
				hCbn = ConstrainedBayesianNetwork.createRandomModel(dataModel, params);
			}


			hCbn.setModelWeight(1.0/params.getNumBaseModels());
			baseModels.add(hCbn);
		}

		return baseModels;
	}

	public List<ConstrainedBayesianNetwork> runGreedyBaggingSearch() throws Exception {
		List<ConstrainedBayesianNetwork> baseModels = new ArrayList<ConstrainedBayesianNetwork>();
		FoldGenerator foldGenerator = generateBootstrapSamples(params.getNumBaseModels());

		for(int h=0; h<params.getNumBaseModels(); h++) {
			DataModel bootstrapDataModel = foldGenerator.getFold(h).get(0);

			BRLSearch hBrlSearch = new BRLSearch(params, bootstrapDataModel);
			ConstrainedBayesianNetwork hCbn = hBrlSearch.runGreedyBestFirstSearchWithComplexityPenalty();

			hCbn.setModelWeight(1.0/params.getNumBaseModels());
			baseModels.add(hCbn);
		}

		return baseModels;
	}

	/**
	 * SAMME algorithm for multi-class AdaBoost (Zhu et al., 2006).
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<ConstrainedBayesianNetwork> runGreedyBoostingSearch() throws Exception {
		List<ConstrainedBayesianNetwork> baseModels = new ArrayList<ConstrainedBayesianNetwork>();

		DataModel boostedDataModel = initInstanceWeights(dataModel);

		for(int h=0; h<params.getNumBaseModels(); h++) {
			BRLSearch hBrlSearch = new BRLSearch(params, boostedDataModel);
			ConstrainedBayesianNetwork hCbn = hBrlSearch.runGreedyBestFirstSearchWithComplexityPenalty();

			RuleModel hRuleModel = translateIntoRuleModel(hCbn, boostedDataModel);
			double boost_err = computeBoostingError(hRuleModel, boostedDataModel);
			double boost_alpha = computeBoostingAlpha(boost_err, boostedDataModel);

			hCbn.setModelWeight(boost_alpha);
			baseModels.add(hCbn);

			if(boost_err == 0.0 || boost_err == 1.0) { 
				// weighted boosting cannot help a perfectly incorrect (alpha=Inf)
				// or perfectly correct (alpha=-Inf) classifier.
				break;
			}

			boostedDataModel = updateInstanceWeights(boost_alpha, hRuleModel, boostedDataModel);
		}
		//System.exit(0);
		normalizeModelWeights(baseModels);
		dataModel = resetInstanceWeights(dataModel);

		return baseModels;
	}

	private DataModel initInstanceWeights(DataModel data) {
		List<Double> instanceWeights = new LinkedList<Double>();
		//uniform weights
		for(int i=0; i<data.getNumRows(); i++) {
			instanceWeights.add(1.0/(double)data.getNumRows());
		}

		data.setInstanceWeights(instanceWeights);

		return data;
	}
	
	private DataModel resetInstanceWeights(DataModel data) {
		List<Double> instanceWeights = new LinkedList<Double>();
		//uniform weights
		for(int i=0; i<data.getNumRows(); i++) {
			instanceWeights.add(1.0);
		}

		data.setInstanceWeights(instanceWeights);

		return data;
	}

	private double computeBoostingError(RuleModel ruleModel, DataModel data) {
		double error = 0.0;
		
		List<Double> currentInstanceWeights = data.getInstanceWeights();
		/*
		double sum = computeSum(currentInstanceWeights);
		List<Double> originalInstanceWeights = new LinkedList<Double>();
		 

		//uniform weights
		for(int i=0; i<data.getNumRows(); i++) {
			originalInstanceWeights.add(1.0/(double)data.getNumRows());
		}
		*/
		//double sum = computeSum(originalInstanceWeights);
		double sum = computeSum(currentInstanceWeights);

		for(int i=0; i<data.getNumRows(); i++) {
			//error += originalInstanceWeights.get(i)*instanceIsMisclassified(ruleModel, data, i)/sum;
			error += currentInstanceWeights.get(i)*instanceIsMisclassified(ruleModel, data, i)/sum;
		}

		return error;
	}

	private double computeSum(List<Double> items) {
		double sum = 0.0;

		for(double item: items) {
			sum += item;
		}

		return sum;
	}

	private double instanceIsMisclassified(RuleModel ruleModel, DataModel data, int rowIndex) {
		double instanceIsMisclassified = 0.0;

		String pred = ruleModel.classifyInstance(data, rowIndex);
		if(!pred.equalsIgnoreCase(data.getClassLabelForRow(rowIndex))) {
			instanceIsMisclassified = 1.0; 
		}

		return instanceIsMisclassified;
	}

	private RuleModel translateIntoRuleModel(ConstrainedBayesianNetwork brlModel, DataModel data) {
		RuleModel translatedRuleModel = new RuleModel();

		List<LeafNode> leaves = brlModel.getClassLocalStructure().getLeafNodes();
		for(LeafNode leaf: leaves) {
			StringBuilder leafRuleStr = new StringBuilder();
			String[] leafLhsRhs = leaf.toString().split(" -> ");
			leafRuleStr.append(leafLhsRhs[0]).append(" -> ");

			Map<String, Double> parameterPosteriorDistribution = leaf.getParameterPosteriorDistribution();
			String maxClassLabel = "";
			double maxProbability = -1.0;
			for(String classValue: parameterPosteriorDistribution.keySet()) {
				if(parameterPosteriorDistribution.get(classValue) > maxProbability) {
					maxProbability = parameterPosteriorDistribution.get(classValue);
					maxClassLabel = classValue;
				}
			}
			leafRuleStr.append("(").append(leaf.getDataAttribute().getAttributeName());
			leafRuleStr.append("=").append(maxClassLabel).append(")");
			Rule leafRule = GeneralRule.parseString(leafRuleStr.toString());
			leafRule.setProbabilities(parameterPosteriorDistribution);
			leafRule.computeRuleStatistics(data);
			translatedRuleModel.addRule(leafRule);
		}

		return translatedRuleModel;
	}

	private double computeBoostingAlpha(double error, DataModel data) {
		//smoothing from "Improved Boosting Algorithms..." by Schapire & Singer, 1999 section 4.2.
		double epsilon = 1.0/data.getNumRows();
		return Math.log((1.0 - error + epsilon)/(error + epsilon)) + Math.log(data.getClassLabels().size() - 1.0); 
	}

	private DataModel updateInstanceWeights(double alpha, RuleModel ruleModel, DataModel data) {
		List<Double> updatedInstanceWeights = data.getInstanceWeights();

		for(int i=0; i<data.getNumRows(); i++) {
			double update = updatedInstanceWeights.get(i)*Math.pow(Math.E, alpha*instanceIsMisclassified(ruleModel, data, i));
			updatedInstanceWeights.add(i, update);
		}

		data.setInstanceWeights(normalizeWeights(updatedInstanceWeights, data));

		return data;
	}

	private List<Double> normalizeWeights(List<Double> weights, DataModel data) {
		List<Double> normalizedWeights = new LinkedList<Double>();

		double sum = computeSum(weights);

		for(int i=0; i<weights.size(); i++) {
			double normWeight = (weights.get(i)/sum)*data.size();
			normalizedWeights.add(normWeight);
		}

		return normalizedWeights;
	}

	private void normalizeModelWeights(List<ConstrainedBayesianNetwork> models) {
		double sumOfModelWeights = 0.0;
		for(ConstrainedBayesianNetwork model: models) sumOfModelWeights += model.getModelWeight();

		for(ConstrainedBayesianNetwork model: models) model.setModelWeight(model.getModelWeight()/sumOfModelWeights);
	}


	private boolean onlyGenerateCompleteTrees;
	private boolean doCompleteSplit;
	private boolean doBinarySplit;
	private boolean doMerge;

	private List<DataAttribute> attributes;

	private BRLSearchParameters params;
	private DataModel dataModel;
}
