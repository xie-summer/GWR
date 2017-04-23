create table failure_order as 
select * from ticket_order where createtime<to_date('20120101','yyyymmdd') and status not like 'paid%' and order_type='ticket';--1111597

delete from ticket_order where createtime<to_date('20110101','yyyymmdd') and status not like 'paid%' and order_type='ticket'; --119264
delete from ticket_order where createtime<to_date('20110601','yyyymmdd') and status not like 'paid%' and order_type='ticket'; --119942
delete from ticket_order where createtime<to_date('20110720','yyyymmdd') and status not like 'paid%' and order_type='ticket'; --82065
delete from ticket_order where createtime<to_date('20110801','yyyymmdd') and status not like 'paid%' and order_type='ticket'; --94530
delete from ticket_order where createtime<to_date('20110820','yyyymmdd') and status not like 'paid%' and order_type='ticket'; --108329
delete from ticket_order where createtime<to_date('20110915','yyyymmdd') and status not like 'paid%' and order_type='ticket'; --95125
delete from ticket_order where createtime<to_date('20111110','yyyymmdd') and status not like 'paid%' and order_type='ticket'; --107402
delete from ticket_order where createtime<to_date('20111201','yyyymmdd') and status not like 'paid%' and order_type='ticket'; --132892
delete from ticket_order where createtime<to_date('20111220','yyyymmdd') and status not like 'paid%' and order_type='ticket'; --119271
delete from ticket_order where createtime<to_date('20120101','yyyymmdd') and status not like 'paid%' and order_type='ticket'; --132777


delete from ORDER_OPENSEAT t where exists (select recordid from failure_order s where s.recordid=t.ORDER_ID) and rownum<100000;

create table discount_item_failure as 
select * from discount_item m where m.orderid in (select recordid from failure_order t);

delete from discount_item m where m.orderid in (select recordid from failure_order t);

insert into ticket_order select * from failure_order where status='new_confirm' and addtime>to_date('20111201','yyyymmdd');
delete from failure_order where status='new_confirm' and addtime>to_date('20111201','yyyymmdd');


----------------------20120516-------------------------------------
alter table SELLSEAT_HIST modify remark varchar2(1500);
insert into sellseat_hist select * from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('2012-05-01','yyyy-mm-dd'));--1915307
delete from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('2012-01-01','yyyy-mm-dd'));--0
delete from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('2012-02-01','yyyy-mm-dd'));--476366
delete from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('2012-03-01','yyyy-mm-dd'));--549430
delete from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('2012-04-01','yyyy-mm-dd'));--289515
delete from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('2012-05-01','yyyy-mm-dd'));--599996

insert into failure_order select * from ticket_order where createtime<to_date('20120501','yyyymmdd') and status not like 'paid%' and order_type='ticket';
delete from ticket_order where createtime<to_date('20120215','yyyymmdd') and status not like 'paid%' and order_type='ticket';--442642
delete from ticket_order where createtime<to_date('20120301','yyyymmdd') and status not like 'paid%' and order_type='ticket';--138874
delete from ticket_order where createtime<to_date('20120315','yyyymmdd') and status not like 'paid%' and order_type='ticket';--41338
delete from ticket_order where createtime<to_date('20120401','yyyymmdd') and status not like 'paid%' and order_type='ticket';--55706
delete from ticket_order where createtime<to_date('20120415','yyyymmdd') and status not like 'paid%' and order_type='ticket';--103764
delete from ticket_order where createtime<to_date('20120501','yyyymmdd') and status not like 'paid%' and order_type='ticket';--118244

insert into discount_item_failure select * from discount_item m where m.orderid in (select recordid from failure_order t);--95952
delete from discount_item m where m.orderid in (select recordid from failure_order t);--95952

-----------------------------------------------------------------------------------------------------------
create table sellseat_hist as select * from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('2011-12-15','yyyy-mm-dd'));

delete from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('2011-02-15','yyyy-mm-dd'));
delete from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('2011-05-15','yyyy-mm-dd'));
delete from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('2011-06-15','yyyy-mm-dd'));
delete from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('2011-07-15','yyyy-mm-dd'));
delete from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('2011-08-01','yyyy-mm-dd'));
delete from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('2011-08-15','yyyy-mm-dd'));
delete from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('2011-09-01','yyyy-mm-dd'));
delete from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('2011-10-01','yyyy-mm-dd'));
delete from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('2011-11-01','yyyy-mm-dd'));
delete from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('2011-12-15','yyyy-mm-dd'));

--------------------------------------------20120922---------------------------
alter table failure_order add wabi number(8);
create table ticket_order_bak20120922 as select * from ticket_order;

