insert into gewaconfig (RECORDID, DESCRIPTION, UPDATETIME, NEWCONTENT)
values (1112, 'baidu wallet', sysdate, '{"pc":"34068893","wap":"34068893"}');

-----------------2013-12-04---------------------------------------------
UPDATE WEBDATA.ELECCARD_BATCH SET SOLD_TYPE='S',DELAY_DAYS=60,DELAY_USE_DAYS=60,DELAY_FEE=15 WHERE RECORDID IN (
SELECT BATCHID FROM ELECCARD_EXTRA S WHERE SELLPRICE>0);

UPDATE WEBDATA.ELECCARD_BATCH SET SOLD_TYPE='P' WHERE RECORDID IN (
SELECT BATCHID FROM ELECCARD_EXTRA WHERE SELLPRICE=0);

UPDATE WEBDATA.ELECCARD_BATCH SET SOLD_TYPE='P' WHERE RECORDID IN (
SELECT BATCHID FROM ELECCARD_EXTRA WHERE SELLPRICE IS NULL);

alter table MEMBER add bindstatus varchar2(10) default 'N' not null;

update member set bindstatus='Y' where mobile is not null and bindstatus='N';


create table temp_member (
  recordid number(19) not null,
  mobile varchar2(11),
  memberid number(19),
  tmppwd varchar2(32),
  flag varchar2(10) not null,
  otherinfo varchar2(200) not null,
  ip varchar2(20) not null,
  status varchar2(10) not null,
  addtime timestamp(6) not null
);

alter table temp_member add membertype varchar2(10) not null;

alter table temp_member
  add constraint PK_temp_member primary key (RECORDID) using index tablespace tbs_index;
alter table TEMP_MEMBER
  add constraint UK_TEMP_MEMBER1 unique (MOBILE) using index tablespace TBS_INDEX;
alter table TEMP_MEMBER
  add constraint UK_TEMP_MEMBER2 unique (MEMBERID) using index tablespace TBS_INDEX;

grant select,update,insert on temp_member to shanghai;

alter table MOVIEVIDEO drop constraint uk_movievideo cascade;
create index IDX_MOVIEVIDEO_MOVIEID on MOVIEVIDEO(MOVIEID) tablespace TBS_INDEX;


alter table MOVIEVIDEO add constraint uk_movievideo unique (MOVIEID) using index tablespace tbs_index;
alter table MOVIEVIDEO add constraint uk_movievideo2 unique (VIDEOID) using index tablespace tbs_index;
-----------2013-11-26---------------------------------------------------------
create table member_first_order (
memberid number(19),
trade_no varchar(20),
order_type varchar(10)
);

insert into member_first_order
select memberid,min(trade_no),'ticket' from ticket_order where order_type='ticket' and status='paid_success' group by memberid

insert into member_first_order
select memberid,min(trade_no),'drama' from ticket_order where order_type='drama' and status='paid_success' group by memberid

insert into member_first_order
select memberid,min(trade_no),'sport' from ticket_order where order_type='sport' and status='paid_success' group by memberid

grant select on member_first_order to shanghai;
-----------------------------------------------------------------
revoke select on eleccard from shanghai;

-----------------------------------------------------------------------
alter table ELECCARD_HIST rename column NICKNAME to cardpass2;
alter table ELECCARD rename column NICKNAME to cardpass2;
create or replace view veleccard as select recordid,cardno,cardpass2 as cardpass, status,deltime,deluserid,batchid,possessor,card_version,gainer,mobile,remark,orderid,begintime,endtime from eleccard;
grant select,update on veleccard to shanghai;

update eleccard set cardpass2=null where cardpass2 is not null;
alter table ELECCARD add constraint uk_cardpass2 unique (CARDPASS2) using index tablespace tbs_index;

insert into JSONDATA (dkey,DATA,TAG,validtime) values('upgradeElecCard','0','hisdata',sysdate + 30);

---------2013-11-06----------------------------------------------------
insert into JSONDATA (dkey,DATA,TAG,validtime) values('pointLastTime','0','hisdata',sysdate + 365*20);
insert into JSONDATA (dkey,DATA,TAG,validtime) values('smsLastTime','0','hisdata',sysdate + 365*20);
insert into JSONDATA (dkey,DATA,TAG,validtime) values('orderLastTime','2009-10-01 00:00:00','hisdata',sysdate + 365*20);

update jsondata set data='0' where dkey='smsLastTime';

create synonym point4his for pointhis130301;

---------2013-11-06-----------------------------------------------------
create table failure_order_stats as 
select to_char(createtime,'yyyy-mm-dd') createdate, citycode, status, count(1) as totalnum, sum(quantity) as totalquantity 
from webdata.failure_order group by to_char(createtime,'yyyy-mm-dd'),citycode, status


create view view_failure_order_stats as 
select CREATEDATE,CITYCODE,TOTALNUM,TOTALQUANTITY,status from webdata.failure_order_stats;
grant select on failure_order_stats to baobiao;

insert into failure_order_stats
select to_char(createtime,'yyyy-mm-dd') createdate, citycode, status, count(1) as totalnum, sum(quantity) as totalquantity 
from webdata.ticket_order where status not like 'paid%' and createtime<to_date('2013-11-06','yyyy-mm-dd') group by to_char(createtime,'yyyy-mm-dd'), citycode, status


-----------2013-11-05--------------------------------------------------
alter table ROOMSEAT drop constraint UK_ROOMSEAT_LINE_RANK cascade;
alter table ROOMSEAT add constraint UK_ROOMSEAT_LINE_RANK unique (ROOMID, LINENO, RANKNO) using index tablespace tbs_index;
alter table ROOMSEAT drop constraint UK_ROOMSEAT_LINE_RANK2 cascade;
alter table ROOMSEAT add constraint UK_ROOMSEAT_LINE_RANK2 unique (ROOMID, SEATLINE, SEATRANK) using index tablespace WEBDATA;
-------------------------------------------------------------------------
alter table PRIZE add topPrize varchar2(5) default 'N' not null;
-----------------------------------------------------------------
alter table specialdiscount add costtype varchar2(10);
alter table specialdiscount add costnum number(4);
alter table specialdiscount add verifytype varchar2(15);

create table SPCODE
(
  RECORDID number(19) not null,
  CODEPASS   VARCHAR2(100) not null,
  sdid  NUMBER(19) not null,
  CODE_VERSION number(8) default 0 not null,
  memberid  NUMBER(19),
  mobile VARCHAR2(11),
  orderid  NUMBER(19),
  sendtime  TIMESTAMP,
  usedcount number(8) not null
);
alter table SPCODE
  add constraint PK_SPCODE primary key (RECORDID) using index tablespace tbs_index;
alter table SPCODE
  add constraint UK_SPCODE unique (CODEPASS) using index tablespace tbs_index;
grant insert,update,select,delete on SPCODE to shanghai;


alter table specialdiscount drop column LIMITMAXNUM;
ALLOWADDNUM
PARTNERID
APPLYCITY
APPLYDEPT
APPLYTYPE
ORDERALLOWANCE
UNITALLOWANCE
MAXALLOWANCE
-----------------------------------------------------------------------------------------
update smsrecord set status='DN' where TIMETAG='3h' and status='N' and tradeNo in (
select TRADENO from WEBDATA.ORDER_NOTE 
where smallitemid in (138072161,138072162,138072164,138072166,138072167,139898531,138072165,139891605,138072206,138072208,138072209,
138072210,138072211,138072204,138072188,138072189,138072159,138072168,138072174,138072205,138072207,138072187,138072169,138072170,
138072171,138072173,138072160,138072163,138072190,139891604,139891606,139891607,139891611,139891608,139891609,139891610,138072172) and smallitemtype='dramaplayitem'
);
--------------------------------------------------------------------
update cinemaprofile set opentype=null where cinemaid in (91505022);
update cinema set updatetime=sysdate where recordid=91505022;
--update cinemaroom set roomtype='DX',updatetime=sysdate where  cinemaid in (91505022);


insert into gewaconfig (RECORDID, DESCRIPTION, UPDATETIME, NEWCONTENT)
values (400, 'mark count scale', sysdate, '{"movie32607903":"1.2"}');

insert into gewaconfig (RECORDID, DESCRIPTION, UPDATETIME, NEWCONTENT)
values (400, 'mark count scale', sysdate, '{"movie18038859":"1.1"}');

update ELECCARD_EXTRA set APPLYDEPT='0103' where APPLYDEPT='01';--9
update ELECCARD_EXTRA set APPLYDEPT='0104' where APPLYDEPT='02';--44
update ELECCARD_EXTRA set APPLYDEPT='0101' where APPLYDEPT='04';--40
update ELECCARD_EXTRA set APPLYDEPT='0202' where APPLYDEPT='06';--170
update ELECCARD_EXTRA set APPLYDEPT='0201' where APPLYDEPT='07';--1
update ELECCARD_EXTRA set APPLYDEPT='0301' where APPLYDEPT='08';--679
update ELECCARD_EXTRA set APPLYDEPT='0302' where APPLYDEPT='09';--1458
update ELECCARD_EXTRA set APPLYDEPT='0303' where APPLYDEPT='10';--3101
update ELECCARD_EXTRA set APPLYDEPT='0406' where APPLYDEPT='11';--50

----------------------------------------------------------------------------------------
update cinemaprofile set opentype=null where recordid in (127992453);
update cinemaroom set roomtype='JY',updatetime=sysdate where  cinemaid in (127992453);
91505022
update cinemaprofile set opentype=null where cinemaid in (37949591,19518602,1253468,67584696,64514880,38096532,126744465,39712846,38819698,60080583);
update cinemaroom set roomtype='GEWA',updatetime=sysdate where  cinemaid in (37949591,19518602,1253468,67584696,64514880,38096532,126744465,39712846,38819698,60080583);
update cinema set updatetime=sysdate where  cinemaid in (37949591,19518602,1253468,67584696,64514880,38096532,126744465,39712846,38819698,60080583);
alter table specialdiscount add expression varchar2(2000);
------------------------------------------------------------
update autosetter set price_script= replace(price_script, '\r\n if(opi != null){opi.setFee(8);}','') where price_script is not null;

update autosetter set price_script= replace(price_script, 'if(opi != null){opi.setFee(0);}','') where price_script is not null and instr(price_script,'if(opi != null){opi.setFee(0);}')>0;
update autosetter set price_script= replace(price_script, 'if(opi != null){opi.setFee(1);}','') where price_script is not null and instr(price_script,'if(opi != null){opi.setFee(1);}')>0;
update autosetter set price_script= replace(price_script, 'if(opi != null){opi.setFee(2);}','') where price_script is not null and instr(price_script,'if(opi != null){opi.setFee(2);}')>0;
update autosetter set price_script= replace(price_script, 'if(opi != null){opi.setFee(3);}','') where price_script is not null and instr(price_script,'if(opi != null){opi.setFee(3);}')>0;
update autosetter set price_script= replace(price_script, 'if(opi != null){opi.setFee(4);}','') where price_script is not null and instr(price_script,'if(opi != null){opi.setFee(4);}')>0;
update autosetter set price_script= replace(price_script, 'if(opi != null){opi.setFee(5);}','') where price_script is not null and instr(price_script,'if(opi != null){opi.setFee(5);}')>0;
update autosetter set price_script= replace(price_script, 'if(opi != null){opi.setFee(6);}','') where price_script is not null and instr(price_script,'if(opi != null){opi.setFee(6);}')>0;
update autosetter set price_script= replace(price_script, 'if(opi != null){opi.setFee(7);}','') where price_script is not null and instr(price_script,'if(opi != null){opi.setFee(7);}')>0;
update autosetter set price_script= replace(price_script, 'if(opi != null){opi.setFee(8);}','') where price_script is not null and instr(price_script,'if(opi != null){opi.setFee(8);}')>0;
update autosetter set price_script= replace(price_script, 'if(opi != null){opi.setFee(9);}','') where price_script is not null and instr(price_script,'if(opi != null){opi.setFee(9);}')>0;
update autosetter set price_script= replace(price_script, 'if(opi != null){opi.setFee(10);}','') where price_script is not null and instr(price_script,'if(opi != null){opi.setFee(10);}')>0;
update autosetter set price_script= replace(price_script, 'if(opi != null){opi.setFee(11);}','') where price_script is not null and instr(price_script,'if(opi != null){opi.setFee(11);}')>0;
update autosetter set price_script= replace(price_script, 'if(opi != null){opi.setFee(12);}','') where price_script is not null and instr(price_script,'if(opi != null){opi.setFee(12);}')>0;
update autosetter set price_script= replace(price_script, 'if(opi != null){opi.setFee(13);}','') where price_script is not null and instr(price_script,'if(opi != null){opi.setFee(13);}')>0;

update autosetter set price_script=replace(price_script,chr(10)||' '||chr(10),chr(10)) where instr(price_script, chr(10)||' '||chr(10)) >1;

update autosetter set limit_script=replace(limit_script,'function limit(){','') where limit_script is not null;
update autosetter set limit_script=trim(replace(limit_script,'}','')) where limit_script is not null;
update autosetter set limit_script=trim(replace(limit_script,chr(10),'')) where limit_script is not null;
update autosetter set limit_script=trim(replace(limit_script,'return','')) where limit_script is not null;

update autosetter set price_script=replace(price_script,'function set(){','') where price_script is not null;
update autosetter set price_script=trim(replace(price_script,'}','')) where price_script is not null;
update autosetter set price_script=trim(replace(price_script,' ','')) where price_script is not null;

-------------------------------------------------------

insert into apiuser (RECORDID, PARTNERKEY, CONTENT, PRIVATEKEY, PARTNERNAME, UPDATETIME, CLERK, STATUS, BRIEFNAME, ROLES, PARTNERPATH, CITYCODE, DEFAULTCITY)
values (50000080, 'huashu', '华数机顶盒', 'kd12RTkfeS582SA', '华数', sysdate, 5, 'open', 'huashu', 'apiuser', 'huashu', '000000', '310000');

----------2013-08-07-------------xss------------------
update DIARYCOMMENT set status='N_DELETE' where recordid in(3005259,3005416,3005441,3009089,3009090,3010160,3010161,3010162,3010367,3018332,3018334,3018335,3018336,3018337,3024253,3025203,3032464,3032465,3040125,3040126,3040127,3043321,3043322,3044679,3044680,3044681,3044682,3044683,3044685,3044686,3044688,3044689,3044691,3044692,3044693,3051849,3051850,3051851,3051852,3051853,3051854,3051855,3051856,3051857,3051858,3051859,3051860,3051861,3052316,3053171,3053172,3053174,3053176,3059005,3060891,3060916,3060920,3060922,3061041,3064245,3064247,3065562,3066861,3070330,3070331,3070332,3070334,3070335,3070336,3070338,3070339,3070340,3070341,3070342,3070343,3076142,3078777,3081274,3086219,3086220,3086222,3086223,3086224,3086225,3086226,3086227,3086228,3086230,3086232,3086233,3089162,3090919,3091523,3097262,3099189,3116216,3121033,3127509,3130235,3130370,3130462,3130916,3137677,3161444,3169626,3169630,3170045,3176768,3211572,3211676,3212708,3213928,3214816,3229384,3233644,3233645,3247084,3288604,3293812,13667,15442,15443,17482,18263,18426,18908,19292,19887,20648,20743,20745,21185,21981,24339,24346,24349,24804,29132,29134,29315,29962)
update CUSTOMER_QUESTION set status='N_DELETE' where recordid in(10380501,11880937,11880940,11891558,47913943,59512552,67562284,82543241,84856059,87094130,88670955);
delete from USER_MESSAGE where recordid in(120138014,120138960,120139159,120149422,120149511,120149638)

