--------------------liqilun-------------------------
insert into webdata.gewaconfig(recordid,description,updatetime,newcontent) values(84,'stop membercard',sysdate,'2013-01-01 00:00:00');
-- Create table
create table webdata.MEMBER_CARD_INFO
(
  RECORDID       NUMBER(19) not null,
  TYPEID         NUMBER(19) not null,
  MEMBERID       NUMBER(19) not null,
  MEMBERCARDCODE VARCHAR2(50) not null,
  NAME           VARCHAR2(50),
  SEX            VARCHAR2(20),
  MOBILE         VARCHAR2(11) not null,
  VALIDTIME      TIMESTAMP(6),
  OVERMONEY      NUMBER(5) not null,
  CARDSTATUS     VARCHAR2(20) not null,
  FITITEM        VARCHAR2(500),
  ADDTIME        TIMESTAMP(6) not null,
  CARDTYPEUKEY   VARCHAR2(50) not null,
  BELONGVENUE    VARCHAR2(500) not null,
  TRADENO        VARCHAR2(30),
  TYPETITLE      VARCHAR2(50),
  BINDSTATUS     CHAR(1) default 'N' not null
);

alter table webdata.MEMBER_CARD_INFO add constraint PK_MEMBER_CARD_INFO primary key (RECORDID)  using index tablespace tbs_index;
alter table webdata.MEMBER_CARD_INFO add constraint UK_MEMBER_CARD_INFO_MCI unique (MEMBERCARDCODE) using index tablespace tbs_index;
create index IDX_MEMBER_CARD_INFO_MID on webdata.MEMBER_CARD_INFO (MEMBERID) tablespace tbs_index;
grant select, insert, update on webdata.MEMBER_CARD_INFO to SHANGHAI;


create table webdata.MEMBER_CARD_TYPE
(
  CARDTYPEUKEY VARCHAR2(60) not null,
  CARDTYPECODE VARCHAR2(60) not null,
  CARDTYPE     CHAR(1) not null,
  MONEY        NUMBER(5) not null,
  OVERNUM      NUMBER(5) not null,
  RESERVE      NUMBER(5) not null,
  DESCRIPTION  VARCHAR2(2000),
  VALIDTIME    NUMBER(5),
  PRICE        NUMBER(5) not null,
  DISCOUNT     NUMBER(5) not null,
  FITITEM      VARCHAR2(500),
  BELONGVENUE  VARCHAR2(500) not null,
  BUSINESSID   NUMBER(19) not null,
  SALES        NUMBER(5) not null,
  COSTRATE     NUMBER(3),
  MINGAIN      NUMBER(5) not null,
  STATUS       VARCHAR2(20) not null,
  OPENTIME     TIMESTAMP(6) not null,
  CLOSETIME    TIMESTAMP(6) not null,
  SPFLAG       VARCHAR2(30),
  MINPOINT     NUMBER(5) not null,
  MAXPOINT     NUMBER(5) not null,
  ELECARD      VARCHAR2(20),
  REMARK       VARCHAR2(500),
  OTHERINFO    VARCHAR2(500) not null,
  RECORDID     NUMBER(19) not null,
  ADDTIME      TIMESTAMP(6) not null,
  GEWAPRICE    NUMBER(5),
  NOTIFYMSG    VARCHAR2(100)
);

alter table webdata.MEMBER_CARD_TYPE add constraint PK_MEMBER_CARD_TYPE primary key (RECORDID) using index tablespace tbs_index;
alter table webdata.MEMBER_CARD_TYPE add constraint UK_MEMBER_CARD_TYPE unique (CARDTYPEUKEY) using index tablespace tbs_index;
grant select, insert, update on webdata.MEMBER_CARD_TYPE to SHANGHAI;

-- Create table
create table webdata.MEMBER_CARD_TYPE_PLACE
(
  RECORDID NUMBER(19) not null,
  PLACEID  NUMBER(19) not null,
  MCTID    NUMBER(19) not null
);

alter table webdata.MEMBER_CARD_TYPE_PLACE add constraint PK_MEMBER_CARD_TYPE_PLACE primary key (RECORDID) using index tablespace tbs_index;
alter table webdata.MEMBER_CARD_TYPE_PLACE add constraint UK_MEMBER_CARD_TYPE_PLACE unique (PLACEID, MCTID) using index tablespace tbs_index;
grant select, insert, update, delete on webdata.MEMBER_CARD_TYPE_PLACE to SHANGHAI;

