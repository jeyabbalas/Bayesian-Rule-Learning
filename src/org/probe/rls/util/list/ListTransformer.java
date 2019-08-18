package org.probe.rls.util.list;

import java.util.LinkedList;
import java.util.List;

public class ListTransformer {

	public static List<Double> copyListStringToDouble(List<String> columnItems) {
		List<Double> copyColumnItems = new LinkedList<Double>();
		for(String columnItem : columnItems){
			Double val = Double.parseDouble(columnItem);
			copyColumnItems.add(val);
		}
		return copyColumnItems;
	}
}