-----------20130724---------------------------------
update bindmobile set totalcheck=mod(checkcount,500) where totalcheck =0;
alter table BINDMOBILE modify ADDTIME null;
alter table BINDMOBILE modify STATUS null;
alter table BINDMOBILE modify VALIDTIME not null;
alter table BINDMOBILE add ukey varchar2(30) ;


create table bindmobilebak as select * from bindmobile;
alter table BINDMOBILE drop constraint PK_BINDMOBILE cascade;
drop index PK_BINDMOBILE;
alter table BINDMOBILE modify recordid null;

delete from bindmobile;
alter table BINDMOBILE modify ukey not null;
alter table BINDMOBILE add constraint pk_bindmobile primary key (UKEY) using index tablespace tbs_index;

alter table BINDMOBILE drop column recordid;
alter table BINDMOBILE drop column addtime;
alter table BINDMOBILE drop column BINDVALUE;
alter table BINDMOBILE drop column MEMBERID;

alter table BINDMOBILE add totalcheck number(8) default 0 not null;
alter table BINDMOBILE add ip varchar2(20);


---通知报表view----
alter table specialdiscount  drop column LIMITMAXNUM;
alter table specialdiscount  drop column ALLOWADDNUM;

----------------------------------------------------------------
alter table OPEN_PLAYITEM_EXT modify costadjust null;
alter table OPEN_PLAYITEM_EXT modify realprice null;

alter table OPEN_PLAYITEM_EXT DROP COLUMN costadjust;
alter table OPEN_PLAYITEM_EXT DROP COLUMN realprice;

--mapping
alter table OPEN_PLAYITEM_EXT add costadjust number(4) default 0 not null;

------------------2013-07-06--------------------------------------------
create table SDRECORD
(
  TRADENO   VARCHAR2(16) not null,
  SPCOUNTERID  NUMBER(19) not null,
  QUANTITY  NUMBER(4) not null,
  VALIDTIME  TIMESTAMP not null,
  CPCOUNTERS VARCHAR2(200)
);
alter table SDRECORD
  add constraint PK_SDRECORD primary key (TRADENO) using index tablespace tbs_index;
create index IDX_SDRECORD on SDRECORD(VALIDTIME) tablespace TBS_INDEX;
grant insert,update,select,delete on SDRECORD to shanghai;

alter table CPCOUNTER add ALLORDERNUM number(6) default 0 not null;
alter table CPCOUNTER add ALLQUANTITY number(6) default 0 not null;

----之后更新--------------
update CPCOUNTER set ALLORDERNUM=sellorder,ALLQUANTITY=sellquantity;

update spcounter s set ALLQUANTITY=(select sellcount from specialdiscount t where t.spcounterid=s.recordid)
where s.recordid in (
126168300,126168737,126169319,126169797,126174426,126174775,126175400,126175782,126176108,126176428,126176745,126177368,126177758,126178400,126179005,126179221,126184082,126191738,126191967
);

select t.recordid, t.flag, t.sellcount, s.SELLQUANTITY,s.SELLORDERNUM,s.ALLQUANTITY,s.ALLORDERNUM from webdata.specialdiscount t left join WEBDATA.SPCOUNTER s on t.spcounterid=s.recordid where s.recordid in{
126168300,126168737,126169319,126169797,126174426,126174775,126175400,126175782,126176108,126176428,126176745,126177368,126177758,126178400,126179005,126179221,126184082,126191738,126191967
);



select relatedid, (select spcounterid from webdata.specialdiscount k where k.recordid=m.relatedid) spcounterid, count(1), sum(quantity) from (select t.orderid, s.quantity, t.relatedid from (select distinct ORDERID,relatedid from WEBDATA.DISCOUNT_ITEM where relatedid in (
select recordid from webdata.specialdiscount where spcounterid in (126168300,126168737,126169319,126169797,126174426,126174775,126175400,126175782,126176108,126176428,126176745,126177368,126177758,126178400,126179005,126179221,126184082,126191738,126191967)
)) t inner join webdata.ticket_order s on t.orderid=s.recordid and s.status='paid_success') m group by relatedid order by relatedid;

--update spcounter set allordernum=\2 where recordid=\1;

update spcounter set sellordernum=allordernum,sellquantity=allquantity where recordid in (
126168300,126168737,126169319,126169797,126174426,126174775,126175400,126175782,126176108,126176428,126176745,126177368,126177758,126178400,126179005,126179221,126184082,126191738,126191967
);

update spcounter set PERIODMINUTE=43200 where PERIODMINUTE is null or PERIODMINUTE>1440;
update spcounter set PERIODMINUTE=43200 where PERIODMINUTE is null or PERIODMINUTE>43200;
update spcounter set PERIODMINUTE=43200 where PERIODMINUTE!=1440 and PERIODMINUTE!=43200;
alter table spcounter modify PERIODMINUTE not null;
















alter table spcounter drop column opentype;
-----------------------------------------------
alter table APIUSER add CATEGORY varchar2(15) ;
alter table APIUSER drop column LOGINNAME;
alter table APIUSER drop column LOGINPASS;

----2013-06-06---------------------------------------
update movielist set opentype='GEWA' where opentype is null
alter table SPECIALDISCOUNT add cardukey varchar2(15) ;
update WEBDATA.USER_OPERATION set OPKEY = replace(opkey,'spd121396223','spd121394291') WHERE OPKEY like 'spd121396223:%' and OPKEY not in('spd121396223:6225757500901187','spd121396223:6225768700932732','spd121396223:6227003090010037308');
update WEBDATA.USER_OPERATION set OPKEY = replace(opkey,'spd122257543','spd121394291') WHERE OPKEY like 'spd122257543:%' and OPKEY not in('spd122257543:6221682230842705','spd122257543:6225380067386575');

update WEBDATA.SPECIALDISCOUNT set spcounterid=null where SPCOUNTERID = 0;
-----------------------------------------------------
alter table PARTNERCLOSERULE add updatetime timestamp;
update PARTNERCLOSERULE set updatetime=sysdate;

----2013-05-20-----------------------------------------
update CINEMAPROFILE set opentype='WD' where cinemaid in (63364,2088696,37925152);
-------------------------------------------------------
alter table order_extra add processLevel varchar2(10) ;
alter table order_extra_his add processLevel varchar2(10);
update order_extra_his set processLevel = 'finish' ;

update order_extra set processLevel = 'finish' where addtime<to_date('2013-04-16 00:00:00','yyyy-mm-dd hh24:mi:ss');
update order_extra set processLevel = 'finish' where addtime>to_date('2013-04-16 17:30:00','yyyy-mm-dd hh24:mi:ss') and addtime<to_date('2013-04-16 17:50:00','yyyy-mm-dd hh24:mi:ss') and processLevel is null

alter table order_extra modify processlevel not null;
---2013-04-13------------------------------------------------------------------
alter table movielist modify seqno varchar2(30);
alter table movielisthis modify seqno varchar2(30);

alter table open_playitem modify seqno varchar2(30);
alter table open_playitem_his modify seqno varchar2(30);



-------------------------------------------------------------------------------
create sequence SEQ_ORDER minvalue 1 start with 130000000 increment by 1 cache 20;
grant select on seq_order to shanghai;

create sequence seq_eleccard minvalue 1 start with 120000000 increment by 1 cache 20;
grant select on seq_eleccard to shanghai;

alter table ELECCARD_EXTRA add ISSUECOUNT number(8);
alter table ELECCARD_HIS_STATUS add ISSUECOUNT number(8);
update ELECCARD_HIS_STATUS t set ISSUECOUNT=(select count(*) from eleccard_his s where s.batchid=t.batchid and s.gainer is not null)

insert into gewaconfig (RECORDID, DESCRIPTION, UPDATETIME, NEWCONTENT)
values (41, 'enable mtx socket timeout error control', sysdate, '5,15');


-----------2013-03-14--------------------------------
alter table merchant add contact varchar2(40);
alter table merchant add ADDTIME timestamp(6);

insert into merchant(recordid, loginname, loginpass, mername, roles, status, company, opentype, relatelist, contact, ADDTIME)
select c.recordid, t.email as loginname, t.password as loginpass, t.nickname, 'merReport,merchant', 'Y', name as company, 'HFH', relatedid,contactphone as contact, c.addtime 
from customer c left join member t on c.memberid=t.recordid where tag='cinema' and c.relatedid is not null and t.email is not null;


alter table ticket_order add playtime timestamp(6);
alter table failure_order add playtime timestamp(6);
create index idx_ticket_playtime on TICKET_ORDER (playtime)  tablespace TBS_INDEX;

update ticket_order t set playtime=(select playtime from open_playitem_his s where s.mpid=t.relatedid) where order_type='ticket' and addtime<to_date('2011-01-01','yyyy-mm-dd');
update ticket_order t set playtime=(select playtime from open_playitem_his s where s.mpid=t.relatedid) where order_type='ticket' and addtime>=to_date('2011-01-01','yyyy-mm-dd') and  addtime<to_date('2011-07-01','yyyy-mm-dd');
update ticket_order t set playtime=(select playtime from open_playitem_his s where s.mpid=t.relatedid) where order_type='ticket' and addtime>=to_date('2011-07-01','yyyy-mm-dd') and  addtime<to_date('2012-01-01','yyyy-mm-dd');

update ticket_order t set playtime=(select playtime from open_playitem s where s.mpid=t.relatedid) where order_type='ticket' and addtime>=to_date('2012-01-01','yyyy-mm-dd') and  addtime<to_date('2012-07-01','yyyy-mm-dd');
update ticket_order t set playtime=(select playtime from open_playitem s where s.mpid=t.relatedid) where order_type='ticket' and addtime>=to_date('2012-07-01','yyyy-mm-dd') and  addtime<to_date('2013-01-01','yyyy-mm-dd');
update ticket_order t set playtime=(select playtime from open_playitem s where s.mpid=t.relatedid) where order_type='ticket' and addtime>=to_date('2013-01-01','yyyy-mm-dd');

update ticket_order t set playtime=(select playtime from open_playitem_his s where s.mpid=t.relatedid) where order_type='ticket' and playtime is null;
update ticket_order t set playtime=(select playtime from open_playitem s where s.mpid=t.relatedid) where order_type='ticket' and playtime is null;

update ticket_order t set playtime=(select playtime from open_playitem s where s.mpid=t.relatedid) 
where order_type='ticket' and addtime>=to_date('2013-03-14','yyyy-mm-dd') and playtime is null;


-----------------------------------------------------
create table MERCHANT
(
  RECORDID   NUMBER(19) not null,
  LOGINNAME  VARCHAR2(50) not null,
  LOGINPASS  VARCHAR2(50) not null,
  MERNAME    VARCHAR2(100) not null,
  ROLES      VARCHAR2(200),
  STATUS     VARCHAR2(1) not null,
  COMPANY    VARCHAR2(100) not null,
  OPENTYPE   VARCHAR2(10) not null,
  RELATELIST VARCHAR2(1000)
);
alter table MERCHANT
  add constraint PK_MERCHANT primary key (RECORDID) using index tablespace tbs_index;
alter table MERCHANT
  add constraint UK_MERCHANT_MERCHANTNAME unique (LOGINNAME)
  using index tablespace tbs_index;

grant select, insert, update on MERCHANT to SHANGHAI;
-----------------------------------------------------------------------
create table webdata.LAST_OPERATION(
	LASTKEY VARCHAR2(50) not null,
	LASTVALUE VARCHAR2(100) not null,
	TAG VARCHAR2(20) not null,
	LASTTIME  timestamp(6) NOT NULL,
	VALIDTIME  timestamp(6) NOT NULL,
	constraint pk_LASTOP primary key (LASTKEY)
	using index (create unique index webdata.idxpk_LAST_OPERATION on webdata.LAST_OPERATION(LASTKEY) tablespace tbs_index)
);

-------------------------------------------------------------------------
alter table user_operation add secondkey varchar2(100);
alter table SPCOUNTER add DATA_VERSION number(8) default 0 not null;
alter table CPCOUNTER add DATA_VERSION number(8) default 0 not null;
--TODO:remove
--alter table SPCOUNTER remove partnercounter
--alter table SPCOUNTER remove citycounter

---------------------------------------------------------------------
alter table eleccard_batch add COSTNUM3D number(3);
----------------------2013-01-18---------------------------------------
alter table Goods modify pratio null;
alter table OPEN_DRAMA_ITEM  modify pratio null;
alter table OPEN_PLAYITEM  modify pratio null;
alter table OPEN_TIMETABLE  modify pratio null;
alter table OPEN_TIMEITEM  modify pratio null;

-------------------------2012-11-26------------------------------------
alter table ADJUSTMENT add LOCK_VERSION number(8) default 0 not null;
----------------------------------------------------------------------
update news set content = replace(content,'"/shanghai/userfiles','"http://img.gewara.cn/userfiles') where content like '%"/shanghai/userfiles%';
update news_page set content = replace(content,'"/shanghai/userfiles','"http://img.gewara.cn/userfiles') where content like '%"/shanghai/userfiles%';
alter table APP_USER add USERTYPE varchar2(10);
UPDATE APP_USER SET USERTYPE='sso';

insert into gewaconfig(recordid, newcontent, description, updatetime) values(999,'12345','ticket-cinemaID',sysdate);
----测试数据-----
update GEWAQUESTION set memberid = (select min(recordid) from member where rownum=floor(fn_random*145038)) where memberid not in (select recordid from member);
------------------------------------------------------------2012-09-19-------------------------------------------------------------------------------------------------------
create table webdata.REPORT(
	RECORDID number(19) NOT NULL,
	NAME VARCHAR2(50) not null,
	CATEGORY VARCHAR2(30) not null,
	QRYSQL VARCHAR2(1000) not null,
	PARAMS VARCHAR2(500),
	FIELDS VARCHAR2(500),
	DISPLAYNAME VARCHAR2(500),
	MAXNUM number(4) not null,
	ROLES VARCHAR2(500) not null,
	DESCRIPTION VARCHAR2(2000) not null,
	constraint pk_REPORT primary key (recordid)
	using index (create unique index webdata.idxpk_REPORT on webdata.REPORT(recordid) tablespace tbs_index)
);

ALTER TABLE REPORT ADD NAME VARCHAR2(50) not null;
alter table report modify recordid not null;
alter table report modify CATEGORY not null;
alter table report modify recordid not null;

