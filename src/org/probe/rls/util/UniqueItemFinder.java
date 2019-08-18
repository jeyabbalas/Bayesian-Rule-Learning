package org.probe.rls.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class UniqueItemFinder {
	public static List<String> findUnqiueItems(List<String> items){
		final HashSet<String> uniqueItemHashSet = new HashSet<String>();
		final List<String> uniqueItems =  new LinkedList<String>();
		
		for(String item : items){
			if(!uniqueItemHashSet.contains(item)){
				uniqueItemHashSet.add(item);
				uniqueItems.add(item);
			}
		}
		
		return uniqueItems;
	}
}
