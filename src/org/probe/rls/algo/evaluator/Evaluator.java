package org.probe.rls.algo.evaluator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.probe.rls.algo.evaluator.roc.ROCCurve;
import org.probe.rls.data.DataModel;
import org.probe.rls.models.Classifier;
import org.probe.rls.models.Prediction;


public class Evaluator {

	public Evaluator(DataModel trainData) {
		this.classLabels = trainData.getClassLabels();
		this.confusionMatrix = new double[classLabels.size()][classLabels.size()];
	}

	public void evaluateClassifierOnTest(Classifier classifier, DataModel testData) {
		List<Prediction> classifierPredictions = getPredictions(classifier, testData);
		confusionMatrix = getConfusionMatrix(classifierPredictions, testData);

		// predictive performance
		accuracy = computeAccuracy(confusionMatrix);
		precision = computePrecision(confusionMatrix);
		recall = computeRecall(confusionMatrix);
		fMeasure = computeFMeasure(confusionMatrix);
		auc = computeAuroc(classifierPredictions, testData);
		prg = computeAuprg(classifierPredictions, testData);
		modelScore = computeModelScore(classifier);

		// calibration performance
		brierScore = computeBrierScore(classifierPredictions, testData);
		ece = computeEce(classifierPredictions, testData, 10);
		mce = computeMce(classifierPredictions, testData, 10);

		// semantic complexity performance
		nRules = computeNumRules(classifier);
		nVars = computeNumVariables(classifier);
	}

	private List<Prediction> getPredictions(Classifier classifier, DataModel testData) {
		List<Prediction> classifierPredictions = new LinkedList<Prediction>();

		for(int i=0; i<testData.getNumRows(); i++) {
			classifierPredictions.add(classifier.predictInstance(testData, i));
		}

		return classifierPredictions;
	}

	/** Deprecated for a more efficient namesake method
	public void evaluateClassifierOnTest(Classifier classifier, DataModel testData) {
		confusionMatrix = getConfusionMatrix(classifier, testData);

		// predictive performance
		accuracy = computeAccuracy(confusionMatrix);
		precision = computePrecision(confusionMatrix);
		recall = computeRecall(confusionMatrix);
		fMeasure = computeFMeasure(confusionMatrix);
		auc = computeAuroc(classifier, testData);

		// calibration performance
		brierScore = computeBrierScore(classifier, testData);
	}
	 */

	public void evaluateClassifierOnTestFolds(List<Classifier> classifierList, List<DataModel> testDataList) {
		List<double[][]> foldConfusionMatrices = getConfusionMatrixOnTestFolds(classifierList, testDataList);
		confusionMatrix = aggregateConfusionMatricesAcrossFolds(foldConfusionMatrices);

		// predictive performance
		accuracy = computeAccuracy(confusionMatrix);
		precision = computePrecisionOnTestFolds(foldConfusionMatrices);
		recall = computeRecallOnTestFolds(foldConfusionMatrices);
		fMeasure = computeFMeasureOnTestFolds(foldConfusionMatrices);
		auc = computeAurocOnTestFolds(classifierList, testDataList);

		// calibration performance
		brierScore = computeBrierScoreOnTestFolds(classifierList, testDataList);
	}

	public void aggregateTestEvaluators(List<Evaluator> foldsEvaluator) {
		List<double[][]> foldConfusionMatrices = new LinkedList<double[][]>();
		for (Evaluator foldEvaluator : foldsEvaluator) {
			foldConfusionMatrices.add(foldEvaluator.getConfusionMatrix());
		}
		confusionMatrix = aggregateConfusionMatricesAcrossFolds(foldConfusionMatrices);

		// predictive performance
		accuracy = computeAccuracy(confusionMatrix);
		precision = computePrecisionOnTestFolds(foldConfusionMatrices);
		recall = computeRecallOnTestFolds(foldConfusionMatrices);
		fMeasure = computeFMeasureOnTestFolds(foldConfusionMatrices);
		auc = computeAurocOnTestEvaluators(foldsEvaluator);
		prg = computeAuprgOnTestEvaluators(foldsEvaluator);
		modelScore = computeModelScoreOnTestEvaluators(foldsEvaluator);

		// calibration performance
		brierScore = computeBrierScoreOnTestEvaluators(foldsEvaluator);
		ece = computeEceOnTestEvaluators(foldsEvaluator);
		mce = computeMceOnTestEvaluators(foldsEvaluator);
		
		// semantic complexity performance
		nRules = computeNumRulesOnTestEvaluators(foldsEvaluator);
		nVars = computeNumVariablesOnTestEvaluators(foldsEvaluator);
	}

