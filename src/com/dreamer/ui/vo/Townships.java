package com.dreamer.ui.vo;

/**
 * ¶mÂí¥« 
 * 
 * @author user
 */
public class Townships {
    private String townshipName;
    
    private String personalTableName;
    
    public Townships() {}
    
    public Townships(String townshipName, String personalTableName) {
	this.townshipName = townshipName;
	this.personalTableName = personalTableName;
    }
    
    public String getTownshipName() {
        return townshipName;
    }

    public void setTownshipName(String townshipName) {
        this.townshipName = townshipName;
    }
    
    public String getPersonalTableName() {
        return personalTableName;
    }

    public void setPersonalTableName(String personalTableName) {
        this.personalTableName = personalTableName;
    }
    
    @Override
    public String toString() {
	return townshipName;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((personalTableName == null) ? 0 : personalTableName.hashCode());
	result = prime * result + ((townshipName == null) ? 0 : townshipName.hashCode());
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
	Townships other = (Townships) obj;
	if (personalTableName == null) {
	    if (other.personalTableName != null)
		return false;
	}
	else if (!personalTableName.equals(other.personalTableName))
	    return false;
	if (townshipName == null) {
	    if (other.townshipName != null)
		return false;
	}
	else if (!townshipName.equals(other.townshipName))
	    return false;
	return true;
    }
}
