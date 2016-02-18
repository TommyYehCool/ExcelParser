package com.dreamer.excelparser.vo;

public class CompanyInfo {
    private String counties;
    
    private String townships;
    
    private String companyName;
    
    private String address;

    private String phoneNo;
    
    public CompanyInfo() {
    }

    public CompanyInfo(String companyName, String address, String phoneNo) {
	this.companyName = companyName;
	this.address = address;
	this.phoneNo = phoneNo;
    }

    public String getCounties() {
        return counties;
    }

    public void setCounties(String counties) {
        this.counties = counties;
    }

    public String getTownships() {
        return townships;
    }

    public void setTownships(String townships) {
        this.townships = townships;
    }

    public String getCompanyName() {
	return companyName;
    }

    public void setCompanyName(String companyName) {
	this.companyName = companyName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNo() {
	return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
	this.phoneNo = phoneNo;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((address == null) ? 0 : address.hashCode());
	result = prime * result
		+ ((companyName == null) ? 0 : companyName.hashCode());
	result = prime * result
		+ ((counties == null) ? 0 : counties.hashCode());
	result = prime * result + ((phoneNo == null) ? 0 : phoneNo.hashCode());
	result = prime * result
		+ ((townships == null) ? 0 : townships.hashCode());
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
	CompanyInfo other = (CompanyInfo) obj;
	if (address == null) {
	    if (other.address != null)
		return false;
	} else if (!address.equals(other.address))
	    return false;
	if (companyName == null) {
	    if (other.companyName != null)
		return false;
	} else if (!companyName.equals(other.companyName))
	    return false;
	if (counties == null) {
	    if (other.counties != null)
		return false;
	} else if (!counties.equals(other.counties))
	    return false;
	if (phoneNo == null) {
	    if (other.phoneNo != null)
		return false;
	} else if (!phoneNo.equals(other.phoneNo))
	    return false;
	if (townships == null) {
	    if (other.townships != null)
		return false;
	} else if (!townships.equals(other.townships))
	    return false;
	return true;
    }
}
