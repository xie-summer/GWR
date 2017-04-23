---合并浙江地区“富阳站”至“杭州站” 
update CINEMA set citycode='330100', countycode='' where citycode='330183';
update OPEN_PLAYITEM set citycode='330100' where citycode='330183';
update MOVIELIST set citycode='330100' where citycode='330183';
update TICKET_ORDER set citycode='330100' where citycode='330183';
update FAILURE_ORDER set citycode='330100' where citycode='330183';
update BULLETIN set citycode='330100' where citycode='330183';
update SURVEY set citycode='330100' where citycode='330183';
update ADVERTISING set citycode='330100' where citycode='330183';
update GOODS set citycode='330100' where citycode='330183';     		  
update GRABTICKETSUBJECT set citycode='330100' where citycode='330183';  


update DIARY set citycode='330100' where citycode='330183';
update ACTIVITY set citycode='330100' where citycode='330183';
update NEWS set citycode='330100' where citycode='330183';
update GEWAQUESTION set citycode='330100' where citycode='330183';
update QUESTION set citycode='330100' where citycode='330183';
update DIARYCOMMENT c set citycode='330100' where citycode='330183';
update CUSTOMER_ANSWER set citycode='330100' where citycode='330183';
update CUSTOMER_QUESTION set citycode='330100' where citycode='330183';

update GEWACOMMEND set citycode='330100' where citycode='330183';
update USER_INVOICE set citycode='330100' where citycode='330183';
update PHONEADVERTISEMENT set citycode='330100' where citycode='330183';  
update PLACE set citycode='330100' where citycode='330183';               
update RELATETOCITY set citycode='330100' where citycode='330183';        
update CITYPRICE set citycode='330100' where citycode='330183';           

update GEWAMACHINE set citycode='330100' where citycode='330183';         --?
update ELECCARD_BATCH set citycode='330100' where citycode='330183';      --?
update SPECIALDISCOUNT set citycode='330100' where citycode='330183';     --?
update REWARD_CONFIG set citycode='330100' where citycode='330183';       --?
--用户注册
update MEMBERINFO set FROMCITY=330100 where FROMCITY=330183;
--TODO 唯一键约束
--每个城市的最低卖价不一样，升级完后，电商负责改动一下
--update MOVIEPRICE set citycode='330100' where citycode='330183';    


delete from MOVIEPRICE where citycode='330183';  
delete from MOVIEPRICE where citycode in('331200','330282');
delete from MOVIEPRICE where citycode in('320581','320583');

---合并“慈溪、余姚”至“宁波”站

update CINEMA set citycode='330200', countycode='' where citycode in('331200','330282');
update OPEN_PLAYITEM set citycode='330200' where citycode in('331200','330282');
update MOVIELIST set citycode='330200' where citycode in('331200','330282');
update TICKET_ORDER set citycode='330200' where citycode in('331200','330282');
update FAILURE_ORDER set citycode='330200' where citycode in('331200','330282');
update BULLETIN set citycode='330200' where citycode in('331200','330282');
update SURVEY set citycode='330200' where citycode in('331200','330282');
update ADVERTISING set citycode='330200' where citycode in('331200','330282');
update GOODS set citycode='330200' where citycode in('331200','330282');		  
update GRABTICKETSUBJECT set citycode='330200' where citycode in('331200','330282');


update DIARY set citycode='330200' where citycode in('331200','330282');
update ACTIVITY set citycode='330200' where citycode in('331200','330282');
update NEWS set citycode='330200' where citycode in('331200','330282');
update GEWAQUESTION set citycode='330200' where citycode in('331200','330282');
update QUESTION set citycode='330200' where citycode in('331200','330282');
update DIARYCOMMENT c set citycode='330200' where citycode in('331200','330282');
update CUSTOMER_ANSWER set citycode='330200' where citycode in('331200','330282');
update CUSTOMER_QUESTION set citycode='330200' where citycode in('331200','330282');

update GEWACOMMEND set citycode='330200' where citycode in('331200','330282');
update USER_INVOICE set citycode='330200' where citycode in('331200','330282');
update PHONEADVERTISEMENT set citycode='330200' where citycode in('331200','330282');
update PLACE set citycode='330200' where citycode in('331200','330282');       
update RELATETOCITY set citycode='330200' where citycode in('331200','330282');
update CITYPRICE set citycode='330200' where citycode in('331200','330282');  

update GEWAMACHINE set citycode='330200' where citycode in('331200','330282');         --?
update ELECCARD_BATCH set citycode='330200' where citycode in('331200','330282');      --?
update SPECIALDISCOUNT set citycode='330200' where citycode in('331200','330282');     --?
update REWARD_CONFIG set citycode='330200' where citycode in('331200','330282');       --?
update MEMBERINFO set FROMCITY='330200' where FROMCITY in('331200','330282');



--昆山、常熟合并到苏州；

update CINEMA set citycode='320500', countycode='' where citycode in('320581','320583');
update OPEN_PLAYITEM set citycode='320500' where citycode in('320581','320583');
update MOVIELIST set citycode='320500' where citycode in('320581','320583');
update TICKET_ORDER set citycode='320500' where citycode in('320581','320583');
update FAILURE_ORDER set citycode='320500' where citycode in('320581','320583');
update BULLETIN set citycode='320500' where citycode in('320581','320583');
update SURVEY set citycode='320500' where citycode in('320581','320583');
update ADVERTISING set citycode='320500' where citycode in('320581','320583');
update GOODS set citycode='320500' where citycode in('320581','320583');	  
update GRABTICKETSUBJECT set citycode='320500' where citycode in('320581','320583');


