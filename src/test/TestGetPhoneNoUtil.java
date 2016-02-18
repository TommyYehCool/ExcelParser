package test;

import java.util.HashSet;

import com.dreamer.common.util.GetPhoneNoUtil;
import com.dreamer.excelparser.vo.CompanyInfo;

public class TestGetPhoneNoUtil {

    public static void main(String[] args) {
	CompanyInfo company1 = new CompanyInfo();
	
	company1.setCompanyName("�ثn�ӷ~�Ȧ�ѥ��������q");
	company1.setCounties("��饫");
	company1.setTownships("�[����");
	
	CompanyInfo[] companies = new CompanyInfo[] { 
		company1
	};

	long startTime = System.currentTimeMillis();

	int allCompany = companies.length;
	System.out.println("Start to query, company counts: <" + allCompany + ">");

	HashSet<CompanyInfo> successCompanies = GetPhoneNoUtil.getPhoneNo(null, companies);
	int successCounts = successCompanies.size();

	System.out.println("SpentTime: " + (System.currentTimeMillis() - startTime) + " ms");

	System.out.println();

	System.out.println("���\���: " + successCounts + ", �����: " + (allCompany - successCounts));

	System.out.println();
	System.out.println("���\��Ʀp�U:");
	for (CompanyInfo successCompany : successCompanies) {
	    System.out.println("���q�W��: " + successCompany.getCompanyName() + "\t\t�a�}: " + successCompany.getAddress() + "\t\t�q��: " + successCompany.getPhoneNo());
	}
    }
}