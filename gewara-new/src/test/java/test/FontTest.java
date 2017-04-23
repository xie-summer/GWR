package test;



public class FontTest {
	public static void main(String[] args) throws Exception{
		/*System.out.println(StringUtil.enabbr("1中加 1", 6));
		System.out.println(StringUtil.enabbr("1中加 1", 8));
		System.out.println(StringUtil.enabbr("中加中 ", 5));
		System.out.println(StringUtil.enabbr("中加1中 ", 5));
		System.out.println(StringUtil.enabbr("中加1中中加1中中加1中中加1中", 15));
		String s = "2012爵士上海音乐节绿色音符";
		System.out.println(s.length());
		System.out.println(VmUtils.escabbr(s, 18));*/
		/*String result = HttpUtils.getUrlAsString("http://api.map.baidu.com/?qt=rgc&x=12957516.98&y=4827438.43&dis_poi=100&poi_num=10&ie=utf-8&oue=0&res=api").getResponse();
		System.out.println(result);
		Map result1 = JsonUtils.readJsonToObject(Map.class, result);
		System.out.println(result1);*/
		/*String en = URLEncoder.encode("alone_小白", "utf-8");
		en = URLEncoder.encode(en, "utf-8");
		System.out.println(en);*/
		Exception my = null;
		try{
			System.err.println("xxxx");
			throw new Exception("xxxx");
		}catch(Exception e){
			my = e;
			System.err.println("bbbbb");
			throw e;
		}finally{
			System.err.println("aaaa" + my);
		}
	}
}
