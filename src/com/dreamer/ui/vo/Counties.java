package com.dreamer.ui.vo;

import java.util.LinkedHashMap;

/**
 * ┐дел
 * 
 * @author user
 */
public class Counties {
    private String countiesName;

    private LinkedHashMap<String, Townships> allTownships;
    
    public Counties() {}
    
    public Counties(String countiesName, LinkedHashMap<String, Townships> allTownships) {
	this.countiesName = countiesName;
	this.allTownships = allTownships;
    }

    public String getCountiesName() {
	return countiesName;
    }

    public void setCountiesName(String countiesName) {
	this.countiesName = countiesName;
    }

    public void addTownships(Townships townships) {
	if (allTownships == null) {
	    allTownships = new LinkedHashMap<String, Townships>();
	}
	allTownships.put(townships.getTownshipName(), townships);
    }

    public Townships[] getAllTownships() {
	return allTownships != null ? allTownships.values().toArray(new Townships[0]) : new Townships[0];
    }

    public Townships getTownshipes(String townshipsName) {
	return allTownships.get(townshipsName);
    }

    @Override
    public String toString() {
	return countiesName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
        	+ ((allTownships == null) ? 0 : allTownships.hashCode());
        result = prime * result
        	+ ((countiesName == null) ? 0 : countiesName.hashCode());
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
        Counties other = (Counties) obj;
        if (allTownships == null) {
            if (other.allTownships != null)
        	return false;
        } else if (!allTownships.equals(other.allTownships))
            return false;
        if (countiesName == null) {
            if (other.countiesName != null)
        	return false;
        } else if (!countiesName.equals(other.countiesName))
            return false;
        return true;
    }
}