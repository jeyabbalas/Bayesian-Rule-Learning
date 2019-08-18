package org.probe.rls.algo.rule.brl.datastructures;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.probe.rls.data.DataAttribute;

public class BRLStructurePrior {
	
	public BRLStructurePrior(String filename, String sep) {
		this.attributeSetWeights = new HashMap<Set<String>, Double>();
		this.lambda = 0.0;
		this.fileName = filename;
		
		loadStructurePriors(this.fileName, sep);
	}
	
	public double computeStructurePrior(Set<DataAttribute> candidateEdges) {
		Set<String> candidateVars = new HashSet<String>();
		
		for(DataAttribute edge: candidateEdges) {
			candidateVars.add(edge.getAttributeName());
		}
		
		double sumWeights = 0.0;
		
		for(Set<String> varSet: attributeSetWeights.keySet()) {
			double weight = attributeSetWeights.get(varSet);
			for(String var: varSet) {
				if(candidateVars.contains(var)) {
					sumWeights = sumWeights + weight;
					candidateVars.remove(var);
					break;
				}
			}
			
			if(candidateVars.size() == 0) {
				break;
			}
		}
		
		double power_term = lambda*(sumWeights);
		double structurePrior = Math.pow(Math.E, power_term);
		return structurePrior;
	}
	
	private double getLambdaValue(String line){
		String[] elements = line.split("=");
		
		return Double.valueOf(elements[1]);
	}
	
	private void addVariablesToSet(BufferedReader buffReader, Set<String> variableSet) throws IOException {
		String line = null;
		
		while(true) {
			line = buffReader.readLine();
			
			if(line == null || line.contains("<END>")) {
				break;
			}
			else {
				variableSet.add(line);
			}
		}
	}
	
	public void loadStructurePriors(String fileName, String sep) {
		try {
			BufferedReader buffReader = new BufferedReader(new FileReader(fileName));
			String line = null;

			while(true) {
				line = buffReader.readLine();
				if(line == null) {  
					break; 
				} 
				else {
					if(line.contains("lambda")) {
						lambda = getLambdaValue(line);
						continue;
					} else if(line.split(sep).length == 2){
						Set<String> newVariableSet = new HashSet<String>();
						Double weight = Double.parseDouble(line.split(sep)[1]);
						addVariablesToSet(buffReader, newVariableSet);
						attributeSetWeights.put(newVariableSet, weight);
					}
				}
			}

			buffReader.close();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public Map<Set<String>, Double> getAttributeSetWeights() {
		return attributeSetWeights;
	}

	public void setAttributeWeights(Map<Set<String>, Double> attributeSetWeights) {
		this.attributeSetWeights = attributeSetWeights;
	}

	public Double getLambda() {
		return lambda;
	}

	public void setLambda(Double lambda) {
		this.lambda = lambda;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	
	private Map<Set<String>, Double> attributeSetWeights;
	private Double lambda = 0.0;
	private String fileName;
}
