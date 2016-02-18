package com.dreamer.ui.enu;

import java.util.ArrayList;

public enum CheckBoxStatus {
    COMPANY(0b00001),
    PERSONAL(0b00010),
    LEGAL_PERSON(0b00100),
    OTHERS(0b01000),
    NEVER_CONTACT(0b10000);
    
    private int values;
    
    private CheckBoxStatus(int value) {
	this.values = value;
    }
    
    public int getValue() {
	return values;
    }
    
    public static CheckBoxStatus[] convertByValue(int value) {
	ArrayList<CheckBoxStatus> listSelected = new ArrayList<CheckBoxStatus>();
	for (CheckBoxStatus checkBoxStatus : CheckBoxStatus.values()) {
	    if ((value & checkBoxStatus.values) == checkBoxStatus.values) {
		listSelected.add(checkBoxStatus);
	    }
	}
	return listSelected.toArray(new CheckBoxStatus[0]);
    }
}