alter table ticket_order drop column AID;
alter table ticket_order drop column  BUSSPAID;
alter table ticket_order drop column  STTID;
alter table ticket_order drop column  SNO;
alter table ticket_order drop column  DESCRIPTION;

update failure_order set wabi=0;

insert into failure_order 
select * from ticket_order where createtime<to_date('20120801','yyyymmdd') and status not like 'paid%' and order_type='ticket'; --527585 rows created.

delete from ticket_order where createtime<to_date('20120601','yyyymmdd') and status not like 'paid%' and order_type='ticket';--181764
delete from ticket_order where createtime<to_date('20120615','yyyymmdd') and status not like 'paid%' and order_type='ticket';--77720
delete from ticket_order where createtime<to_date('20120710','yyyymmdd') and status not like 'paid%' and order_type='ticket';--161646
delete from ticket_order where createtime<to_date('20120801','yyyymmdd') and status not like 'paid%' and order_type='ticket';--106455

insert into failure_order 
select * from ticket_order where createtime<to_date('20120801','yyyymmdd') and status not like 'paid%';--65820
delete from ticket_order where createtime<to_date('20120801','yyyymmdd') and status not like 'paid%';--65820

--//TODO:删除此备份
drop table ticket_order_bak20120922
-------------------20121122----------------------
delete from order_openseat where order_id<51005939;--1333367 rows deleted
delete from order_openseat where order_id<55005939;--708589
delete from order_openseat where order_id<60005939;--1447957
delete from order_openseat where order_id<63000000;--535739
delete from order_openseat where order_id<67922484;--1277605,addtime<sysdate-180
delete from order_openseat where order_id<78552034;--1874088
delete from order_openseat where order_id<84552034;--1348417,addtime<sysdate-60
delete from discount_item m where m.orderid in (select recordid from failure_order t);--141534

------------------20121228------------------------
insert into failure_order 
select * from ticket_order where createtime<to_date('20121201','yyyymmdd') and status not like 'paid%' and order_type='ticket'; -- 877008 rows created.
delete from ticket_order where createtime<to_date('20120901','yyyymmdd') and status not like 'paid%' and order_type='ticket';--284014
delete from ticket_order where createtime<to_date('20121001','yyyymmdd') and status not like 'paid%' and order_type='ticket';--228091
delete from ticket_order where createtime<to_date('20121101','yyyymmdd') and status not like 'paid%' and order_type='ticket';--135220
delete from ticket_order where createtime<to_date('20121201','yyyymmdd') and status not like 'paid%' and order_type='ticket';--229683

insert into failure_order 
select * from ticket_order where createtime<to_date('20121201','yyyymmdd') and status not like 'paid%'; -- 67085
delete from ticket_order where createtime<to_date('20121201','yyyymmdd') and status not like 'paid%';

delete from discount_item m where m.orderid in (select recordid from failure_order t);--169560
delete from order_openseat where order_id in (select recordid from failure_order t);--771240
delete from sellseat where orderid in (select recordid from failure_order);--900001
------------------------------------------------------
----------------2013-05-10----------------------------
delete from shares where addtime < sysdate -100;---400w
delete from FAILURE_ORDER;
drop table ELECCARD_BATCH_20120730;
drop table BUYMOBILE2;
drop table BILL_RECORD;
----------------2013-05-13--------------------------------------------
insert into failure_order 
select * from ticket_order where createtime<to_date('20130101','yyyymmdd') and status not like 'paid%';--550170 rows created.
delete from ticket_order where createtime<to_date('20130101','yyyymmdd') and status not like 'paid%';----550170 rows deleted.
delete from ORDER_OPENSEAT where order_id in (select recordid from failure_order); --1048606 rows deleted.
delete from SELLSEAT where orderid in (select recordid from failure_order);

insert into failure_order 
select * from ticket_order where createtime<to_date('20130201','yyyymmdd') and status not like 'paid%' and order_type='ticket';--322999 rows created.
delete from ticket_order where createtime<to_date('20130201','yyyymmdd') and status not like 'paid%' and order_type='ticket';--322999 rows deleted.

insert into failure_order 
select * from ticket_order where createtime<to_date('20130301','yyyymmdd') and status not like 'paid%' and order_type='ticket';--265938 rows created.
delete from ticket_order where createtime<to_date('20130301','yyyymmdd') and status not like 'paid%' and order_type='ticket';--265938 rows deleted.