insert into apiuser(RECORDID,PARTNERKEY,PRIVATEKEY,PARTNERNAME,UPDATETIME,CLERK,STATUS,LOGINNAME,LOGINPASS,BRIEFNAME,ROLES,PARTNERPATH,CITYCODE,defaultcity) 
values(50000994,'zhoukang','zhoukangienoi1nxe','周康网',sysdate,5,'open','zhoukang','78f238965dd584d60385ae6ea577172c','zhoukang','apiuser,queryOrder','zhoukang','310000','310000')

ALTER TABLE eleccard_his_status ADD LOCKCOUNT NUMBER(8) DEFAULT 0 NOT NULL;

ALTER TABLE ELECCARD_EXTRA ADD LOCKCOUNT NUMBER(8) DEFAULT 0 NOT NULL;


insert into gewaconfig(recordid, newcontent, description, updatetime) values(78,'http://api.gewara.com','apiUrl',sysdate);

insert into apiuser(RECORDID,PARTNERKEY,PRIVATEKEY,PARTNERNAME,UPDATETIME,CLERK,STATUS,LOGINNAME,BRIEFNAME,ROLES,CITYCODE,defaultcity) 
values(50000005,'gewainnerapi','gewainnerapi','gewainnerapi',sysdate,1000,'open','gewainnerapi','gewainnerapi','apiuser','000000','310000');

--------------------------------------------------
alter table CINEMASETTLE ADD nexttime timestamp(6);
update cinemasettle set nexttime = curtime + 30 where nexttime is null;
--------------------------------------------------
delete from push where ISAUTO='Y';
--------------------------------------------------

alter table hfh_film add updatetime timestamp;
update hfh_film set updatetime=addtime where updatetime is null;
alter table hfh_film modify addtime not null;

alter table hfh_show add hcid varchar2(10);
alter table hfh_show modify cinemaid null;
update hfh_show set hcid=cinemaid where hcid is null;
alter table hfh_show modify hcid not null;

--alter table hfh_show drop column cinemaid;
---------------------------------------------------------
select order_type,to_char(addtime,'yyyy-mm'),count(*) from ticket_order 
where status ='paid_success' and settle!='Y' group by order_type, to_char(addtime,'yyyy-mm') order by to_char(addtime,'yyyy-mm')

update ticket_order set settle='Y' where status ='paid_success' and settle!='Y' and order_type!='ticket';--20792

-----------------------------------------------------------
alter table ACCOUNT_REFUND
  add constraint uk_account_refund unique (TRADENO)
  using index tablespace TBS_INDEX;


---8月1号前结算过的影院，paid_return订单的settle为Y的要改为N，其他日期类似：1120727135942266
select t.trade_no, t.relatedid, t.amount, t.totalcost, t.costprice, t.quantity, t.unitprice, t.discount, t.discount_reason, t.status from ticket_order t where status='paid_return' and settle='Y' and addtime<to_date('2012-08-01','yyyy-mm-dd')
-------------------------------------------------
---订单总成本问题:全部为0！841718 条，2009~2012----
select to_char(addtime,'yyyy-mm'), status, count(*),max(addtime),min(addtime) from ticket_order where totalcost!=quantity*costprice and status like 'paid%' and totalcost=0 and costprice>0 and order_type='ticket' group by to_char(addtime,'yyyy-mm'),status order by to_char(addtime,'yyyy-mm');
update ticket_order set totalcost=costprice*quantity where totalcost!=quantity*costprice and status like 'paid%' and totalcost=0 and costprice>0 and order_type='ticket';
----更改成本价订单导致总成本不正确-----
select t.trade_no, t.relatedid, t.amount, t.totalcost, t.costprice, t.quantity, t.unitprice, t.discount, t.discount_reason, t.status 
from ticket_order t where totalcost!=quantity*costprice and status like 'paid%' and order_type='ticket' order by addtime desc
---1120807172344028~1120905173633344, 50条
update ticket_order set totalcost=costprice*quantity where totalcost!=quantity*costprice and status like 'paid%' and order_type='ticket' and addtime>sysdate-30;

---运动等其他订单有无问题--------
select t.trade_no, t.relatedid, t.amount, t.totalcost, t.costprice, t.quantity, t.unitprice, t.discount, t.discount_reason, t.status from ticket_order t where totalcost!=quantity*costprice and status like 'paid%' order by addtime desc


select t.tradeno,t.status,t.ORDERSTATUS,t.REFUNDTYPE,t.ADDTIME,t.REFUNDTIME,t.EXPIRETIME,t.OLDSETTLE,t.NEWSETTLE,s.changehis from order_refund t left join ticket_order s on t.tradeno=s.trade_no where t.orderstatus='paid_success' and t.opmark like '%cancelTicket%' and t.refundtime<t.expiretime and t.status='success' and s.changehis like '%fixed=true=====>false%' order by addtime 

update order_refund set newsettle=0, status='finish' where tradeno in 
('1120823230814199','1120824194014206','1120824171303398','1120820225610492','1120823224645371','1120825014322407','1120824170148270','1120824203224173','1120824212318003',
'1120824211744682','1120820165853013','1120826000914084','1120825152445321','1120827073826132','1120827074936786','1120825132100565','1120824224850943','1120827222123236',
'1120826162637257','1120826163847457','1120827100723196','1120826102504484','1120827120922799','1120828201039103','1120828222403759','1120828172318092','1120830000600957',
'1120829144712940','1120829184335659','1120830101614902','1120831100916708','1120831101931218','1120831100600709','1120830125415726','1120831125625203','1120830160141532',
'1120830184017873','1120831084543021','1120831100037883','1120830190027498','1120830193013161','1120901090103406','1120831204456640','1120831222415052','1120830224150569',
'1120830115008228','1120901113555962','1120831105753459','1120831121902723','1120831114337202','1120901124836383','1120831003704437','1120830220234533','1120831104435593',
'1120831134044969','1120831204750513','1120831223350971','1120831215325859','1120902113945337','1120831223304829','1120901165940272','1120901123447830','1120901113426478',
'1120831212702867','1120901224042066','1120903094919327','1120901151453461','1120901034103450','1120901103938434','1120904071702368','1120904104008159','1120903131010539',
'1120904104138698','1120904125601608','1120904162043432','1120904164645240','1120904205642561','1120904210018400');

select t.tradeno,t.status,t.ORDERSTATUS,t.REFUNDTYPE,t.ADDTIME,t.REFUNDTIME,t.EXPIRETIME,t.OLDSETTLE,t.NEWSETTLE,s.changehis from order_refund t left join ticket_order s on t.tradeno=s.trade_no where t.orderstatus='paid_success' and t.opmark like '%cancelTicket%' and t.status='success' order by addtime 


select t.tradeno,t.status,t.ORDERSTATUS,t.REFUNDTYPE,t.ADDTIME,t.REFUNDTIME,t.EXPIRETIME,t.OLDSETTLE,t.NEWSETTLE,s.changehis from order_refund t left join ticket_order s on t.tradeno=s.trade_no where t.orderstatus='paid_success' and t.status='success' and refundtype='supplement' order by addtime
update order_refund set status='finish' where tradeno in
('1120815234729872','1120821175618150','1120823211804431','1120824114454461','1120826124243454','1120827044133762','1120828120954807')

select tradeno, opmark, t.refundtype, t.oldsettle, t.newsettle, s.settle, s.status from order_refund t left join ticket_order s on t.tradeno=s.trade_no where (t.status='finish' or t.status='success') and t.orderstatus='paid_success' and s.settle='Y'

------影院已退款，不结算-------
select tradeno, opmark, t.refundtype, t.oldsettle, t.newsettle, s.settle, t.refundtime, t.expiretime, t.orderstatus||'-->'||s.status 
from order_refund t left join ticket_order s on t.tradeno=s.trade_no
update order_refund t set t.newsettle=0, status='finish' 
where t.refundtime>t.expiretime and t.tradeno in ('1120824222111222','1120828095247804','1120823143702815','1120830162053540','1120830162428127','1120830200430844','1120831072234036','1120830215753221','1120814151954612',
'1120827073336101','1120830134526079','1120901095942851','1120831180108831','1120901121149683','1120901104957755','1120827121555282','1120827174802203',
'1120828120756861','1120828174708947','1120828180042337','1120828201150057','1120827121438773','1120827124136349','1120827202435266','1120827213054392','1120823123427574',
'1120828090345954','1120824123943017','1120828094025057','1120828100503128','1120824121812545','1120828113914107','1120828151519034','1120829083004512','1120829084957960',
'1120824205402702','1120829111806674','1120824194814863','1120823083855015','1120824220429522','1120829112721061','1120824224732713','1120827215436596','1120825114417115',
'1120829103457352','1120825110728212','1120827222924470','1120824203031023','1120828131443995','1120824235228548','1120828143238943','1120825140652617','1120828165548265',
'1120825111534041','1120829015622386','1120824110815612','1120829100138525','1120826093306531','1120826085703543','1120825223040715','1120825222623049','1120825211341689',
'1120825224441380','1120827195340392','1120727135942266','1120827195948491','1120827200526276','1120828050545030','1120828082632038','1120826135805649','1120828114258557',
'1120829001311922','1120827100443194','1120827100759844','1120827124514872','1120827180110736','1120827205102254','1120827213524810','1120827215228479','1120827221656623',
'1120829095129134','1120901155122413','1120829125727628','1120827192429507','1120828121551151','1120828222424434','1120825212458610','1120829124920637','1120827115256946',
'1120827220635884','1120828192416212','1120829102400584','1120829140049789','1120827144326314','1120827233522743','1120828224456561','1120826195900964','1120824164101957',
'1120827183130024','1120827190453700','1120827111718241','1120828165859099','1120825223406758','1120830092126811','1120828203658505','1120829131605844','1120901233541105',
'1120827224444745','1120903111129339','1120903104442653','1120903104208044','1120903084732627','1120902230138848','1120902185805879','1120822095809988','1120903115541406',
'1120901212131754','1120901014141964','1120901235932820','1120902103644319','1120830221929083','1120901133912711','1120901044358942','1120901224816255','1120826211245869',
'1120828091103436','1120904080931169','1120904081224863','1120904081627913','1120904081738824','1120904081850713','1120904171600004','1120904102220617','1120904093949243',
'1120904084412292','1120904084236764','1120904083849736','1120904083658376','1120904083329817','1120904083152317','1120904083041927','1120904082623580','1120904082538169',
'1120904082446586','1120904082401527','1120904082201557');
update order_refund t set t.newsettle=0, status='finish' where tradeno in 
('1120901235932820','1120901133912711','1120901212131754','1120901014141964','1120902103644319','1120830221929083','1120901044358942','1120901224816255');

--------------------------------------------------------
update order_refund set oldsettle=0, newsettle=0, status='finish' where orderstatus like 'paid_failure%' and status='success'; 


create table CINEMASETTLE(
  RECORDID    NUMBER(19) not null,
  CINEMAID     VARCHAR2(16) not null,
  TIMEFROM   TIMESTAMP(6) not null,
  TIMETO   TIMESTAMP(6) not null,
  LASTTIME   TIMESTAMP(6) not null,
  CURTIME   TIMESTAMP(6) not null,
  QUANTITY     NUMBER(8),
  AMOUNT      NUMBER(8),
  LASTORDERREFUND     NUMBER(4),
  CURORDERREFUND      NUMBER(4)
);
alter table CINEMASETTLE modify QUANTITY number(8);
alter table CINEMASETTLE modify AMOUNT number(8);
alter table CINEMASETTLE modify LASTORDERREFUND number(8);
alter table CINEMASETTLE modify CURORDERREFUND number(8);

alter table CINEMASETTLE add adjustment number(8) default 0 not null;
alter table CINEMASETTLE ADD REMARK VARCHAR2(500);

alter table webdata.CINEMASETTLE
  add constraint PK_CINEMASETTLE primary key (RECORDID)
  using index (create unique index WEBDATA.IDXPK_CINEMASETTLE on webdata.CINEMASETTLE(recordid) tablespace tbs_index);
alter table CINEMASETTLE add STATUS varchar2(1) NOT NULL;

-----------------2012-09-04---------------------------
alter table ORDER_REFUND add settletype varchar2(10);
alter table ORDER_REFUND add oldsettle number(4);
alter table ORDER_REFUND add newsettle number(4);
alter table ORDER_REFUND add ordertype varchar2(10);
alter table ORDER_REFUND add placeid number(19);

update order_refund set ordertype='ticket' where tradeno like '11%' and ordertype is null;
commit;
update order_refund set placeid=(select cinemaid from ticket_order t where t.trade_no=tradeno) where placeid is null and ordertype='ticket';
commit;
update order_refund t set oldsettle=(select costprice*quantity from ticket_order where trade_no=t.tradeno and settle='Y') where oldsettle is null;
commit;
update order_refund t set oldsettle=0,newsettle=0 where orderstatus like 'paid_failure%' and oldsettle is null;
commit;

select trade_no, addtime, updatetime, status, hfhpass, s.REFUNDTIME from ticket_order t left join orderrefund s on t.trade_no=s.tradeno where t.status != 'paid_success' and t.settle='Y' and t.order_type='ticket' and t.hfhpass is not null
select trade_no, addtime, status, hfhpass from ticket_order where status != 'paid_success' and settle='Y' and order_type='ticket';
select trade_no, addtime, status, hfhpass from ticket_order where status = 'paid_success' and settle!='Y' and order_type='ticket';


--------------------------------------------------------------
create table ORDER_REFUND(
  RECORDID    NUMBER(19) not null,
  TRADENO     VARCHAR2(16) not null,
  MEMBERID    NUMBER(19) not null,
  MOBILE VARCHAR2(11) not null,
  TIMETYPE    VARCHAR2(10) not null,
  ORDERSTATUS VARCHAR2(30) not null,
  REFUNDTYPE  VARCHAR2(10) not null,
  PARTNERID   NUMBER(19) not null,
  GEWARETAMOUNT     NUMBER(4) not null,
  MERRETAMOUNT      NUMBER(4) not null,
  CARDNO   VARCHAR2(100),
  OPMARK   VARCHAR2(100),
  REASON   VARCHAR2(50),
  RETBACK  VARCHAR2(1),
  APPLYUSER   NUMBER(19) not null,
  ADDTIME   TIMESTAMP(6) not null,
  REFUNDTIME   TIMESTAMP(6),
  STATUS     VARCHAR2(20) not null,
  APPLYINFO  VARCHAR2(300),
  DEALINFO   VARCHAR2(300)
);
alter table ORDER_REFUND add EXPIRETIME TIMESTAMP(6);
UPDATE ORDER_REFUND F SET EXPIRETIME=(SELECT PLAYTIME FROM OPEN_PLAYITEM O WHERE O.MPID=(SELECT RELATEDID FROM TICKET_ORDER T WHERE T.TRADE_NO=F.TRADENO));
alter table ORDER_REFUND modify EXPIRETIME not null;

alter table ORDER_REFUND drop column TIMETYPE;
alter table ORDER_REFUND add preinfo varchar2(200);
alter table ORDER_REFUND add cancelinfo varchar2(200);
alter table ORDER_REFUND add REFUND_VERSION NUMBER(8) DEFAULT 0 NOT NULL;

