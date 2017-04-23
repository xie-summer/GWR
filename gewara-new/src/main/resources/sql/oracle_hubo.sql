--- 20120709 MongoDB API同步
INSERT INTO gewaconfig (recordid,newcontent,description,updatetime) 
VALUES (199, '2012-07-09','MongoDB API synch', sysdate)



--- 20120208 gewaConfig表中加入白名单
INSERT INTO gewaconfig (recordid,newcontent,description,updatetime) 
VALUES (140, '222.68.188.78,180.169.7.58','gewa write list', sysdate)




---- 20111207 Cinema表中 otherinfo 小写3d变大写3D --------------------
update cinema t set t.otherinfo = regexp_replace(t.otherinfo,'3d','3D')





----------*************** 以后更新的内容为该分割线之前**********************************************************--------------------------
----------*************************************************************************************************--------------------------
-------------------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------------------

---- 20111205 GEWAKEYSEARCH物化视图做table --------------------
create table webdata.NEWSEARCHKEY(
NAME 		  VARCHAR2(200),
SEARCHKEY 	  VARCHAR2(1000),
KEYCODE 	  VARCHAR2(52),
RELATEDID 	  NUMBER(19),
TAG 		  VARCHAR2(50),
CATEGORY 	  VARCHAR2(12),
SKSORT  	  NUMBER,
STATUS  	  VARCHAR2(20),
TIMENUM 	  NUMBER
) tablespace webdata;

