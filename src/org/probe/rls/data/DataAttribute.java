package org.probe.rls.data;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DataAttribute {
	
	public DataAttribute(DataAttribute dataAttribute) {
		this.attributeIndex = dataAttribute.getAttributeIndex();
		this.attributeName = dataAttribute.getAttributeName();
		this.attributeValues = new HashSet<String>(dataAttribute.getAttributeValues());
		
		this.isClass = dataAttribute.isClass;
		this.isInstance = dataAttribute.isInstance;
		this.isNominal = dataAttribute.isNominal;
		this.isOrdinal = dataAttribute.isOrdinal;
	}

	public DataAttribute(int attributeIndex, String attributeName) {
		this.attributeIndex = attributeIndex;
		this.attributeName = attributeName;
		this.attributeValues = new HashSet<String>();
	}

	public String getAttributeName() {
		return attributeName;
	}
	
	public int getAttributeIndex() {
		return attributeIndex;
	}
	
	public List<String> getAttributeValues() {
		return new LinkedList<String>(attributeValues);
	}

	public void setAttributeValues(Set<String> attributeValues) {
		this.attributeValues = attributeValues;
	}
	
	public void clearAttributeValues() {
		this.attributeValues = new HashSet<String>();
	}
	
	public void addAttributeValue(String attributeValue) {
		this.attributeValues.add(attributeValue);
	}
	
	public boolean isClass() {
		return isClass;
	}
	
	public boolean isInstance() {
		return isInstance;
	}
	
	public void setIsInstance(boolean isInstance) {
		this.isInstance = isInstance;
	}

	public void setIsClass(boolean isClass) {
		this.isClass = isClass;
	} 
	
	public boolean isNominal() {
		return isNominal;
	}

	public void setNominal(boolean isNominal) {
		this.isNominal = isNominal;
	}
	
	public boolean isOrdinal() {
		return isOrdinal;
	}

	public void setOrdinal(boolean isOrdinal) {
		this.isOrdinal = isOrdinal;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + attributeIndex;
		result = prime * result
				+ ((attributeName == null) ? 0 : attributeName.hashCode());
		result = prime * result + (isClass ? 1231 : 1237);
		result = prime * result + (isInstance ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataAttribute other = (DataAttribute) obj;
		return this.attributeIndex == other.attributeIndex;
	}

	private final int attributeIndex;
	private final String attributeName;
	private Set<String> attributeValues;
	
	private boolean isInstance = false;
	private boolean isClass = false;
	
	private boolean isNominal = false;
	private boolean isOrdinal = false;
}