update order_refund set refundtype='full' where refundtype in ('all','unknown');

alter table webdata.ORDER_REFUND
  add constraint PK_ORDER_REFUND primary key (RECORDID)
  using index (create unique index webdata.idxpk_ORDER_REFUND on webdata.ORDER_REFUND(recordid) tablespace tbs_index);
create unique index uk_orderrefund on ORDER_REFUND (tradeno) tablespace TBS_INDEX;
--------------------------------------------------------
alter table point drop column from_member;
--alter table point_hist drop column from_member;

--------------------20120817-----------------------------
alter table discount_item add status varchar2(1);

update discount_item d set d.status='Y' where exists(select recordid from ticket_order t where t.status='paid_success' and t.recordid = d.orderid and t.addtime<to_date('2011-08-01','yyyy-mm-dd'));
commit;--244787
update discount_item d set d.status='Y' where exists(select recordid from ticket_order t where t.status='paid_success' and t.recordid = d.orderid and t.addtime>=to_date('2011-08-01','yyyy-mm-dd') and t.addtime<to_date('2011-12-01','yyyy-mm-dd'));
commit;--254368
update discount_item d set d.status='Y' where exists(select recordid from ticket_order t where t.status='paid_success' and t.recordid = d.orderid and t.addtime>=to_date('2011-12-01','yyyy-mm-dd') and t.addtime<to_date('2012-02-01','yyyy-mm-dd'));
commit;--149328
update discount_item d set d.status='Y' where exists(select recordid from ticket_order t where t.status='paid_success' and t.recordid = d.orderid and t.addtime>=to_date('2012-02-01','yyyy-mm-dd') and t.addtime<to_date('2012-05-01','yyyy-mm-dd'));
commit;--253991
update discount_item d set d.status='Y' where exists(select recordid from ticket_order t where t.status='paid_success' and t.recordid = d.orderid and t.addtime>=to_date('2012-05-01','yyyy-mm-dd') and t.addtime<to_date('2012-08-01','yyyy-mm-dd'));
commit;--301141
update discount_item d set d.status='Y' where exists(select recordid from ticket_order t where t.status='paid_success' and t.recordid = d.orderid and t.addtime>=to_date('2012-08-01','yyyy-mm-dd'));

update discount_item d set d.status='N' where exists(select recordid from ticket_order t where t.status='paid_return' and t.recordid = d.orderid and t.addtime<=to_date('2012-01-01','yyyy-mm-dd'));
update ticket_order set status='paid_return' where status = 'paid_failure_unfix' and addtime<=to_date('2012-01-01','yyyy-mm-dd');
commit;--40
update discount_item d set d.status='Y' where status is null and exists(select recordid from ticket_order t where t.status='paid_return' and t.recordid = d.orderid and t.addtime<=to_date('2012-01-01','yyyy-mm-dd'));
commit;--14
update ticket_order set status='paid_return' where status = 'paid_failure' and addtime<=to_date('2012-01-01','yyyy-mm-dd');
commit;--641
update discount_item d set d.status='N' where status is null and exists(select recordid from ticket_order t where t.status='paid_return' and t.recordid = d.orderid and t.addtime<=to_date('2012-01-01','yyyy-mm-dd'));
commit;--90
update discount_item set status='Y' where orderid in (select recordid from ticket_order where trade_no in ('1110116112423672','1101002135308558'))
commit;--4
update discount_item t set t.status='N' where not exists(select recordid from ticket_order where recordid=t.orderid and status like 'paid%');
commit;--163722

update discount_item set status='N' where status is null;

update discount_item t set t.status='N' where status is null and  not exists(select recordid from ticket_order where recordid=t.orderid and status like 'paid%');
update discount_item d set d.status='N' where exists(select recordid from ticket_order t where t.status='paid_return' and t.recordid = d.orderid and t.addtime>to_date('2012-01-01','yyyy-mm-dd'));
update discount_item d set d.status='Y' where status is null and  exists(select recordid from ticket_order t where t.status='paid_success' and t.recordid = d.orderid and t.addtime>to_date('2012-01-01','yyyy-mm-dd'));
update discount_item d set d.status='Y' where exists(select recordid from ticket_order t where t.status='paid_success' and t.recordid = d.orderid and t.addtime>to_date('2012-08-16','yyyy-mm-dd'));
---这笔订单钱款  5111022095242489  2011-10-22 09:52:42.458	paid_unconfirm
update ticket_order set status='paid_success' where trade_no='5111022095242489';
update discount_item set status='Y' where orderid in (select recordid from ticket_order where trade_no='5111022095242489');

select trade_no, addtime, paidtime, updatetime, status from ticket_order t where status='paid_success' and exists (select orderid from discount_item d where status!='Y' and d.orderid=t.recordid );
select * from discount_item where status!='Y' and exists(select recordid from ticket_order t where t.recordid=orderid and t.status='paid_success');





--------------------20120813更新订单相关---------------------
--TODO：订单升级
alter table ticket_order drop column AID;
alter table ticket_order drop column  BUSSPAID;
alter table ticket_order drop column  STTID;
alter table ticket_order drop column  SNO;
alter table ticket_order drop column  DESCRIPTION;

--alter table failure_order drop column AID;
--alter table failure_order drop column  BUSSPAID;
--alter table failure_order drop column  STTID;
--alter table failure_order drop column  SNO;
--alter table failure_order drop column  DESCRIPTION;

alter table ticket_order add settle varchar2(10) default 'O' not null;
alter table failure_order add settle varchar2(10) default 'O' not null;

update ticket_order set settle='Y' where status = 'paid_success' and addtime<to_date('2011-01-01','yyyy-mm-dd');
commit;--138382
update ticket_order set settle='Y' where status = 'paid_success' and addtime>=to_date('2011-01-01','yyyy-mm-dd') and addtime<to_date('2011-06-01','yyyy-mm-dd');
commit;--151812
update ticket_order set settle='Y' where status = 'paid_success' and addtime>=to_date('2011-06-01','yyyy-mm-dd') and addtime<to_date('2011-08-01','yyyy-mm-dd');
commit;--159988
update ticket_order set settle='Y' where status = 'paid_success' and addtime>=to_date('2011-08-01','yyyy-mm-dd') and addtime<to_date('2011-09-01','yyyy-mm-dd');
commit;--132886
update ticket_order set settle='Y' where status = 'paid_success' and addtime>=to_date('2011-09-01','yyyy-mm-dd') and addtime<to_date('2011-11-01','yyyy-mm-dd');
commit;--113089
update ticket_order set settle='Y' where status = 'paid_success' and addtime>=to_date('2011-11-01','yyyy-mm-dd') and addtime<to_date('2011-12-01','yyyy-mm-dd');
commit;--137602
update ticket_order set settle='Y' where status = 'paid_success' and addtime>=to_date('2011-12-01','yyyy-mm-dd') and addtime<to_date('2012-01-01','yyyy-mm-dd');
commit;--143972
update ticket_order set settle='Y' where status = 'paid_success' and addtime>=to_date('2012-01-01','yyyy-mm-dd') and addtime<to_date('2012-02-01','yyyy-mm-dd');
commit;--161734
update ticket_order set settle='Y' where status = 'paid_success' and addtime>=to_date('2012-02-01','yyyy-mm-dd') and addtime<to_date('2012-03-01','yyyy-mm-dd');
commit;--197089
update ticket_order set settle='Y' where status = 'paid_success' and addtime>=to_date('2012-03-01','yyyy-mm-dd') and addtime<to_date('2012-04-01','yyyy-mm-dd');
commit;--117748
update ticket_order set settle='Y' where status = 'paid_success' and addtime>=to_date('2012-04-01','yyyy-mm-dd') and addtime<to_date('2012-04-15','yyyy-mm-dd');
commit;--108927
update ticket_order set settle='Y' where status = 'paid_success' and addtime>=to_date('2012-04-15','yyyy-mm-dd') and addtime<to_date('2012-05-01','yyyy-mm-dd');
commit;--135631
update ticket_order set settle='Y' where status = 'paid_success' and addtime>=to_date('2012-05-01','yyyy-mm-dd') and addtime<to_date('2012-06-01','yyyy-mm-dd');
commit;--232372
update ticket_order set settle='Y' where status = 'paid_success' and addtime>=to_date('2012-06-01','yyyy-mm-dd') and addtime<to_date('2012-07-01','yyyy-mm-dd');
commit;--215682
update ticket_order set settle='Y' where status = 'paid_success' and addtime>=to_date('2012-07-01','yyyy-mm-dd') and addtime<to_date('2012-08-01','yyyy-mm-dd');
commit;--173460
update ticket_order set settle='Y' where status = 'paid_success' and settle!='Y' and order_type='ticket';
commit;--82307
---------------------------------------
alter table AUTOSETTER add ordernum number(8) default 0 not null;

--create user jiesuan identified by jiesuan default tablespace jiesuan quota unlimited on jiesuan
grant connect,resource to jiesuan;
--TODO: UK_MOVIELIST MOVIEID CINEMAID ROOMID PLAYDATE PLAYTIME
alter table movielist drop constraints uk_movielist

alter table AUTOSETTER modify REMARK VARCHAR2(600);
alter table AUTOSETTER modify ROOMNUM VARCHAR2(100);
---------20120730--------------------
create table eleccard_batch_20120730 as select * from eleccard_batch;

--alter table ELECCARD_BATCH drop column ADDTIME;
--alter table ELECCARD_BATCH drop column ADDUSERID;
--alter table ELECCARD_BATCH drop column BATCHNUM;
--alter table ELECCARD_BATCH drop column QUANTITY;
--alter table ELECCARD_BATCH drop column SOLDTIME;
--alter table ELECCARD_BATCH drop column SELLERID;
--alter table ELECCARD_BATCH drop column CHANNEL;
--alter table ELECCARD_BATCH drop column MINCARDNO;
--alter table ELECCARD_BATCH drop column MAXCARDNO;
--alter table ELECCARD_BATCH drop column LIMITNUM;
--alter table ELECCARD_BATCH drop column SELLPRICE;
--alter table ELECCARD_BATCH drop column ISSUERID;
--alter table ELECCARD_BATCH drop column CATEGORY1;
--alter table ELECCARD_BATCH drop column CATEGORY2;
--alter table ELECCARD_BATCH drop column MERCHANTID;
--alter table ELECCARD_BATCH drop column APPLYCITY;
--alter table ELECCARD_BATCH drop column APPLYDEPT;
--alter table ELECCARD_BATCH drop column APPLYTYPE;

--------------------------------------------------
ALTER TABLE MOVIELIST ADD UPDATETIME TIMESTAMP(6);
ALTER TABLE MOVIELIST ADD ROOMNUM VARCHAR2(2);

ALTER TABLE MOVIELISTHIS ADD UPDATETIME TIMESTAMP(6);
ALTER TABLE MOVIELISTHIS ADD ROOMNUM VARCHAR2(2);

ALTER TABLE AUTOSETTER ADD ROOMNUM VARCHAR2(20);
ALTER TABLE AUTOSETTER ADD STATUS VARCHAR2(10);
update movielist t set t.roomnum=(select r.roomnum from cinemaroom r where r.recordid=t.roomid);
update AUTOSETTER set status='open' where status is null;
-------------------------------------------------
create table AUTOSETTER
(
  RECORDID    NUMBER(19) not null,
  NAME        VARCHAR2(50) not null,
  DESCRIPTION VARCHAR2(300),
  CINEMAID    NUMBER(19) not null,
  MOVIES      VARCHAR2(500),
  PLAYTIME1   TIMESTAMP(6) not null,
  PLAYTIME2   TIMESTAMP(6) not null,
  PRICE1      NUMBER(4) not null,
  PRICE2      NUMBER(4) not null,
  WEEKTYPE    VARCHAR2(7) not null,
  TIMESCOPE   VARCHAR2(100) not null,
  EDITION     VARCHAR2(100) not null,
  COSTPRICE   NUMBER(4) not null,
  GEWAPRICE   NUMBER(4) not null,
  ELECARD     VARCHAR2(10),
  REMARK      VARCHAR2(200)
);
alter table webdata.AUTOSETTER
  add constraint PK_AUTOSETTER primary key (RECORDID)
  using index (create unique index webdata.idxpk_AUTOSETTER on webdata.AUTOSETTER(recordid) tablespace tbs_index);
--------------------------------------------------------
alter table NEW_ACTIVITY modify DUETIME not null;
alter table NEW_ACTIVITY modify FROMTIME not null;
-------------------------2012-06-06---------------------
ALTER TABLE HFH_FILM ADD PLANG VARCHAR2(20);
update hfh_film set plang=language where plang is null;

ALTER TABLE HFH_CINEMA ADD CHANNEL VARCHAR2(15);

UPDATE HFH_CINEMA T SET CHANNEL=(SELECT CHANNEL FROM CINEMAPROFILE S WHERE T.GCID=S.CINEMAID);
COMMIT;
select count(*) from HFH_CINEMA WHERE CHANNEL IS NULL AND GCID IS NULL;
DELETE FROM HFH_CINEMA WHERE CHANNEL IS NULL AND GCID IS NULL;

alter table HFH_CINEMA modify channel not null;
alter table CINEMAPROFILE add cminute number(4) default 120 not null;
update CINEMAPROFILE set cminute = chour;
commit;

--alter table cinemaprofile drop column chour;
--alter table cinemaprofile drop column channel;
--alter table cinemaprofile drop column NOTIFYMSG3;

--alter table OPEN_PLAYITEM drop column SECTIONID;
--alter table OPEN_PLAYITEM drop column FILMID;
--alter table OPEN_PLAYITEM drop column SHOWSEQNO;
--alter table OPEN_PLAYITEM drop column NOTIFYMSG1;
--alter table OPEN_PLAYITEM drop column NOTIFYMSG2;
--alter table OPEN_PLAYITEM drop column NOTIFYMSG3;

--alter table OPEN_PLAYITEM_HIS drop column SECTIONID;
--alter table OPEN_PLAYITEM_HIS drop column FILMID;
--alter table OPEN_PLAYITEM_HIS drop column SHOWSEQNO;
--alter table OPEN_PLAYITEM_HIS drop column NOTIFYMSG1;
--alter table OPEN_PLAYITEM_HIS drop column NOTIFYMSG2;
--alter table OPEN_PLAYITEM_HIS drop column NOTIFYMSG3;
------------------------20120531------------------------
ALTER TABLE SPECIALDISCOUNT ADD COSTPRICE1 NUMBER(8) DEFAULT 0 NOT NULL;
ALTER TABLE SPECIALDISCOUNT ADD COSTPRICE2 NUMBER(8) DEFAULT 100 NOT NULL;

create table SPCOUNTER(
  RECORDID  NUMBER(19) not null,
  CTLMEMBER   VARCHAR2(1) not null,
  CTLTYPE  VARCHAR2(10) not null,
  LIMITMAXNUM NUMBER(5) not null,
  SELLQUANTITY NUMBER(5) not null,
  SELLORDERNUM NUMBER(5) not null,
  CITYCOUNTER VARCHAR2(500),
  PARTNERCOUNTER VARCHAR2(500)
);
alter table SPCOUNTER
  add constraint PK_SPCOUNTER primary key (RECORDID)
  using index tablespace tbs_index;