insert into NEWSEARCHKEY SELECT c.name, searchkey, 'cinema'||recordid as keycode, recordid as relatedid, 'cinema' as tag, 'cinema' as category, 95 as sksort, 'Y' as status, to_number(to_char(adddate,'yyyymmddhhmiss')) as timenum FROM cinema c;
insert into NEWSEARCHKEY SELECT b.name, searchkey, 'bar'||recordid as keycode, recordid as relatedid, 'bar' as tag, 'bar' as category, 90 as sksort, 'Y' as status, to_number(to_char(adddate,'yyyymmddhhmiss')) as timenum FROM bar b;
insert into NEWSEARCHKEY SELECT s.name, searchkey, 'sport'||recordid as keycode, recordid as relatedid, 'sport' as tag, 'sport' as category, 90 as sksort, 'Y' as status, to_number(to_char(adddate,'yyyymmddhhmiss')) as timenum FROM sport s;
insert into NEWSEARCHKEY SELECT g.name, searchkey, 'gym'||recordid as keycode, recordid as relatedid, 'gym' as tag, 'gym' as category, 90 as sksort, 'Y' as status, to_number(to_char(adddate,'yyyymmddhhmiss')) as timenum FROM gym g;
insert into NEWSEARCHKEY SELECT k.name, searchkey, 'ktv'||recordid as keycode, recordid as relatedid, 'ktv' as tag, 'ktv' as category, 90 as sksort, 'Y' as status, to_number(to_char(adddate,'yyyymmddhhmiss')) as timenum FROM ktv k;
insert into NEWSEARCHKEY SELECT t.name, searchkey, 'theatre'||recordid as keycode, recordid as relatedid, 'theatre' as tag, 'theatre' as category, 90 as sksort, 'Y' as status, to_number(to_char(adddate,'yyyymmddhhmiss')) as timenum FROM theatre t;
insert into NEWSEARCHKEY SELECT m.moviename as name, searchkey, 'movie'||recordid as keycode, recordid as relatedid, 'cinema' as tag, 'movie' as category, 95 as sksort, 'Y' as status, to_number(to_char(adddate,'yyyymmddhhmiss')) as timenum FROM movie m;
insert into NEWSEARCHKEY SELECT d.dramaname as name, searchkey, 'drama'||recordid as keycode, recordid as relatedid, 'theatre' as tag, 'drama' as category, 95 as sksort, 'Y' as status, to_number(to_char(adddate,'yyyymmddhhmiss')) as timenum FROM drama d;
insert into NEWSEARCHKEY SELECT ds.name, searchkey, 'dramastar'||recordid as keycode, recordid as relatedid, 'drama' as tag, 'dramastar' as category, 80 as sksort, 'Y' as status, to_number(to_char(addtime,'yyyymmddhhmiss')) as timenum FROM drama_star ds;
insert into NEWSEARCHKEY SELECT a.title as name, searchkey, 'activity'||recordid as keycode, recordid as relatedid, tag, 'activity' as category, 80 as sksort, a.status as status, to_number(to_char(addtime,'yyyymmddhhmiss')) as timenum FROM activity a;
insert into NEWSEARCHKEY SELECT bs.singername as name, searchkey, 'barsinger'||recordid as keycode, recordid as relatedid, 'bar' as tag, 'barsinger' as category, 80 as sksort, 'Y' as status, to_number(to_char(addtime,'yyyymmddhhmiss')) as timenum FROM barsinger bs;
insert into NEWSEARCHKEY SELECT gc.coachname as name, searchkey, 'gymcoach'||recordid as keycode, recordid as relatedid, 'gym' as tag, 'gymcoach' as category, 80 as sksort, 'Y' as status, to_number(to_char(addtime,'yyyymmddhhmiss')) as timenum FROM gymcoach gc;
insert into NEWSEARCHKEY SELECT c.coursename as name, searchkey, 'gymcourse'||recordid as keycode, recordid as relatedid, 'gym' as tag, 'gymcourse' as category, 100 as sksort, 'Y' as status, to_number(to_char(updatetime,'yyyymmddhhmiss')) as timenum FROM gymcourse c where recordid>0;
insert into NEWSEARCHKEY SELECT si.itemname as name, searchkey, 'sportservice'||recordid as keycode, recordid as relatedid, 'sport' as tag, 'sportservice' as category, 100 as sksort, 'Y' as status, to_number(to_char(updatetime,'yyyymmddhhmiss')) as timenum FROM sportitem si;
insert into NEWSEARCHKEY SELECT title as name, title as searchkey, 'gewaquestion'||recordid as keycode, recordid as relatedid, tag, 'gewaquestion' as category, 80 as sksort, gq.status as status, to_number(to_char(addtime,'yyyymmddhhmiss')) as timenum FROM gewaquestion gq;
insert into NEWSEARCHKEY SELECT newstitle as name, newstitle as searchkey, 'news'||recordid as keycode, recordid as relatedid, tag, 'news' as category, 80 as sksort, 'Y' as status, to_number(to_char(addtime,'yyyymmddhhmiss')) as timenum FROM news n;
insert into NEWSEARCHKEY SELECT videotitle as name, videotitle as searchkey, 'video'||recordid as keycode, recordid as relatedid, 'cinema' as tag, 'video' as category, 88 as sksort, 'Y' as status, to_number(to_char(addtime,'yyyymmddhhmiss')) as timenum FROM video v;
insert into NEWSEARCHKEY SELECT subject as name, subject as searchkey, 'diary'||recordid as keycode, recordid as relatedid, tag, 'diary' as category, 80 as sksort, d.status as status, to_number(to_char(addtime,'yyyymmddhhmiss')) as timenum FROM diary d;
insert into NEWSEARCHKEY SELECT name, name as searchkey, 'commu'||recordid as keycode, recordid as relatedid, tag, 'commu' as category, 85 as sksort, c.status as status, to_number(to_char(addtime,'yyyymmddhhmiss')) as timenum FROM COMMU c;


alter table webdata.NEWSEARCHKEY add constraint pk_NEWSEARCHKEY primary key (keycode) 
  using index( create unique index webdata.idxpk_NEWSEARCHKEY on webdata.NEWSEARCHKEY(keycode) tablespace tbs_index);



---- 20111205 场馆数据加入字段otherinfo--------------------
alter table KTV add OTHERINFO VARCHAR2(1000) ;
alter table GYM add OTHERINFO VARCHAR2(1000) ;
alter table BAR add OTHERINFO VARCHAR2(1000) ;

---- 20111202 哇啦话题增加条数  --------
alter table MODERATOR add COMMENTCOUNT number(19) default 0 not null;



---- 20111104 GewaConfig 加入 表拆分升级后版本控制
INSERT INTO gewaconfig (recordid,content,description,updatetime) 
VALUES (99, '{"pointUpdate":"2011-09-01 00:00:00"}','history update Timestamp control', sysdate)



---- 20111027 GewaConfig 加入 MemCache版本控制
INSERT INTO gewaconfig (recordid,content,description,updatetime) VALUES (35, '{}','memcached version control', sysdate)
{"oneMin":"v1","tenMin":"v10","halfHour":"v30","oneHour":"v60","twoHour":"v120","halfDay":"v720"}