insert into failure_order 
select * from ticket_order where createtime<to_date('20130401','yyyymmdd') and status not like 'paid%' and order_type='ticket';--229152 rows created.
delete from ticket_order where createtime<to_date('20130401','yyyymmdd') and status not like 'paid%' and order_type='ticket';--229152 rows deleted.

delete from ORDER_OPENSEAT where order_id in (select recordid from failure_order); --1598753 rows deleted.
delete from SELLSEAT where orderid in (select recordid from failure_order);--522572 rows deleted.


---万达订单价格错误----
update ticket_order set costprice=80,totalcost=80*quantity where trade_no in ('1130704132618730','1130704123806036');
update open_playitem set costprice=80 where mpid=16976062;
---2013-08-30-------------------------------------------------------------------------------------------------------------------------------
alter table failure_order add AREAID NUMBER(19);
alter table failure_order add EXPRESS CHAR(1);

insert into FAILURE_ORDER select * from ticket_order where createtime<to_date('20130501','yyyymmdd') and status not like 'paid%' and order_type='ticket';--350863 rows created.
delete from ticket_order where createtime>=to_date('20130401','yyyymmdd') and createtime<to_date('20130501','yyyymmdd') and status not like 'paid%' and order_type='ticket';--350863 rows deleted.

insert into FAILURE_ORDER select * from ticket_order where createtime>=to_date('20130501','yyyymmdd') and createtime<to_date('20130801','yyyymmdd') and status not like 'paid%' and order_type='ticket';--1101068 rows created.

delete from ticket_order where  createtime>=to_date('20130501','yyyymmdd') and createtime<to_date('20130516','yyyymmdd') and status not like 'paid%' and order_type='ticket';--251736 rows deleted.
delete from ticket_order where  createtime>=to_date('20130516','yyyymmdd') and createtime<to_date('20130601','yyyymmdd') and status not like 'paid%' and order_type='ticket';--186199 rows deleted.

delete from ticket_order where  createtime>=to_date('20130601','yyyymmdd') and createtime<to_date('20130616','yyyymmdd') and status not like 'paid%' and order_type='ticket';--142982 rows deleted.
delete from ticket_order where  createtime>=to_date('20130616','yyyymmdd') and createtime<to_date('20130701','yyyymmdd') and status not like 'paid%' and order_type='ticket';--171652 rows deleted.
delete from ticket_order where  createtime>=to_date('20130701','yyyymmdd') and createtime<to_date('20130716','yyyymmdd') and status not like 'paid%' and order_type='ticket';--145974 rows deleted.
delete from ticket_order where  createtime>=to_date('20130716','yyyymmdd') and createtime<to_date('20130801','yyyymmdd') and status not like 'paid%' and order_type='ticket';--202525 rows deleted.

delete from ORDER_OPENSEAT t where not exists (select recordid from ticket_order s where s.recordid=t.order_id);--2826695 rows deleted.
delete from sellseat where orderid in (select recordid from failure_order); --954874 rows deleted.
delete from discount_item m where m.orderid in (select recordid from failure_order t); --244166 rows deleted.
delete from sellseat where validtime<to_date('2012-12-01','yyyy-mm-dd'); --475971 rows deleted.
delete from sellseat where validtime<to_date('2013-02-01','yyyy-mm-dd'); --694077 rows deleted.
delete from sellseat where validtime<to_date('2013-04-01','yyyy-mm-dd'); --840947 rows deleted.
delete from sellseat where validtime<to_date('2013-05-01','yyyy-mm-dd'); --261496 rows deleted.
delete from sellseat where validtime<to_date('2013-06-01','yyyy-mm-dd'); --515262 rows deleted.
delete from sellseat where validtime<to_date('2013-07-01','yyyy-mm-dd'); --937731 rows deleted.
delete from sellseat where validtime<to_date('2013-08-01','yyyy-mm-dd'); --506441 rows deleted.

--select to_char(VALIDTIME,'yyyy-mm') vt,count(1) from WEBDATA.SELLSEAT where validtime<sysdate group by to_char(VALIDTIME,'yyyy-mm') order by to_char(VALIDTIME,'yyyy-mm')
---2013-11-06----------------------------------------
insert into FAILURE_ORDER select * from ticket_order where createtime<to_date('20130816','yyyymmdd') and status not like 'paid%' and order_type='ticket';--309207
delete from ticket_order where createtime<to_date('20130816','yyyymmdd') and status not like 'paid%' and order_type='ticket';--

insert into FAILURE_ORDER select * from ticket_order where createtime<to_date('20130901','yyyymmdd') and status not like 'paid%' and order_type='ticket';--235040
delete from ticket_order where createtime<to_date('20130901','yyyymmdd') and status not like 'paid%' and order_type='ticket';--235040 rows deleted.