alter table SPCOUNTER add ALLOWADDNUM number(5) not null;
--------------------------------------------------------------------------
update payment set paybank='68209440' where paymethod='ccardPay' and status='paid_success' and payseqno in ('G1202171240005372','G1202171240005423','G1202171240005424','G1202171240005425');

insert into apiuser(RECORDID,PARTNERKEY,PRIVATEKEY,PARTNERNAME,UPDATETIME,CLERK,STATUS,LOGINNAME,BRIEFNAME,ROLES,CITYCODE,defaultcity) values(50000065,'gymiphone','gymiphone','gymiphone',sysdate,1000,'open','gymiphone','gymiphone','apiuser,gewaApi,sportApi','000000','310000');
insert into apiuser(RECORDID,PARTNERKEY,PRIVATEKEY,PARTNERNAME,UPDATETIME,CLERK,STATUS,LOGINNAME,BRIEFNAME,ROLES,CITYCODE,defaultcity) values(50000066,'gymandroid','gymandroid','gymandroid',sysdate,1000,'open','gymandroid','gymandroid','apiuser,gewaApi,sportApi','000000','310000');

---------------------------------------------------------------------
ALTER TABLE CHECK_RECORD ADD WABI_SUM NUMBER(8) DEFAULT 0 NOT NULL;
-------------------------------------------------------------
ALTER TABLE PAYMENT ADD CHARGE_VERSION NUMBER(8) DEFAULT 0 NOT NULL;
-------------------------------------------------------------
update eleccard_batch set PRICE=80 where cardtype='A' and price is null;
alter table eleccard_batch modify PRICE not null;
alter table OPEN_PLAYITEM_EXT ADD CREATEUSER number(19);
update OPEN_PLAYITEM_EXT set CREATEUSER=openuser where CREATEUSER is null;
alter table OPEN_PLAYITEM_EXT modify CREATEUSER not null;

update member set rejected='Y' where recordid=39248268;

 alter table apiuser drop column REMARK;
 alter table apiuser drop column CINEMAIDLIST;
 alter table apiuser drop column OPENTYPE;
 alter table apiuser drop column PAYURL;
--------------------------------------------------------------

update hfh_showupdate set updatetime=sysdate -1 where updatetime is null;
alter table HFH_SHOWUPDATE add UPDATETIME timestamp;
alter table SPECIALDISCOUNT add CREATETIME timestamp;

update SPECIALDISCOUNT s set createtime=(select min(t.addtime)-1 as addtime from discount_item d, ticket_order t 
where d.orderid=t.recordid and t.status='paid_success' and d.tag='partner' and d.cardtype='M' and d.relatedid=s.recordid) where createtime is null;


----------------------------------------------------
insert into apiuser (RECORDID, PARTNERKEY, CONTENT, PRIVATEKEY, PARTNERNAME, PARTNERIP, UPDATETIME, CLERK, STATUS, REMARK, LOGINNAME, LOGINPASS, BRIEFNAME, CINEMAIDLIST, LOGO, ROLES, PARTNERPATH, OPENTYPE, CITYCODE, PAYURL, QRYURL, PUSHURL, SECRETKEY, PUSHFLAG, ADDORDERURL, NOTIFYURL, OTHERINFO, DEFAULTCITY, USERTYPE)
values (50000046, 'barandroid', 'barandroid', 'gewabar20111201', 'barandroid', '', sysdate, 1000, 'open', '', 'gewabar', 'dsfsd', 'gewabar', '', '', 'apiuser,gewaApi', 'barandroid', 'all', '310000', '', '', '', '', '', '', '', '', '310000', 'ticket');
alter table OPEN_PLAYITEM_EXT add createtime timestamp not null;

-------------------------------------------------
alter table CINEMAROOM ADD PLAYTYPE varchar2(6);
-- Create table
create table OPEN_PLAYITEM_EXT(
  MPID      NUMBER(19) not null,
  MOVIEID   NUMBER(19) not null,
  CINEMAID  NUMBER(19) not null,
  REALPRICE NUMBER(4) not null,
  TOTALCOST NUMBER(5) not null
);
alter table OPEN_PLAYITEM_EXT
  add constraint PK_PLAYITEMEXT_MPID primary key (MPID)
  using index tablespace tbs_index;

alter table OPEN_PLAYITEM_EXT add createtime timestamp not null;
alter table OPEN_PLAYITEM_EXT add opentime timestamp not null;
alter table OPEN_PLAYITEM_EXT add openuser number(19) not null;

alter table OPEN_PLAYITEM_EXT drop column MOVIEID;
alter table OPEN_PLAYITEM_EXT drop column CINEMAID;
alter table OPEN_PLAYITEM_EXT drop column updatetime;
alter table OPEN_PLAYITEM_EXT drop column updatecount;

alter table OPEN_PLAYITEM_EXT add REMARK varchar2(500);
alter table OPEN_PLAYITEM_EXT add SEATNUM NUMBER(5) DEFAULT 0 NOT NULL;
alter table OPEN_PLAYITEM_EXT add DELAYMIN NUMBER(10) DEFAULT 0 NOT NULL;

alter table movielist add createtime timestamp;
alter table MOVIELISTHIS add createtime timestamp;
alter table movielist add openStatus varchar2(10);
alter table MOVIELISTHIS add openStatus varchar2(10);

update movielist set openstatus='init' where openstatus is null;
update movielist t set openstatus='open' where openstatus ='init' and exists(select recordid from open_playitem s where s.mpid=t.recordid);

insert into open_playitem_ext 
select mpid,0,0,playtime-2,playtime-2,0,'',0,0 from open_playitem where playtime>sysdate 
and mpid not in (select mpid from open_playitem_ext);
update movielist set createtime=playdate-2 where createtime is null;

update open_playitem_ext set delaymin=abs(delaymin) where delaymin<0;

------------------------------------------------------------------------------
create table webdata.bak_c_comment as select * from webdata.c_comment;
update webdata.c_comment a set a.PICTURENAME=regexp_substr(a.body,'<img(\s)+src="([^"]+)"/>',1,1) where regexp_instr(a.body,'<img(\s)+src="([^"]+)"/>') !=0;
update c_comment set picturename = regexp_replace(picturename,'.*"(.*)".*','\1') where picturename is not null;
update c_comment set picturename = substr(picturename,2) where picturename like '/%';
update c_comment set picturename=null where picturename like 'http%';

update webdata.c_comment a set a.body=regexp_replace(a.body,'<img(\s)+src="([^"]+)"/>',' ') where regexp_instr(a.body,'<img(\s)+src="([^"]+)"/>') !=0;
update webdata.c_comment a set a.body=regexp_replace(a.body,'<img(\s)+src=""/>','') where regexp_instr(a.body,'<img(\s)+src=""/>') !=0;

-----------------------------------------------------------------------
drop materialized view DISCOUNT_STATS;
drop materialized view DISCOUNT_QUDAO;
drop materialized view MEAL_ORDER;
drop materialized view ELECCARD_ORDER;


ALTER TABLE GEWACONFIG ADD NEWCONTENT VARCHAR2(2000);
UPDATE GEWACONFIG SET NEWCONTENT=CONTENT WHERE RECORDID NOT IN(1,8,10);
delete from gewaconfig where  RECORDID IN(1,8,10);
alter table gewaconfig drop column content;

------------------------------------------------------------------
insert into gewaconfig(recordid, newcontent, description, updatetime) values(186,'test','cinema activity date',sysdate);
alter table ELECCARD_BATCH add applycity varchar2(6);
alter table ELECCARD_BATCH add applydept varchar2(6);
alter table ELECCARD_BATCH add applytype varchar2(6);

alter table SPECIALDISCOUNT add applycity varchar2(6);
alter table SPECIALDISCOUNT add applydept varchar2(6);
alter table SPECIALDISCOUNT add applytype varchar2(6);
alter table SPECIALDISCOUNT add orderAllowance number(8) default 0 not null;
alter table SPECIALDISCOUNT add unitAllowance number(8) default 0 not null;
alter table SPECIALDISCOUNT add maxAllowance number(8) default 0 not null;

------------------------------------------------------------------
--Member、MemberInfo更新
alter table memberinfo add logintime timestamp;
update memberinfo set logintime=addtime;
alter table memberinfo add NICKNAME varchar2(100);
alter table memberinfo add ADDTIME timestamp;
alter table memberinfo add UPDATETIME timestamp;
alter table memberinfo add HEADPIC varchar2(300);

update memberinfo t set nickname=(select nickname from member s where s.recordid=t.recordid);
update memberinfo t set ADDTIME=(select ADDTIME from member s where s.recordid=t.recordid);
update memberinfo t set UPDATETIME=(select UPDATETIME from member s where s.recordid=t.recordid);
update memberinfo t set HEADPIC=(select HEADPIC from member s where s.recordid=t.recordid);

update memberinfo t set HEADPIC=(select HEADPIC from member s where s.recordid=t.recordid) where headpic is null and exists (select HEADPIC from member s where s.recordid=t.recordid and s.headpic is not null);
update memberinfo t set nickname=(select nickname from member s where s.recordid=t.recordid) where t.nickname is null;
update memberinfo t set ADDTIME=(select ADDTIME from member s where s.recordid=t.recordid) where addtime is null;

alter table member modify addtime null;

------------------------------------------------------------------
update Open_Drama_Item set roomid = 35030284 where roomid=35030254;
create table webdata.PhoneAdvertisement(
recordid               number(19,0),
advlink          varchar2(200),
link             varchar2(200),
title            varchar2(50),
apptype          varchar2(6),
ostype           varchar2(7),
citycode         varchar2(6),
addtime          timestamp,
isshow           char(1),
status           char(1),
starttime        timestamp,
endtime          timestamp
) tablespace webdata;

alter table webdata.PhoneAdvertisement add constraint pk_PhoneAdvertisement primary key (recordid)
  using index (create unique index webdata.idxpk_PhoneAdvertisement on webdata.PhoneAdvertisement(recordid) tablespace tbs_index);
-------------------------------------------------------
alter table markcount add constraint UK_markcount_tagrelatedid unique (tag, relatedid) using index tablespace WEBDATA;
--------------------------------------------
--注意：未分配的卡号不要移动到历史数据中
insert into eleccard select * from eleccard_hist where batchid in (select recordid from eleccard_batch s where s.pid is null);
delete from eleccard_hist where batchid in (select recordid from eleccard_batch s where s.pid is null);
insert into eleccard select * from eleccard_hist where status='N';

-------------------------------------------
insert into webdata.MOBILE_BUYTIMES
select c.mobile, count(c.recordid) as BUYTIMES, max(c.addtime) as LASTTIME
from ticket_order c
where c.status='paid_success' and c.addtime>sysdate-365
having count(c.recordid)>=2
group by c.mobile;

create table webdata.MOBILE_BUYTIMES(
mobile varchar2(11),
buytimes number(8,0) not null,
lasttime timestamp not null,
constraint pk_MOBILE_BUYTIMES primary key (MOBILE) 
   using index (create unique index webdata.idxpk_MOBILE_BUYTIMES on webdata.MOBILE_BUYTIMES(MOBILE) tablespace tbs_index)
)
tablespace webdata;

--update ticket_order set购买次数
------------1125---------------------
alter table sport_profile add return_money_min number(4,0) default 0;
alter table sport_profile add change_money_min number(4,0) default 0;
alter table sport_profile add limitminutes number(4,0) default 0 not null ;
alter table sport_profile add exitsreturn  char(1);
alter table sport_profile add returnminutes number(4,0) default 0  ;
alter table sport_profile add returnmoneytype char(1) default 'A' ;
alter table sport_profile add returnmoney number(6,2) default 0;
alter table sport_profile add exitschange  char(1);
alter table sport_profile add changeminutes number(4,0) default 0  ;
alter table sport_profile add changemoneytype  char(1) default 'A' ;
alter table sport_profile add changemoney  number(6,2) default 0;
alter table sport_profile add diaryid number(19);
alter table sport_profile add tickettype char(1) default 'A' not null;

alter table sport_profile add constraint chk_sportpfile_rtnmoneymin check (return_money_min>=0);
alter table sport_profile add constraint chk_sportpfile_chgmoneymin check (change_money_min>=0);
alter table sport_profile add constraint chk_sportpfile_returnminutes check (returnminutes >=0);
alter table sport_profile add constraint chk_sportpfile_limitminutes check (limitminutes>=0);

------------------------------------------------------
alter table webdata.discountinfo add title varchar2(60);
alter table webdata.sport_profile add sortnum number(4) default 0 not null;

drop table MERCHANT;
create table webdata.MERCHANT(
RECORDID            number(19) not null,
MERCHANTNAME        varchar2(50) not null,
GEWAPRIKEY	        varchar2(500) not null,
GEWAPUBKEY	        varchar2(500) not null,
MERPUBKEY	        varchar2(500) not null,
MD5KEY              varchar2(500) not null,
TAG                 varchar2(10) not null,
RELATEDID           varchar2(100) not null,
BATCHID             NUMBER(19),
ADDTIME             timestamp not null,
constraint PK_MERCHANT primary key (recordid) 
   using index (create unique index webdata.idxpk_MERCHANT on webdata.MERCHANT(recordid) tablespace tbs_index)
)
tablespace webdata;

-------------------------------------------------------------
alter table ELECCARD_BATCH add EDITION varchar2(8);
alter table ELECCARD_BATCH add CATEGORY1 varchar2(40);
alter table ELECCARD_BATCH add CATEGORY2 varchar2(40);
alter table ELECCARD_BATCH add MERCHANTID number(19) default 0 not null;

create table webdata.GOODS_SPORTGIFT(
recordid            number(19),
goodsid             number(19) not null,
sportid	           number(19) not null,
itemid	           number(19) ,
RATEINFO           varchar2(50),
FROMTIME           timestamp not null,
TOTIME             timestamp not null,
hours             VARCHAR2(1500),
constraint pk_GOODS_SPORTGIFT primary key (recordid) 
   using index (create unique index webdata.idxpk_GOODS_SPORTGIFT on webdata.GOODS_SPORTGIFT(recordid) tablespace tbs_index)
)
tablespace webdata;

------postgres job------------------------
DROP TABLE JOBLOCK;
CREATE TABLE JOBLOCK(
  JOBNAME varchar(40),
  FIRETIME VARCHAR(20),
  IP VARCHAR(15),
  STATUS VARCHAR(10),
  NEXTFIRE VARCHAR(20),
  CONSTRAINT PK_JOBLOCK PRIMARY KEY (JOBNAME, NEXTFIRE)
);
ALTER TABLE joblock OWNER TO job;
-------------------------------------------

alter table PARTNER_PLAYITEM add PLAYDATE DATE;
alter table PARTNER_PLAYITEM add CINEMAID NUMBER(19) NOT NULL;