重要!!!!!!!!!!!!! 提交到74前 更新 gewaconfig

--- 20111014 GEWAMACHINE 长度增加
alter table GEWAMACHINE modify IPREMARK VARCHAR2(200);


--- 20110926 GewaConfig 加入 论坛城市分享 (杭州，南京，广州，成都，重庆)
INSERT INTO gewaconfig VALUES (70, '330100,320100,440100,510100,500000', 'Set Diary CityShare')
-- 需要后台加入


--- 20110913 HeadInfo-头信息 需要加入标识
alter table HEADINFO add BOARD VARCHAR2(20) ;
update HEADINFO t set t.BOARD = 'movie' where t.BOARD is null;



-- wala URL重写
#wala
RewriteRule "^/wala/(\d+)$" "/wala/microMemberDetail.xhtml?memberid=$1" [QSA,PT]



--- 20110901 话剧明星与导演之前的脏数据
select t.name from drama_star t group by t.name having count(*) > 1;
select t.recordid, t.name from  drama_star t where t.startype = 'director' and t.name = ?
select * from drama_to_star t where t.starid in  (42478336,39852913);
delete from drama_to_star t where t.recordid in (42478336,39852913);




--- 20110901 话剧明星加入新字段
alter table drama_star add REPRESENTATIVE VARCHAR2(500) ;
alter table drama_star add REPRESENTATIVERELATE VARCHAR2(1000) ;




-- 20110818 商家后台修改电影院信息报错
alter table CINEMA modify VISACARD VARCHAR2(200);



--- 20110805 MEMBERMARK
alter table MEMBERMARK modify MARKCONTENT VARCHAR2(500);



--- 20110803 MEMBERINFO
ALTER TABLE MEMBERINFO ADD INVITEID number(19);
UPDATE MEMBERINFO T SET T.INVITEID = (SELECT S.INVITEID FROM MEMBER S WHERE S.RECORDID = T.RECORDID) where t.INVITEID is null;

ALTER TABLE MEMBER DROP COLUMN INVITEID;


--- 20110803 MEMBERINFO
update memberinfo t set t.fromcity = '310000';
ALTER TABLE memberinfo DROP COLUMN fromprovince;
ALTER TABLE memberinfo DROP COLUMN fromcounty;
ALTER TABLE memberinfo DROP COLUMN fromindexarea;
ALTER TABLE memberinfo DROP COLUMN workprovince;
ALTER TABLE memberinfo DROP COLUMN workcity;
ALTER TABLE memberinfo DROP COLUMN workcounty;
ALTER TABLE memberinfo DROP COLUMN workindexarea;



--- 20110729 point表添加区别标识
alter table POINT add UNIQUETAG VARCHAR2(100) ;


--- 20110726 GYM Bug
alter table GYM modify SEARCHKEY VARCHAR2(2000);



---- 20110725 
select count(*) from favoritetag t where t.tag is null ;
delete from favoritetag t where t.tag is null ;
1. 删除tag为空的记录
2. 去掉之前的 recordid PK
3. 去掉recordid 字段.
4. 加入tag_pk 主键.



--- 20110721 哇啦
--更改哇啦话题物化视图
drop materialized view MODERATOR_STAT;
create materialized view MODERATOR_STAT
refresh complete on demand
start with to_date('01-01-2011 14:59:39', 'dd-mm-yyyy hh24:mi:ss') next SYSDATE+1/72 
as
select t.topic, count(*) as total
from c_comment t where t.topic is not null
group by t.topic; 



---20110714 GewaCommend 表中加入 smalllogo 字段,方便新闻双图片
alter table GEWACOMMEND add SMALLLOGO VARCHAR2(200) ;



---20110713 哇啦 加入话题字段
alter table C_COMMENT add TOPIC VARCHAR2(200) ;
update C_COMMENT m set m.TOPIC = regexp_substr(regexp_substr(m.body, '#([^#]+)#'), '([^#]+)') where m.topic is null;
-- Drop indexes 
drop index IDX_ADDRESS;
-- Create/Recreate indexes 
create index idx_comment_topic on C_COMMENT (topic);

select max(length(body)) from c_comment;




--- 20110711 圈子背景图
alter table COMMU add commubg VARCHAR2(500) ;