insert into FAILURE_ORDER select * from ticket_order where createtime<to_date('20130916','yyyymmdd') and status not like 'paid%' and order_type='ticket';--141538
delete from ticket_order where createtime<to_date('20130916','yyyymmdd') and status not like 'paid%' and order_type='ticket';--141538

insert into FAILURE_ORDER select * from ticket_order where createtime<to_date('20131001','yyyymmdd') and status not like 'paid%' and order_type='ticket';--177859
delete from ticket_order where createtime<to_date('20131001','yyyymmdd') and status not like 'paid%' and order_type='ticket';--177859

delete from sellseat where validtime<to_date('2013-08-16','yyyy-mm-dd');--504350 rows deleted.
delete from sellseat where validtime<to_date('2013-09-01','yyyy-mm-dd');--484412 rows deleted.
delete from sellseat where validtime<to_date('2013-09-16','yyyy-mm-dd');--292204 rows deleted.
delete from sellseat where validtime<to_date('2013-10-01','yyyy-mm-dd');--470973 rows deleted.
--select count(1),max(s.recordid),min(s.recordid) from webdata.ORDER_OPENSEAT s where s.recordid < 94552035 and not exists(select recordid from webdata.sellseat t where t.recordid=s.OPENSEAT_ID)
delete from webdata.ORDER_OPENSEAT s where s.recordid < 90552035 and not exists(select recordid from webdata.sellseat t where t.recordid=s.OPENSEAT_ID);--584814
delete from webdata.ORDER_OPENSEAT s where s.recordid < 100552035 and not exists(select recordid from webdata.sellseat t where t.recordid=s.OPENSEAT_ID);--1332686
delete from webdata.ORDER_OPENSEAT s where s.recordid < 110552035 and not exists(select recordid from webdata.sellseat t where t.recordid=s.OPENSEAT_ID);--1193894
delete from webdata.ORDER_OPENSEAT s where s.recordid < 135552035 and not exists(select recordid from webdata.sellseat t where t.recordid=s.OPENSEAT_ID);--1170693
delete from webdata.ORDER_OPENSEAT s where s.recordid < 145552035 and not exists(select recordid from webdata.sellseat t where t.recordid=s.OPENSEAT_ID);--299851



--alter table open_playitem drop column ADDRESS;
--alter table open_playitem drop column DESCLINK;
--alter table open_playitem drop column PRATIO;
--alter table open_playitem drop column SENDSMS;

insert into open_playitem_his select RECORDID,MPID,MOVIEID,CINEMAID,ROOMID,MOVIENAME,CINEMANAME,ROOMNAME,PLAYTIME,OPENTIME,GEWAPRICE,STATUS,REMARK,CLOSETIME,OPENTYPE,COSTPRICE,LOWEST,SEQNO,ELECARD,EDITION,PRICE,LANGUAGE,TOPICID,PARTNER,MINPOINT,MAXPOINT,DAYOTIME,DAYCTIME,SEATNUM,GSELLNUM,CSELLNUM,LOCKNUM,UPDATETIME,BUYLIMIT,CITYCODE,OTHERINFO,GIVEPOINT,SPFLAG,ASELLNUM,FEE from open_playitem where playtime<to_date('2012-07-01','yyyy-mm-dd');--762779 rows created.
delete from open_playitem where playtime<to_date('2012-04-01','yyyy-mm-dd');--338031 rows deleted.
delete from open_playitem where playtime<to_date('2012-07-01','yyyy-mm-dd');--424748 rows deleted.

insert into open_playitem_his select RECORDID,MPID,MOVIEID,CINEMAID,ROOMID,MOVIENAME,CINEMANAME,ROOMNAME,PLAYTIME,OPENTIME,GEWAPRICE,STATUS,REMARK,CLOSETIME,OPENTYPE,COSTPRICE,LOWEST,SEQNO,ELECARD,EDITION,PRICE,LANGUAGE,TOPICID,PARTNER,MINPOINT,MAXPOINT,DAYOTIME,DAYCTIME,SEATNUM,GSELLNUM,CSELLNUM,LOCKNUM,UPDATETIME,BUYLIMIT,CITYCODE,OTHERINFO,GIVEPOINT,SPFLAG,ASELLNUM,FEE from open_playitem where playtime<to_date('2013-01-01','yyyy-mm-dd');--1138033 rows created.
delete from open_playitem where playtime<to_date('2012-10-01','yyyy-mm-dd');--551244 rows deleted.
delete from open_playitem where playtime<to_date('2013-01-01','yyyy-mm-dd');--586789 rows deleted.








