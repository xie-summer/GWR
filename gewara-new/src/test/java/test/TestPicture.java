package test;

import java.io.File;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.util.StringUtil;

public class TestPicture {
	public static void main(String[] args) {
		System.out.println(xx());
		System.out.println(Runtime.getRuntime().availableProcessors());
		System.out.println(StringUtil.toUnicode("<script><script>abcÖÐ¹ú"));
	}
	public static String xx(){
		String xfwd = "125.39.224.100, 125.39.66.153, 172.22.1.141";
		String gewaip = null;
		if (StringUtils.isNotBlank(xfwd)) {
			String[] ipList = xfwd.split(",");
			for(int i=ipList.length -1; i>=0; i--){
				String ip = ipList[i];
				ip = StringUtils.trim(ip);
				if(Config.isGewaServerIp(ip)){
					gewaip = ip;
				}else if (!ip.equals("127.0.0.1") && !ip.equals("localhost")){
					return ip;
				}
			}
		}
		if(gewaip!=null) return gewaip; 
		return "xxxxxx";
	
	}

	public static String getFormatInFile(File f) {
		try {
			ImageInputStream iis = ImageIO.createImageInputStream(f);
			Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
			if (!iter.hasNext()) {
				return null;
			}
			ImageReader reader = iter.next();
			iis.close();
			return reader.getFormatName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}