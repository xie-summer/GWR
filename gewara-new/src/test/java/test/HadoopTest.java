package test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;

public class HadoopTest {
	private static String url = "hdfs://test109.gewara.cn";
	public static void main(String[] args) throws Exception {
		Map model = new HashMap();
		model.put("tradeNo", "5110418133006773");
		model.put("from", "manual");
		model.put("ignoreCheckPoint", "true");
		model.put("check", "c404ed286a");
		HttpResult s = HttpUtils.postUrlAsString("http://manage.gewara.com/processOrder.xhtml", model);
		System.out.println(s.getResponse());
	}
	public static void testWrite(String dir, String srcFile) throws Exception{
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(URI.create(url), conf);
		InputStream in = null;
		try {
			Path p = new Path(dir);
			//FsPermission permission = FsPermission.createImmutable("");
			if(!fs.exists(p)) {
				fs.mkdirs(p);
			}
			FSDataOutputStream os = fs.create(new Path(dir + System.currentTimeMillis() + ".jpg"));
			in = new FileInputStream(srcFile);
			IOUtils.copyBytes(in, os, 4096, true);
		} finally {
			in.close();
			IOUtils.closeStream(in);
		}
	}
}
