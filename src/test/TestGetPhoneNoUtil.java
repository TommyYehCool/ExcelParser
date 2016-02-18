package test;

import java.util.HashSet;

import com.dreamer.common.util.GetPhoneNoUtil;
import com.dreamer.excelparser.vo.CompanyInfo;

public class TestGetPhoneNoUtil {

    public static void main(String[] args) {
	CompanyInfo company1 = new CompanyInfo();
	
	company1.setCompanyName("華南商業銀行股份有限公司");
	company1.setCounties("桃園市");
	company1.setTownships("觀音區");
	
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

	System.out.println("成功找到: " + successCounts + ", 未找到: " + (allCompany - successCounts));

	System.out.println();
	System.out.println("成功資料如下:");
	for (CompanyInfo successCompany : successCompanies) {
	    System.out.println("公司名稱: " + successCompany.getCompanyName() + "\t\t地址: " + successCompany.getAddress() + "\t\t電話: " + successCompany.getPhoneNo());
	}
    }
}