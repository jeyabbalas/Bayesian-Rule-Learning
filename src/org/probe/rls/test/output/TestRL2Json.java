package org.probe.rls.test.output;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.probe.rls.models.rulemodel.Rule;
import org.probe.rls.models.rulemodel.RuleModel;
import org.probe.rls.models.rulemodel.RuleStatistics;
import org.probe.rls.models.rulemodel.deterministic.Conjunct;
import org.probe.rls.models.rulemodel.deterministic.DeterministicRule;
import org.probe.rls.models.rulemodel.ensemble.EnsembleRuleModel;
import org.probe.rls.output.JsonWriters;
import org.probe.rls.util.visualize.brevity.BayesianRuleEnsembleTree;
import org.probe.rls.util.visualize.brevity.formatter.BrevityForJsonFormatter;

public class TestRL2Json {

	@Test
	public void testRlRules2Json()	{
		String rulesFileName = "Test//TrainMACE2.rules";
		
		RuleModel ruleModel = new RuleModel();
		
		String rulePatternStr = "^\\d+\\.\\s+(.*)";
		String ruleStatsPatternStr = "^\\s+CF=(.*?),.*?TP=(.*?),.*?FP=(.*?),.*";
		
		Pattern rulePattern = Pattern.compile(rulePatternStr);
		Pattern ruleStatsPattern = Pattern.compile(ruleStatsPatternStr);
		
		List<String> classValues = new LinkedList<String>();
		classValues.add("0");
		classValues.add("1");
		
		try (BufferedReader br = new BufferedReader(new FileReader(rulesFileName))) {
			String strCurrentLine;

			while ((strCurrentLine = br.readLine()) != null) {
				Matcher matchRule = rulePattern.matcher(strCurrentLine);
				if(matchRule.find()) {
					String parsedRuleStr = matchRule.group(1).replace("==>", "->").replace("@", "");
					System.out.println(parsedRuleStr);
					DeterministicRule newRule = DeterministicRule.parseString(parsedRuleStr);
					
					strCurrentLine = br.readLine();
					Matcher matchRuleStats = ruleStatsPattern.matcher(strCurrentLine);
					if(matchRuleStats.find()) {
						Map<String, Double> probabilities = new TreeMap<String, Double>();
						for(String classValue: classValues) {
							if(newRule.getRHSValue().equalsIgnoreCase(classValue)) {
								double predProb = Double.parseDouble(matchRuleStats.group(1));
								probabilities.put(classValue, predProb);
							} else {
								probabilities.put(classValue, 0.0);
							}
						}
						
						Integer truePositives = Integer.parseInt(matchRuleStats.group(2));
						Integer falsePositives = Integer.parseInt(matchRuleStats.group(3));
						
						RuleStatistics newRuleStats = new RuleStatistics(newRule, probabilities, truePositives, falsePositives);
						newRule.setRuleStatistics(newRuleStats);
						
						ruleModel.addRule(newRule);
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(ruleModel);
		
		JsonWriters.writeJsonFile(ruleModel, "Test//tempJson.txt");
	}
	
	@Test
	public void testBrlRules2Json()	{
		String rulesFileName = "Test//brlRuleModel.txt";
		
		RuleModel ruleModel = new RuleModel();
		
		String rulePatternStr = "^\\d+\\.\\s+(.*)";
		String ruleStatsPatternStr = "^\\s+Confidence = (.*?),\\s+TP = (\\d+),\\s+FP = (\\d+).*";
		
		Pattern rulePattern = Pattern.compile(rulePatternStr);
		Pattern ruleStatsPattern = Pattern.compile(ruleStatsPatternStr);
		
		List<String> classValues = new LinkedList<String>();
		classValues.add("Yes");
		classValues.add("No");
		
		
		try (BufferedReader br = new BufferedReader(new FileReader(rulesFileName))) {
			String strCurrentLine;
			
			while ((strCurrentLine = br.readLine()) != null) {
				Matcher matchRule = rulePattern.matcher(strCurrentLine);
				if(matchRule.find()) {
					String parsedRuleStr = matchRule.group(1);
					DeterministicRule newRule = DeterministicRule.parseString(parsedRuleStr);
					
					strCurrentLine = br.readLine();
					Matcher matchRuleStats = ruleStatsPattern.matcher(strCurrentLine);
					if(matchRuleStats.find()) {
						Map<String, Double> probabilities = new TreeMap<String, Double>();
						double predProb = Double.parseDouble(matchRuleStats.group(1));
						for(String classValue: classValues) {
							if(newRule.getRHSValue().equalsIgnoreCase(classValue)) {
								probabilities.put(classValue, predProb);
							} else {
								double compProb = 1.0-predProb;
								double scale = Math.pow(10, 4);
								compProb = Math.round(compProb*scale)/scale;
								probabilities.put(classValue, compProb);
							}
						}
						
						int truePositives = Integer.parseInt(matchRuleStats.group(2));
						int falsePositives = Integer.parseInt(matchRuleStats.group(3));
						
						RuleStatistics newRuleStats = new RuleStatistics(newRule, probabilities, truePositives, falsePositives);
						newRule.setRuleStatistics(newRuleStats);
						
						
						ruleModel.addRule(newRule);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		setUniqueIdentifiersForEachConjunct(ruleModel);
		
		JsonWriters.writeJsonFile(ruleModel, "Test//brlAsJson.json");
	}
	
	private void setUniqueIdentifiersForEachConjunct(RuleModel ruleModel) {
		Set<String> conjunctSet = new TreeSet<String>();
		Set<String> rhsSet = new TreeSet<String>();
		
		for(Rule rule: ruleModel.getRules()) {
			for(Conjunct conj: ((DeterministicRule) rule).getLHS().getConjuncts()) {
				String fieldValue = conj.getField()+":"+conj.getValue();
				conjunctSet.add(fieldValue);
			}
			
			Conjunct conj = ((DeterministicRule) rule).getRHS();
			String fieldValue = conj.getField()+":"+conj.getValue();
			rhsSet.add(fieldValue);
		}
		
		Map<String, String> conjunctIdentifiers = new HashMap<String, String>();
		int counter = 1;
		for(String conjunct: conjunctSet) {
			conjunctIdentifiers.put(conjunct, ""+(counter++));
		}
		Map<String, String> rhsIdentifiers = new HashMap<String, String>();
		int rhsCounter = 1;
		for(String rhs: rhsSet) {
			rhsIdentifiers.put(rhs, ""+(rhsCounter++));
		}
		
		for(Rule rule: ruleModel.getRules()) {
			for(Conjunct conj: ((DeterministicRule) rule).getLHS().getConjuncts()) {
				String fieldValue = conj.getField()+":"+conj.getValue();
				String conjIdentifier = "Conjunct:"+conjunctIdentifiers.get(fieldValue);
				conj.setIdentifier(conjIdentifier);
			}
			
			Conjunct conj = ((DeterministicRule) rule).getRHS();
			String fieldValue = conj.getField()+":"+conj.getValue();
			String rhsIdentifier = "RHS:"+rhsIdentifiers.get(fieldValue);
			conj.setIdentifier(rhsIdentifier);
		}
		
	}
	
	
	@Test
	public void testEbrlRules2Json()	{
		String rulesFileName = "Test//ebrlRuleModel.txt";
		String modelDelimiter = String.join("", Collections.nCopies(91, "="));
		
		String rulePatternStr = "^\\d+\\.\\s+(.*)";
		String ruleStatsPatternStr = "^\\s+Confidence = (.*?),\\s+TP = (\\d+),\\s+FP = (\\d+).*";
		
		Pattern rulePattern = Pattern.compile(rulePatternStr);
		Pattern ruleStatsPattern = Pattern.compile(ruleStatsPatternStr);
		
		List<String> classValues = new LinkedList<String>();
		classValues.add("Yes");
		classValues.add("No");
		
		RuleModel ruleModel = new RuleModel();
		
		
		try (BufferedReader br = new BufferedReader(new FileReader(rulesFileName))) {
			Scanner scanner = new Scanner(br);
			scanner.useDelimiter(modelDelimiter);
			int modelID = 0;
			
			while(scanner.hasNext()) {
				String modelStr = scanner.next();
				
				modelID++;
				String[] lines = modelStr.split("\\n");
				
				for(int i=0; i<lines.length; i++) {
					String line = lines[i];
					
					if(line.contains("Variable importance")) {
						break;
					}
					
					if(line != null) {
						Matcher matchRule = rulePattern.matcher(line);
						if(matchRule.find()) {
							String parsedRuleStr = matchRule.group(1);
							DeterministicRule newRule = DeterministicRule.parseString(parsedRuleStr);
							String identifierWithModelID = "Model:"+modelID+"::"+newRule.getIdentifier();
							newRule.setIdentifier(identifierWithModelID);
							
							i++;
							line = lines[i];
							Matcher matchRuleStats = ruleStatsPattern.matcher(line);
							if(matchRuleStats.find()) {
								Map<String, Double> probabilities = new TreeMap<String, Double>();
								double predProb = Double.parseDouble(matchRuleStats.group(1));
								for(String classValue: classValues) {
									if(newRule.getRHSValue().equalsIgnoreCase(classValue)) {
										probabilities.put(classValue, predProb);
									} else {
										double compProb = 1.0-predProb;
										double scale = Math.pow(10, 4);
										compProb = Math.round(compProb*scale)/scale;
										probabilities.put(classValue, compProb);
									}
								}
								
								int truePositives = Integer.parseInt(matchRuleStats.group(2));
								int falsePositives = Integer.parseInt(matchRuleStats.group(3));
								
								RuleStatistics newRuleStats = new RuleStatistics(newRule, probabilities, truePositives, falsePositives);
								newRule.setRuleStatistics(newRuleStats);
								
								
								ruleModel.addRule(newRule);
							}
						}
					}
				}
			}
			
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		setUniqueIdentifiersForEachConjunct(ruleModel);
		
		JsonWriters.writeJsonFile(ruleModel, "Test//ebrlAsJson.json");
		
	}
	
	@Test
	public void testBrevity2Json() {
		EnsembleRuleModel eRM = new EnsembleRuleModel();
		
		//String rulesFileName = "Test//ebrlRuleModel.txt";
		String rulesFileName = "temp//Classifiers_GSE19429//brlBmc_model.txt";
		//String rulesFileName = "temp//hs_baggedG.txt";
		String modelDelimiter = String.join("", Collections.nCopies(91, "="));
		
		String rulePatternStr = "^\\d+\\.\\s+(.*)";
		String ruleStatsPatternStr = "^\\s+Confidence = (.*?),\\s+TP = (\\d+),\\s+FP = (\\d+).*";
		
		Pattern rulePattern = Pattern.compile(rulePatternStr);
		Pattern ruleStatsPattern = Pattern.compile(ruleStatsPatternStr);
		
		List<String> classValues = new LinkedList<String>();
		//classValues.add("Yes");
		//classValues.add("No");
		classValues.add("Case");
		classValues.add("Normal");
		
		
		try (BufferedReader br = new BufferedReader(new FileReader(rulesFileName))) {
			Scanner scanner = new Scanner(br);
			scanner.useDelimiter(modelDelimiter);
			
			int count = 0;
			while(scanner.hasNext()) {
				String modelStr = scanner.next();
				RuleModel ruleModel = new RuleModel();
				
				String[] lines = modelStr.split("\\n");
				
				for(int i=0; i<lines.length; i++) {
					String line = lines[i];
					
					if(line.contains("Variable importance")) {
						break;
					}
					
					if(line != null) {
						Matcher matchRule = rulePattern.matcher(line);
						if(matchRule.find()) {
							String parsedRuleStr = matchRule.group(1);
							DeterministicRule newRule = DeterministicRule.parseString(parsedRuleStr);
							
							i++;
							line = lines[i];
							
							Matcher matchRuleStats = ruleStatsPattern.matcher(line);
							if(matchRuleStats.find()) {
								Map<String, Double> probabilities = new TreeMap<String, Double>();
								double predProb = Double.parseDouble(matchRuleStats.group(1));
								for(String classValue: classValues) {
									if(newRule.getRHSValue().equalsIgnoreCase(classValue)) {
										probabilities.put(classValue, predProb);
									} else {
										double compProb = 1.0-predProb;
										double scale = Math.pow(10, 4);
										compProb = Math.round(compProb*scale)/scale;
										probabilities.put(classValue, compProb);
									}
								}
								
								int truePositives = Integer.parseInt(matchRuleStats.group(2));
								int falsePositives = Integer.parseInt(matchRuleStats.group(3));
								
								RuleStatistics newRuleStats = new RuleStatistics(newRule, probabilities, truePositives, falsePositives);
								newRule.setRuleStatistics(newRuleStats);
								
								
								ruleModel.addRule(newRule);
							}
						}
						
					}
				}
				
				
				//if(ruleModel.getRules().size() > 0) {
				//	eRM.addRuleModel(ruleModel, 0.1);//Only works for Bagged BRL with 10 models.
				//}
				if(ruleModel.getRules().size() > 0) {
					if(count == 0) {
						eRM.addRuleModel(ruleModel, 0.06701909426455076);
					} else if(count == 1) {
						eRM.addRuleModel(ruleModel, 0.1092542123904475);
					} else if(count == 2) {
						eRM.addRuleModel(ruleModel, 0.12383682086250065	);
					} else if(count == 3) {
						eRM.addRuleModel(ruleModel, 0.09554936270951639);
					} else if(count == 4) {
						eRM.addRuleModel(ruleModel, 0.10107184705865903);
					} else if(count == 5) {
						eRM.addRuleModel(ruleModel, 0.0749897170545346);
					} else if(count == 6) {
						eRM.addRuleModel(ruleModel, 0.11237576123887917);
					} else if(count == 7) {
						eRM.addRuleModel(ruleModel, 0.13255301908455927);
					} else if(count == 8) {
						eRM.addRuleModel(ruleModel, 0.1142509560251573);
					} else {
						eRM.addRuleModel(ruleModel, 0.06909920931119554);
					}
					count++;
				}
				
			}
			
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		BayesianRuleEnsembleTree brevity = new BayesianRuleEnsembleTree(eRM);
		BrevityForJsonFormatter formattedBrevity = new BrevityForJsonFormatter(brevity);
		
		JsonWriters.writeJsonFile(formattedBrevity, "temp//GSE19429_bmc.json");
	}
	
}