alter table APIUSER add USERTYPE varchar2(20);
update apiuser set usertype='ticket';
alter table SPECIALDISCOUNT ADD PARTNERID NUMBER(19) DEFAULT 0 NOT NULL;
----point 20111102------------------------------------
一、建表
CREATE TABLE "WEBDATA"."POINT_HIST"
   (	"RECORDID" NUMBER(19,0) ,
	"TO_MEMBERID" NUMBER(19,0) CONSTRAINT CONS_NN_PNT_HIST_TOMEMID NOT NULL ENABLE,
	"FROM_MEMBERID" NUMBER(19,0),
	"TAG" VARCHAR2(100) CONSTRAINT CONS_NN_PNT_HIST_TAG NOT NULL ENABLE,
	"TAGID" NUMBER(19,0),
	"POINTVALUE" NUMBER(19,0) CONSTRAINT CONS_NN_PNT_HIST_PNTVAL NOT NULL ENABLE,
	"REASON" VARCHAR2(500),
	"ADMINID" NUMBER(19,0),
	"ADDTIME" TIMESTAMP (6) CONSTRAINT CONS_NN_PNT_HIST_ADDTIME NOT NULL ENABLE,
	"UNIQUETAG" VARCHAR2(100),
	 CONSTRAINT "PK_PIONT_HIST" PRIMARY KEY ("RECORDID")
    USING INDEX (CREATE UNIQUE INDEX WEBDATA.IDXPK_PNT_HIST_RECID ON WEBDATA.POINT_HIST(RECORDID) TABLESPACE "TBS_INDEX")
) SEGMENT CREATION IMMEDIATE  TABLESPACE "WEBDATA";


从point插入数据导point_hist
insert into webdata.point_hist select * from webdata.point where addtime<to_timestamp('2011-09-01 00:00:00','yyyy-mm-dd hh24:mi:ss');

创建索引：
create index webdata.idx_pnt_group on webdata.point(to_memberid,addtime) tablespace tbs_index;

从point中删除数据
delete from webdata.point where addtime<to_timestamp('2011-09-01 00:00:00','yyyy-mm-dd hh24:mi:ss');


创建定时程序，在每月的第一日，进行数据转移和清理，具体语句如下：
insert into webdata.point_hsit select * from webdata.point where addtime<add_months(trunc(sysdate,'mm'),-2);
delete from webdata.point where addtime<add_months(trunc(sysdate,'mm'),-2);

-----------------------------------------------------------------
alter table APP_USER add rolenames varchar2(2000);
alter table WEBMODULE add rolenames varchar2(2000);
alter table USER_ROLE add id number(19);
alter table WEBMODULE_ROLE add id number(19);
update USER_ROLE set id=rownum;
update WEBMODULE_ROLE set id=rownum;
alter table USER_ROLE modify id not null;
alter table WEBMODULE_ROLE modify id not null;


--------------------------------------------------20111101----------------------------------
alter table GEWACONFIG add UPDATETIME TIMESTAMP;
insert into gewaconfig(recordid, content, description,updatetime) values(90,'{"":"","":""}','ticket queue control',sysdate);
insert into gewaconfig(recordid,content,description,updatetime) values(91,'{}','cinema maxtimes config',sysdate);
commit;
alter table ACCUSATION add newbody varchar2(1000);
update ACCUSATION set newbody = body;
commit;
alter table ACCUSATION rename column body to oldbody;
alter table ACCUSATION rename column newbody to body;

alter table CINEMAROOM add NEWSEATMAP varchar2(4000);
update CINEMAROOM set NEWSEATMAP = SEATMAP;
commit;
alter table CINEMAROOM rename column SEATMAP to OLDSEATMAP;
alter table CINEMAROOM rename column NEWSEATMAP to SEATMAP;

alter table EMAILRECORD add NEWCONTENT varchar2(4000);
update EMAILRECORD set NEWCONTENT = CONTENT;
commit;
alter table EMAILRECORD rename column CONTENT to OLDCONTENT;
alter table EMAILRECORD rename column NEWCONTENT to CONTENT;


alter table JSONDATA add NEWDATA varchar2(4000);
update JSONDATA set NEWDATA = DATA;
commit;
alter table JSONDATA rename column DATA to OLDDATA;
alter table JSONDATA rename column NEWDATA to DATA;


alter table MAILCONTENT add NEWCONTENT varchar2(4000);
update MAILCONTENT set NEWCONTENT = CONTENT;
commit;
alter table MAILCONTENT rename column CONTENT to OLDCONTENT;
alter table MAILCONTENT rename column NEWCONTENT to CONTENT;

alter table RECOMMENT add newbody varchar2(2000);
update RECOMMENT set newbody = body;
commit;
alter table RECOMMENT rename column body to oldbody;
alter table RECOMMENT rename column newbody to body;

alter table SPECIAL_ACTIVITY add NEWCONTENT varchar2(4000);
update SPECIAL_ACTIVITY set NEWCONTENT = CONTENT;
commit;
alter table SPECIAL_ACTIVITY rename column CONTENT to OLDCONTENT;
alter table SPECIAL_ACTIVITY rename column NEWCONTENT to CONTENT;

alter table SYSTEM_MESSAGE_ACTION add newbody varchar2(2000);
update SYSTEM_MESSAGE_ACTION set newbody = body;
commit;
alter table SYSTEM_MESSAGE_ACTION rename column body to oldbody;
alter table SYSTEM_MESSAGE_ACTION rename column newbody to body;


alter table USER_MESSAGE add NEWCONTENT varchar2(2500);
update USER_MESSAGE set NEWCONTENT = CONTENT;
commit;
alter table USER_MESSAGE rename column CONTENT to OLDCONTENT;
alter table USER_MESSAGE rename column NEWCONTENT to CONTENT;


alter table ACCUSATION drop column OLDBODY;
alter table CINEMAROOM drop column OLDSEATMAP;
alter table EMAILRECORD drop column OLDCONTENT;
alter table MAILCONTENT drop column OLDCONTENT;
alter table JSONDATA drop column OLDDATA;
alter table RECOMMENT drop column OLDBODY;
alter table SPECIAL_ACTIVITY drop column OLDCONTENT;
alter table SYSTEM_MESSAGE_ACTION drop column OLDBODY;
alter table USER_MESSAGE drop column OLDCONTENT;

drop table DIARYCOMMENT01;
drop table DIARY02;
drop table DIARY_BACK;
drop table DIARY_COMMENT_BACK;
drop table OLD_ACTIVITY;
drop table ACTIVITY02;
drop table BARACTIVITY_SINGER cascade constraints;
drop table BARACTIVITY cascade constraints;

--------20111017-----------------
alter table PARTNERCLOSERULE add PMATCH varchar2(8) ;
alter table PARTNERCLOSERULE add CMATCH varchar2(8) ;
update PARTNERCLOSERULE set pmatch='include',cmatch='include';
------------------------------------------------
\create table VALIDATE_CONNECT(TESTFIELD CHAR(1));
-- Create table
create table PHONEPUSH
(
  RECORDID NUMBER(19) not null,
  TITLE    VARCHAR2(200),
  CONTENT  VARCHAR2(500),
  TYPE     VARCHAR2(10),
  ADDTIME  TIMESTAMP(6)
);
alter table PHONEPUSH
  add constraint PK_PHONEPUSH primary key (RECORDID)
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
create table RELATIONSHIP
(
  RECORDID   NUMBER(19) not null,
  TAG        VARCHAR2(100),
  RELATEDID1 NUMBER(19),
  RELATEDID2 NUMBER(19)
);
alter table RELATIONSHIP
  add constraint PK_RELATIONSHIP primary key (RECORDID)
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
create table MAILPOSTMESSAGE
(
  MESSAGEID    VARCHAR2(200) not null,
  RECEIVER     VARCHAR2(200) not null,
  RECEIVEDTIME DATE not null,
  MAILTO       VARCHAR2(500),
  CONTENT      VARCHAR2(4000) not null,
  BATCH        VARCHAR2(10),
  RECORDID     NUMBER(19) not null
);
alter table MAILPOSTMESSAGE
  add constraint PK_POSTMESSAGE primary key (RECORDID)
  using index 
  tablespace WEBDATA
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 576K
    minextents 1
    maxextents unlimited
  );
alter table MAILPOSTMESSAGE
  add constraint UK_POSTMESSAGE_MSGID unique (MESSAGEID)
  using index 
  tablespace WEBDATA
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 3M
    minextents 1
    maxextents unlimited
  );

#中文语句前执行：
#LANG=zh_CN.GBK

update open_playitem t set t.gsellnum = (
select sum(s.quantity) from ticket_order s where s.relatedid=t.mpid and s.status='paid_success')
where t.playtime>sysdate - 15 and exists (select recordid from ticket_order s where s.relatedid=t.mpid and s.status='paid_success');
commit;

alter table SELLSEAT modify MEMBERID null;
alter table SELLSEAT modify OSEATID null;
alter table SELLSEAT add orderid number(19);
alter table SELLSEAT add oldid number(19);
update sellseat set oldid=recordid;
commit;

create table sellseat_his as
select * from sellseat t where t.mpid in (
select mpid from open_playitem o where o.playtime<to_date('20110101','yyyymmdd')
);

delete from sellseat t where t.mpid in (select mpid from open_playitem o where o.playtime<to_date('20110101','yyyymmdd'));
commit;

delete from order_openseat t where t.order_id in (select recordid from ticket_order o where o.status like 'cancel%');
commit;
delete from order_openseat t where t.order_id in (select recordid from ticket_order t 
  where t.relatedid in (select mpid from open_playitem o where o.playtime<to_date('20110101','yyyymmdd')));
commit;

alter table order_openseat add oldseatid number(19);
update order_openseat set oldseatid=openseat_id;
commit;

delete from order_openseat where openseat_id not in (select recordid from sellseat);
commit;


--等待25分钟后做此操作
create materialized view tmp_orderseat_success as
select t.openseat_id, t.order_id, m.status, m.addtime, m.memberid 
from order_openseat t left join ticket_order m on t.order_id=m.recordid
where m.status='paid_success' and m.relatedid in (
  select mpid from open_playitem o where o.playtime>=to_date('20110101','yyyymmdd')
);

update sellseat t set t.orderid = (
   select s.order_id from tmp_orderseat_success s 
   where s.openseat_id = t.oseatid and t.memberid=s.memberid
) where exists(
  select s.order_id from tmp_orderseat_success s 
  where s.openseat_id = t.oseatid and t.memberid=s.memberid);
commit;

drop materialized view tmp_orderseat_success;

update order_openseat t set t.openseat_id=(
select s.oseatid from sellseat s where s.recordid=t.openseat_id)
where exists (select s.oseatid from sellseat s where s.recordid=t.openseat_id);

update sellseat t set t.recordid=t.oseatid;


alter table MEMBER_LOCUS drop column ADDTIME;
alter table MEMBER_LOCUS modify UPDATETIME not null;

create table tmp_locus as
select max(recordid) rid, t.memberfrom, t.memberto from member_locus t
group by t.memberfrom, t.memberto;


delete member_locus s where s.recordid not in (select rid from tmp_locus);
commit;
drop table tmp_locus;


delete hfh_to_cinema where gewa_cinemaid=13711579;

alter table SELLSEAT
  drop constraint UK_SELLSEAT cascade;
alter table SELLSEAT drop column OSEATID;
alter table SELLSEAT drop column MEMBERID;
-- Create/Recreate primary, unique and foreign key constraints 

--ALFTER TOMORROW
alter table SELLSEAT drop column OLDID;
alter table order_openseat drop COLUMN oldseatid;
drop table sellseat_his;


-- Create table
create table UPLOAD_PIC
(
  PICNAME    varchar2(200) not null,
  MEMBERID   number(19),
  MODIFYTIME number(19),
  PICSIZE    number(11),
  STATUS     varchar2(10)
);

-- Create/Recreate primary, unique and foreign key constraints 
alter table UPLOAD_PIC add constraint PK_UPLOADPIC primary key (PICNAME);
alter table PARTNER_PLAYITEM add FLAG varchar2(10);
alter table APP_USER add CITYCODE varchar2(6);
UPDATE APP_USER SET CITYCODE='310000';

update upload_pic set status='checked' where status is null;
commit;
--3.18
alter table CINEMAROOM add seatmap varchar2(3000);

update memberinfo set otherinfo='' where otherinfo like 'coupon%';
update memberinfo set otherinfo='{"openMember":"sina"}' where otherinfo ='openMember=sina';
update memberinfo set otherinfo='{"openMember":"sdo"}' where otherinfo ='openMember=sdo';
update memberinfo set otherinfo='{"openMember":"alipay"}' where otherinfo ='openMember=alipay';

alter table SMSRECORD drop column SEQNO;
alter table SMSRECORD add seqno2 varchar2(20);

update gewaconfig set content='mlink' where recordid=19;
create table smsrecordhis as select * from smsrecord where sendtime < to_date('20110320','yyyymmdd');
delete smsrecord where sendtime < to_date('20110320','yyyymmdd');

--important：record for using after update 
select max(recordid) from albumimage;

update albumimage set recordid = recordid+32120000 where recordid<3031;
delete from albumimage where recordid=29648235;

insert into picture_new(recordid, foreignid, picturename, description, posttime, tag, status, addtype, name) 
select t.recordid, t.albumid, t.logo, t.imagedescription, t.addtime, 'album', 'Y', 'foreground', t.name from albumimage t
where t.recordid <=37789065 and addtime is not null;

insert into picture_new(recordid, foreignid, picturename, description, posttime, tag, status, addtype, name) 
select t.recordid, t.albumid, t.logo, t.imagedescription, sysdate, 'album', 'Y', 'foreground', t.name from albumimage t
where t.recordid <=37789065 and addtime is null;

--after update:
insert into picture_new(recordid, foreignid, picturename, description, posttime, tag, status, addtype, name) 
select t.recordid, t.albumid, t.logo, t.imagedescription, t.addtime, 'album', 'Y', 'foreground', t.name from albumimage t
where t.recordid >37789065;

alter table CINEMAPROFILE add channel char(2);
alter table HFH_CINEMA add GCID number(19);
update hfh_cinema t set t.gcid=(select s.gewa_cinemaid from hfh_to_cinema s where s.hfh_cinemaid=t.cinemaid);
alter table HFH_CINEMA add constraint uk_hfhcinema unique (GCID);

insert into gewaconfig (RECORDID, CONTENT, DESCRIPTION) values (20, '{"sh":"sh","gz":"gz"}', 'hfh channel mapping');


--更新售票用户
update diary t set t.flag=t.flag||',ticket'
where t.category='movie' and t.type='comment' and (flag not like '%ticket%' or flag is null) and 
exists(select recordid from ticket_order s where s.status='paid_success' and s.memberid=t.memberid and s.movieid=t.categoryid and s.addtime<t.addtime);

commit;
update diary set flag=substr(flag,2) where flag like ',%';
commit;

update c_comment t set flag = t.flag||',ticket'
where t.tag='movie' and (flag not like '%ticket%' or flag is null) and 
exists(select recordid from ticket_order s where s.status='paid_success' 
and s.memberid=t.memberid and s.movieid=t.relatedid and s.addtime<t.addtime);
commit;

