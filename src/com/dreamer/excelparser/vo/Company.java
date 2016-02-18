package com.dreamer.excelparser.vo;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.dreamer.common.constant.StrConstant;
import com.dreamer.excelparser.format.TspSheet1AndResultDef;

public class Company {
    private String owner;
    private String counties;
    private String townships;
    private String segment;
    private String landNo;

    private long landPrice;
    private double area;
    private String landUsePartition;
    private int numbersOfBuilding;
    private int privateLegalPerson;

    private int naturalPerson;
    private String alreadyContact = StrConstant.NO;
    private String phoneNo;
    private String extensionUnitUndertaker;
    private String chatContent;

    private long date;

    public Company(String owner, String content) {
	this.owner = owner;

	String[] splitResults = content.split("\t");

	for (TspSheet1AndResultDef resultCel : TspSheet1AndResultDef.values()) {
	    switch (resultCel) {
	    case COUNTIES:
		this.counties = splitResults[resultCel.getResultWithHiddenCellIndex()];
		break;

	    case TOWNSHIPS:
		this.townships = splitResults[resultCel.getResultWithHiddenCellIndex()];
		break;

	    case SEGMENT:
		this.segment = splitResults[resultCel.getResultWithHiddenCellIndex()];
		break;

	    case LAND_NO:
		this.landNo = splitResults[resultCel.getResultWithHiddenCellIndex()];
		break;

	    case LAND_PRICE:
		this.landPrice = (long) Double.parseDouble(splitResults[resultCel.getResultWithHiddenCellIndex()]);
		break;

	    case AREA:
		this.area = Double.parseDouble(splitResults[resultCel.getResultWithHiddenCellIndex()]);
		break;

	    case LAND_USED_PARTITON:
		this.landUsePartition = splitResults[resultCel.getResultWithHiddenCellIndex()];
		break;

	    case NUMBERS_OF_LAND_BUILDING:
		this.numbersOfBuilding = (int) Double.parseDouble(splitResults[resultCel.getResultWithHiddenCellIndex()]);
		break;

	    case PRIVATE_LEGAL_PERSON:
		this.privateLegalPerson = (int) Double.parseDouble(splitResults[resultCel.getResultWithHiddenCellIndex()]);
		break;

	    case NATURAL_PERSON:
		this.naturalPerson = (int) Double.parseDouble(splitResults[resultCel.getResultWithHiddenCellIndex()]);
		break;

	    default:
		break;
	    }
	}
    }

    public String getOwner() {
	return owner;
    }

    public void setOwner(String owner) {
	this.owner = owner;
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

    public String getSegment() {
	return segment;
    }

    public void setSegment(String segment) {
	this.segment = segment;
    }

    public String getLandNo() {
	return landNo;
    }

    public void setLandNo(String landNo) {
	this.landNo = landNo;
    }

    public long getLandPrice() {
	return landPrice;
    }

    public void setLandPrice(long landPrice) {
	this.landPrice = landPrice;
    }

    public double getArea() {
	return area;
    }

    public void setArea(double area) {
	this.area = area;
    }

    public String getLandUsePartition() {
	return landUsePartition;
    }

    public void setLandUsePartition(String landUsePartition) {
	this.landUsePartition = landUsePartition;
    }

    public int getNumbersOfBuilding() {
	return numbersOfBuilding;
    }

    public void setNumbersOfBuilding(int numbersOfBuilding) {
	this.numbersOfBuilding = numbersOfBuilding;
    }

    public int getPrivateLegalPerson() {
	return privateLegalPerson;
    }

    public void setPrivateLegalPerson(int privateLegalPerson) {
	this.privateLegalPerson = privateLegalPerson;
    }

    public int getNaturalPerson() {
	return naturalPerson;
    }

    public void setNaturalPerson(int naturalPerson) {
	this.naturalPerson = naturalPerson;
    }

    public String getAlreadyContact() {
	return alreadyContact;
    }

    public void setAlreadyContact(String alreadyContact) {
	this.alreadyContact = alreadyContact;
    }

    public String getPhoneNo() {
	return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
	this.phoneNo = phoneNo;
    }

    public String getExtensionUnitUndertaker() {
	return extensionUnitUndertaker;
    }

    public void setExtensionUnitUndertaker(String extensionUnitUndertaker) {
	this.extensionUnitUndertaker = extensionUnitUndertaker;
    }

    public String getChatContent() {
	return chatContent;
    }

    public void setChatContent(String chatContent) {
	this.chatContent = chatContent;
    }

    public long getDate() {
	return date;
    }

    public void setDate(long date) {
	this.date = date;
    }

    public void setPsValue(PreparedStatement ps) throws SQLException {
	int index = 1;

	ps.setNString(index++, owner == null ? "" : owner.trim());
	ps.setNString(index++, counties == null ? "" : counties.trim());
	ps.setNString(index++, townships == null ? "" : townships.trim());
	ps.setNString(index++, segment == null ? "" : segment.trim());
	ps.setNString(index++, landNo == null ? "" : landNo.trim());

	ps.setLong(index++, landPrice);
	ps.setDouble(index++, area);
	ps.setNString(index++, landUsePartition == null ? "" : landUsePartition.trim());
	ps.setInt(index++, numbersOfBuilding);
	ps.setInt(index++, privateLegalPerson);

	ps.setInt(index++, naturalPerson);
	ps.setNString(index++, alreadyContact == null ? "" : alreadyContact.trim());
	ps.setNString(index++, phoneNo == null ? "" : phoneNo.trim());
	ps.setNString(index++, extensionUnitUndertaker == null ? "" : extensionUnitUndertaker.trim());
	ps.setNString(index++, chatContent == null ? "" : chatContent.trim());

	ps.setLong(index++, date);
    }

    @Override
    public String toString() {
	return "Company [owner=" + owner + ", counties=" + counties + ", townships=" + townships + ", segment=" + segment + ", landNo=" + landNo
		+ ", landPrice=" + landPrice + ", area=" + area + ", landUsePartition=" + landUsePartition + ", numbersOfBuilding="
		+ numbersOfBuilding + ", privateLegalPerson=" + privateLegalPerson + ", naturalPerson=" + naturalPerson + ", alreadyContact="
		+ alreadyContact + ", phoneNo=" + phoneNo + ", extensionUnitUndertaker=" + extensionUnitUndertaker + ", chatContent=" + chatContent
		+ ", date=" + date + "]";
    }
}