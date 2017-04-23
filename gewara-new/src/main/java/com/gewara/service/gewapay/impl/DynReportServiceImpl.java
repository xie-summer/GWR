package com.gewara.service.gewapay.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Service;

import com.gewara.model.acl.User;
import com.gewara.model.report.Report;
import com.gewara.service.gewapay.DynReportService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ErrorCode;
@Service("dynReportService")
public class DynReportServiceImpl extends BaseServiceImpl implements DynReportService {
	@Override
	public List<Map<String, Object>> queryMapBySQL(final String sql, final int from,
			final int maxnum,final  Object... params) {
		return hibernateTemplate.executeFind(new HibernateCallback(){
			@Override
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query=session.createSQLQuery(sql);
				query.setFirstResult(from).setMaxResults(maxnum).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
				if(params != null)
					for (int i = 0,length=params.length; i < length; i++) {
						query.setParameter(i, params[i]);
					}
				return query.list();
			}
		});
	}

	@Override
	public List<Report> getDynReportList() {
		return baseDao.getObjectList(Report.class, "id", false, 0, 5000);
	}

	@Override
	public ErrorCode saveReport(Report report, User user) {
		// TODO: 检查报表中的敏感信息，如密码等
		baseDao.saveObject(report);
		return ErrorCode.SUCCESS;
	}

	@Override
	public void checkRights(Report report, User user) {
		// TODO: 限制权限？？
	}

	@Override
	public List<Map<String, Object>> getReportDataList(Report report, int from, List params, User user) {
		// TODO: 检查报表中的敏感信息，如密码等
		String sql = report.getQrysql();
		List<Map<String, Object>> result = queryMapBySQL(sql, from, report.getMaxnum(), params.toArray());
		return result;
	}

}