--- 20110624 将哇啦/帖子中flag为null的字段飞掉
update c_comment s set s.flag = replace(s.flag, 'null,', '') where s.flag like '%null,%';
update diary s set s.flag = replace(s.flag, 'null,', '') where s.flag like '%null,%'




---20110613 后台专题模板 加入手动URL
alter table SPECIAL_ACTIVITY add REALURL VARCHAR2(500) ;



---20110531 后台专题模板 加入状态
alter table SPECIAL_ACTIVITY add STATUS VARCHAR2(10) default 'Y';
--update SPECIAL_ACTIVITY t set t.status = 'Y';




-- 修正帖子关联哇啦时 数据库问题
-- 1. 新建字段 xxx_count 
ALTER TABLE c_comment ADD xxx_count  number(19);

-- 2. 将数值更新到 xxx_count
update c_comment m set m.xxx_count = to_number(regexp_substr(regexp_substr(m.body, 'http://www.gewara.com/blog/t(\d+)'), '(\d+)')) 
where m.body like '%http://www.gewara.com/blog/t%' and m.tag = 'topic';

-- 3. 更新字段
update c_comment t set t.tag = 'diary_wala', t.relatedid = t.xxx_count 
where t.xxx_count is not null and t.tag = 'topic' ;

-- 4. 废除字段
ALTER TABLE c_comment DROP COLUMN xxx_count;



--- 数据库定时任务更新语句
update movie m set m.boughtcount = (select sum(t.quantity) from ticket_order t 
where t.status='paid_success' and t.order_type = 'ticket' and m.recordid=t.movieid)
where exists(select t.movieid from ticket_order t where t.status='paid_success' and t.order_type = 'ticket' and m.recordid=t.movieid);

--- 话剧院字段修改
ALTER TABLE THEATRE RENAME COLUMN CONTENT TO CONTENT1;
ALTER TABLE THEATRE ADD CONTENT  clob;
UPDATE THEATRE SET CONTENT = CONTENT1 ;
ALTER TABLE THEATRE DROP COLUMN CONTENT1;



---- 20110428 JsonData 增加字段(针对全局站内信)
alter table JSONDATA add TAG VARCHAR2(50);
alter table JSONDATA add VALIDTIME TIMESTAMP(6);




--- 20110426 Movie增加购票人数字段, 仅供页面查询使用
alter table MOVIE add BOUGHTCOUNT NUMBER(19);
update MOVIE t set t.boughtcount = 0 ;



--- 20110402 更新新闻 修改显示所在城市字段
alter table NEWS modify CITYCODE VARCHAR2(300);
update news t set t.citycode = '000000' where t.division = 'N';
alter table NEWS drop column DIVISION;

--- 20110402 更新活动 修改城市字段长度
alter table ACTIVITY modify CITYCODE VARCHAR2(300);




--- 20110325 图片上传公用类 添加字段
alter table UPLOAD_PIC add TAG VARCHAR2(20);
alter table UPLOAD_PIC add RELATEDID NUMBER(19);


--- 20110324 去掉圈子成员黑名单字段, 加入标识字段
alter table COMMUMEMBER drop column BLACKMEMBER;
alter table COMMUMEMBER add FLAG varchar2(20);


--- 20110322 修改站内信状态字段(原字段太长)
update system_message_action s set s.action = 'commu_apply' where s.action = 'commu_applycertification';
commit;



--- 20110310 投诉建议 回复
create table CUSTOMER_ANSWER
(
  RECORDID   NUMBER(19) not null,
  QUESTIONID NUMBER(19),
  MEMBERID NUMBER(19),
  BODY VARCHAR2(4000),
  ADDTIME  TIMESTAMP(6),
  STATUS VARCHAR2(100),
  UPDATETIME  TIMESTAMP(6)
);

alter table CUSTOMER_ANSWER
  add primary key (RECORDID)
  using index 
  tablespace WEBDATA
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
commit;



--- 20110310 投诉建议 提问
create table CUSTOMER_QUESTION
(
  RECORDID   NUMBER(19) not null,
  MEMBERID NUMBER(19),
  MEMBERNAME VARCHAR2(100),
  SUBJECT VARCHAR2(500),
  BODY VARCHAR2(4000),
  ADDTIME  TIMESTAMP(6),
  CLICKEDTIMES  NUMBER(19),
  TAG VARCHAR2(100),
  STATUS VARCHAR2(100),
  REPLYTIME  TIMESTAMP(6),
  REPLYCOUNT NUMBER(19),
  REPLYID NUMBER(19),
  REPLYNAME VARCHAR2(100),
  TYPE VARCHAR2(100),
  UPDATETIME  TIMESTAMP(6),
  CITYCODE VARCHAR2(100)
);

