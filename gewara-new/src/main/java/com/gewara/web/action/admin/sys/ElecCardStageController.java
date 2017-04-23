/** 
 */
package com.gewara.web.action.admin.sys;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.untrans.hbase.ChangeLogService;
import com.gewara.util.BeanUtil;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.AnnotationController;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  May 17, 2013  11:49:30 AM
 */
@Controller
public class ElecCardStageController extends AnnotationController{
	@Autowired@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Autowired@Qualifier("changeLogService")
	private ChangeLogService changeLogService;

	@RequestMapping("/admin/sysmgr/goQueryElecCardStage.xhtml")
	public String goQueryElecCardStage(){
		return "admin/sysmgr/eclecCardStage.vm";
	}
	@RequestMapping("/admin/sysmgr/queryElecCardStage.xhtml")
	public String queryElecCardStage(String memberIds, ModelMap model, HttpServletResponse response) throws IOException{
		if (StringUtils.isBlank(memberIds))
			return showJsonError(model, "no member ids parameter");
		response.reset();  
		response.setHeader("Content-Disposition", "attachment; filename=ElecCard.txt");  
		response.setContentType("application/octet-stream; charset=utf-8");  
		final Writer writer = response.getWriter();
		writer.write("possessor" + "\t" + "cardno" + "\t" + "status" + "\t" + "orderid" + "\t" + "bangding\t" +  "log" + "\n\n");
		List<Long> idList = BeanUtil.getIdList(memberIds, ",");
		for (Long memberId:idList){
			String sql = "select possessor,cardno,status,orderid " +
					"from webdata.veleccard t where possessor = ? order by cardno";
			final Long mid= memberId;
			jdbcTemplate.query(sql, new Object[]{memberId}, new ResultSetExtractor(){
				@Override
				public Object extractData(ResultSet rs) throws SQLException,
						DataAccessException {
					int bind = 0;
					int total = 0;
					int unbind = 0;
					while(rs.next()){
						total ++;
						String possessor = rs.getString("possessor");
						String cardno  = rs.getString("cardno");
						String status = rs.getString("status");
						String orderid = rs.getString("orderid");
						try{
							writer.write(possessor + "\t" + cardno + "\t"  + status + "\t" + orderid + "\t");
							Map<Long, Map<String, String>> logList  = changeLogService.getChangeLogList("GEWARA", "ElecCard", cardno);
							Collection<Map<String, String>> logs = logList.values();
							Iterator<Map<String, String>> iterator = logs.iterator();
							while (iterator.hasNext()){
								Map<String, String> log = iterator.next();
								String changeLog = log.get("change");
								if(StringUtils.contains(changeLog, "regcard")){
									bind ++;
									writer.write("binding");
									break;
								}else if(StringUtils.contains(changeLog, "releaseCard")){
									unbind ++; 
									writer.write("unbinding");
									break;
								}else{
									if (!StringUtils.isBlank(changeLog)){
										Map<String, String> changeLogMap = JsonUtils.readJsonToMap(changeLog);
										String prssessor = changeLogMap.get("possessor");
										if (!StringUtils.isBlank(prssessor)){
											if (prssessor.startsWith("=====>")){
												bind ++;
												writer.write("binding");
												break;
											}else if (prssessor.endsWith("=====>")){
												unbind ++;
												writer.write("unbinding");
											}
										}
									}
								}
							}
							writer.write("\t" + logs);
							writer.write("\n");
						}catch(Exception e){
						}
					}
					try {
						writer.write("memberId:" + mid + ",total:" + total + ",bind:" + bind + ",unbind:" + unbind + "\n" );
						writer.write(StringUtils.repeat("-", 200) + "\n");
					} catch (IOException e) {
					}
					return null;
				}
				
			});
			writer.write("\n\n");
		}
		writer.close();
		return null;
	}
}