	private double[][] getConfusionMatrix(Classifier classifier, DataModel testData) {
		double[][] testConfusionMatrix = new double[classLabels.size()][classLabels.size()];

		for(int i=0; i<testData.getNumRows(); i++) {
			String actualLabel = testData.getClassLabelForRow(i);
			String predictedLabel = classifier.classifyInstance(testData, i);
			testConfusionMatrix[classLabels.indexOf(predictedLabel)][classLabels.indexOf(actualLabel)] += 1.0;
		}

		return testConfusionMatrix;
	}

	private double[][] getConfusionMatrix(List<Prediction> classifierPredictions, DataModel testData) {
		double[][] testConfusionMatrix = new double[classLabels.size()][classLabels.size()];

		for(int i=0; i<testData.getNumRows(); i++) {
			String actualLabel = testData.getClassLabelForRow(i);
			String predictedLabel = classifierPredictions.get(i).getPredictedClass();
			testConfusionMatrix[classLabels.indexOf(predictedLabel)][classLabels.indexOf(actualLabel)] += 1.0;
		}

		return testConfusionMatrix;
	}

	private List<double[][]> getConfusionMatrixOnTestFolds(List<Classifier> classifierList, List<DataModel> testDataList) {
		List<double[][]> foldConfusionMatrices = new ArrayList<double[][]>();

		for(int fold=0; fold<testDataList.size(); fold++) {
			double[][] foldConfusionMatrix = getConfusionMatrix(classifierList.get(fold), testDataList.get(fold));
			foldConfusionMatrices.add(foldConfusionMatrix);
		}

		return foldConfusionMatrices;
	}

	private double[][] aggregateConfusionMatricesAcrossFolds(List<double[][]> confusionMatrices) {
		double[][] aggregateConfusionMatrix = new double[classLabels.size()][classLabels.size()];

		for(double[][] foldConfusionMatrix: confusionMatrices) {
			for(int row=0; row<foldConfusionMatrix.length; row++) {
				for(int col=0; col<foldConfusionMatrix[row].length; col++) {
					aggregateConfusionMatrix[row][col] += foldConfusionMatrix[row][col];
				}
			}
		}

		return aggregateConfusionMatrix;
	}

	private double computeAccuracy(double[][] confusionMatrix) {
		double numerator = 0.0;
		double denominator = 0.0;

		for(int row=0; row<confusionMatrix.length; row++) {
			for(int col=0; col<confusionMatrix[row].length; col++) {
				if(row==col) {
					numerator += confusionMatrix[row][col];
				}
				denominator += confusionMatrix[row][col];
			}
		}

		return (numerator/denominator)*100.0;
	}

	private double[] computePrecision(double[][] confusionMatrix) {
		double[] precision = new double[classLabels.size()];

		for(String positiveClass: classLabels) {
			double TP = getTruePositive(confusionMatrix, positiveClass);
			double FP = getFalsePositive(confusionMatrix, positiveClass);
			if(((int)TP == 0) && ((int)FP == 0)) {
				precision[classLabels.indexOf(positiveClass)] = 0.0;
			} else {
				precision[classLabels.indexOf(positiveClass)] = TP/(TP+FP);
			}
		}

		return precision;
	}

	private double[] computePrecisionOnTestFolds(List<double[][]> confusionMatrices) {
		double[] precision = new double[classLabels.size()];

		for(double[][] confusionMatrix: confusionMatrices) {
			double[] foldPrecision = computePrecision(confusionMatrix);
			for(int i=0; i<foldPrecision.length; i++) {
				precision[i] += foldPrecision[i]/(double) confusionMatrices.size();
			}
		}

		return precision;
	}