alter table CUSTOMER_QUESTION
  add primary key (RECORDID)
  using index 
  tablespace WEBDATA
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
commit;



-- 圈王之王 后台菜单
-- /admin/audit/checkCommuKingList.xhtml

---------------------------------------------- 圈王之王系列
-- 1. 建立关联表, 该表可以为系统共用
create table RELATIONSHIP
(
   RECORDID    NUMBER(19) not null,
   TAG        VARCHAR2(100),      -- 标识:( 关联的标签)
     RELATEDID1  NUMBER(19),
     RELATEDID2  NUMBER(19)
);
alter table RELATIONSHIP
  add primary key (RECORDID)
  using index 
  tablespace WEBDATA
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
commit;

-- 2. 圈王之王 表
create table COMMUKING
(
  RECORDID    NUMBER(19) not null,
  TAG        VARCHAR2(100),      -- 标识:( 参与活动的标签)
  ACTIVIID   NUMBER(19),
  COMMUID   NUMBER(19),
  MEMBERID   NUMBER(19),
  VOTECOUNT   NUMBER(19),
  STATUS   VARCHAR2(100)
);

alter table COMMUKING
  add primary key (RECORDID)
  using index 
  tablespace WEBDATA
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
commit;
alter table COMMUKING add addtime TIMESTAMP(6);

-- 20110221 个人中心模块, 添加 兴趣爱好, 教育经历, 职业信息, 保存在一张表里.

-- memberinfo 表里增加字段
alter table MEMBERINFO add FAVORTAG VARCHAR2(500);

create table MEMBERINFOMORE
(
  RECORDID NUMBER(19) not null,
  MEMBERID NUMBER(19) not null,
  TAG VARCHAR2(20),
  NAME VARCHAR2(200),
  SCHOOLADDRESS VARCHAR2(200),
  SCHOOLTYPE VARCHAR2(20),
  EDUCOMEINYEAR  VARCHAR2(20),
  JOBPROVINCE    VARCHAR2(50),
  JOBCITY      VARCHAR2(50),
  JOBDEP        VARCHAR2(100),
  JOBCOMPANYEMAIL  VARCHAR2(100)
);

alter table MEMBERINFOMORE
  add primary key (RECORDID)
  using index 
  tablespace WEBDATA
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
commit;


-- 兴趣标签表
create table FAVORITETAG
(
  RECORDID    NUMBER(19) not null,
  TAG        VARCHAR2(100),      -- 标识:( 用户输入的标签)
  CLICKCOUNT  NUMBER(19) 
);

alter table FAVORITETAG
  add primary key (RECORDID)
  using index 
  tablespace WEBDATA
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
commit;



-- 20101229 修改 drama 表 出品人 字段长度
alter table DRAMA modify DRAMACOMPANY VARCHAR2(4000);
--********* 2011 / 01 / 01 ****************************************************************************--
--20110121 更新 diary,  question 增加 citycode 字段, 同时, 更新为上海(310000)

alter table QUESTION add CITYCODE VARCHAR2(10);
update QUESTION t set t.citycode = '310000';
commit;

alter table GEWAQUESTION add CITYCODE VARCHAR2(10);
update GEWAQUESTION t set t.citycode = '310000';
commit;

alter table DIARY add CITYCODE VARCHAR2(10);
update DIARY t set t.citycode = '310000';
commit;

alter table DIARYCOMMENT add CITYCODE VARCHAR2(10);
update DIARYCOMMENT t set t.citycode = '310000';
commit;

alter table EXAM_LIB add CITYCODE VARCHAR2(10);
update EXAM_LIB t set t.citycode = '310000';
commit;




-- 20101207 话剧_明星 关联表
create table DRAMA_TO_STAR
(
  RECORDID		NUMBER(19) not null,
  DRAMAID		NUMBER(19) not null,
  STARID		 	NUMBER(19) not null
);

