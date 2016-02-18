package com.dreamer.common.vo;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import com.dreamer.common.constant.StrConstant;

public class LandInfo {
    private SimpleStringProperty owner;
    private SimpleStringProperty counties;
    private SimpleStringProperty townships;
    private SimpleStringProperty segment;
    private SimpleStringProperty landNo;

    private SimpleLongProperty landPrice;
    private SimpleDoubleProperty area;
    private SimpleStringProperty landUsePartition;
    private SimpleIntegerProperty numbersOfBuilding;
    private SimpleIntegerProperty privateLegalPerson;

    private SimpleIntegerProperty naturalPerson;
    private SimpleStringProperty alreadyContact = new SimpleStringProperty(StrConstant.NO);
    private SimpleStringProperty phoneNo;
    private SimpleStringProperty extensionUnitUndertaker;
    private SimpleStringProperty address;
    private SimpleStringProperty chatContent;

    private SimpleLongProperty date;

    public LandInfo(String owner, String counties, String townships, String segment, String landNo, long landPrice, double area,
	    	    String landUsePartition, int numbersOfBuilding, int privateLegalPerson, int naturalPerson, String alreadyContact, String phoneNo,
	    	    String extensionUnitUndertaker, String address, String chatContent, long date) {
	this.owner = new SimpleStringProperty(owner);
	this.counties = new SimpleStringProperty(counties);
	this.townships = new SimpleStringProperty(townships);
	this.segment = new SimpleStringProperty(segment);
	this.landNo = new SimpleStringProperty(landNo);
	this.landPrice = new SimpleLongProperty(landPrice);
	this.area = new SimpleDoubleProperty(area);
	this.landUsePartition = new SimpleStringProperty(landUsePartition);
	this.numbersOfBuilding = new SimpleIntegerProperty(numbersOfBuilding);
	this.privateLegalPerson = new SimpleIntegerProperty(privateLegalPerson);
	this.naturalPerson = new SimpleIntegerProperty(naturalPerson);
	this.alreadyContact = new SimpleStringProperty(alreadyContact);
	this.phoneNo = new SimpleStringProperty(phoneNo);
	this.extensionUnitUndertaker = new SimpleStringProperty(extensionUnitUndertaker);
	this.address = new SimpleStringProperty(address);
	this.chatContent = new SimpleStringProperty(chatContent);
	this.date = new SimpleLongProperty(date);
    }

    public String getOwner() {
	return owner.get();
    }

    public void setOwner(String owner) {
	this.owner.set(owner);
    }

    public String getCounties() {
	return counties.get();
    }

    public void setCounties(String counties) {
	this.counties.set(counties);
    }

    public String getTownships() {
	return townships.get();
    }

    public void setTownships(String townships) {
	this.townships.set(townships);
    }

    public String getSegment() {
	return segment.get();
    }

    public void setSegment(String segment) {
	this.segment.set(segment);
    }

    public String getLandNo() {
	return landNo.get();
    }

    public void setLandNo(String landNo) {
	this.landNo.set(landNo);
    }

    public long getLandPrice() {
	return landPrice.get();
    }

    public void setLandPrice(long landPrice) {
	this.landPrice.set(landPrice);
    }

    public double getArea() {
	return area.get();
    }

    public void setArea(double area) {
	this.area.set(area);
    }

    public String getLandUsePartition() {
	return landUsePartition.get();
    }

    public void setLandUsePartition(String landUsePartition) {
	this.landUsePartition.set(landUsePartition);
    }

    public int getNumbersOfBuilding() {
	return numbersOfBuilding.get();
    }

    public void setNumbersOfBuilding(int numbersOfBuilding) {
	this.numbersOfBuilding.set(numbersOfBuilding);
    }

    public int getPrivateLegalPerson() {
	return privateLegalPerson.get();
    }

    public void setPrivateLegalPerson(int privateLegalPerson) {
	this.privateLegalPerson.set(privateLegalPerson);
    }

    public int getNaturalPerson() {
	return naturalPerson.get();
    }

    public void setNaturalPerson(int naturalPerson) {
	this.naturalPerson.set(naturalPerson);
    }

    public String getAlreadyContact() {
	return alreadyContact.get();
    }

    public void setAlreadyContact(String alreadyContact) {
	this.alreadyContact.set(alreadyContact);
    }

    public String getPhoneNo() {
	return phoneNo.get();
    }

    public void setPhoneNo(String phoneNo) {
	this.phoneNo.set(phoneNo);
    }

    public String getExtensionUnitUndertaker() {
	return extensionUnitUndertaker.get();
    }

    public void setExtensionUnitUndertaker(String extensionUnitUndertaker) {
	this.extensionUnitUndertaker.set(extensionUnitUndertaker);
    }
    
    public String getAddress() {
	return address.get();
    }
    
    public void setAddress(String address) {
	this.address.set(address);
    }

    public String getChatContent() {
	return chatContent.get();
    }

    public void setChatContent(String chatContent) {
	this.chatContent.set(chatContent);
    }

    public long getDate() {
	return date.get();
    }

    public void setDate(long date) {
	this.date.set(date);
    }

    @Override
    public String toString() {
	return "LandInfo [owner=" + owner + ", counties=" + counties
		+ ", townships=" + townships + ", segment=" + segment
		+ ", landNo=" + landNo + ", landPrice=" + landPrice + ", area="
		+ area + ", landUsePartition=" + landUsePartition
		+ ", numbersOfBuilding=" + numbersOfBuilding
		+ ", privateLegalPerson=" + privateLegalPerson
		+ ", naturalPerson=" + naturalPerson + ", alreadyContact="
		+ alreadyContact + ", phoneNo=" + phoneNo
		+ ", extensionUnitUndertaker=" + extensionUnitUndertaker
		+ ", address=" + address + ", chatContent=" + chatContent
		+ ", date=" + date + "]";
    }
}