update c_comment t set flag = t.flag||',ticket'
where t.tag='cinema' and (flag not like '%ticket%' or flag is null) and 
exists(select recordid from ticket_order s where s.status='paid_success' 
and s.memberid=t.memberid and s.cinemaid=t.relatedid and s.addtime<t.addtime);
commit;

update c_comment set flag=substr(flag,2) where flag like ',%';
commit;

--价格改变

alter table PARTNER_PRICE modify PARTNERID not null;
alter table PARTNER_PRICE modify CINEMAID not null;
alter table PARTNER_PRICE modify DISCOUNT not null;
alter table PARTNER_PRICE modify CLERKID not null;
alter table PARTNER_PRICE modify UPDATETIME not null;

alter table PARTNER_PRICE add timefrom timestamp not null;
alter table PARTNER_PRICE add timeto timestamp not null;
alter table PARTNER_PRICE add time1 char(4) not null;
alter table PARTNER_PRICE add time2 char(4) not null;
alter table PARTNER_PRICE add pricegap number(2) not null;
alter table PARTNER_PRICE add price1 number(3) not null;
alter table PARTNER_PRICE add price2 number(3) not null;

alter table PARTNER_PRICE drop column PLAYDATE;
alter table PARTNER_PRICE drop column PERCENTAGE;
alter table PARTNER_PRICE drop column CHANGEHIS;
alter table PARTNER_PRICE drop column FROMDATE;
alter table PARTNER_PRICE drop column TODATE;

alter table CINEMAPROFILE add chour number(2) default 1 not null;
alter table APIUSER add citycode varchar2(300);
update CINEMAPROFILE set chour=60 where chour!=60;

alter table MOVIELIST add CITYCODE char(6);
alter table MOVIELISTHIS add CITYCODE char(6);
alter table open_playitem add CITYCODE char(6);

update movielisthis t set t.citycode=(select citycode from cinema c where t.cinemaid=c.recordid) where citycode is null;
update movielist t set t.citycode=(select citycode from cinema c where t.cinemaid=c.recordid) where citycode is null;
update open_playitem t set t.citycode=(select citycode from cinema c where t.cinemaid=c.recordid) where citycode is null;

alter table MOVIELIST modify CITYCODE not null;
alter table open_playitem modify CITYCODE not null;

alter table HFH_FILM add FLAG varchar2(10);
update apiuser set citycode='310000' where citycode is null;
alter table TICKET_ORDER modify TRADE_NO VARCHAR2(30);

alter table APIUSER add PAYURL varchar2(200);
alter table APIUSER add QRYURL varchar2(200);
alter table APIUSER add PUSHURL varchar2(200);
alter table APIUSER add SECRETKEY varchar2(200);

update apiuser t set t.qryurl='http://movie.online.sh.cn/movie/order/checkOrder.shtml', t.payurl='http://movie.online.sh.cn/movie/order/recOrderInfo.shtml' where t.recordid=50000150;
update apiuser t set t.qryurl='', t.payurl='http://www.anxin1688.com/film/genOrder.action' where t.recordid=50000170;

alter table APIUSER add PUSHFLAG varchar2(100);
update apiuser t set t.pushflag = 'success' where t.recordid in (50000160,50000140,50000145);
update apiuser t set t.pushflag = 'new,success' where t.recordid = 50000160;

alter table apiuser drop column HASCHILD;
alter table apiuser drop column PID;

alter table CALLBACK_ORDER add PUSHFLAG varchar2(50);

alter table CALLBACK_ORDER add RECORDID number(19);
UPDATE callback_order SET RECORDID=ORDERID;
alter table CALLBACK_ORDER modify RECORDID not null;
alter table CALLBACK_ORDER drop constraint PK_CALLBACKORDER cascade;
drop index PK_CALLBACKORDER;

alter table CALLBACK_ORDER
  add constraint PK_CALLBACKORDER primary key (RECORDID)
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

alter table CALLBACK_ORDER
  add constraint UK_CALLBACKORDER unique (ORDERID, PUSHFLAG);
  
alter table APIUSER modify PRIVATEKEY not null;
alter table APIUSER modify CLERK not null;
alter table APIUSER add addorderurl varchar2(200);
alter table APIUSER add notifyurl varchar2(200);
update apiuser set addorderurl=payurl;
update apiuser t set t.notifyurl='http://manage.gewara.com/pay/partnerPayNotify.xhtml' where t.recordid>50000100;
update apiuser t set t.notifyurl='http://m.gewara.com/payOrder.xhtml?orderId=${orderid}' where t.recordid<50000100;

alter table APIUSER add OTHERINFO varchar2(200);


alter table CALLBACK_ORDER drop constraint PK_CALLBACKORDER cascade;
alter table CALLBACK_ORDER drop constraint UK_CALLBACKORDER cascade;
alter table CALLBACK_ORDER modify RECORDID null;

alter table CALLBACK_ORDER drop column RECORDID;
alter table CALLBACK_ORDER drop column PUSHFLAG;
alter table CALLBACK_ORDER add constraint pk_callbackorder primary key (ORDERID);


alter table MOVIE add AVGPRICE number(4) default 0 not null;
alter table CINEMAROOM add loveflag char(1);
update cinemaroom set loveflag='N';
commit;
update cinemaroom r set loveflag='Y' where exists (select s.recordid from roomseat s where s.roomid=r.recordid and s.loveind='1');
commit;

alter table MOVIE drop column LOWESTPRICE;
alter table MOVIE drop column DIARYID;
alter table GYMCOURSE drop column DIARYID;
alter table DRAMA drop column DIARYID;
alter table SPORTITEM drop column DIARYID;

alter table GOODS add spflag varchar2(20);
alter table GOODS add otherinfo varchar2(1000);

alter table SPECIALDISCOUNT add LIMITMAXNUM number(8) default 999999 not null;
alter table SPECIALDISCOUNT add sellcount number(8) default 0 not null;
alter table SPECIALDISCOUNT add updatetime timestamp;

update SPECIALDISCOUNT set updatetime=sysdate;

create sequence member_sequence start with 38700000 increment by 1;
alter table TICKET_ORDER add OTHERINFO varchar2(1000);
alter table OPEN_PLAYITEM add SPFLAG varchar2(100);

alter table SPECIALDISCOUNT add TAG varchar2(10) default 'movie' not null;
alter table SPECIALDISCOUNT add TIME1 char(4) default '0000' not null;
alter table SPECIALDISCOUNT add TIME2 char(4) default '2400' not null;
alter table SPECIALDISCOUNT add PRICEGAP number(3) default 1 not null;
alter table SPECIALDISCOUNT add PRICE1 number(6) default 1 not null;
alter table SPECIALDISCOUNT add PRICE2 number(6) default 1000 not null;
alter table SPECIALDISCOUNT add CITYCODE varchar2(200) default '000000' not null;
alter table SPECIALDISCOUNT add relatedid varchar2(500);
alter table SPECIALDISCOUNT add categoryid varchar2(500);
alter table SPECIALDISCOUNT add itemid varchar2(500);
alter table SPECIALDISCOUNT add opentype char(1) default 'S' not null;

alter table SPECIALDISCOUNT modify TIME2 default '2359';
alter table SPECIALDISCOUNT add ADDTIME1 char(4) default '0000' not null;
alter table SPECIALDISCOUNT add ADDTIME2 char(4) default '2359' not null;
alter table SPECIALDISCOUNT add ADDWEEK varchar2(20);
alter table SPECIALDISCOUNT add WEEKTYPE varchar2(20);

alter table OPENMEMBER drop constraint UK_OPENMEMBER1 cascade;
alter table OPENMEMBER add constraint UK_OPENMEMBER3 unique (MEMBERID, SOURCE);

alter table JSONDATA modify DATA VARCHAR2(4000);


create index idx_opi_cinemaid on OPEN_PLAYITEM (cinemaid);
create index idx_opi_movieid on OPEN_PLAYITEM (movieid);
create index idx_opi_citycode on OPEN_PLAYITEM (citycode);

alter table SPECIALDISCOUNT add ADCONTENT varchar2(4000);
alter table SPECIALDISCOUNT add BANNER varchar2(200);

alter table HFH_FILM add recordid varchar2(40);
alter table HFH_FILM drop constraint PK_FILMID cascade;
drop index PK_FILMID;
alter table HFH_FILM add CINEMAID varchar2(20);
alter table HFH_FILM add ADDTIME timestamp;

alter table SPECIALDISCOUNT add BUYNUM number(3) default 5;


alter table SPECIALDISCOUNT add REBATES number(3) default 0 not null;
alter table SPECIALDISCOUNT add REBATESMAX number(4) default 0 not null;
alter table SPECIALDISCOUNT add REBATESTYPE char(1) default 'Y' not null;

alter table PAYMENT modify PAYBANK VARCHAR2(20);
------------20110621-----------
alter table LINE2STATION add STATIONORDER NUMBER(4);

update cinema set stationid=101 where stationid=170202;
update cinema set stationid=104 where stationid=170199;
update cinema set stationid=107 where stationid=170196;
update cinema set stationid=106 where stationid=170197;
update cinema set stationid=30 where stationid=170195;
update cinema set stationid=109 where stationid=170151;
update cinema set stationid=2 where stationid=40;
update cinema set stationid=29 where stationid=170152;
update cinema set stationid=36 where stationid=7700836;
update cinema set stationid=100 where stationid=170160;
update cinema set stationid=28 where stationid=240850;
update cinema set stationid=102 where stationid=170201;
update cinema set stationid=34 where stationid=7700834;
update cinema set stationid=35 where stationid=7700835;
update cinema set stationid=99 where stationid=170204;
update cinema set stationid=3 where stationid=39;
update cinema set stationid=21 where stationid=170185;
update cinema set stationid=103 where stationid=170200;
update cinema set stationid=36 where stationid=7700836;
update cinema set stationid=34 where stationid=7700834;
update cinema set stationid=21 where stationid=98;
update cinema set stationid=30 where stationid=108;
update cinema set stationid=35 where stationid=7700835;

update sport set stationid=101 where stationid=170202;
update sport set stationid=104 where stationid=170199;
update sport set stationid=107 where stationid=170196;
update sport set stationid=106 where stationid=170197;
update sport set stationid=30 where stationid=170195;
update sport set stationid=109 where stationid=170151;
update sport set stationid=2 where stationid=40;
update sport set stationid=29 where stationid=170152;
update sport set stationid=36 where stationid=7700836;
update sport set stationid=100 where stationid=170160;
update sport set stationid=28 where stationid=240850;
update sport set stationid=102 where stationid=170201;
update sport set stationid=34 where stationid=7700834;
update sport set stationid=35 where stationid=7700835;
update sport set stationid=99 where stationid=170204;
update sport set stationid=3 where stationid=39;
update sport set stationid=21 where stationid=170185;
update sport set stationid=103 where stationid=170200;
update sport set stationid=36 where stationid=7700836;
update sport set stationid=34 where stationid=7700834;
update sport set stationid=21 where stationid=98;
update sport set stationid=30 where stationid=108;
update sport set stationid=35 where stationid=7700835;

update gym set stationid=101 where stationid=170202;
update gym set stationid=104 where stationid=170199;
update gym set stationid=107 where stationid=170196;
update gym set stationid=106 where stationid=170197;
update gym set stationid=30 where stationid=170195;
update gym set stationid=109 where stationid=170151;
update gym set stationid=2 where stationid=40;
update gym set stationid=29 where stationid=170152;
update gym set stationid=36 where stationid=7700836;
update gym set stationid=100 where stationid=170160;
update gym set stationid=28 where stationid=240850;
update gym set stationid=102 where stationid=170201;
update gym set stationid=34 where stationid=7700834;
update gym set stationid=35 where stationid=7700835;
update gym set stationid=99 where stationid=170204;
update gym set stationid=3 where stationid=39;
update gym set stationid=21 where stationid=170185;
update gym set stationid=103 where stationid=170200;
update gym set stationid=36 where stationid=7700836;
update gym set stationid=34 where stationid=7700834;
update gym set stationid=21 where stationid=98;
update gym set stationid=30 where stationid=108;
update gym set stationid=35 where stationid=7700835;

update bar set stationid=101 where stationid=170202;
update bar set stationid=104 where stationid=170199;
update bar set stationid=107 where stationid=170196;
update bar set stationid=106 where stationid=170197;
update bar set stationid=30 where stationid=170195;
update bar set stationid=109 where stationid=170151;
update bar set stationid=2 where stationid=40;
update bar set stationid=29 where stationid=170152;
update bar set stationid=36 where stationid=7700836;
update bar set stationid=100 where stationid=170160;
update bar set stationid=28 where stationid=240850;
update bar set stationid=102 where stationid=170201;
update bar set stationid=34 where stationid=7700834;
update bar set stationid=35 where stationid=7700835;
update bar set stationid=99 where stationid=170204;
update bar set stationid=3 where stationid=39;
update bar set stationid=21 where stationid=170185;
update bar set stationid=103 where stationid=170200;
update bar set stationid=21 where stationid=98;
update bar set stationid=30 where stationid=108;

update ktv set stationid=101 where stationid=170202;
update ktv set stationid=104 where stationid=170199;
update ktv set stationid=107 where stationid=170196;
update ktv set stationid=106 where stationid=170197;
update ktv set stationid=30 where stationid=170195;
update ktv set stationid=109 where stationid=170151;
update ktv set stationid=2 where stationid=40;
update ktv set stationid=29 where stationid=170152;
update ktv set stationid=36 where stationid=7700836;
update ktv set stationid=100 where stationid=170160;
update ktv set stationid=28 where stationid=240850;
update ktv set stationid=102 where stationid=170201;
update ktv set stationid=34 where stationid=7700834;
update ktv set stationid=35 where stationid=7700835;
update ktv set stationid=99 where stationid=170204;
update ktv set stationid=3 where stationid=39;
update ktv set stationid=21 where stationid=170185;
update ktv set stationid=103 where stationid=170200;
update ktv set stationid=21 where stationid=98;
update ktv set stationid=30 where stationid=108;

update theatre set stationid=101 where stationid=170202;
update theatre set stationid=104 where stationid=170199;
update theatre set stationid=107 where stationid=170196;
update theatre set stationid=106 where stationid=170197;
update theatre set stationid=30 where stationid=170195;
update theatre set stationid=109 where stationid=170151;
update theatre set stationid=2 where stationid=40;
update theatre set stationid=29 where stationid=170152;
update theatre set stationid=36 where stationid=7700836;
update theatre set stationid=100 where stationid=170160;
update theatre set stationid=28 where stationid=240850;
update theatre set stationid=102 where stationid=170201;
update theatre set stationid=34 where stationid=7700834;
update theatre set stationid=35 where stationid=7700835;
update theatre set stationid=99 where stationid=170204;
update theatre set stationid=3 where stationid=39;
update theatre set stationid=21 where stationid=170185;
update theatre set stationid=103 where stationid=170200;
update theatre set stationid=21 where stationid=98;
update theatre set stationid=30 where stationid=108;

