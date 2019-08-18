package org.probe.rls.util.list;

import java.util.LinkedList;
import java.util.List;

public class ListCopier {

	public static List<String> createShallowCopy(List<String> columnItems) {
		List<String> copyColumnItems = new LinkedList<String>();
		for(String columnItem : columnItems){
			copyColumnItems.add(columnItem);
		}
		return copyColumnItems;
	}
	
}