alter table DRAMA_TO_STAR
  add primary key (RECORDID)
  using index 
  tablespace WEBDATA
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
  
 
-- 修改star content的长度
/*修改原字段名*/
ALTER TABLE DRAMA_STAR RENAME COLUMN CONTENT TO CONTENT1;
/*添加一个和原字段同名的字段*/
ALTER TABLE DRAMA_STAR ADD CONTENT  clob;
/*更新*/
UPDATE DRAMA_STAR SET CONTENT = CONTENT1 ;
/*删除原来的备份字段*/
ALTER TABLE DRAMA_STAR DROP COLUMN CONTENT1;
 



--20101130 添加话剧明星表
drop table DRAMA_STAR;
create table DRAMA_STAR
(
  RECORDID      NUMBER(19) not null,
  name			VARCHAR2(100),
  birthday      TIMESTAMP(6),	
  state   		 VARCHAR2(20),
  bloodtype		 VARCHAR2(20),				
  constellation		 	 VARCHAR2(20),
  height       VARCHAR2(10),
  graduated		 VARCHAR2(200),	
  hometown		VARCHAR2(200),
  job        VARCHAR2(200),
  troupe        NUMBER(19),
  searchkey        VARCHAR2(500),
  establishtime      TIMESTAMP(6),	-- 剧团成立时间
  startype		VARCHAR2(10),			-- 类型(明星/剧团)
  englishname        VARCHAR2(100),
  pinyin        VARCHAR2(100),
  content        VARCHAR2(500),
  briefname        VARCHAR2(100),
  seotitle        VARCHAR2(100),
  seodescription        VARCHAR2(500),
  addtime        TIMESTAMP(6),	
  updatetime        TIMESTAMP(6),	
  updatemember        NUMBER(19),
  hotvalue        NUMBER(10),
  logo        VARCHAR2(500),
  clickedtimes        NUMBER(10),
  quguo         NUMBER(10),
  xiangqu         NUMBER(10),
  collectedtimes        NUMBER(10),
  generalmark         NUMBER(10),
  generalmarkedtimes         NUMBER(10),
  avggeneral        NUMBER(10)
);

alter table DRAMA_STAR
  add primary key (RECORDID)
  using index 
  tablespace WEBDATA
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
-- 20101207 添加字段 website
alter table DRAMA_STAR add WEBSITE VARCHAR2(200);







----------------  话剧 -------------------------------------------------------------
--20101128 话剧
--# 话剧 后台菜单
话剧数据
	- 推荐数据 /admin/drame/dramaCommend.xhtml
	- 明星管理 /admin/drama/dramaStarList.xhtml
	
	- 提问登录: 修改权限 /qa/mod* -> member组

--# 20101122 commu表 新增状态字段
alter table COMMU add CHECKSTATUS VARCHAR2(10);
-- 更新外网数据库, 设置所有为 'N' - 未审核
update COMMU set CHECKSTATUS = 'N';

--# 20101125 commu_manage 表添加 支付宝姓名/支付宝账户
alter table COMMU_MANAGE add ALIPAYNAME VARCHAR2(20);
alter table COMMU_MANAGE add ALIPAY VARCHAR2(100);

--# 20101126 创建圈子-财务 关联表
create table COMMU_ACTIVTY_PAY
(
  RECORDID      NUMBER(19) not null,
  RELATEDID		 VARCHAR2(100),			-- 收费活动ID, 多个
  PARENTID      NUMBER(19) not null,	-- 圈子ID
  CHECKSTATUS   VARCHAR2(10),
  APPLYFEE		 NUMBER(10),				
  PAYFEE		 	 NUMBER(10),
  ADDTIME       TIMESTAMP(6),
  PAYTIME		 TIMESTAMP(6),				-- 支付时间
  REMARK        VARCHAR2(500)
);

alter table COMMU_ACTIVTY_PAY
  add primary key (RECORDID)
  using index 
  tablespace WEBDATA
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );


--  补充圈子后台菜单
圈子管理
	|-		/admin/audit/checkApplyCommuList.xhtml		圈子认证管理
	|-		/admin/audit/checkApplyActivityList.xhtml	活动审核管理
	|-		/admin/audit/checkofFinanceList.xhtml			活动财务审核







