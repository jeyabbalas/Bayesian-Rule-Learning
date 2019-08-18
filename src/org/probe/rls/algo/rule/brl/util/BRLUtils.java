package org.probe.rls.algo.rule.brl.util;

import java.util.LinkedList;
import java.util.List;

public class BRLUtils {

	public static List<List<List<String>>> partitionIntoAllBinarySetCombinations(List<String> items) {
		List<List<List<String>>> listOfBinarySetCombinations = new LinkedList<List<List<String>>>();
		
		for(int i=1; i<(Math.pow(2,items.size()-1)); i++) {
			List<List<String>> binarySetCombination = new LinkedList<List<String>>();
			
			int value = i;
			boolean[] bitArray = new boolean[items.size()];
			
			int index = items.size()-1;
			while(value > 0) {
				if(value%2 == 1) {
					bitArray[index--] = true;
				} else {
					bitArray[index--] = false;
				}
				value /= 2;
			}
			
			List<String> listA = new LinkedList<String>();
			List<String> listB = new LinkedList<String>();
			
			for(int j=0; j<bitArray.length; j++) {
				if(bitArray[j]) {
					listA.add(items.get(j));
				}
				else {
					listB.add(items.get(j));
				}
			}
			
			binarySetCombination.add(0, listA);
			binarySetCombination.add(1, listB);
			listOfBinarySetCombinations.add(binarySetCombination);
		}
		
		return listOfBinarySetCombinations;
	}
	
	public static List<List<List<String>>> partitionIntoOrderedBinarySetCombinations(List<String> items) {
		List<List<List<String>>> listOfBinarySetCombinations = new LinkedList<List<List<String>>>();
		
		for(int i=0; i<items.size()-1; i++) {
			List<List<String>> binarySetCombination = new LinkedList<List<String>>();
			List<String> listA = new LinkedList<String>();
			List<String> listB = new LinkedList<String>();
			
			for(int j=0; j<items.size(); j++) {
				if(j<=i) {
					listA.add(items.get(j));
				} else {
					listB.add(items.get(j));
				}
			}
			
			binarySetCombination.add(0, listA);
			binarySetCombination.add(1, listB);
			listOfBinarySetCombinations.add(binarySetCombination);
		}
		
		return listOfBinarySetCombinations;
	}
	
}
