package com.gewara.untrans.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.untrans.BaoKuService;
import com.gewara.untrans.hbase.HBaseService;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;

@Service("baoKuService")
public class BaoKuServiceImpl implements BaoKuService {

	private final String TABLE_BAOKU = "baoku";
	private final String FILEPATH_BAOKU = "/opt/BAOKU/";//
	final String BAOKU_EMAIL = "email";
	final String SHELE_TABLE="menchao";
	// private final String FILEPATH_BAOKU = "D:\\密码\\email\\email\\";//

	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(BaoKuServiceImpl.class,
			Config.getServerIp(), Config.SYSTEMID);

	@Autowired
	private HBaseService hbaseService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public boolean isDanger(String email, String pwd) {
		try {
			if ("".equals(pwd) || !(email.indexOf("@") > 1 && email.indexOf(".") > 1)) {
				return false;
			}

			Map<String, String> rowMap = hbaseService.getRow(TABLE_BAOKU, email.getBytes());
			if (rowMap == null) {
				return false;
			}
			String md5 = rowMap.get("md5");
			String pwdDanger = rowMap.get("md5");
			if ("true".equals(md5)) {
				return StringUtil.md5(pwd).toLowerCase().equals(pwdDanger.toLowerCase());
			} else {
				return pwd.equals(pwdDanger);
			}
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean isDangerMD5(String email, String pwdMD5) {
		try {
			if ("".equals(pwdMD5) || !(email.indexOf("@") > 1 && email.indexOf(".") > 1)) {
				return false;
			}

			Map<String, String> rowMap = hbaseService.getRow(TABLE_BAOKU, email.getBytes());
			if (rowMap == null) {
				return false;
			}
			String md5 = rowMap.get("md5");
			String pwdDanger = rowMap.get("pwd");
			if ("true".equals(md5)) {
				return pwdMD5.toLowerCase().equals(pwdDanger.toLowerCase());
			} else {
				return pwdMD5.toLowerCase().equals(StringUtil.md5(pwdDanger).toLowerCase());
			}
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean loadBaoKuTxt(String fileName, boolean isLoad) {
		if (fileName == null || fileName.indexOf("..") > 0 || fileName.indexOf("//") > 0 || fileName.indexOf("\\") > 0) {
			return false;
		}

		FileReader reader = null;
		BufferedReader br = null;

		// FileWriter writerMima=null;
		// FileWriter writerMD5=null;
		try {
			// writerMima=new FileWriter(FILEPATH_BAOKU + "mima.txt",true);
			// writerMD5=new FileWriter(FILEPATH_BAOKU + "md5.txt",true);
			reader = new FileReader(FILEPATH_BAOKU + fileName);
			br = new BufferedReader(reader);
			String s1 = null;
			
			List<Map<String, String>> rowList = new ArrayList<Map<String, String>>();
			dbLogger.error("开始导入数据：" + fileName);
			int rowSize = 0;
			long start = System.currentTimeMillis();
			while ((s1 = br.readLine()) != null) {
				String[] s = null;
				if (fileName.startsWith("d-REYE")) {
					s = loadDREYE(s1);
				} else if (fileName.startsWith("duowan")) {
					s = loadDuowan(s1);
				} else if (fileName.startsWith("tianya")) {
					s = loadTianya(s1);
				} else if (fileName.startsWith("renren")) {
					s = loadRenren(s1);
				} else if (fileName.startsWith("gewara")) {
					s = loadGewara(s1);
				}

				if (s != null) {
					String emailS = s[0];
					String pwd = s[1];
					String md5 = s[2];
					if (!"".equals(pwd) && emailS.indexOf("@") > 1 && emailS.indexOf(".") > 1) {
						Map<String, String> map = new HashMap();
						map.put(BAOKU_EMAIL, emailS.toLowerCase());
						map.put("pwd", pwd);
						map.put("md5", md5);
						rowList.add(map);
						// if("true".equals(md5)){
						// writerMD5.write(emailS.toLowerCase()+"\t"+pwd+"\n");
						// writerMD5.flush();
						// }else{
						// writerMima.write(emailS.toLowerCase()+"\t"+pwd+"\n");
						// writerMima.flush();
						// }
					}
				} else {
					if (isLoad) {
						dbLogger.error(fileName + "数据有问题:" + " s.length=" + ArrayUtils.getLength(s) + " authenticode:"
								+ s1);
					}
				}

				if (rowList.size() >= 5000) {
					if (isLoad) {
						hbaseService.saveRowListByString(TABLE_BAOKU, BAOKU_EMAIL, rowList);
						dbLogger.error(fileName + "已经导入：" + rowSize);
					}
					rowSize = rowSize + rowList.size();
					rowList.clear();
				}
			}

			if (rowList.size() > 0) {
				if (isLoad) {
					hbaseService.saveRowListByString(TABLE_BAOKU, BAOKU_EMAIL, rowList);
				}
				rowSize = rowSize + rowList.size();
				rowList.clear();
			}
			dbLogger.error("导入数据完成：" + fileName + " 导入数量:" + rowSize + " 耗时:" + (System.currentTimeMillis() - start)
					/ 1000);
		} catch (Exception e) {
			dbLogger.error("导入错误：", e);
		} finally {
			try {
				br.close();
			} catch (Exception e) {
			}
			try {
				reader.close();
			} catch (Exception e) {
			}

			// try {
			// if(writerMima != null) writerMima.close();
			// } catch (Exception e) {
			// }
			// try {
			// if(writerMD5 != null) writerMD5.close();
			// } catch (Exception e) {
			// }
		}

		return false;
	}

	@Override
	public void loadBaoKuFiles(boolean isLoad) {
		File file = new File(FILEPATH_BAOKU);
		String[] files = file.list();
		for (String fileName : files) {
			if (fileName.startsWith("mima") || fileName.startsWith("md5")) {
				continue;
			}
			loadBaoKuTxt(fileName, isLoad);
		}
	}

	private String[] loadGewara(String line) {
		String[] lines = line.split("\t");
		if (lines.length < 2) {
			return null;
		}
		String[] linesss = { lines[0], lines[1], "true" };
		return linesss;
	}

	private String[] loadRenren(String line) {
		String[] lines = line.split("\t");
		if (lines.length < 2) {
			lines = line.split(" ");
		}
		
		if (lines.length < 2) {
			return null;
		}
		String[] linesss = { lines[0], lines[1], "false" };
		return linesss;
	}

	private String[] loadTianya(String line) {
		String[] lines = line.split(" ");
		if (lines.length <= 2) {
			lines = line.split(",");
		}

		if (lines.length <= 2) {
			return null;
		}
		List<String> lst = new ArrayList<String>();
		for (String liness : lines) {
			if (!"".equals(liness.trim())) {
				lst.add(liness);
			}
		}
		if (lst.size() <= 2) {
			return null;
		}
		String[] linesss = { lst.get(2), lst.get(1), "false" };
		return linesss;
	}

	private String[] loadDuowan(String line) {
		String[] lines = line.split("\t");
		if (lines.length <= 4) {
			return null;
		}
		String[] linessss = { lines[2], lines[3], "true" };
		if (lines.length == 5) {
			String[] linesss = { lines[3], lines[2], "true" };
			return linesss;
		}
		return linessss;
	}

	private String[] loadDREYE(String line) {
		String[] lines = line.split("\t");

		if (lines.length <= 2) {
			if (lines.length == 2) {
				String[] ddd = lines[1].split(" ");
				if (ddd.length == 2) {
					String[] linesss = { ddd[0], ddd[1], "false" };
					return linesss;
				}
			}
			return null;
		}
		if (lines.length > 3) {
			List<String> lst = new ArrayList<String>();
			for (String key : lines) {
				if (!"".equals(key.trim())) {
					lst.add(key);
				}
				if (lst.size() == 3) {
					String[] linesss = { lst.get(1), lst.get(2), "false" };
					return linesss;
				}
			}

		}

		String[] linesss = { lines[1], lines[2], "false" };
		return linesss;
	}

	public static void main(String[] arg) {
		// new BaoKuServiceImpl().loadBaoKuTxt("d-REYE_txt.1",false);

		// new BaoKuServiceImpl().loadBaoKuTxt("duowan_user.txt", false);

		// new BaoKuServiceImpl().loadBaoKuTxt("tianya_5.txt", false);
		// new BaoKuServiceImpl().loadBaoKuTxt("renren.txt", false);
		// new BaoKuServiceImpl().loadBaoKuTxt("jiakechong.txt", false);

		new BaoKuServiceImpl().loadBaoKuFiles(false);
	}

	@Override
	public void scanLoadBaoKu() {
		long max = jdbcTemplate.queryForLong("select count(*) from "+SHELE_TABLE+".detacted_member");
		long start = 0;
		int limit = 5000;
		long end = limit;
		int isExistCount = 0;
		while (true) {
			if (max < end) {
				end = max;
			}
			try {
				List<String> lst = jdbcTemplate.queryForList(
						"SELECT oldemail FROM ( SELECT A.*, ROWNUM RN from ( select oldemail from "+SHELE_TABLE+".detacted_member ) A WHERE ROWNUM <= "
								+ end + " ) WHERE RN > " + start, String.class);
				for (String email : lst) {
					if (isExist(email)) {
						dbLogger.error("邮件已经存在：" + email);
						isExistCount = isExistCount + 1;
					}
				}
			} catch (Exception e) {
			}
			if (max <= end) {
				break;
			}
			start = start + limit;
			end = end + limit;
		}
		dbLogger.error("邮件已经存在数量：" + isExistCount);

	}
	
	@Override
	public void loadGewaraBaoKu() {
		long max = jdbcTemplate.queryForLong("select count(*) from "+SHELE_TABLE+".detacted_member");
		long start = 0;
		int limit = 5000;
		long end = limit;
		int isExistCount = 0;
		while (true) {
			if (max < end) {
				end = max;
			}
			try {
				List<Map<String, Object>> lst = jdbcTemplate.queryForList(
						"SELECT oldemail,oldpass FROM ( SELECT A.*, ROWNUM RN from ( select oldemail,oldpass from "+SHELE_TABLE+".detacted_member ) A WHERE ROWNUM <= "
								+ end + " ) WHERE RN > " + start);
				List rowList=new ArrayList<String>();
				for (Map<String, Object> emails : lst) {
					Map<String, String> map = new HashMap();
					map.put(BAOKU_EMAIL, emails.get("OLDEMAIL").toString());
					map.put("pwd", emails.get("OLDPASS").toString());
					map.put("md5", "true");
					rowList.add(map);
				}
				hbaseService.saveRowListByString(TABLE_BAOKU, BAOKU_EMAIL, rowList);
				dbLogger.error("已经导入：" + rowList.size());
				isExistCount=isExistCount+rowList.size();
			} catch (Exception e) {
			}
			if (max <= end) {
				break;
			}
			start = start + limit;
			end = end + limit;
		}
		dbLogger.error("邮件已经存在数量：" + isExistCount);

	}

	@Override
	public void scanMemberBaoKu() {
		new ScanMemberBaoKu(jdbcTemplate, this).start();
	}

	public boolean isExist(String email) {
		try {
			if (!(email.indexOf("@") > 1 && email.indexOf(".") > 1)) {
				return false;
			}

			Map<String, String> rowMap = hbaseService.getRow(TABLE_BAOKU, email.getBytes());
			if (rowMap == null) {
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	private static class ScanMemberBaoKu extends Thread {
		
		private static final transient GewaLogger logger = LoggerUtils.getLogger(ScanMemberBaoKu.class,
				Config.getServerIp(), Config.SYSTEMID);

		private static boolean isRuning = false;
		private JdbcTemplate jdbcTemplate;
		private BaoKuService baoKuService;

		public ScanMemberBaoKu(final JdbcTemplate jdbcTemplate, final BaoKuService baoKuService) {
			this.jdbcTemplate = jdbcTemplate;
			this.baoKuService = baoKuService;
		}

		@Override
		public void run() {
			if (isRuning) {
				logger.error("ScanMemberBaoKu已经运行");
				return;
			}
			isRuning = true;
			logger.error("邮件与密码比对开始运行");
			try{
				long max = jdbcTemplate.queryForLong("select count(1) from webdata.member");
				long start = 0;
				int limit = 5000;
				long end = limit;
				int isExistCount = 0;
				while (true) {
					if (max < end) {
						end = max;
					}
					try {
						List<Map<String, Object>> lst = jdbcTemplate
								.queryForList("SELECT recordid,email,password FROM ( SELECT A.*, ROWNUM RN from ( select recordid,email,password from webdata.member ) A WHERE ROWNUM <= "
										+ end + " ) WHERE RN > " + start);
						for (Map<String, Object> email : lst) {
							Object emailObj=email.get("EMAIL");
							if(emailObj==null){
								continue;
							}
							if (baoKuService.isDangerMD5(emailObj.toString(), email.get("PASSWORD").toString())) {
								dbLogger.error("邮件与密码已经存在：" + email.get("RECORDID").toString());
								isExistCount = isExistCount + 1;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (max <= end) {
						break;
					}
					start = start + limit;
					end = end + limit;
					logger.error("当前数量：" + start);
				}
				logger.error("邮件与密码已经存在数量：" + isExistCount);
				
			}catch (Exception e) {
				dbLogger.warn("", e);
			}
			
			isRuning = false;
		}

	}

}
