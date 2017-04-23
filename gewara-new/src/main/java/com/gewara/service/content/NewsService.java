package com.gewara.service.content;

import java.sql.Timestamp;
import java.util.List;

import com.gewara.model.content.News;
import com.gewara.model.content.NewsPage;

public interface NewsService {
	//3. 新闻
	List<News> getCurrentNewsByTag(String citycode, String tag, String newstype, final int from, final int num);
	/**
	* 某个版块的新闻列表
	* @param tag (value1,value2,value3)
	* @param from
	* @param num
	* @return
	*/
	List<News> getNewsListByTag(String citycode, String tag, String newstype, String searchKey, Timestamp addtime, String order, final int from, final int num);
	/**
	 * 某个版块的新闻数量
	 * @param tag (value1,value2,value3)
	 * @return
	 */
	Integer getNewsCountByTag(String citycode, String tag, String newstype, String searchKey);
	/**
	 * 某个对象的新闻：例如：哈利波特的新闻
	 * @param tag
	 * @param relatedid
	 * @return
	 */
	List<News> getNewsByRelatedidAndTag(String tag,Long relatedid, int from, int maxnum);
	/**
	 * 某个对象相关的新闻：例如：哈利波特的相关新闻
	 * @param id
	 * @param tag
	 * @param category
	 * @return
	 */
	List<News> getNewsListByTagAndCategory(String citycode, String tag,String newslabel, int from, int maxnum);
	/**
	 * 得到新闻的第几页内容
	 * @param nid
	 * @param pageno
	 * @return
	 */
	NewsPage getNewsPageByNewsidAndPageno(Long nid,Integer pageno);
	/**
	 * 根据新闻的id得到它的分页
	 * @param newsid
	 * @return
	 */
	List<NewsPage> getNewsPageListByNewsid(Long newsid);
	List<News> getNewsListByTagAndRelatedId(String citycode, String tag, Long relatedid, String flag, String... type);
	List<News> getNewsList(String citycode, String tag, Long relatedid, String newstype, String title, int from, int maxnum);
	List<News> getNewsList(String citycode, String tag, Long relatedid, String newstype, String title, String order, boolean asc, int from, int maxnum);
	//TODO ???? 2012-10-12 查询数量与List参数名称不对应
	Integer getNewsCount(String citycode, String tag, String newstype, Long relatedid, String title);
	News getNextNews(String tag, Long nid);
	List<News> getNewsList(String citycode, String tag, Long relatedid, String newstype, int from, int maxnum);
	String validateNews(Long newsid);
	/**
	 * 根据tag, newstype查询新闻列表
	 * @param tag
	 * @param newstype
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<News> getNewsListByNewstype(String citycode, String tag, Long relatedid, String[] newstype, int from, int maxnum);
	/**
	 * 根据tag, newstype查询新闻数量
	 * @param tag
	 * @param newstype
	 * @return
	 */
	Integer getNewsCountByNewstype(String citycode, String tag, Long relatedid, String[] newstype);
	
	/**
	 * 查询电影信息
	 * @param citycode
	 * @param tag
	 * @param relatedid
	 * @param category
	 * @param categoryid
	 * @param order
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<News> getNewsList(String citycode, String tag, Long relatedid, String category, Long categoryid, String order, int from, int maxnum);
	void updateTips(Long nid);
}