--------------------weikai---------------------------
alter table webdata.open_timeitem add saleInd varchar2(19);

--------------------zyy------------------------------
alter table webdata.member_account add depositcharge number(11) default 0 not null;
alter table webdata.ADJUSTMENT add depositcharge number(5) default 0 not null;

create table webdata.guarantee(
  recordid number(19) not null,
  name varchar2(50),
  price number(5) not null,
  costprice number(5) default 0 not null,
  status varchar2(10) not null,
  otherinfo varchar2(1000),
  citycode varchar2(8) not null,
  remark varchar2(100),
  description varchar2(2000),
  createuser number(19),
  addtime timestamp(6) not null,
  updatetime timestamp(6) not null,
  ordermsg varchar2(200),
  constraint pk_guarantee primary key(recordid) using index tablespace tbs_index
);
grant select, insert, update on webdata.guarantee to shanghai;

create table webdata.open_timesale(
  recordid number(19) not null,
  sportid number(19) not null,
  itemid number(19) not null,
  ottid number(19) not null,
  bindInd varchar2(19) not null,
  lowerprice number(5) not null,
  curprice number(5) not null,
  dupprice number(5) not null,
  auctionprice number(5) not null,
  otiids varchar2(500) not null,
  starttime varchar2(5) not null,
  endtime varchar2(5) not null,
  version number(10) not null,
  nickname varchar2(50),
  memberid number(19),
  playdate date not null,
  opentime timestamp(6) not null,
  closetime timestamp(6) not null,
  validtime timestamp(6) not null,
  paidvalidtime timestamp(6) not null,
  joinnum number(10) default 0 not null,
  jointime timestamp(6) not null,
  status varchar2(10) not null,
  citycode varchar2(8) not null,
  lockStatus varchar2(20) not null,
  addtime timestamp(6) not null,
  guaranteeid number(19),
  otherinfo varchar2(1000),
  orderid number(19),
  constraint pk_open_timesale primary key(recordid) using index tablespace tbs_index
);
grant select,insert,update on webdata.open_timesale to shanghai;
create index webdata.idx_open_timesale_ottid on webdata.open_timesale(ottid) tablespace tbs_index;
create index webdata.idx_open_timesale1 on webdata.open_timesale(citycode,opentime,closetime,validtime) tablespace tbs_index;
create index webdata.idx_open_timesale2 on webdata.open_timesale(memberid,paidvalidtime) tablespace tbs_index;

create table webdata.open_timesale_member(
	recordid number(19) not null,
	otsid number(19) not null,
	memberid number(19) not null,
	nickname varchar2(50) not null,
	price number(10) not null,
	dupprice number(10) not null,
	addtime timestamp(6) not null,
	constraint pk_open_timesale_member primary key(recordid) using index tablespace tbs_index
);
grant select, insert on webdata.open_timesale_member to shanghai;
create index webdata.idx_open_timesale_member on webdata.open_timesale_member(otsid) tablespace tbs_index;
create index webdata.idx_open_timesale_member2 on webdata.open_timesale_member(memberid) tablespace tbs_index;

create table webdata.sell_deposit(
  recordid number(19) not null,
  version number(10) default 0 not null,
  otsid number(19) not null,
  memberid number(19) not null,
  price number(5) not null,
  addtime timestamp(6) not null,
  validtime timestamp(6) not null,
  status varchar2(20) not null,
  chargeid number(19) not null,
  mobile varchar2(12) not null,
  constraint pk_sell_deposit primary key(recordid) using index tablespace tbs_index,
  constraint uk_sell_deposit unique(chargeid) using index tablespace tbs_index
);
grant select, insert, update on webdata.sell_deposit to shanghai;
create index webdata.idx_pk_sell_deposit on webdata.sell_deposit(otsid, memberid, status) tablespace tbs_index;

alter table webdata.open_timeitem add itemtype varchar2(4);
alter table webdata.open_timeitem add auctionprice number(5);
alter table webdata.open_timeitem add bindInd varchar2(19);
alter table webdata.open_timeitem add otsid number(19);

alter table webdata.open_timesale add mobile varchar2(15);
alter table webdata.open_timesale add message varchar2(500);

alter table webdata.open_timesale add fieldid number(19) not null;