update line2station set sid=101 where sid=170202;
update line2station set sid=104 where sid=170199;
update line2station set sid=107 where sid=170196;
update line2station set sid=106 where sid=170197;
update line2station set sid=30 where sid=170195;
update line2station set sid=109 where sid=170151;
update line2station set sid=2 where sid=40;
update line2station set sid=29 where sid=170152;
update line2station set sid=100 where sid=170160;
update line2station set sid=28 where sid=240850;
update line2station set sid=102 where sid=170201;
update line2station set sid=99 where sid=170204;
update line2station set sid=3 where sid=39;
update line2station set sid=21 where sid=170185;
update line2station set sid=103 where sid=170200;
update line2station set sid=21 where sid=98;
update line2station set sid=30 where sid=108;


delete from line2station s where s.sid not in (select recordid from subwaystation)
or s.lid not in (select recordid from subwayline);

delete from line2station where sid in(7700836,7700834,7700835);

alter table LINE2STATION
  add constraint fk_subwayline foreign key (LID)
  references subwayline (RECORDID) on delete cascade;
alter table LINE2STATION
  add constraint fk_station foreign key (SID)
  references subwaystation (RECORDID) on delete cascade;


alter table CINEMA drop constraint FK_CINEMA_INDEXAREACODE;
alter table CINEMA drop constraint FK_CINEMA_LINEID;

alter table BAR drop constraint FK_BAR_INDEXAREACODE;
alter table BAR drop constraint FK_BAR_LINEID;

alter table KTV drop constraint FK_KTV_INDEXAREACODE;
alter table KTV drop constraint FK_KTV_LINEID;

alter table GYM drop constraint FK_GYM_INDEXAREACODE;
alter table GYM drop constraint FK_GYM_LINEID;

alter table SPORT drop constraint FK_SPORT_INDEXAREACODE;
alter table SPORT drop constraint FK_SPORT_LINEID;


alter table GYM_GYMCOACH add RECORDID number(19);
UPDATE GYM_GYMCOACH SET RECORDID=ROWNUM;

DELETE FROM GYM_GYMCOACH WHERE RECORDID IN (SELECT RID FROM (SELECT T.GYMID, T.GYMCOACHID, MAX(T.RECORDID) RID,COUNT(T.RECORDID) FROM GYM_GYMCOACH T GROUP BY T.GYMID, T.GYMCOACHID HAVING COUNT(T.RECORDID)>1) TMP)

-- Add/modify columns 
alter table GYM_GYMCOACH modify RECORDID not null;
alter table GYM_GYMCOACH add constraint UK_GYM2COACH unique (GYMID, GYMCOACHID);
alter table GYM_GYMCOACH add constraint PK_GYM2COACH primary key (RECORDID);


create materialized view view_gymcoach_stats
refresh complete on demand
start with to_date('20-06-2011 18:13:52', 'dd-mm-yyyy hh24:mi:ss') next SYSDATE+10 
as
select count(distinct c.gymcoachid) as brandcount, g.brandname, gc.courseid 
from gym_gymcoach c inner join gym g on g.recordid=c.gymid 
left join gymcoach_course gc on c.gymcoachid=gc.coachid
group by g.brandname, gc.courseid 
----------------------------------------------------

alter table SUBWAYSTATION drop column STATIONORDER;
alter table CINEMA drop column LINEID;
alter table BAR drop column LINEID;
alter table KTV drop column LINEID;
alter table GYM drop column LINEID;
alter table SPORT drop column LINEID;
alter table THEATRE drop column LINEID;

alter table SPORT_SPORTITEM add RECORDID number(19);
DELETE FROM SPORT_SPORTITEM T WHERE T.SPORTID NOT IN (SELECT RECORDID FROM SPORT);
commit;
UPDATE SPORT_SPORTITEM T SET RECORDID=ROWNUM;
commit;
delete SPORT_SPORTITEM where recordid in (select rid from (SELECT t.sportid, t.sportitemid, max(t.recordid) rid, count(t.recordid) from SPORT_SPORTITEM T group by t.sportid, t.sportitemid having count(t.recordid)>1)tmp);
commit;

alter table SPORT_SPORTITEM add constraint FK_SPORT foreign key (SPORTID) references sport (RECORDID) on delete cascade;
alter table SPORT_SPORTITEM add constraint UK_SPORT2ITEM unique (SPORTID, SPORTITEMID);

alter table SPORT_SPORTITEM modify RECORDID not null;
alter table SPORT_SPORTITEM add constraint pk_sport2item primary key (RECORDID);
----------------------------------------------------------------------------------

alter table TICKET_ORDER drop column DESCRIPTION;

alter table smsrecordhis add channel varchar2(15);
alter table smsrecord_his add seqno2 varchar2(15);
alter table smsrecord_his add channel varchar2(15);
alter table smsrecord_his drop column seqno;
insert into smsrecord_his select * from smsrecordhis;
drop table smsrecordhis;
-- Drop primary, unique and foreign key constraints 
alter table MOVIELISTHIS drop constraint FK_MOVIELISTHIS_CINEMA;
alter table MOVIELISTHIS drop constraint FK_MOVIELISTHIS_MOVIE;
drop index IDX_MOVIELISTHIS_MOVIE;
drop index IDX_MOVIELISTHIS_PLAYDATE;


drop table COMMUKING;
drop table COMMUKING_MEMBER;
drop table LOG4J;

alter table APIUSER add DEFAULTCITY VARCHAR2(6);
UPDATE APIUSER SET DEFAULTCITY='310000';
alter table HFH_FILM add LANGUAGE varchar2(20);
alter table CINEMAROOM add ROOMTYPE varchar2(10);

update cinemaprofile set notifymsg1=replace(notifymsg1,'partner','') where notifymsg1 like '%partner%'; 
update cinemaprofile set notifymsg2=replace(notifymsg2,'partner','') where notifymsg2 like '%partner%'; 
update cinemaprofile set notifymsg3=replace(notifymsg3,'partner','') where notifymsg3 like '%partner%'; 

update open_playitem set notifymsg1=replace(notifymsg1,'partner','') where notifymsg1 like '%partner%'; 
update open_playitem set notifymsg2=replace(notifymsg2,'partner','') where notifymsg2 like '%partner%'; 
update open_playitem set notifymsg3=replace(notifymsg3,'partner','') where notifymsg3 like '%partner%'; 

alter table MEMBER_ACCOUNT add BANKCHARGE number(11) default 0 not null;
alter table MEMBER_ACCOUNT add OTHERCHARGE number(11) default 0 not null;
alter table MEMBER_ACCOUNT add tmpCHARGE number(11);
-----------------------------------------------------------------------------
update member_account t set t.tmpcharge=(
  select sum(total_fee) from payment p where p.memberid =t.memberid and p.status='paid_success' 
  and p.paymethod in('lakalaPay', 'directPay', 'pnrPay')) where t.banlance>0;

update member_account t set t.bankcharge=t.banlance,t.othercharge=0 where t.banlance>0 and t.tmpcharge>=t.banlance;
update member_account t set t.bankcharge=t.tmpcharge,t.othercharge=t.banlance-t.tmpcharge where t.banlance>0 and t.tmpcharge<t.banlance;

alter table ELECCARD_BATCH add BINDPAY varchar2(50);
alter table SPECIALDISCOUNT add ENABLEREMARK varchar2(500);
alter table SPECIALDISCOUNT add RECOMMENDREMARK varchar2(500);

alter table PICTURE_NEW add posttime2 timestamp;
update PICTURE_NEW set posttime2=posttime;
commit;


alter table BAR add FIRSTPIC varchar2(200);
alter table CINEMA add FIRSTPIC varchar2(200);
alter table KTV add FIRSTPIC varchar2(200);
alter table GYM add FIRSTPIC varchar2(200);
alter table SPORT add FIRSTPIC varchar2(200);
alter table THEATRE add FIRSTPIC varchar2(200);

update cinema t set t.firstpic=(select min(p.picturename) from picture_new p where p.tag='cinema' and p.foreignid=t.recordid) where t.firstpic is null;
update bar t set t.firstpic=(select min(p.picturename) from picture_new p where p.tag='bar' and p.foreignid=t.recordid)  where t.firstpic is null;
update ktv t set t.firstpic=(select min(p.picturename) from picture_new p where p.tag='ktv' and p.foreignid=t.recordid) where t.firstpic is null;
update gym t set t.firstpic=(select min(p.picturename) from picture_new p where p.tag='gym' and p.foreignid=t.recordid) where t.firstpic is null;
update sport t set t.firstpic=(select min(p.picturename) from picture_new p where p.tag='sport' and p.foreignid=t.recordid)  where t.firstpic is null;
update theatre t set t.firstpic=(select min(p.picturename) from picture_new p where p.tag='theatre' and p.foreignid=t.recordid)  where t.firstpic is null;

alter table ELECCARD_BATCH add BINDGOODS number(19);
alter table ELECCARD_BATCH add BINDRATIO number(1);
alter table ELECCARD_BATCH add costtype varchar2(10);
alter table ELECCARD_BATCH add costnum number(3);
alter table ELECCARD_BATCH add ADDTIME1 CHAR(4) default '0000' not null;
alter table ELECCARD_BATCH add ADDTIME2 CHAR(4) default '2359' not null;
alter table ELECCARD_BATCH add ADDWEEK VARCHAR2(20);

alter table ORDER_OPENSEAT drop column OLDSEATID;
alter table ORDER_OPENSEAT add RECORDID number(19);
UPDATE ORDER_OPENSEAT SET RECORDID=ROWNUM;

----------------------------------------------------------------
alter table PICTURE_NEW rename column POSTTIME to POSTTIME3;
alter table PICTURE_NEW modify POSTTIME3 null;
alter table PICTURE_NEW rename column POSTTIME2 to POSTTIME;
drop POSTTIME3 status addtype.....

alter table SPECIALDISCOUNT add bindgoods number(19);
alter table SPECIALDISCOUNT add bindnum number(1);

alter table SPECIALDISCOUNT modify BUYNUM not null;
alter table SPECIALDISCOUNT add MINBUY NUMBER(3) default 1 not null;
alter table SPECIALDISCOUNT add validateurl varchar2(200);

create materialized view LATEST_ACCOUNT_RECORD
refresh complete on demand as
select s.* from account_record s where s.recordid in(
select rid from (select max(v.recordid) as rid,v.accountid from account_record v group by v.accountid))

-- Add/modify columns 
alter table OPEN_PLAYITEM add ASellNUM number(4) default 9999 not null;
comment on column OPEN_PLAYITEM.ASellNUM is 'allow sell num';

alter table CINEMAROOM add ALLOWSELLNUM number(4) default 9999 not null;

create table point2011list as select * from point where addtime < to_date('20110101','yyyymmdd');
delete from  point where addtime<to_date('20110101','yyyymmdd');

insert into point 
select min(recordid) as recordid, to_memberid, 0 as from_memberid, '2011' as tag, 2011 as tagid, sum(pointvalue) as pointvalue, 'before 2011 total' as reason, 3 as adminid, sysdate as addtime 
from point2011list group by to_memberid;

create index idx_memberid on C_COMMENT (memberid);
create index idx_comment_tag on C_COMMENT (tag);
create index idx_comment_relatedid on C_COMMENT (relatedid);
create index idx_mpi_citycode on MOVIELIST (citycode);

alter table TICKET_ORDER add createtime timestamp;
UPDATE TICKET_ORDER SET CREATETIME=ADDTIME WHERE CREATETIME IS NULL;
create index idx_ticket_create on TICKET_ORDER (createtime);
alter table CINEMAPROFILE modify CHANNEL varchar2(5);

update member set email=lower(email) where email!=lower(email) and email not like 'x1y2z3%';

alter table SPECIALDISCOUNT add uniqueby varchar2(10);
update SPECIALDISCOUNT SET uniqueby='mobile' where uniqueby is null;
alter table ELECCARD_BATCH add CITYPATTERN varchar2(10) default 'include' not null;

alter table APP_USER add mobile varchar2(11);
alter table SPECIALDISCOUNT MODIFY SORTNUM not null;

insert into apiuser (RECORDID, PARTNERKEY, CONTENT, PRIVATEKEY, PARTNERNAME, PARTNERIP, UPDATETIME, CLERK, STATUS, REMARK, LOGINNAME, LOGINPASS, BRIEFNAME, CINEMAIDLIST, LOGO, ROLES, PARTNERPATH, OPENTYPE, CITYCODE, PAYURL, QRYURL, PUSHURL, SECRETKEY, PUSHFLAG, ADDORDERURL, NOTIFYURL, OTHERINFO, DEFAULTCITY)
values (50000621, 'youbao20110802', 'youbao2', 'dkek#fh@3Ktx1', 'youbao2', '', sysdate, 5, 'open', 'youbao2', 'youbao2', 'dsfsd', 'shidai', '', '', 'apiuser,ticketApi,addOrderApi', 'youbao2', 'all', '310000', '', '', '', 'dkdkdkd', '', '', '', '', '310000');

insert into apiuser (RECORDID, PARTNERKEY, CONTENT, PRIVATEKEY, PARTNERNAME, PARTNERIP, UPDATETIME, CLERK, STATUS, REMARK, LOGINNAME, LOGINPASS, BRIEFNAME, CINEMAIDLIST, LOGO, ROLES, PARTNERPATH, OPENTYPE, CITYCODE, PAYURL, QRYURL, PUSHURL, SECRETKEY, PUSHFLAG, ADDORDERURL, NOTIFYURL, OTHERINFO, DEFAULTCITY)
values (50000622, 'youbao20110803', 'youbao3', 'kdjWsMw51t5@e', 'youbao3', '', sysdate, 5, 'open', 'youbao3', 'youbao3', 'dsfsd', 'shidai', '', '', 'apiuser,ticketApi,addOrderApi', 'youbao3', 'all', '310000', '', '', '', 'dkdkdkd', '', '', '', '', '310000');
--0812
update c_comment set tag='diary' where tag='diary_wala';
update c_comment set tag='qa' where tag='qa_wala';

attachmovie_wala|movienews|sportcoach|barsinger|video|picture|barnews|sportservice|sportnews|ktvnews|dramastar|gymnews|qa_wala|news


alter table SPECIALDISCOUNT add ALLOWADDNUM number(6) default 400 not null;
insert into gewaconfig(recordid,content,description) values(62,'13651668441','yunyin mobile');

col CONSTRAINT_NAME format a30;
col COLUMN_NAME format a30;
col CONSTRAINT_TYPE format a30;
col index_name format a30;
select con.CONSTRAINT_NAME,COL.COLUMN_NAME,con.CONSTRAINT_TYPE,con.index_name
from user_constraints con,user_cons_columns col where
con.CONSTRAINT_NAME=col.CONSTRAINT_NAME and con.table_name=col.table_name and con.table_name=upper('theatre_seat_price');

select con.CONSTRAINT_NAME,con.CONSTRAINT_TYPE,con.index_name
from user_constraints con where con.table_name=upper('theatre_seat_price');