	private double[] computeRecall(double[][] confusionMatrix) {
		double[] recall = new double[classLabels.size()];

		for(String positiveClass: classLabels) {
			double TP = getTruePositive(confusionMatrix, positiveClass);
			double FN = getFalseNegative(confusionMatrix, positiveClass);
			if(((int)TP == 0) && ((int)FN == 0)) {
				recall[classLabels.indexOf(positiveClass)] = 0.0;
			} else {
				recall[classLabels.indexOf(positiveClass)] = TP/(TP+FN);
			}
		}

		return recall;
	}

	private double[] computeRecallOnTestFolds(List<double[][]> confusionMatrices) {
		double[] recall = new double[classLabels.size()];

		for(double[][] confusionMatrix: confusionMatrices) {
			double[] foldRecall = computeRecall(confusionMatrix);
			for(int i=0; i<foldRecall.length; i++) {
				recall[i] += foldRecall[i]/(double) confusionMatrices.size();
			}
		}

		return recall;
	}

	private double[] computeFMeasure(double[][] confusionMatrix) {
		double[] fMeasure = new double[classLabels.size()];

		for(String positiveClass: classLabels) {
			double TP = getTruePositive(confusionMatrix, positiveClass);
			double FP = getFalsePositive(confusionMatrix, positiveClass);
			double FN = getFalseNegative(confusionMatrix, positiveClass);
			if(((int)TP == 0) && ((int)FP == 0) && ((int)FN == 0)) {
				fMeasure[classLabels.indexOf(positiveClass)] = 0.0;
			} else {
				fMeasure[classLabels.indexOf(positiveClass)] = (2.0*TP)/((2.0*TP)+FP+FN);
			}
		}

		return fMeasure;
	}

	private double[] computeFMeasureOnTestFolds(List<double[][]> confusionMatrices) {
		double[] fMeasure = new double[classLabels.size()];

		for(String positiveClass: classLabels) {
			double TP = 0.0;
			double FP = 0.0;
			double FN = 0.0;
			for(double[][] confusionMatrix: confusionMatrices) {
				TP += getTruePositive(confusionMatrix, positiveClass);
				FP += getFalsePositive(confusionMatrix, positiveClass);
				FN += getFalseNegative(confusionMatrix, positiveClass);
			}

			if(((int)TP == 0) && ((int)FP == 0) && ((int)FN == 0)) {
				fMeasure[classLabels.indexOf(positiveClass)] = 0.0;
			} else {
				fMeasure[classLabels.indexOf(positiveClass)] = (2.0*TP)/((2.0*TP)+FP+FN);
			}
		}

		return fMeasure;
	}

	private double[] computeAuroc(Classifier classifier, DataModel testData) {
		double[] aurocs = new double[classLabels.size()];
		List<String> actual = new LinkedList<String>();
		Map<String, List<Double>> predicted = new TreeMap<String, List<Double>>();

		for(String classLabel: classLabels) {
			predicted.put(classLabel, new LinkedList<Double>());
		}

		for(int i=0; i<testData.getNumRows(); i++) {
			actual.add(testData.getClassLabelForRow(i));
			Map<String, Double> instanceProbabilities = classifier.getProbabilities(testData, i);
			for(String classLabel: classLabels) {
				predicted.get(classLabel).add(instanceProbabilities.get(classLabel));
			}
		}

		for(String classLabel: classLabels) {
			ROCCurve classROC = new ROCCurve(classLabel, actual, predicted.get(classLabel));
			aurocs[classLabels.indexOf(classLabel)] = classROC.getAUROC();
		}

		return aurocs;
	}