update DIARY set citycode='320500' where citycode in('320581','320583');
update ACTIVITY set citycode='320500' where citycode in('320581','320583');
update NEWS set citycode='320500' where citycode in('320581','320583');
update GEWAQUESTION set citycode='320500' where citycode in('320581','320583');
update QUESTION set citycode='320500' where citycode in('320581','320583');
update DIARYCOMMENT c set citycode='320500' where citycode in('320581','320583');
update CUSTOMER_ANSWER set citycode='320500' where citycode in('320581','320583');
update CUSTOMER_QUESTION set citycode='320500' where citycode in('320581','320583');

update GEWACOMMEND set citycode='320500' where citycode in('320581','320583');
update USER_INVOICE set citycode='320500' where citycode in('320581','320583');
update PHONEADVERTISEMENT set citycode='320500' where citycode in('320581','320583');
update PLACE set citycode='320500' where citycode in('320581','320583');      
update RELATETOCITY set citycode='320500' where citycode in('320581','320583');
update CITYPRICE set citycode='320500' where citycode in('320581','320583'); 

update GEWAMACHINE set citycode='320500' where citycode in('320581','320583');         --?
update ELECCARD_BATCH set citycode='320500' where citycode in('320581','320583');      --?
update SPECIALDISCOUNT set citycode='320500' where citycode in('320581','320583');     --?
update REWARD_CONFIG set citycode='320500' where citycode in('320581','320583');      --?
update MEMBERINFO set FROMCITY='320500' where FROMCITY in('320581','320583');


--报表系统需要更新

update baobiaodata.ticketorder set citycode='330100' where citycode='330183';
update baobiaodata.goodsorder set citycode='330100' where citycode='330183';
update baobiaodata.ticketorder set citycode='330200' where citycode in('331200','330282');
update baobiaodata.goodsorder set citycode='330200' where citycode in('331200','330282');
update baobiaodata.ticketorder set citycode='320500' where citycode in('320581','320583');
update baobiaodata.goodsorder set citycode='320500' where citycode in('320581','320583');

update baobiaodata.open_playitem set citycode='330100' where citycode='330183';
update baobiaodata.open_playitem set citycode='330100' where citycode='330183';
update baobiaodata.open_playitem set citycode='330200' where citycode in('331200','330282');
update baobiaodata.open_playitem set citycode='330200' where citycode in('331200','330282');
update baobiaodata.open_playitem set citycode='320500' where citycode in('320581','320583');
update baobiaodata.open_playitem set citycode='320500' where citycode in('320581','320583');



select n.recordid as 影院ID, n.name as 影院名称, n.citycode as 城市代码, 
(select cityname from city c where c.citycode=n.citycode) as 城市名称 
from cinema n where n.citycode in('330183','331200','330282','320581','320583') order by citycode desc;

---更新到的影院
影院ID		影院名称					城市代码		城市名称
60318275	余姚天一时代影城			331200		余姚市
60318767	余姚新泗门时代影院		331200		余姚市
37795844	余姚华星影院				331200		余姚市
37796123	余姚影城					331200		余姚市
37796254	余姚龙山剧院				331200		余姚市
37796283	丈亭电影院				331200		余姚市
37796318	余姚人民电影院			331200		余姚市
37795554	余姚奥斯卡影院			331200		余姚市
37796444	慈溪时代电影大世界		330282		慈溪市
37796403	师桥电影院				330282		慈溪市
72453429	慈溪星美国际影城			330282		慈溪市
37795807	梅林电影院				330282		慈溪市
60362159	慈溪新浦晨亮影城			330282		慈溪市
60361629	慈溪欢乐小马影城			330282		慈溪市
60360806	慈溪恒丰数码影院			330282		慈溪市
60359876	周巷奥克雷影城			330282		慈溪市
38000760	富阳横店电影城			330183		富阳市
38000365	富阳时代电影大世界		330183		富阳市
38214782	富阳影剧院				330183		富阳市
47820975	昆山世茂时尚欢乐影城		320583		昆山市
47821367	中影国际影城-昆山花桥店	320583		昆山市
47835652	金逸国际电影城-昆山店		320583		昆山市
47835236	西园影城					320583		昆山市
38812160	常熟京门影城				320581		常熟市
38817703	常熟欢乐印象影城			320581		常熟市
38798985	星美国际影城-常熟店		320581		常熟市
38810725	卢米埃世茂影城			320581		常熟市
38816535	大地数字影院-美城休闲广场	320581		常熟市


---需要清除缓存
com.gewara.model.movie.Cinema
com.gewara.model.ticket.OpenPlayItem
com.gewara.model.movie.MoviePlayItem
com.gewara.model.common.Bulletin
com.gewara.model.common.Survey
com.gewara.model.ad.Advertising
com.gewara.model.common.GewaCommend
com.gewara.model.common.CityPrice
com.gewara.model.pay.ElecCardBatch
com.gewara.model.pay.BaseGoods
com.gewara.model.pay.Goods
com.gewara.model.pay.SportGoods
com.gewara.model.movie.GrabTicketSubject
com.gewara.model.relate.SpecialDiscount
