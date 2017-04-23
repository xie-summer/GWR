package com.gewara.model.movie;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.common.BaseEntity;
/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public class Movie extends BaseEntity implements Comparable<Movie> {
	private static final long serialVersionUID = 7175746260709239571L;
	private String language;
	private String moviename;
	private String moviealias;
	private String director;
	private String playwright;
	private String actors;
	private String filmfirm;
	private String originalcountry;
	private Date releasedate;
	private String type;
	private String honor;
	private String website;
	private Integer videolen;	//影片时长：以分钟为单位
	private String remark;
	private String state;
	private String prevideo;
	private String highlight; //经典一句话
	private String playdate;
	private Integer avgprice;
	private Integer minprice;
	private Integer maxprice;
	private String imdbid;
	private String flag;
	private Integer boughtcount;	// 购票人次
	private String otherinfo;	
	private String edition;   //电影版本
	private String colorEggs; //电影彩蛋
	private static Map<String, Integer[]> priceMap = new HashMap();
	static{
		priceMap.put("1", new Integer[]{0,30});
		priceMap.put("2", new Integer[]{30,50});
		priceMap.put("3", new Integer[]{50,80});
		priceMap.put("4", new Integer[]{80,1000});
	}
	public Movie() {
	}
	public Movie(String moviename) {
		this.generalmark = 14;
		this.generalmarkedtimes = 2;
		this.quguo = 1;
		this.xiangqu = 1;
		this.clickedtimes = 20;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.collectedtimes = 1;
		this.avgprice=0;
		this.minprice = 0;
		this.maxprice = 0;
		this.boughtcount = 0;	// 默认0
		this.moviename = moviename;
	}

	public String getPrevideo() {
		return prevideo;
	}

	public void setPrevideo(String prevideo) {
		this.prevideo = prevideo;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getLanguage() {
		return this.language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getMoviename() {
		return this.moviename;
	}

	public void setMoviename(String name) {
		this.moviename = StringUtils.trimToNull(name);
	}
	
	public String getName(){
		return moviename;
	}

	public String getDirector() {
		return this.director;
	}

	public void setDirector(String director) {
		this.director = director;
	}

	public String getPlaywright() {
		return this.playwright;
	}

	public void setPlaywright(String playwright) {
		this.playwright = playwright;
	}

	public String getActors() {
		return this.actors;
	}

	public void setActors(String actors) {
		this.actors = actors;
	}

	public String getFilmfirm() {
		return this.filmfirm;
	}

	public void setFilmfirm(String filmfirm) {
		this.filmfirm = filmfirm;
	}

	public String getOriginalcountry() {
		return this.originalcountry;
	}

	public void setOriginalcountry(String originalcountry) {
		this.originalcountry = originalcountry;
	}

	public Date getReleasedate() {
		return this.releasedate;
	}

	public void setReleasedate(Date releasedate) {
		this.releasedate = releasedate;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}
	public void setMovietype(String type){
		this.type = type;
	}
	public String getHonor() {
		return this.honor;
	}

	public void setHonor(String honor) {
		this.honor = honor;
	}

	public String getWebsite() {
		return this.website;
	}

	public void setWebsite(String website) {
		if(StringUtils.isNotBlank(website) && !StringUtils.startsWith(website, "http://")) website = "http://" + website;
		this.website = website;
	}

	public String getLength() {
		if(videolen==null) return "";
		return this.videolen + "分钟";
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getMoviealias() {
		return moviealias;
	}

	public void setMoviealias(String moviealias) {
		this.moviealias = moviealias;
	}
	public int compareTo(Movie another) {
		if(another.equals(this)) return 0; 
		int result = (this.getHotvalue() - another.getHotvalue()) + (this.getClickedtimes() - another.getClickedtimes()); 
		return -result;
	}

	
	public static String getPricetype(String priceStr){
		try{
			int price = Integer.parseInt(priceStr);
			for(String key: priceMap.keySet()){
				if(price >= priceMap.get(key)[0] && price < priceMap.get(key)[1]) return key;
			}
		}catch(Exception e){
		}
		return "";
	}
	public String getUrl(){
		return "movie/" + this.id;
	}
	public String getLogo() {
		return logo;
	}
	public String getLimg(){
		if(StringUtils.isBlank(logo)) return "img/default_movie.png";
		return logo;
	}
	public String getHighlight() {
		return highlight;
	}
	public void setHighlight(String highlight) {
		this.highlight = highlight;
	}
	public String getPlaydate() {
		return playdate;
	}
	public void setPlaydate(String playdate) {
		this.playdate = playdate;
	}
	public String getImdbid() {
		return imdbid;
	}
	public void setImdbid(String imdbid) {
		this.imdbid = imdbid;
	}
	public String getCname() {
		return this.moviename;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public Integer getAvgprice() {
		return avgprice;
	}
	public void setAvgprice(Integer avgprice) {
		this.avgprice = avgprice;
	}
	
	public Integer getMinprice() {
		return minprice;
	}
	public void setMinprice(Integer minprice) {
		this.minprice = minprice;
	}
	public Integer getMaxprice() {
		return maxprice;
	}
	public void setMaxprice(Integer maxprice) {
		this.maxprice = maxprice;
	}
	/***
	 * 本次1=(本次*2到3之间的随机数)；
	 * 1、	购票人次= 上次购票+本次1
		2、	想看人次= 实际想看人数；
		3、	看过人次= 实际看过人数+本次1；
		4、	感兴趣人次= 实际点击感兴趣人次+想看人次；
		5、	浏览数= 上次浏览次数+本次1；
		6、	参与评分人= (实际参与评分人次+购票人次)/2；
	 * 
	 **/
	public Integer getRxiangqu(){
		return this.xiangqu;
	}
	public Integer getRquguo(){
		return  this.quguo + this.boughtcount;
	}
	public Integer getRcollectedtimes(){
		return  this.collectedtimes + this.xiangqu;
	}
	public Integer getRclickedtimes() {
		return this.clickedtimes;
	}
	public Integer getVideolen() {
		return videolen;
	}
	public void setVideolen(Integer videolen) {
		this.videolen = videolen;
	}

	public String getDirAndAct(){
		return (StringUtils.isNotBlank(this.director)?this.director:"") + (StringUtils.isNotBlank(this.actors)?" / "+this.actors:"");
	}

	public Integer getBoughtcount() {
		return boughtcount;
	}
	public void setBoughtcount(Integer boughtcount) {
		this.boughtcount = boughtcount;
	}
	
	public void addBoughtcount(int num){
		this.boughtcount += num;
	}
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	public String getEdition() {
		return edition;
	}
	public void setEdition(String edition) {
		this.edition = edition;
	}
	public String getColorEggs() {
		return colorEggs;
	}
	public void setColorEggs(String colorEggs) {
		this.colorEggs = colorEggs;
	}
}
	