	private double[] computeAuroc(List<Prediction> classifierPredictions, DataModel testData) {
		double[] aurocs = new double[classLabels.size()];
		List<String> actual = new LinkedList<String>();
		Map<String, List<Double>> predicted = new TreeMap<String, List<Double>>();

		for(String classLabel: classLabels) {
			predicted.put(classLabel, new LinkedList<Double>());
		}

		for(int i=0; i<testData.getNumRows(); i++) {
			actual.add(testData.getClassLabelForRow(i));
			Map<String, Double> instanceProbabilities = classifierPredictions.get(i).getPredictedProbabilities();
			for(String classLabel: classLabels) {
				predicted.get(classLabel).add(instanceProbabilities.get(classLabel));
			}
		}

		for(String classLabel: classLabels) {
			ROCCurve classROC = new ROCCurve(classLabel, actual, predicted.get(classLabel));
			aurocs[classLabels.indexOf(classLabel)] = classROC.getAUROC();
		}

		return aurocs;
	}

	private double[] computeAuprg(List<Prediction> classifierPredictions, DataModel testData) {
		double[] auprgs = new double[classLabels.size()];
		List<String> actual = new LinkedList<String>();
		Map<String, List<Double>> predicted = new TreeMap<String, List<Double>>();

		for(String classLabel: classLabels) {
			predicted.put(classLabel, new LinkedList<Double>());
		}

		for(int i=0; i<testData.getNumRows(); i++) {
			actual.add(testData.getClassLabelForRow(i));
			Map<String, Double> instanceProbabilities = classifierPredictions.get(i).getPredictedProbabilities();
			for(String classLabel: classLabels) {
				predicted.get(classLabel).add(instanceProbabilities.get(classLabel));
			}
		}

		for(String classLabel: classLabels) {
			auprgs[classLabels.indexOf(classLabel)] = computePrgFromPythonScript(
					labelsToIntListString(actual, classLabel),
					doubleListToString(predicted.get(classLabel)));
		}

		return auprgs;
	}

	private String labelsToIntListString(List<String> actualLabel, String positiveLabel) {
		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < actualLabel.size(); i++) {
			if(actualLabel.get(i).equalsIgnoreCase(positiveLabel)) {
				sb.append("1");
			} else {
				sb.append("0");
			}

			if(i != (actualLabel.size() - 1)) {
				sb.append(",");
			}
		}

