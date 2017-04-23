package test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;





public class TestXml {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws  
	 */
	public static void main(String[] args) throws Exception {
		/*//HttpResult result = HttpUtils.getUrlAsString("https://unionpaysecure.com/api/Query.action");
		HttpResult result = HttpUtils.getUrlAsString("https://www.gewara.com");
		System.out.println(result.getResponse());
		String xml = IOUtils.toString(new FileReader("D:\\www\\test.txt"));
		GymListResponse response = (GymListResponse) ApiUtils.xml2Object(ApiUtils.getBeanReader("data", GymListResponse.class), xml);
		System.out.println(response);
		
		int result = markValue(1517*10/214, 214, 138, 853*10/138, 6439);
		System.out.println(result);
		
		System.out.println(VmUtils.formatPercent(45, 99));
		
		List a = new ArrayList();
		a.add(2);
		a.add(3);
		List b = a;
		b.set(0, 1);
		System.out.println(a.toString());
		System.out.println(b.toString());*/
		List<Integer> aList = new ArrayList<Integer>();
		aList.add(1);
		aList.add(2);
		aList.add(3);
		List<Integer> bList = new ArrayList<Integer>();
		bList.add(1);
		bList.add(2);
		Collection list = CollectionUtils.union(aList, bList);
		System.out.println(list.toString());
	}
	
	public static int markValue(int a, int b, int c, int d, int n){
		int result =((a*(b/(b+c+5*(b+c+n)))+d))/(1+(b/(b+c+5*(b+c+n))));
		return result;
	}
	public static int markValue(){
		
		int unbookingmarks = 1517;		//非购票用户总评分
		int unbookingtimes = 214;		//非购票用户评分人数
		int bookingmarks = 853;			//购票用户总评分
		int bookingtimes = 138;			//购票用户评分次数
		int maxMarktimes = 6439;		//一个月内所有电影中单个评分最大次数
		int markConstant = 5;			//评分常量
		
		int unavgbookingmarks = unbookingmarks * 10 / unbookingtimes;
		int avgbookingmarks = bookingmarks * 10 / bookingtimes;
		
		int result = 0;
		Integer marks = unavgbookingmarks * unbookingtimes;
		Integer times = unbookingtimes + bookingtimes;
		if (times == 0) times = 1;
		if(bookingtimes < 20){ 
			result = 70;
		}else{
			Integer commonX = (times + markConstant)*(times + maxMarktimes);
			Integer top = marks / commonX + avgbookingmarks;
			Integer bottom = 1+(unbookingtimes / commonX);
			result = top/bottom > 100 ? 100 :top/bottom;
		}
		return result;
	}
}