--------------------------------------------------------------------------------------------
--#20101108 - 圈子认证
create table COMMU_MANAGE
(
  RECORDID      NUMBER(19) not null,
  COMMUID       NUMBER(19) not null,
  APPLYMEMBERID NUMBER(19) not null,
  CHECKSTATUS   VARCHAR2(10),
  ADDTIME       TIMESTAMP(6),
  REALNAME      VARCHAR2(50),
  SEX           NUMBER(2),
  IDNUMBER      VARCHAR2(30),
  CONTACTPHONE  VARCHAR2(15),
  EMAIL   		 VARCHAR2(50),
  QQ         	 VARCHAR2(20),
  MSN         	 VARCHAR2(20),
  REMARK        VARCHAR2(500)
)

alter table COMMU_MANAGE
  add primary key (RECORDID)
  using index 
  tablespace WEBDATA
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
  
--# 20101111 活动表 新增字段
alter table ACTIVITY add NEEDPREPAY VARCHAR2(10);
alter table ACTIVITY add PREPAYPRICE VARCHAR2(20);
alter table ACTIVITY add PREPAYNOTE VARCHAR2(200);

--# 20101111 Goods 表 新增字段
alter table GOODS add PARENTID NUMBER(19);

-- #20101111 后台管理
22|22  	 	圈子管理
2201|2201  	/admin/common/checkApplyCommuList.xhtml  	审核管理







#20100929 新闻模板 添加模板图片字段
-- Add/modify columns 
alter table NEWS add TPLLOGO VARCHAR2(100);


----	圈子系列 ----------------------------------------------------
--#20101014 圈子表 增加字段 兴趣标签
alter table COMMU add INTERESTTAG varchar2(300);
--#20101021 圈子表 增加字段 兴趣标签
alter table COMMU add INDEXAREACODE varchar2(10);



--#20101027 -哇啦 -创建视图
create materialized view moderator_stat
refresh complete on demand
start with to_date('27-10-2010 13:56:23', 'dd-mm-yyyy hh24:mi:ss') next SYSDATE+1/48 
as
select t.recordid, t.title, count(*) as total
from moderator t left join c_comment c on c.body like '%'||t.title||'%'
group by t.recordid, t.title





#20100928 修改原字段的长度
-- Add/modify columns 
alter table SPECIAL_ACTIVITY modify SEOKEYWORDS VARCHAR2(200);
alter table SPECIAL_ACTIVITY modify SEODESCRIPTION VARCHAR2(500);
alter table SPECIAL_ACTIVITY modify WALATITLE VARCHAR2(100);
alter table SPECIAL_ACTIVITY modify ACTTITLE VARCHAR2(100);
alter table SPECIAL_ACTIVITY modify SURVEYTITLE VARCHAR2(100);
alter table SPECIAL_ACTIVITY modify ANSWERTITLE VARCHAR2(100);
alter table SPECIAL_ACTIVITY modify BLOGTITLE VARCHAR2(100);
alter table SPECIAL_ACTIVITY modify TEAMPICTITLE VARCHAR2(100);


#20100925 添加
模块URL：	/admin/blog/newscommentList.xhtml?tag=cinemanews
标题：		新闻回复
主菜单：	论坛管理(20)
alter table SPECIAL_ACTIVITY add LOGO NUMBER(19);

-- Add/modify columns  表 SPECIAL_ACTIVITY
alter table SPECIAL_ACTIVITY modify CONTENT null;
alter table SPECIAL_ACTIVITY add ADDTIME TIMESTAMP(6);
alter table SPECIAL_ACTIVITY add SEOKEYWORDS VARCHAR2(100);
alter table SPECIAL_ACTIVITY add SEODESCRIPTION VARCHAR2(200);
alter table SPECIAL_ACTIVITY add HEADPIC NUMBER(19);
alter table SPECIAL_ACTIVITY add WALATITLE VARCHAR2(20);
alter table SPECIAL_ACTIVITY add ACTTITLE VARCHAR2(20);
alter table SPECIAL_ACTIVITY add SURVEYTITLE VARCHAR2(20);
alter table SPECIAL_ACTIVITY add ANSWERTITLE VARCHAR2(20);
alter table SPECIAL_ACTIVITY add BLOGTITLE VARCHAR2(20);
alter table SPECIAL_ACTIVITY add BLOGPIC NUMBER(19);
alter table SPECIAL_ACTIVITY add TEAMPICTITLE VARCHAR2(20);


后台左侧menu
/admin/message/batchPM.xhtml  	发布公告
/admin/sns/simpleSearchSnsListPage.xhtml 用户查询 (放在用户管理模块下)