		return sb.toString();
	}

	private String doubleListToString(List<Double> doubleList) {
		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < doubleList.size(); i++) {
			sb.append(doubleList.get(i));
			if(i != (doubleList.size() - 1)) {
				sb.append(",");
			}
		}

		return sb.toString();
	}

	private Double computePrgFromPythonScript(String trueLabels, String predictedProbabilities) {
		String pythonScriptPath = "./python_scripts/prg.py";
		String[] cmd = new String[4];
		cmd[0] = "python";
		cmd[1] = pythonScriptPath;
		cmd[2] = trueLabels; 
		cmd[3] = predictedProbabilities;

		// create runtime to execute external command
		Runtime rt = Runtime.getRuntime();
		Process pr = null;
		try {
			pr = rt.exec(cmd);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// retrieve output from python script
		BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = "";
		Double auprg = 0.0;
		try {
			while((line = br.readLine()) != null) {
				auprg = Double.parseDouble(line);
			}
		} catch (NumberFormatException | IOException e) {
			auprg = 0.0;
		}
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return auprg;
	}

	private double[] computeAurocOnTestFolds(List<Classifier> classifierList, List<DataModel> testDataList) {
		double[] aurocs = new double[classLabels.size()];

		for(int fold=0; fold<classifierList.size(); fold++) {
			double[] foldAurocs = computeAuroc(classifierList.get(fold), testDataList.get(fold));
			for(int j=0; j<foldAurocs.length; j++) {
				aurocs[j] += foldAurocs[j]/((double) classifierList.size());
			}
		}

		return aurocs;
	}

	private double[] computeAurocOnTestEvaluators(List<Evaluator> foldsEvaluator) {
		double[] aurocs = new double[classLabels.size()];

		for(Evaluator foldEvaluator: foldsEvaluator) {
			double[] foldAurocs = foldEvaluator.getAuc();
			for(int j=0; j<foldAurocs.length; j++) {
				aurocs[j] += foldAurocs[j]/((double) foldsEvaluator.size());
			}
		}

		return aurocs;
	}
	
	private double computeNumVariablesOnTestEvaluators(List<Evaluator> foldsEvaluator) {
		double avgNumVars = 0.0;

		for(Evaluator foldEvaluator: foldsEvaluator) {
			avgNumVars += foldEvaluator.getNumVariables()/((double) foldsEvaluator.size());
		}

		return avgNumVars;
	}

	private double computeNumRulesOnTestEvaluators(List<Evaluator> foldsEvaluator) {
		double avgNumRules = 0.0;

		for(Evaluator foldEvaluator: foldsEvaluator) {
			avgNumRules += foldEvaluator.getNumRules()/((double) foldsEvaluator.size());
		}

		return avgNumRules;
	}

	private double[] computeMceOnTestEvaluators(List<Evaluator> foldsEvaluator) {
		double[] mces = new double[classLabels.size()];

		for(Evaluator foldEvaluator: foldsEvaluator) {
			double[] foldMces = foldEvaluator.getMce();
			for(int j=0; j<foldMces.length; j++) {
				mces[j] += foldMces[j]/((double) foldsEvaluator.size());
			}
		}

		return mces;
	}

	private double[] computeEceOnTestEvaluators(List<Evaluator> foldsEvaluator) {
		double[] eces = new double[classLabels.size()];

		for(Evaluator foldEvaluator: foldsEvaluator) {
			double[] foldEces = foldEvaluator.getEce();
			for(int j=0; j<foldEces.length; j++) {
				eces[j] += foldEces[j]/((double) foldsEvaluator.size());
			}
		}

		return eces;
	}

	private double computeModelScoreOnTestEvaluators(List<Evaluator> foldsEvaluator) {
		double avgModelScore = 0.0;

		for(Evaluator foldEvaluator: foldsEvaluator) {
			avgModelScore += foldEvaluator.getModelScore()/((double) foldsEvaluator.size());
		}

		return avgModelScore;
	}

	private double[] computeAuprgOnTestEvaluators(List<Evaluator> foldsEvaluator) {
		double[] auprgs = new double[classLabels.size()];

		for(Evaluator foldEvaluator: foldsEvaluator) {
			double[] foldAuprgs = foldEvaluator.getPrg();
			for(int j=0; j<foldAuprgs.length; j++) {
				auprgs[j] += foldAuprgs[j]/((double) foldsEvaluator.size());
			}
		}

		return auprgs;
	}

	private double computeModelScore(Classifier classifier) {
		return classifier.getModelScore();
	}

	private double[] computeBrierScore(Classifier classifier, DataModel testData) {
		double[] brierScores = new double[classLabels.size()];

		for(int i=0; i<testData.getNumRows(); i++) {
			String actualLabel = testData.getClassLabelForRow(i);
			Map<String, Double> instanceProbabilities = classifier.getProbabilities(testData, i);

			for(String classLabel: classLabels) {
				double actualLabelAsDouble = (actualLabel.equalsIgnoreCase(classLabel)) ? 1.0 : 0.0;
				double predictedProbability = instanceProbabilities.get(classLabel);

				brierScores[classLabels.indexOf(classLabel)] += 
						Math.pow((predictedProbability - actualLabelAsDouble), 2.0)/((double) testData.getNumRows());
			}
		}

		return brierScores;
	}

	private double[] computeBrierScore(List<Prediction> classifierPredictions, DataModel testData) {
		double[] brierScores = new double[classLabels.size()];

		for (int i = 0; i < testData.getNumRows(); i++) {
			String actualLabel = testData.getClassLabelForRow(i);
			Map<String, Double> instanceProbabilities = classifierPredictions.get(i).getPredictedProbabilities();

			for (String classLabel : classLabels) {
				double actualLabelAsDouble = (actualLabel.equalsIgnoreCase(classLabel)) ? 1.0 : 0.0;
				double predictedProbability = instanceProbabilities.get(classLabel);

				brierScores[classLabels.indexOf(classLabel)] += Math.pow((predictedProbability - actualLabelAsDouble),
						2.0) / ((double) testData.getNumRows());
			}
		}

		return brierScores;
	}

	private double[] computeEce(List<Prediction> classifierPredictions, DataModel testData, int nBins) {
		double[] eces = new double[classLabels.size()];

		int binSize = (int) Math.floor(classifierPredictions.size()/nBins);
		if(binSize == 0) binSize = 1;

		double nItemsInBin = 0.0;
		double totalItems = classifierPredictions.size();
		double[] nPositiveItemsInBin = new double[classLabels.size()]; 
		double[] sumPredProbsInBin = new double[classLabels.size()];
		
		for (int i = 0; i < testData.getNumRows(); i++) {
			String actualLabel = testData.getClassLabelForRow(i);
			Map<String, Double> instanceProbabilities = classifierPredictions.get(i).getPredictedProbabilities();

			nItemsInBin += 1.0;
			for (String classLabel : classLabels) {
				nPositiveItemsInBin[classLabels.indexOf(classLabel)] += (actualLabel.equalsIgnoreCase(classLabel)) ? 1.0 : 0.0;
				sumPredProbsInBin[classLabels.indexOf(classLabel)] += instanceProbabilities.get(classLabel);
			}
			
			if(((i+1)%binSize == 0) || (i == testData.getNumRows()-1)) {
				double weight =  nItemsInBin/totalItems;
				double[] observed_bin = new double[classLabels.size()];
				double[] expected_bin = new double[classLabels.size()];

				for (String classLabel : classLabels) {
					observed_bin[classLabels.indexOf(classLabel)] = 
							nPositiveItemsInBin[classLabels.indexOf(classLabel)]/nItemsInBin;
					expected_bin[classLabels.indexOf(classLabel)] = 
							sumPredProbsInBin[classLabels.indexOf(classLabel)]/nItemsInBin;

					eces[classLabels.indexOf(classLabel)] = weight*Math.abs( 
									observed_bin[classLabels.indexOf(classLabel)] - 
									expected_bin[classLabels.indexOf(classLabel)]
									);
					
					//reset
					nPositiveItemsInBin[classLabels.indexOf(classLabel)] = 0.0;
					sumPredProbsInBin[classLabels.indexOf(classLabel)] = 0.0;
				}
				//reset
				nItemsInBin = 0.0;
			}
		}

		return eces;
	}

	private double[] computeMce(List<Prediction> classifierPredictions, DataModel testData, int nBins) {
		double[] mces = new double[classLabels.size()];

		int binSize = (int) Math.floor(classifierPredictions.size()/nBins);
		if(binSize == 0) binSize = 1;
		
		double nItemsInBin = 0.0;
		double[] nPositiveItemsInBin = new double[classLabels.size()]; 
		double[] sumPredProbsInBin = new double[classLabels.size()];
		
		for (int i = 0; i < testData.getNumRows(); i++) {
			String actualLabel = testData.getClassLabelForRow(i);
			Map<String, Double> instanceProbabilities = classifierPredictions.get(i).getPredictedProbabilities();

			nItemsInBin += 1.0;
			for (String classLabel : classLabels) {
				nPositiveItemsInBin[classLabels.indexOf(classLabel)] += (actualLabel.equalsIgnoreCase(classLabel)) ? 1.0 : 0.0;
				sumPredProbsInBin[classLabels.indexOf(classLabel)] += instanceProbabilities.get(classLabel);
			}
			
			if(((i+1)%binSize == 0) || (i == testData.getNumRows()-1)) {
				double[] observed_bin = new double[classLabels.size()];
				double[] expected_bin = new double[classLabels.size()];

				for (String classLabel : classLabels) {
					observed_bin[classLabels.indexOf(classLabel)] = 
							nPositiveItemsInBin[classLabels.indexOf(classLabel)]/nItemsInBin;
					expected_bin[classLabels.indexOf(classLabel)] = 
							sumPredProbsInBin[classLabels.indexOf(classLabel)]/nItemsInBin;
					
					double binCalibError = Math.abs( 
							observed_bin[classLabels.indexOf(classLabel)] - 
							expected_bin[classLabels.indexOf(classLabel)]
							);
					if(binCalibError > mces[classLabels.indexOf(classLabel)]) {
						mces[classLabels.indexOf(classLabel)] = binCalibError;
					}
					
					//reset
					nPositiveItemsInBin[classLabels.indexOf(classLabel)] = 0.0;
					sumPredProbsInBin[classLabels.indexOf(classLabel)] = 0.0;
				}
				//reset
				nItemsInBin = 0.0;
			}
		}

		return mces;
	}

	private double[] computeBrierScoreOnTestFolds(List<Classifier> classifierList, List<DataModel> testDataList) {
		double[] brierScores = new double[classLabels.size()];

		for(int fold=0; fold<classifierList.size(); fold++) {
			double[] foldBrierScores = computeBrierScore(classifierList.get(fold), testDataList.get(fold));
			for(int j=0; j<foldBrierScores.length; j++) {
				brierScores[j] += foldBrierScores[j]/((double) classifierList.size());
			}
		}

		return brierScores;
	}

	private double[] computeBrierScoreOnTestEvaluators(List<Evaluator> foldsEvaluator) {
		double[] brierScores = new double[classLabels.size()];

		for(Evaluator foldEvaluator: foldsEvaluator) {
			double[] foldBrierScores = foldEvaluator.getBrierScore();
			for(int j=0; j<foldBrierScores.length; j++) {
				brierScores[j] += foldBrierScores[j]/((double) foldsEvaluator.size());
			}
		}

		return brierScores;
	}

	private double computeNumRules(Classifier classifier) {
		return classifier.getNumRules();
	}

	private double computeNumVariables(Classifier classifier) {
		return classifier.getNumVariables();
	}

	private double getTruePositive(double[][] confusionMatrix, String positiveClass) {
		int positiveIndex = classLabels.indexOf(positiveClass);
		return confusionMatrix[positiveIndex][positiveIndex];
	}

	public double getFalsePositive(double[][] confusionMatrix, String positiveClass) {
		int positiveIndex = classLabels.indexOf(positiveClass);
		double sumPredictedPositive = 0.0;

		for(int i=0; i<confusionMatrix[positiveIndex].length; i++) {
			sumPredictedPositive += confusionMatrix[positiveIndex][i];
		}

		return sumPredictedPositive - getTruePositive(confusionMatrix, positiveClass);
	}

	private double getFalseNegative(double[][] confusionMatrix, String positiveClass) {
		int positiveIndex = classLabels.indexOf(positiveClass);
		double sumActualPositive = 0.0;

		for(int i=0; i<confusionMatrix.length; i++) {
			sumActualPositive += confusionMatrix[i][positiveIndex];
		}

		return sumActualPositive - getTruePositive(confusionMatrix, positiveClass);
	}

	@SuppressWarnings("unused")
	public double getTrueNegative(double[][] confusionMatrix, String positiveClass) {
		double sum = 0.0;

		for(int i=0; i<confusionMatrix.length; i++) {
			for(int j=0; j<confusionMatrix[i].length; j++) {
				sum += confusionMatrix[i][j];
			}
		}

		return sum - getTruePositive(confusionMatrix, positiveClass)
				- getFalsePositive(confusionMatrix, positiveClass)
				- getFalseNegative(confusionMatrix, positiveClass);
	}

	public double[][] getConfusionMatrix() {
		return confusionMatrix;
	}

	public void setConfusionMatrix(double[][] confusionMatrix) {
		this.confusionMatrix = confusionMatrix;
	}

	public double getAccuracy() {
		return accuracy;
	}

	public double getPrecision(String positiveClass) {
		return precision[classLabels.indexOf(positiveClass)];
	}

	public double getRecall(String positiveClass) {
		return recall[classLabels.indexOf(positiveClass)];
	}

	public double getFMeasure(String positiveClass) {
		return fMeasure[classLabels.indexOf(positiveClass)];
	}

	public double getAUC(String positiveClass) {
		return auc[classLabels.indexOf(positiveClass)];
	}

	public double getBrierScore(String positiveClass) {
		return brierScore[classLabels.indexOf(positiveClass)];
	}

	public double[] getAuc() {
		return auc;
	}

	public void setAuc(double[] auc) {
		this.auc = auc;
	}
	
	public double getPrg(String positiveClass) {
		return prg[classLabels.indexOf(positiveClass)];
	}
	
	public double[] getPrg() {
		return prg;
	}

	public double[] getBrierScore() {
		return brierScore;
	}

	public void setBrierScore(double[] brierScore) {
		this.brierScore = brierScore;
	}
	
	public double getEce(String positiveClass) {
		return ece[classLabels.indexOf(positiveClass)];
	}
	
	public double[] getEce() {
		return ece;
	}
	
	public double[] getMce() {
		return mce;
	}
	
	public double getMce(String positiveClass) {
		return mce[classLabels.indexOf(positiveClass)];
	}

	public double getModelScore() {
		return this.modelScore;
	}

	public double getNumRules() {
		return this.nRules;
	}

	public double getNumVariables() {
		return this.nVars;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("\n\nModel Evaluation:\n").append("=================\n\n");

		int classLength = getLengthOfLongestClassName();
		final String fmt = "%-"+classLength+"s %-15s %-15s %-15s %-15s %-15s %-15s";

		sb.append(String.format(fmt, "Class", "Accuracy(%)", "AUC", "Precision", "Recall", "F-measure", "Brier score")).append("\n");

		final int[] fmt_lineBreak = new int [7];
		fmt_lineBreak[0] = classLength;
		fmt_lineBreak[1] = fmt_lineBreak[2] = fmt_lineBreak[3] = 15;
		fmt_lineBreak[4] = fmt_lineBreak[5] = fmt_lineBreak[6] = 15;

		for(int header = 0; header < fmt_lineBreak.length; header++) {
			for(int i = 0; i < fmt_lineBreak[header]; i++) {
				sb.append("-");
			}
			sb.append(" ");
		}
		sb.append("\n\n");


		for(int classIndex=0; classIndex<classLabels.size(); classIndex++) {
			sb.append(String.format(fmt, classLabels.get(classIndex), 
					df4.format(accuracy), 
					df4.format(auc[classIndex]), 
					df4.format(precision[classIndex]), 
					df4.format(recall[classIndex]), 
					df4.format(fMeasure[classIndex]), 
					df4.format(brierScore[classIndex])));
			sb.append("\n");
		}
		sb.append("\n");

		sb.append(confusionMatrixString());

		return sb.toString();
	}

	private int getLengthOfLongestClassName() {
		int lengthOfLongestClassName = 8;

		for(int t=0; t<classLabels.size(); t++) {
			if(classLabels.get(t).length() > lengthOfLongestClassName) {
				lengthOfLongestClassName = classLabels.get(t).length();
			}
		}

		return lengthOfLongestClassName+5;
	}

	private String confusionMatrixString() {
		StringBuilder sb = new StringBuilder();
		int classLength = getLengthOfLongestClassName();
		String fmt = "%-"+classLength+"s";

		sb.append("\nConfusion matrix: \n");
		sb.append("=================").append("\n\n");
		sb.append(String.format("%-20s", "Predicted \\ Actual ")).append("|");
		for(String classLabel: classLabels) {
			sb.append(String.format(fmt, classLabel));
		}
		sb.append("\n");
		for(int i=0; i<(21+(classLength*classLabels.size())); i++) {
			sb.append("-");
		}
		sb.append("\n");
		for(int actual=0; actual<classLabels.size(); actual++) {
			sb.append(String.format("%-20s", classLabels.get(actual))).append("|");
			for(int pred=0; pred<classLabels.size(); pred++) {
				sb.append(String.format(fmt, (int)confusionMatrix[actual][pred]));
			}
			sb.append("\n");
		}
		sb.append("\n");

		return sb.toString();
	}


	private List<String> classLabels;

	// predictive performance metrics
	private double[][] confusionMatrix;
	private double accuracy;
	private double[] precision;
	private double[] recall;
	private double[] fMeasure;
	private double[] auc;
	private double[] prg;
	private double modelScore;

	// calibration performance metrics
	private double[] brierScore;
	private double[] ece;
	private double[] mce;

	// semantic complexity performance
	private double nRules;
	private double nVars;

	private static DecimalFormat df4 = new DecimalFormat("0.0000");
}
