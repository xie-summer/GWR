---2012-08-16
---PROGRAM_ITEM_TIME添加citycode
alter table webdata.PROGRAM_ITEM_TIME add CITYCODE VARCHAR2 (6);
update webdata.PROGRAM_ITEM_TIME set CITYCODE='310000';
update webdata.OPEN_TIMEITEM set CITYCODE='310000' where citycode is null;
update webdata.MOVIE set playdate = to_char(releasedate,'YYYY-MM-DD') where releasedate is not null;

-- 20120619	CUSTOMER_QUESTION添加FEEDBACKTYPE字段
alter table CUSTOMER_QUESTION add FEEDBACKTYPE VARCHAR2(20) default 'other'; --反馈归属类型

-- 20120619	SYNCH添加IP字段
alter table SYNCH add IP VARCHAR2(15);  --同步IP

-- 20121011	TICKET_ORDER添加RESTATUS字段
alter table webdata.TICKET_ORDER add RESTATUS VARCHAR2(4);  --订单删除状态

--20121105
alter table webdata.MOVIE add EDITION VARCHAR2(50); --电影版本
