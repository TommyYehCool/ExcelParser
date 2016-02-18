package test;
import com.dreamer.ui.enu.CheckBoxStatus;

public class TestEnum {
    public static void main(String[] args) {
	int test = 
		CheckBoxStatus.COMPANY.getValue()
	      + CheckBoxStatus.PERSONAL.getValue()
	      + CheckBoxStatus.OTHERS.getValue();

	CheckBoxStatus[] status = CheckBoxStatus.convertByValue(test);
	for (CheckBoxStatus selected : status) {
	    System.out.println(selected + ", value: " + selected.getValue());
	}
    }
}
