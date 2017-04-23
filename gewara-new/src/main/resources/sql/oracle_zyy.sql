---------------------------------------------2013-11-22-------------------------------------------------------------------------------------------------------------------------
alter table webdata.drama add prices varchar2(200);
alter table webdata.open_drama_item add greetings varchar2(5) default 'N' not null;

update webdata.drama d set d.prices = (select wm_concat(distinct price) from webdata.theatre_seat_price t where t.price > 0 and t.status<>'D' 
and exists(select * from webdata.open_drama_item o 
where (o.playtime>=sysdate and o.period='Y' or o.endtime>=sysdate and o.period='N' and o.dpid=t.dpid) 
and t.status='Y') and t.dramaid=d.recordid group by t.dramaid)
where exists(select * from webdata.open_drama_item t 
where (t.playtime>=sysdate and t.period='Y' or t.endtime>=sysdate and t.period='N') and t.status='Y' and t.dramaid=d.recordid);

alter table webdata.theatre_seat_price add dramaid number(19);
create index webdata.theatre_seat_price_dramaid on webdata.theatre_seat_price(dramaid) tablespace tbs_index;

update webdata.theatre_seat_price t set t.dramaid = (select a.dramaid from webdata.theatre_seat_area a where a.recordid = t.areaid)
 where t.dramaid is null and  exists(select * from webdata.theatre_seat_area a where a.recordid = t.areaid);
 
alter table webdata.theatre_seat_price modify dramaid not null;

---------------------------------------------2013-11-18-------------------------------------------------------------------------------------------------------------------------
alter table webdata.pay_method add mangerurl varchar2(300);

---------------------------------------------2013-11-05-------------------------------------------------------------------------------------------------------------------------
alter table webdata.open_playitem_ext add settle varchar2(5);
alter table webdata.open_playitem_ext add imprest varchar2(5);
update webdata.open_playitem_ext set settle='Y' where settle is null;
update webdata.open_playitem_ext set imprest='N' where imprest is null;

alter table webdata.open_playitem_ext modify settle not null;
alter table webdata.open_playitem_ext modify imprest not null;

create index webdata.idx_order_extra_memberid on webdata.order_extra(memberid) tablespace tbs_index;
create index webdata.idx_order_extra_his_memberid on webdata.order_extra_his(memberid) tablespace tbs_index;
create index webdata.idx_invioc_relate_memberid on webdata.invoice_relate(memberid) tablespace tbs_index;

---------------------------------------------2013-10-31-------------------------------------------------------------------------------------------------------------------------
insert into apiuser (RECORDID, PARTNERKEY, CONTENT, PRIVATEKEY, PARTNERNAME, UPDATETIME, CLERK, STATUS,  BRIEFNAME, ROLES, PARTNERPATH, CITYCODE, DEFAULTCITY)
values (50000082, 'dramaMobileAdmin', 'dramaMobileAdmin', '3244711a6e2d9804e9e453b65842eb5d', 'dramaMobileAdmin', sysdate, 5, 'open', 'dramaMobileAdmin', 'apiuser', 'dramaMobileAdmin', '000000', '310000');

insert into apiuser (RECORDID, PARTNERKEY, CONTENT, PRIVATEKEY, PARTNERNAME, UPDATETIME, CLERK, STATUS,  BRIEFNAME, ROLES, PARTNERPATH, CITYCODE, DEFAULTCITY)
values (50000083, 'dramaAdminOffline', 'dramaAdminOffline', '89a68f7552b1525c2e9a752b20bc4ce1', 'dramaAdminOffline', sysdate, 5, 'open', 'dramaAdminOffline', 'apiuser', 'dramaAdminOffline', '000000', '310000');

insert into pay_method(paymethod,paymethod_text) values('offlinePay','后台线下支付');

alter table webdata.member_usefuladdress modify memberid null;
alter table webdata.member_usefuladdress add constraint  member_usefuladdress_ck check (not (memberid is null and mobile is null));
create index webdata.idx_member_usefuaddress_mobile on webdata.member_usefuladdress(mobile) tablespace tbs_index;
---------------------------------------------2013-10-23-------------------------------------------------------------------------------------------------------------------------
alter table webdata.theatreprofile add etickethour number(5);
alter table webdata.theatreprofile add eticketweekhour number(5);
alter table webdata.theatreprofile add addtime timestamp(6);
alter table webdata.theatreprofile add updatetime timestamp(6);

alter table webdata.open_drama_item add etickethour number(5);
alter table webdata.open_drama_item add eticketweekhour number(5);

alter table webdata.specialdiscount add specialrule varchar2(2000);

update webdata.theatreprofile set etickethour = 48 where etickethour is null;
update webdata.theatreprofile set eticketweekhour = 96 where eticketweekhour is null;
update webdata.theatreprofile set addtime = sysdate where addtime is null;
update webdata.theatreprofile set updatetime = addtime where updatetime is null;


update webdata.open_drama_item o set (o.etickethour, o.eticketweekhour)= (select t.etickethour,t.eticketweekhour from webdata.theatreprofile t where t.theatreid=o.theatreid)
where o.etickethour is null or o.eticketweekhour is null;

alter table webdata.theatreprofile modify etickethour not null;
alter table webdata.theatreprofile modify eticketweekhour not null;
alter table webdata.theatreprofile modify addtime not null;
alter table webdata.theatreprofile modify updatetime not null;

alter table webdata.open_drama_item modify etickethour not null;
alter table webdata.open_drama_item modify eticketweekhour not null;


---------------------------------------------2013-10-22-------------------------------------------------------------------------------------------------------------------------
insert into webdata.gewaconfig(recordid,newcontent,description,updatetime) values (401,'40856321','dramaQuestion memberid',sysdate);

---------------------------------------------2013-09-12-------------------------------------------------------------------------------------------------------------------------
alter table webdata.drama add pretype varchar2(2);
alter table webdata.drama add saleCycle varchar2(2);
alter table webdata.open_drama_item add saleCycle varchar2(2);
alter table webdata.order_extra add pretype varchar2(10);
alter table webdata.order_extra_his add pretype varchar2(10);
alter table webdata.user_invoice add pretype varchar2(10);

update webdata.drama set pretype='M' where pretype is null;
update webdata.drama set saleCycle='1' where saleCycle is null;
update webdata.open_drama_item set saleCycle='1' where saleCycle is null;
update webdata.order_extra set pretype='M' where ordertype!='sport' and pretype is null;
update webdata.order_extra set pretype='M' where invoice!='F' and pretype is null;
update webdata.order_extra set pretype='M' where invoice='F' and addtime<=to_date('2013-01-23','yyyy-mm-dd') and pretype is null;
update webdata.order_extra o 
set o.pretype=(select s.pretype from webdata.sport_profile s, webdata.ticket_order t where t.recordid=o.recordid and t.cinemaid=s.sportid) 
where exists(select * from webdata.ticket_order t where t.order_type='sport' 
      and status='paid_success' and t.recordid=o.recordid and addtime >=to_date('2013-01-23','yyyy-mm-dd'))
      and o.ordertype='sport' and o.invoice='F' and o.pretype is null;
update webdata.order_extra o set o.pretype='M' where o.pretype is null;
update webdata.order_extra_his set pretype='M' where pretype is null;
update webdata.user_invoice set pretype='M' where pretype is null;

--------------------------------------------------
update webdata.order_extra set pretype='M' where ordertype!='sport' and ordertype!='drama' and pretype is null;
update webdata.order_extra o 
set o.pretype=(select s.pretype from webdata.sport_profile s, webdata.ticket_order t where t.recordid=o.recordid and t.cinemaid=s.sportid) 
where exists(select * from webdata.ticket_order t where t.order_type='sport' 
      and status='paid_success' and t.recordid=o.recordid and addtime >=to_date('2013-01-23','yyyy-mm-dd'))
      and o.ordertype='sport' and o.invoice='F' and o.pretype is null;
update webdata.order_extra o 
set o.pretype=(select d.pretype from webdata.drama d, webdata.ticket_order t where t.recordid=o.recordid and t.movieid=d.recordid) 
where exists(select * from webdata.ticket_order t where t.order_type='drama' 
      and status='paid_success' and t.recordid=o.recordid and addtime >=to_date('2013-09-15','yyyy-mm-dd'))
      and o.ordertype='drama' and o.pretype is null;

------------update after -----------------------
alter table webdata.drama modify pretype not null;
alter table webdata.drama modify saleCycle not null;
alter table webdata.open_drama_item modify saleCycle not null;
alter table webdata.order_extra modify pretype not null;
alter table webdata.order_extra_his modify pretype not null;
alter table webdata.user_invoice modify pretype not null;
---------------------------------------------2013-08-26-------------------------------------------------------------------------------------------------------------------------
revoke delete on webdata.disquantity from shanghai;

alter table webdata.disquantity add name varchar2(100);
alter table webdata.disquantity add starttime timestamp(6);
alter table webdata.disquantity add endtime timestamp(6);
alter table webdata.disquantity add retail varchar2(10);
alter table webdata.disquantity add status varchar2(10);
alter table webdata.disquantity add seller varchar2(10);
alter table webdata.disquantity add sispseq varchar2(50);

alter table webdata.theatre_seat_price add retail varchar2(10);

alter table webdata.theatre_seat_price add showprice varchar2(10);

update webdata.disquantity set starttime=addtime where seller is null and starttime is null;
alter table webdata.disquantity modify starttime not null;

update webdata.disquantity d set d.endtime=(select i.endtime from webdata.drama_play_item i where i.recordid=d.dpid)
where d.seller is null and d.endtime is null and exists(select * from webdata.drama_play_item i where i.recordid=d.dpid);
alter table webdata.disquantity modify endtime not null;

update webdata.disquantity set status='Y' where status is null;
alter table webdata.disquantity modify status not null;

update webdata.disquantity set retail='N' where retail is null and seller is null;
alter table webdata.disquantity modify retail not null;

update webdata.disquantity set seller='GEWA' where seller is null;
alter table webdata.disquantity modify seller not null;

update webdata.theatre_seat_price set retail = 'Y' where retail is null;
alter table webdata.theatre_seat_price modify retail not null;

update webdata.theatre_seat_price set showprice = 'Y' where showprice is null;
alter table webdata.theatre_seat_price modify showprice not null;
---------------------------------------------2013-08-15-------------------------------------------------------------------------------------------------------------------------
update webdata.SPORT_PROFILE set pretype='M' where pretype is null;
alter table webdata.sport_profile modify pretype not null;
select * from webdata.theatre_seat_price where quantity is null;

update webdata.theatre_seat_price set quantity=0 where quantity is null;
alter table webdata.theatre_seat_price modify quantity not null;
select * from webdata.theatre_seat_price t where t.allowaddnum is null;
update webdata.theatre_seat_price set allowaddnum=0 where allowaddnum is null;
alter table webdata.theatre_seat_price modify allowaddnum not null;
select * from webdata.theatre_seat_price t where t.sellordernum is null;
alter table webdata.theatre_seat_price modify sellordernum not null;
---------------------------------------------2013-08-13-------------------------------------------------------------------------------------------------------------------------
select * from webdata.invoice_relate;

alter table webdata.invoice_relate add trade_no varchar2(30);

update webdata.invoice_relate r set r.trade_no = (select p.trade_no from webdata.payment p where p.recordid=r.orderid and p.memberid=p.memberid)
where r.trade_no is null and exists(select p.trade_no from webdata.payment p where p.recordid=r.orderid and p.memberid=p.memberid and p.status='paid_success');

update webdata.invoice_relate r set r.trade_no = (select p.trade_no from webdata.ticket_order p where p.recordid=r.orderid and p.memberid=p.memberid)
where r.trade_no is null and exists(select p.trade_no from webdata.ticket_order p where p.recordid=r.orderid and p.memberid=p.memberid and p.status='paid_success');

select count(1) from webdata.invoice_relate r where r.trade_no is null;

----delete from webdata.invoice_relate r where r.trade_no is null;
alter table webdata.invoice_relate modify trade_no not null;
alter table webdata.invoice_relate add constraint pk_invoice_relate primary key(trade_no) using index tablespace tbs_index;
---------------------------------------------2013-08-01-------------------------------------------------------------------------------------------------------------------------
create table webdata.ticketface_config(
	recordid varchar2(19) not null,
	facecontent clob,
	addtime timestamp(6) not null,
	updatetime timestamp(6) not null,
	remark varchar2(1000),
	constraint pk_ticketface_config primary key (recordid)
	using index (create unique index webdata.idxpk_ticketface_config on webdata.ticketface_config(recordid) tablespace tbs_index)
);
grant select, insert, update on webdata.ticketface_config to shanghai;

alter table webdata.drama add otherinfo varchar2(1000);

delete webdata.settle_config where recordid=130715436;
update webdata.base_settle set settleid=128402155 where settleid=130715436 and settletype='sport';
update webdata.settle_config set distype='percent' where distype is null;
alter table webdata.settle_config add distype varchar2(20);
alter table webdata.settle_config drop constraint uk_settle_config cascade;
alter table webdata.settle_config add constraint uk_settle_config unique(discount,distype) using index tablespace tbs_index;

grant select,insert,update,delete on webdata.tsp_sale_count to shanghai;

alter table webdata.open_drama_item modify price null;
alter table webdata.open_drama_item modify costprice null;
alter table webdata.open_drama_item modify gewaprice null;

alter table webdata.open_drama_item add ticketfaceid varchar2(19);

update webdata.open_drama_item set gsellnum = 0 where gsellnum is null;
alter table webdata.open_drama_item modify gsellnum default 0 not null;
alter table webdata.open_drama_item add seatnum number(5) default 0 not null;
alter table webdata.open_drama_item add asellnum number(5) default 9999 not null;
alter table webdata.open_drama_item add csellnum number(5) default 0 not null;
alter table webdata.open_drama_item add locknum number(5) default 0 not null;

alter table webdata.theatre_seat_area add gsellnum number(5) default 0 not null;
alter table webdata.theatre_seat_area add csellnum number(5) default 0 not null;
alter table webdata.theatre_seat_area add locknum number(5) default 0 not null;
alter table webdata.theatre_seat_area add otherinfo varchar2(1000);
alter table webdata.disquantity add areaid number(19);
create index webdata.idx_disquantity_areaid on webdata.disquantity(areaid) tablespace tbs_index;

update webdata.theatre_seat_area t set (t.total,t.limitnum) = (select count(1),count(1) from webdata.open_theatreseat o where o.areaid = t.recordid)
where exists(select * from webdata.open_drama_item d where d.dpid=t.dpid and d.open_type='seat'); 

update webdata.theatre_seat_area t set (t.total,t.limitnum) = (select sum(o.quantity),sum(o.quantity) from webdata.theatre_seat_price o where o.areaid = t.recordid)
where exists(select * from webdata.open_drama_item d where d.dpid=t.dpid and d.open_type='price') and exists(select * from webdata.theatre_seat_price o where o.areaid = t.recordid);

update webdata.open_drama_item o set o.seatnum = (select sum(t.total) from webdata.theatre_seat_area t where t.dpid = o.dpid)
where exists(select * from webdata.theatre_seat_area t where t.dpid = o.dpid);

update webdata.disquantity d set d.areaid = (select t.areaid from webdata.theatre_seat_price t where t.recordid=d.tspid)
where exists(select * from webdata.theatre_seat_price t where t.recordid=d.tspid) and areaid is null;
alter table webdata.disquantity modify areaid not null;

update webdata.settle_config set distype='percent' where distype is null;
alter table webdata.settle_config modify distype not null;

---------------------------------------------2013-07-25-------------------------------------------------------------------------------------------------------------------------
alter table webdata.theatre_seat_price add addtime timestamp(6);
alter table webdata.theatre_seat_price add settleid number(19);
alter table webdata.theatre_seat_price add csellnum number(9) default 0 not null;

alter table webdata.disquantity add settleid number(19);

alter table webdata.buyitem add settleid number(19);

update webdata.theatre_seat_price set addtime=updatetime where addtime is null;
alter table webdata.theatre_seat_price modify addtime not null;


insert into webdata.base_settle(recordid, settletype,relatedid,discount,addtime,updatetime) 
select hibernate_sequence.nextval,'drama', s.dramaid ,100,sysdate,sysdate from (select distinct d.dramaid from webdata.drama_play_item d) s 
where not exists(select * from webdata.base_settle t where t.dramaid=s.dramaid and t.settletype='drama');
select recordid from webdata.settle_config where discount=100;
update webdata.theatre_seat_price p set p.settleid=33958910 where p.settleid is null;
--- delete from webdata.theatre_seat_price p where not exists(select * from webdata.theatre_seat_area t where t.recordid=p.areaid)
alter table webdata.theatre_seat_price modify settleid not null;

update webdata.disquantity p set p.settleid=33958910 where p.settleid is null;

select count(1) from webdata.disquantity d where d.settleid is null;
--- select count(1) from webdata.disquantity d where not exists(select t.recordid from webdata.theatre_seat_price t where t.recordid=d.tspid);
--- delete from webdata.disquantity d where not exists(select * from webdata.theatre_seat_price t where t.recordid=d.tspid);
alter table webdata.disquantity modify settleid not null;


create table webdata.blog_data(
	ukey varchar2(30) not null,
	tag varchar2(20) not null,
	relatedid number(19) not null,
	diarycount number(10) default 0 not null,
	commentcount number(10) default 0 not null,
	newscount number(10) default 0 not null,
	activitycount number(10) default 0 not null,
	picturecount number(10) default 0 not null,
	videocount number(10) default 0 not null,
	addtime timestamp(6) not null,
	updatetime timestamp(6) not null,
	constraint pk_blog_data primary key(ukey) using index tablespace tbs_index,
	constraint uk_blog_data unique(tag,relatedid) using index tablespace tbs_index
);
grant select,insert,update on webdata.blog_data to shanghai;

insert into webdata.blog_data(ukey,tag,relatedid,addtime,updatetime) 
	select d.recordid||'drama','drama',d.recordid,sysdate,sysdate from webdata.drama d 
	where not exists(select * from webdata.blog_data b where b.relatedid=d.recordid and b.tag='drama');

create table webdata.blog_data_everyday(
  recordid number(19) not null,
  tag varchar2(20) not null,
  relatedid number(19) not null,
  blogtype varchar(20) not null,
  blogdate date not null,
  blogcount number(10) default 0 not null,
  addtime timestamp(6) not null,
  updatetime timestamp(6) not null,
  constraint pk_blog_data_everyday primary key(recordid) using index tablespace tbs_index,
  constraint uk_blog_data_everyday unique(tag,relatedid,blogtype,blogdate) using index tablespace tbs_index
);
grant select,insert,update on webdata.blog_data_everyday to shanghai;

---------------------------------------------2013-07-24-------------------------------------------------------------------------------------------------------------------------
create table webdata.settle_config(
	recordid number(19) not null,
	discount number(5,2) not null,
	addtime timestamp(6) not null,
	constraint pk_settle_config primary key(recordid) using index tablespace tbs_index,
	constraint uk_settle_config unique(discount) using index tablespace tbs_index
);
grant select, insert on webdata.settle_config to shanghai;

insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,100,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,99,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,98,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,97,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,96,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,95,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,94,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,93,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,92,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,91,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,90,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,85,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,80,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,70,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,75,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,65,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,60,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,50,sysdate);
insert into webdata.settle_config(recordid,discount,addtime) values (hibernate_sequence.nextval,55,sysdate);

create table webdata.base_settle(
	recordid number(19) not null,
	settleid number(19) not null,
	remark varchar2(100),
	addtime timestamp(6) not null,
	settletype varchar2(20) not null,
	relatedid number(19) not null,
	categoryid number(19),
	constraint pk_base_settle primary key(recordid) using index tablespace tbs_index
);
grant select, insert on webdata.base_settle to shanghai;
create index webdata.idx_base_settle on webdata.base_settle(relatedid) tablespace tbs_index;

---------------------------------------------2013-07-15-------------------------------------------------------------------------------------------------------------------------
create table webdata.drama_settle(
  recordid number(19) not null,
  settletype varchar2(5) not null,
  dramaid number(19) not null,
  discount number(6,2) not null,
  remark varchar2(1000),
  addtime timestamp(6) not null,
  updatetime timestamp(6) not null,
  constraint pk_drama_settle primary key(recordid) using index tablespace tbs_index,
  constraint uk_drama_settle unique(settletype,dramaid) using index tablespace tbs_index
);
grant select, insert,update on webdata.drama_settle to shanghai;
create index webdata.idx_drama_settle on webdata.drama_settle(dramaid) tablespace tbs_index;

---------------------------------------------2013-07-12-------------------------------------------------------------------------------------------------------------------------
alter table webdata.drama_remote_order add description CLOB;
update webdata.drama_remote_order set description=otherinfo;
alter table webdata.drama_remote_order drop column otherinfo;
alter table webdata.drama_remote_order rename column description to otherinfo;

update webdata.ticket_order set category='GEWA' where order_type='drama' and category is null;
---------------------------------------------2013-06-27-------------------------------------------------------------------------------------------------------------------------
alter table webdata.drama_play_item add sortnum number(9) default 1;
update webdata.drama_play_item set sortnum =1 where sortnum is null;
alter table webdata.drama_play_item modify sortnum not null;

alter table webdata.open_drama_item add sortnum number(9) default 1;
update webdata.open_drama_item set sortnum =1 where sortnum is null;
alter table webdata.open_drama_item modify sortnum not null;

---------------------------------------------2013-06-23-------------------------------------------------------------------------------------------------------------------------
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
  mobile varchar2(15),
  message varchar2(500),
  fieldid number(19) not null,
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

---------------------------------------------2013-06-21-------------------------------------------------------------------------------------------------------------------------
grant select, update, insert, delete on webdata.OTHERFEE_DETAIL to shanghai;

alter table webdata.open_timeitem add itemtype varchar2(4);
alter table webdata.open_timeitem add auctionprice number(5);
alter table webdata.open_timeitem add bindInd varchar2(19);
alter table webdata.open_timeitem add otsid number(19);
---------------------------------------------2013-06-13-------------------------------------------------------------------------------------------------------------------------
alter table webdata.drama_star add starnum number(9) default 0;
alter table webdata.drama_star add worknum number(9) default 0;

update webdata.drama_star d set d.starnum=(select count(*) from webdata.drama_star s where s.troupe=d.recordid) where startype='troupe' and d.starnum is null;

alter table webdata.theatre_seat_price add maxbuy number(5);
alter table webdata.disquantity add maxbuy number(5);
update webdata.theatre_seat_price set maxbuy =6 where maxbuy is null;
update webdata.disquantity set maxbuy = 6 where maxbuy is null;

update webdata.theatre_field f set f.description = (select r.content from webdata.theatre_room r where r.recordid=f.recordid)
where f.description is null and exists(select * from webdata.theatre_room r where r.recordid=f.recordid and r.content is not null);

alter table webdata.theatre_seat_price modify maxbuy not null;
alter table webdata.disquantity modify maxbuy not null;

---------------------------------------------2013-05-15-------------------------------------------------------------------------------------------------------------------------
update webdata.selldramaseat s set dpid = (select o.dpid from webdata.open_drama_item o where o.recordid=s.odiid)
	where (select o.dpid from webdata.open_drama_item o where o.recordid=s.odiid) and s.areaid is null;
alter table webdata.selldramaseat modify dpid not null;
update webdata.selldramaseat s set areaid (select min(t.recordid) from webdata.theatre_seat_area t where t.dpid=s.dpid)
	where (select min(t.recordid) from webdata.theatre_seat_area t where t.dpid=s.dpid) and s.areaid is null;
alter table webdata.selldramaseat modify areaid not null;

create sequence webdata.gptbs_sequence increment by 1 start with 1 nomaxvalue nocycle cache 20;
grant select on webdata.gptbs_sequence to shanghai;

alter table webdata.show_item modify opentime null;
alter table webdata.show_item modify closetime null;

alter table webdata.theatreprofile add opentype varchar2(10);

create table webdata.drama_remote_order(
	orderid number(19) not null,
	serial varchar2(20) not null,
	hisSerial varchar2(1000),
	addtime timestamp(6) not null,
	locktime timestamp(6) not null,
	seqno varchar2(50) not null,
	areaseqno varchar2(200) not null,
	mobile varchar2(12) not null,
	bookingId varchar2(50),
	confirmationId varchar2(20),
	tickets varchar2(200),
	status varchar2(5) not null,
	seatno varchar2(200) not null,
	seatText varchar2(200),
	checkmark varchar2(100),
	opentype varchar2(10) not null,
	message varchar2(50),
	ordertype varchar2(10) not null,
	updatetime timestamp(6) not null,
	otherinfo varchar2(2000),
	manual varchar2(5) not null,
	constraint pk_drama_remote_order primary key(orderid) using index tablespace tbs_index
);
grant select, insert, update on webdata.drama_remote_order to shanghai;

create table webdata.qry_item_response(
	resid varchar2(30) not null,
	updatetime timestamp(6) not null,
	response clob,
	constraint pk_qry_item_response primary key(resid) using index tablespace tbs_index
);
grant select,insert,update on webdata.qry_item_response to shanghai;

alter table webdata.show_seat add priceseq varchar2(50) not null;

create table webdata.theatre_field(
	recordid number(19) not null,
	theatreid number(19) not null,
	name varchar2(100) not null,
	fieldnum varchar2(5),
	fieldtype varchar2(10) not null,
	logo varchar2(1000),
	description varchar2(500),
	status varchar2(10) not null,
	addtime timestamp(6) not null,
	updatetime timestamp(6) not null,
	constraint pk_theatre_field primary key(recordid) using index tablespace tbs_index,
);
grant select, insert, update on webdata.theatre_field to shanghai;
create index webdata.idx_theatre_field_num on webdata.theatre_field(theatreid, fieldnum) tablespace tbs_index;
insert into webdata.theatre_field(recordid,theatreid,name,fieldnum,fieldtype,status,addtime,updatetime) 
select recordid,theatreid,roomname,roomnum,'GEWA','Y',sysdate,sysdate from webdata.theatre_room;

create table webdata.tmpfield as select recordid,theatreid,rank() over(partition by theatreid order by recordid) rk from webdata.theatre_field order by theatreid;
update webdata.theatre_field f set f.fieldnum = (select t.rk from webdata.tmpfield t where t.recordid=f.recordid);
alter table webdata.theatre_field modify fieldnum not null;
alter table webdata.theatre_field add constraint uk_theatre_field_num unique(theatreid,fieldnum) using index tablespace tbs_index;

alter table webdata.ticket_order add areaid number(19);

alter table webdata.theatre_room add roomtype varchar2(10);
alter table webdata.theatre_room add fieldid number(19);
alter table webdata.theatre_room add hotzone varchar2(1000);
alter table webdata.cinemaroom add firstline number(5) default 1;
alter table webdata.cinemaroom add firstrank number(5) default 1;
alter table webdata.theatre_room add firstline number(5) default 1;
alter table webdata.theatre_room add firstrank number(5) default 1;
create table webdata.tmproom as select recordid,fieldid,rank() over(partition by fieldid order by recordid) rk from webdata.theatre_room order by fieldid;
update webdata.theatre_room f set f.roomnum = (select t.rk from webdata.tmproom t where t.recordid=f.recordid);
alter table webdata.theatre_room add constraint uk_theatre_room_num unique(theatreid,fieldid,roomnum) using index tablespace tbs_index;
update webdata.theatre_room set fieldid=recordid where fieldid is null;
update webdata.theatre_room set roomtype='GEWA' where roomtype is null;

alter table webdata.drama_play_item add name varchar2(50);
alter table webdata.drama_play_item add opentype varchar2(10);
alter table webdata.drama_play_item add endtime timestamp(6);
alter table webdata.drama_play_item add seller varchar2(10);
alter table webdata.drama_play_item add sellerseq varchar2(30);

update webdata.drama_play_item set period = 'Y' where period is null;
update webdata.drama_play_item d set opentype = (select o.open_type from webdata.open_drama_item o where o.dpid=d.recordid)
where exists(select * from webdata.open_drama_item o where o.dpid=d.recordid) and d.opentype is null;
update webdata.drama_play_item set opentype = 'price' where opentype is null;
update webdata.drama_play_item set seller='GEWA' where seller is null; 
update webdata.drama_play_item set endtime=playtime+1 where endtime is null and period='Y';

alter table webdata.open_drama_item add name varchar2(50);
alter table webdata.open_drama_item add endtime timestamp(6);
alter table webdata.open_drama_item add seller varchar2(10);
alter table webdata.open_drama_item add sellerseq varchar2(30);

update webdata.open_drama_item set period = 'Y' where period is null;
update webdata.open_drama_item set endtime=playtime+1 where endtime is null and period='Y';
update webdata.open_drama_item set seller='GEWA' where seller is null; 

alter table webdata.open_theatreseat add areaid number(19);

alter table webdata.drama_play_item add dramaname varchar(100);
alter table webdata.drama_play_item add theatrename varchar(100);

update webdata.drama_play_item d set dramaname=(select s.dramaname from webdata.drama s where s.recordid=d.dramaid);
update webdata.drama_play_item d set theatrename=(select s.name from webdata.theatre s where s.recordid=d.theatreid);

create table webdata.theatre_seat_area(
	recordid number(19) not null,
	dpid number(19) not null,
	theatreid number(19) not null,
	dramaid number(19) not null,
	areaname varchar2(50),
	enname varchar2(50),
	seller varchar2(10) not null,
	sellerseq varchar2(30),
	fieldnum varchar2(5) not null,
	roomnum varchar2(5) not null,
	description varchar2(500),
	standing varchar2(10) not null,
	total number(9) not null,
	limitnum number(9) not null,
	firstline number(5) not null,
	firstrank number(5) not null,
	linenum number(5) not null,
	ranknum number(5) not null,
	hotzone varchar2(1000),
	seatmap varchar2(1000),
	status varchar2(10) not null,
	addtime timestamp(6) not null,
	updatetime timestamp(6) not null,
	constraint pk_theatre_seat_area primary key(recordid) using index tablespace tbs_index,
	constraint uk_theatre_seat_area unique(dpid,roomnum) using index tablespace tbs_index
);
grant select,insert,update on webdata.theatre_seat_area to shanghai;
create index webdata.idx_theatre_seat_area on webdata.theatre_seat_area(dpid) tablespace tbs_index;

update webdata.theatre_seat_price set dpid=odiid where dpid is null;
alter table webdata.theatre_seat_price modify odiid null;

alter table webdata.theatre_seat_price add sispseq varchar2(50);
alter table webdata.theatre_seat_price add seller varchar2(10);
update webdata.theatre_seat_price s set s.seller=(select o.seller from webdata.drama_play_item o where o.recordid=s.dpid) where s.seller is null;

---- 更新场次区域
insert into webdata.theatre_seat_area
       (recordid,dpid,theatreid,dramaid,areaname,seller,sellerseq,fieldnum,roomnum,description,standing,total,limitnum,firstline,firstrank,linenum,ranknum,status,addtime,updatetime)
 select hibernate_sequence.nextval,p.recordid,p.theatreid,p.dramaid,f.name,p.seller,p.sellerseq,f.fieldnum,r.roomnum,f.description,(case when p.opentype='seat' then 'N' else 'Y' end),nvl(r.seat_num,0),nvl(r.seat_num,0)
        ,firstline,r.firstrank,nvl(r.line_num,0),nvl(r.rank_num,0),'Y',sysdate,sysdate 
 from webdata.drama_play_item p, webdata.theatre_field f, webdata.theatre_room r 
 where p.roomid = f.recordid and r.recordid=f.recordid and not exists(select * from webdata.theatre_seat_area a where a.dpid=p.recordid);

insert into webdata.theatre_seat_area
       (recordid,dpid,theatreid,dramaid,areaname,seller,sellerseq,fieldnum,roomnum,description,standing,total,limitnum,firstline,firstrank,linenum,ranknum,status,addtime,updatetime)
 select hibernate_sequence.nextval,p.dpid,p.theatreid,p.dramaid,f.name,p.seller,p.sellerseq,f.fieldnum,r.roomnum,f.description,(case when p.open_type='seat' then 'N' else 'Y' end),nvl(r.seat_num,0),nvl(r.seat_num,0)
        ,firstline,r.firstrank,nvl(r.line_num,0),nvl(r.rank_num,0),'Y',sysdate,sysdate 
 from webdata.open_drama_item p, webdata.theatre_field f, webdata.theatre_room r 
 where p.roomid = f.recordid and r.recordid=f.recordid and not exists(select * from webdata.theatre_seat_area a where a.dpid=p.dpid);
 
update webdata.open_theatreseat t set t.dpid = (select o.dpid from webdata.open_drama_item o where o.recordid=t.odiid) 
where t.dpid is null and exists(select o.dpid from webdata.open_drama_item o where o.recordid=t.odiid);
delete from webdata.open_theatreseat t where t.dpid is null and not exists(select * from webdata.open_drama_item o where o.recordid=t.odiid);

----------本地有重复数据，删除后加唯一约束
select min(recordid) as recordid,dpid,areaid,lineno,rankno,count(*) from webdata.open_theatreseat group by dpid,areaid,lineno,rankno having count(*)>1;
select min(recordid) as recordid,dpid,areaid,seatline,seatrank,count(*) from webdata.open_theatreseat group by dpid,areaid,seatline,seatrank having count(*)>1;
delete from webdata.open_theatreseat o where exists(select * from (select min(recordid) as recordid,dpid,areaid,seatline,seatrank,count(*) from webdata.open_theatreseat group by dpid,areaid,seatline,seatrank having count(*)>1) t where t.recordid=o.recordid);
delete from webdata.open_theatreseat o where exists(select * from (select min(recordid) as recordid,dpid,areaid,lineno,rankno,count(*) from webdata.open_theatreseat group by dpid,areaid,lineno,rankno having count(*)>1) t where t.recordid=o.recordid);
alter table webdata.open_theatreseat add constraint uk_open_theatreseat1 unique(dpid,areaid,lineno,rankno) using index tablespace tbs_index;
alter table webdata.open_theatreseat add constraint uk_open_theatreseat2 unique(dpid,areaid,seatline,seatrank) using index tablespace tbs_index;
create index webdata.idx_open_theatreseat1 on webdata.open_theatreseat(areaid) tablespace tbs_index; 
----------------------------------------

update webdata.theatre_seat_price t set areaid = (select min(a.recordid) from webdata.theatre_seat_area a where a.dpid=t.dpid group by a.dpid) where t.areaid is null;
update webdata.open_theatreseat o set o.areaid = (select a.recordid from webdata.theatre_seat_area a where a.dpid=o.dpid) where o.areaid is null;
 ---- 本地数据有为空，删除。
 alter table webdata.show_price add saseqNo varchar2(30);
update webdata.show_price set saseqNo=partner||areaseq where saseqNo is null;

alter table webdata.disquantity add addtime timestamp(6);
alter table webdata.disquantity add updatetime timestamp(6);

alter table webdata.selldramaseat add costprice number(5) default 0;
alter table webdata.selldramaseat add theatreprice number(5) default 0;
alter table webdata.buyitem add oriprice number(5) default 0;

alter table webdata.buyitem modify oriprice not null;
alter table webdata.selldramaseat modify costprice not null;
alter table webdata.selldramaseat modify theatreprice not null;

update webdata.disquantity set addtime=sysdate where addtime is null;
update webdata.disquantity set updatetime=sysdate where updatetime is null;

alter table webdata.disquantity modify addtime not null;
alter table webdata.disquantity modify updatetime not null;

alter table webdata.theatre_seat_price add price_version number(5) default 0;
alter table webdata.theatre_seat_price add allowaddnum number(5) default 0;
alter table webdata.theatre_seat_price add sellordernum number(5)default 0;
alter table webdata.disquantity add data_version number(5) default 0;
alter table webdata.disquantity add allownum number(5) default 0;
alter table webdata.disquantity add tickettotal number(5) default 0;
alter table webdata.disquantity add sellordernum number(5) default 0;

update webdata.theatre_room set SEAT_NUM=0 where SEAT_NUM is null;
update theatre_room set LINE_NUM =0 where LINE_NUM  is null;
update theatre_room set RANK_NUM =0 where RANK_NUM  is null;

alter table webdata.THEATRE_ROOM modify SEAT_NUM not null;
alter table THEATRE_ROOM modify LINE_NUM not null;
alter table THEATRE_ROOM modify RANK_NUM not null;

alter table webdata.show_area rename column limit to limitnum;

alter table webdata.show_seat add loveInd char(1);
update webdata.show_seat set loveind=0 where loveind is null;
alter table webdata.theatre_seat_price modify allowaddnum not null;
alter table webdata.theatre_seat_price modify sellordernum not null;
alter table webdata.show_seat modify loveind not null;
alter table webdata.theatre_room_seat modify loveind not null;

update webdata.open_theatreseat set loveind = 0 where loveind is null;
alter table webdata.open_theatreseat modify loveind not null;

alter table webdata.disquantity modify allownum not null;
alter table webdata.disquantity modify sellordernum not null;
alter table webdata.disquantity modify tickettotal not null;

alter table webdata.theatre_seat_price modify price_version not null;
alter table webdata.disquantity modify data_version not null;

alter table webdata.show_price modify saseqNo not null;

alter table webdata.open_theatreseat modify dpid not null;

alter table webdata.open_theatreseat modify areaid not null;
alter table webdata.theatre_seat_price modify areaid not null;

alter table webdata.theatre_seat_price modify dpid not null;

alter table webdata.theatre_room modify roomtype not null;
alter table webdata.theatre_room modify fieldid not null;
alter table webdata.theatre_room modify firstline not null;
alter table webdata.theatre_room modify firstrank not null;
alter table webdata.theatre_room modify roomnum not null;

alter table webdata.drama_play_item modify opentype not null;
alter table webdata.drama_play_item modify period not null;
alter table webdata.drama_play_item modify seller not null;
alter table webdata.drama_play_item modify endtime not null;

alter table webdata.open_drama_item modify period not null;
alter table webdata.open_drama_item modify seller not null;
alter table webdata.open_drama_item modify endtime not null;

alter table webdata.theatre_seat_price modify seller not null;

alter table webdata.show_area drop constraint uk_show_area_num;
alter table webdata.show_area drop constraint uk_show_area_serial;
alter table webdata.show_area add constraint uk_show_area_num unique(siseqNo,areanum);
alter table webdata.show_area add constraint uk_show_area_serial unique(siseqNo,areaserial);
alter table webdata.show_item add citycode varchar2(8) not null;

---------------------------------------------2013-05-13-------------------------------------------------------------------------------------------------------------------------
alter table webdata.field_area add fieldnum varchar2(5);
create table webdata.gptbs_place_field_area(
	recordid number(19) not null,
	cnname varchar2(50),
	enname varchar2(50),
	description varchar2(100),
	venueid number(19) not null,
	stadiumid number(19) not null,
	gridwidth number(9) not null,
	gridheight number(9) not null,
	standing varchar2(10),
	hotzone varchar2(100),
	total number(9) not null,
	limit number(9) not null,
	updatetime timestamp(6) not null,
	constraint pk_gptbs_place_field_area primary key(recordid) using index tablespace tbs_index
);
grant select, insert, update on webdata.gptbs_place_field_area to shanghai;

create table webdata.show_item(
	siseq varchar2(30) not null,
	showname varchar2(100) not null,
	dramaid number(19) not null,
	theatreid number(19) not null,
	fieldnum varchar2(5) not null,
	fieldname varchar2(50) not null,
	playtime timestamp(6) not null,
	endtime timestamp(6) not null,
	opentime timestamp(6) not null,
	closetime timestamp(6) not null,
	partner varchar2(10) not null,
	pseqno varchar2(30) not null,
	itemtype varchar2(10) not null,
	period varchar2(10) not null,
	status varchar2(10) not null,
	citycode varchar2(8) not null,
	express varchar2(10),
	createtime timestamp(6) not null,
	updatetime timestamp(6) not null,
	constraint pk_show_item primary key(siseq) using index tablespace tbs_index,
	constraint uk_show_item unique(dramaid, theatreid, fieldnum, playtime) using index tablespace tbs_index
);
grant select, insert, update on webdata.show_item to shanghai;

alter table webdata.gptbs_schedule_area add synchstatus varchar2(10) not null;
alter table webdata.gptbs_schedule_area add updatetype varchar2(2) not null;
alter table webdata.gptbs_schedule_area add programid number(19) not null;

alter table webdata.gptbs_schedule add fixed varchar2(10) not null;
alter table webdata.gptbs_schedule add logistics number(9) not null;

create table webdata.gptbs_program_price(
	recordid number(19) not null,
	programid number(19) not null,
	price number(10) not null,
	deposit number(10) not null,
	color number(19),
	companyId number(19),
	status varchar2(10) not null,
	addtime timestamp(6) not null,
	updatetime timestamp(6) not null,
	constraint pk_gptbs_program_price primary key(recordid) using index tablespace tbs_index
);
grant select,insert,update on webdata.gptbs_program_price to shanghai;

create table webdata.show_area(
	saseqno varchar2(30) not null,
	areaname varchar2(50) not null,
	enname varchar2(50),
	partner varchar2(10) not null,
	areaserial varchar2(19) not null,
	fieldserial varchar2(19) not null,
	pseqno varchar2(19) not null,
	theatreid number(19) not null,
	dramaid number(19) not null,
	fieldnum varchar2(5) not null,
	areanum varchar2(5) not null,
	siseqNo varchar2(30) not null,
	STANDING varchar2(10) not null,
	description varchar2(500),
	total number(5) default 0 not null,
	limit number(5) default 0 not null,
	firstline number(5) default 0 not null,
	firstrank number(5) default 0 not null,
	linenum number(9) not null,
	ranknum number(9) not null,
	hotzone varchar2(100),
	status varchar2(10) not null,
	createtime timestamp(6) not null,
	updatetime timestamp(6) not null,
	constraint pk_show_area primary key(saseqno) using index tablespace tbs_index,
	constraint uk_show_area_serial unique(theatreid,partner,areaserial) using index tablespace tbs_index,
	constraint uk_show_area_num unique(theatreid,partner,areanum) using index tablespace tbs_index
);
grant select,insert, update on webdata.show_area to shanghai;
create index webdata.idx_show_area_siseq on webdata.show_area(siseqNo) tablespace tbs_index;

create table webdata.field_area_seat(
	recordid number(19) not null,
	areaid number(19) not null,
	lineno number(9) not null,
	rankno number(9) not null,
	seatline varchar2(8) not null,
	seatrank varchar2(8) not null,
	loveInd varchar2(2),
	seatno varchar2(19) not null,
	constraint pk_field_area_seat primary key(recordid) using index tablespace tbs_index,
	constraint uk_field_area_seat1 unique(areaid,lineno,rankno) using index tablespace tbs_index,
	constraint uk_field_area_seat2 unique(areaid,seatline,seatrank) using index tablespace tbs_index
);
grant select,insert,update,delete on webdata.field_area_seat to shanghai;


create table webdata.show_seat(
	recordid number(19) not null,
	siseqno varchar2(30) not null,
	arseqno varchar2(30) not null,
	lineno number(9) not null,
	rankno number(9) not null,
	seatline varchar2(9) not null,
	seatrank varchar2(9) not null,
	status varchar2(10) not null,
	ticketid varchar2(30),
	price number(5) not null,
	constraint pk_show_seat primary key(recordid) using index tablespace tbs_index,
	constraint uk_show_seat1 unique(siseqno,arseqno,lineno,rankno) using index tablespace tbs_index,
	constraint uk_show_seat2 unique(siseqno,arseqno,seatline,seatrank) using index tablespace tbs_index
);
grant select,insert,update,delete on webdata.show_seat to shanghai;

create table webdata.show_price(
	sispseq varchar2(30) not null,
	siseq varchar2(30) not null,
	areaseq varchar2(20) not null,
	partner varchar2(10) not null,
	pseqno varchar2(30) not null,
	ticketid varchar2(20) not null,
	dramaid number(19) not null,
	price number(5) not null,
	lowest number(5) not null,
	ticketTotal number(9) not null,
	ticketLimit number(9) not null,
	status varchar2(10) not null,
	createtime timestamp(6) not null,
	updatetime timestamp(6) not null,
	constraint pk_show_price primary key(sispseq) using index tablespace tbs_index,
	constraint uk_show_price unique(siseq,areaseq,ticketid) using index tablespace tbs_index
);
grant select,insert,update on webdata.show_price to shanghai;
---------------------------------------------2013-05-07-------------------------------------------------------------------------------------------------------------------------
--- 删除
create table webdata.gptbs_schedule_price(
	recordid varchar2(30) not null,
	priceid number(19) not null,
	ticketpriceid number(19) not null,
	scheduleareaid number(19) not null,
	scheduleid number(19) not null,
	ticketlimit number(10),
	tickettotal number(10),
	addtime timestamp(6) not null,
	updatetime timestamp(6) not null,
	constraint pk_gptbs_schedule_price primary key (recordid) using index tablespace tbs_index
);
grant select, insert, update, delete on webdata.gptbs_stadium to shanghai;

alter table webdata.gptbs_place_field rename column fieldname to status;
alter table webdata.selldramaseat add areaid number(19) not null;
alter table webdata.gptbs_schedule add stadiumid number(19) not null;
alter table webdata.gptbs_schedule add updatetype varchar2(10) not null;
alter table webdata.gptbs_schedule add synchstatus varchar2(10) not null;
alter table webdata.gptbs_schedule rename column period to itemtype;

alter table webdata.gptbs_schedule_price add status varchar2(10) not null;

create table webdata.gptbs_schedule_logger(
	recordid number(19) not null,
	stadiumid varchar2(30) not null,
	playdate date not null,
	status varchar2(10),
	remark varchar2(100),
	updatetime timestamp(6) not null,
	constraint pk_gptbs_schedule_logger primary key(recordid) using index tablespace tbs_index,
	constraint uk_gptbs_schedule_logger unique (stadiumid,playdate) using index tablespace tbs_index
);
grant select, insert, update on webdata.gptbs_schedule_logger to shanghai;

create table webdata.field(
	recordid number(19) not null,
	name varchar2(50) not null,
	theatreid number(19) not null,
	fieldnum varchar2(5),
	fieldserial varchar2(20) not null,
	status varchar2(10),
	fieldtype varchar2(10) not null,
	logo varchar2(1000),
	updatetime timestamp(6) not null,
	constraint pk_field primary key(recordid) using index tablespace tbs_index,
	constraint uk_field_serial unique(theatreid,fieldserial) using index tablespace tbs_index
);
grant select, insert, update, delete on webdata.field to shanghai;

create table webdata.field_area(
  recordid number(19) not null,
  name varchar2(60),
  theatreid number(19) not null,
  fieldnum varchar2(5),
  fieldserial varchar2(30) not null,
  linenum number(5) not null,
  firstline number(5) not null,
  firstrank number(5) not null,
  ranknum number(5) not null,
  seatnum number(5) not null,
  areanum varchar2(6),
  areaserial varchar2(6) not null,
  areatype varchar2(10) not null,
  hotzone varchar2(100),
  status varchar2(10) not null,
  updatetime timestamp(6) not null,
  constraint pk_field_area primary key(recordid) using index tablespace tbs_index,
  constraint uk_field_area unique (areaserial , fieldserial, theatreid) using index tablespace tbs_index
);
grant select, insert, update on webdata.field_area to shanghai;


---------------------------------------------2013-05-06-------------------------------------------------------------------------------------------------------------------------
alter table webdata.BINDMOBILE add BIND_VERSION number(10) default 0;
update webdata.BINDMOBILE set BIND_VERSION = 0 where BIND_VERSION is null;
alter table webdata.BINDMOBILE modify BIND_VERSION number(10) not null;
create table webdata.gptbs_drama(
	recordid number(19) not null,
	name varchar2(100) not null,
	synchtime timestamp(6) not null,
	constraint pk_gptbs_drama primary key (recordid) using index tablespace tbs_index
);
grant select,insert,update on webdata.gptbs_drama to shanghai;
create table webdata.gptbs_theatre(
	recordid number(19) not null,
	name varchar2(100) not null,
	partnerid varchar2(30),
	partner varchar2(10),
	citycode varchar2(10),
	status varchar2(2) not null,
	synchtime timestamp(6) not null,
	constraint pk_gptbs_theatre primary key(recordid) using index tablespace tbs_index
);
grant select,insert,update on webdata.gptbs_theatre to shanghai;

create table webdata.gptbs_stadium(
  recordid varchar2(20) not null,
  stadiumid number(19) not null,
  cnname varchar2(100),
  enname varchar2(100),
  cnaddress varchar2(1000),
  enaddress varchar2(1000),
  telephone varchar2(20),
  typeid number(19),
  available varchar2(10),
  provincecode varchar2(10),
  citycode varchar2(10),
  cityareacode varchar2(10),
  addtime timestamp(6) not null,
  constraint pk_gptbs_stadium primary key(recordid) using index tablespace tbs_index
);
grant select, insert, update on webdata.gptbs_stadium to shanghai;
create table webdata.gptbs_program(
	recordid number(19) not null,
	code varchar2(50),
	cnname varchar2(100),
	enname varchar2(100),
	stadiumid number(19),
	venueid number(19),
	starttime timestamp(6),
	endtime timestamp(6),
	typeid number(5),
	available varchar2(10),
	dramaid number(19),
	addtime timestamp(6) not null,
	updatetime timestamp(6) not null,
	constraint pk_gptbs_program primary key (recordid) using index tablespace tbs_index
);
grant select, insert, update on webdata.gptbs_program to shanghai;

create table webdata.gptbs_schedule(
	recordid varchar2(30) not null,
	scheduleid number(19) not null,
	code varchar2(30),
	cnname varchar2(100),
	enname varchar2(100),
	status varchar2(10),
	period varchar2(10),
	playtime timestamp(6),
	starttime timestamp(6),
	endtime timestamp(6),
	programid number(19),
	venueid number(19),
	addtime timestamp(6) not null,
	updatetime timestamp(6) not null,
	constraint pk_gptbs_schedule primary key(recordid) using index tablespace tbs_index
);
grant select, insert, update on webdata.gptbs_schedule to shanghai;

create table webdata.gptbs_schedule_area(
	recordid varchar2(30) not null,
	areaid number(19) not null,
	cnname varchar2(100),
	enname varchar2(100),
	description varchar2(500),
	standing varchar2(10),
	total number(9),
	limit number(9),
	venueid number(19),
	gridwidth number(5),
	gridheight number(5),
	venueareaid number(19),
	scheduleid number(19),
	addtime timestamp(6) not null,
	updatetime timestamp(6) not null,
	constraint pk_gptbs_schedule_area primary key(recordid) using index tablespace tbs_index
);
grant select, insert, update on webdata.gptbs_schedule_area to shanghai;

create table webdata.gptbs_place_field(
	recordid varchar2(50) not null,
	fieldid number(19) not null,
	cnname varchar2(100),
	enname varchar2(100),
	logo varchar2(1000),
	stadiumid number(19),
	fieldnum varchar2(5),
	addtime timestamp(6) not null,
	constraint pk_gptbs_place_field primary key(recordid) using index tablespace tbs_index
);
grant select, insert, update on webdata.gptbs_place_field to shanghai;

---- 删除
create table webdata.gptbs_schedule_seat(
	recordid number(19) not null,
	lineno number(5) not null,
	rankno number(5) not null,
	seatline varchar2(4) not null,
	seatrank varchar2(4) not null,
	ticketpriceid number(19) not null,
	venueareaid number(19) not null,
	scheduleid number(19) not null,
	ticketpoolid number(19) not null,
	status varchar2(8) not null,
	programid number(19) not null,
	serialnum number(19),
	addtime timestamp(6) not null,
	updatetime timestamp(6) not null,
	constraint pk_gptbs_schedule_seat primary key(recordid) using index tablespace tbs_index,
	constraint uk_seat_lineno unique(venueareaid,lineno,rankno) using index tablespace tbs_index,
	constraint uk_seat_seatline unique(venueareaid,seatline,seatrank) using index tablespace tbs_index
);
grant select, insert, update, delete on webdata.gptbs_schedule_seat to shanghai;

---------------------------------------------2013-04-01-------------------------------------------------------------------------------------------------------------------------
alter table webdata.open_drama_item add maxbuy number(5);
alter table webdata.open_drama_item add msgminute number(5);
alter table webdata.drama_play_item add addtime timestamp(6);
alter table webdata.drama_play_item add batch number(19);
alter table webdata.goods add msgminute number(5);

update webdata.goods set msgminute = 180 where msgminute is null;
update webdata.open_drama_item set msgminute = 180 where msgminute is null;
update webdata.open_drama_item set maxbuy = 6 where maxbuy is null;
update webdata.drama_play_item set addtime= playtime where addtime is null;

alter table webdata.goods modify msgminute not null;
alter table webdata.drama_play_item modify addtime not null;
alter table webdata.open_drama_item modify maxbuy not null;
alter table webdata.open_drama_item modify msgminute not null;
---------------------------------------------2013-04-01-------------------------------------------------------------------------------------------------------------------------
update webdata.ticket_order t set t.citycode=(select g.citycode from webdata.goods g where g.recordid =t.relatedid) 
where t.order_type='goods' and t.citycode is null  and addtime>sysdate - 20;

create index webdata.idx_goods_fromtime on webdata.goods(fromtime) tablespace tbs_index;
create index webdata.idx_goods_totime on webdata.goods(totime) tablespace tbs_index;
create index webdata.idx_goods_relatedid on webdata.goods(relatedid) tablespace tbs_index;
create index webdata.idx_goods_itemid on webdata.goods(itemid) tablespace tbs_index;
create index webdata.idx_goods_fromvalidtime on webdata.goods(fromvalidtime) tablespace tbs_index;
create index webdata.idx_goods_tovalidtime on webdata.goods(tovalidtime) tablespace tbs_index;
---------------------------------------------2013-03-18-------------------------------------------------------------------------------------------------------------------------
alter table webdata.order_note modify message varchar2(300);
create table webdata.order_note(
  recordid number(19) not null,
  serialno varchar2(30),
  orderid number(19) not null,
  tradeno varchar2(30) not null,
  ordertype varchar2(10) not null,
  mobile varchar2(15) not null,
  placename varchar2(50),
  placetype varchar2(20) not null,
  placeid number(19) not null,
  itemname varchar2(50),
  itemtype varchar2(20) not null,
  itemid number(19) not null,
  checkpass varchar2(20),
  ticketnum number(5) default 1 not null,
  smallitemid number(19) not null,
  smallitemtype varchar2(20) not null,
  addtime timestamp(6) not null,
  updatetime timestamp(6) not null,
  validtime timestamp(6),
  modifytime timestamp(6) not null,
  tasktime timestamp(6),
  result varchar2(20),
  status varchar2(20) not null,
  message varchar2(100),
  description varchar2(2000),
  constraint pk_order_note primary key (recordid) using index tablespace tbs_index
);
grant select,insert,update on webdata.order_note to shanghai;

create index webdata.idx_order_note_serial on webdata.order_note(serialno) tablespace tbs_index;
create index webdata.idx_order_note_orderid on webdata.order_note(orderid) tablespace tbs_index;
create index webdata.idx_order_note_modifytime on webdata.order_note(modifytime) tablespace tbs_index;
create index webdata.idx_order_note_addtime on webdata.order_note(addtime) tablespace tbs_index;
alter table webdata.order_note add constraint uk_order_note unique (serialno) using index tablespace tbs_index;
alter table webdata.order_note modify smallitemid not null;
alter table webdata.order_note modify smallitemtype not null;

alter table webdata.buyitem modify goodsid null;

alter table webdata.buyitem add tag varchar2(10); 				---//商品或场次类型
alter table webdata.buyitem add relatedid number(19); 			---//商品或场次ID
alter table webdata.buyitem add playtime Timestamp(6); 			---//消费时间
alter table webdata.buyitem add costprice number(5); 			---//成本价
alter table webdata.buyitem add totalcost number(5) default 0; 	---//总成本价
alter table webdata.buyitem add totalfee number(5); 			---//商品总金额
alter table webdata.buyitem add discount number(5) default 0; 	---//商品优惠
alter table webdata.buyitem add disreason varchar2(100); 		---//优惠理由
alter table webdata.buyitem add bundle varchar2(1);				---//赠品
alter table webdata.buyitem add remark varchar2(100); 			---//特别说明
alter table webdata.buyitem add placetype varchar2(10); 		---//场馆类型
alter table webdata.buyitem add placeid number(19); 			---//场馆ID
alter table webdata.buyitem add itemtype varchar2(10); 			---//项目类型
alter table webdata.buyitem add itemid number(19); 				---//项目ID
alter table webdata.buyitem add otherinfo varchar2(1000); 		---//其他信息
alter table webdata.buyitem add citycode varchar2(10); 			---//城市代码
alter table webdata.buyitem add description varchar2(1000); 	---//商品描述
alter table webdata.buyitem add smallitemtype varchar2(15);		---//商品卖出方式(价格，座位)
alter table webdata.buyitem add smallitemid number(19);			---//关联对象ID(如价格,座位)
alter table webdata.buyitem add disid number(19);				---//套票ID
alter table webdata.buyitem add disfee number(5) default 0;		---//套票优惠金额
alter table webdata.buyitem add express varchar2(1) default 'N';---//是否快递

alter table webdata.goods add period varchar2(1);				---//是否有时段限制
alter table webdata.goods_price add section varchar2(10);		---//价格区域
alter table webdata.goods add starid number(19);
alter table webdata.goods modify description null;

alter table webdata.ticket_order add pricategory varchar2(10);	---//类别(模块)
-------------------------------------------------------------------------------------------

update webdata.buyitem set relatedid = goodsid where relatedid is null;
alter table webdata.buyitem modify relatedid not null;

update webdata.buyitem set tag = 'goods' where tag is null;
alter table webdata.buyitem modify tag not null;

update webdata.buyitem b set b.costprice=(select g.costprice from webdata.goods g where g.recordid = b.relatedid and g.goods_type='goods')
where exists(select g.costprice from webdata.goods g where g.recordid = b.relatedid and g.goods_type='goods') and costprice is null;

update webdata.buyitem b set b.costprice = 0 where b.costprice is null;
alter table webdata.buyitem modify costprice default 0 not null;

update webdata.buyitem set totalcost = costprice * quantity where totalcost is null;
alter table webdata.buyitem modify totalcost not null;

update webdata.buyitem set totalfee = unitprice * quantity where totalfee is null;
alter table webdata.buyitem modify totalfee not null;
alter table webdata.buyitem modify discount not null;
alter table webdata.buyitem modify disfee not null;
alter table webdata.buyitem modify express not null;

update webdata.goods_price set section = 'A';
alter table webdata.goods_price modify section not null;
alter table webdata.goods_price add constraint uk_goods_price unique(goodsid,price,section,status) using index TABLESPACE tbs_index;

update webdata.goods set period = 'N' where period is null;
alter table webdata.goods modify period not null;
alter table webdata.theatre_seat_price modify status not null;

update webdata.goods set tag = 'activity' where goods_type='goods' and tag='buss_activity';
update webdata.goods set tag = 'groupon' where goods_type='goods' and tag='tg';


update webdata.ticket_order set pricategory = 'drama' where order_type='drama' and pricategory is null;
update webdata.ticket_order set pricategory = 'sport' where order_type='sport' and pricategory is null;
update webdata.ticket_order set pricategory = 'gym' where order_type='gym' and pricategory is null;
update webdata.ticket_order set pricategory = 'pubsale' where order_type='pubsale' and pricategory is null;

update webdata.ticket_order set pricategory = 'movie' where order_type='ticket' and pricategory is null and addtime<to_date('2011-01-01','yyyy-mm-dd');
update webdata.ticket_order set pricategory = 'movie' where order_type='ticket' and pricategory is null and addtime>=to_date('2011-01-01','yyyy-mm-dd') and addtime<to_date('2011-07-01','yyyy-mm-dd');
update webdata.ticket_order set pricategory = 'movie' where order_type='ticket' and pricategory is null and addtime>=to_date('2011-07-01','yyyy-mm-dd') and addtime<to_date('2012-01-01','yyyy-mm-dd');
update webdata.ticket_order set pricategory = 'movie' where order_type='ticket' and pricategory is null and addtime>=to_date('2012-01-01','yyyy-mm-dd') and addtime<to_date('2012-04-01','yyyy-mm-dd');
update webdata.ticket_order set pricategory = 'movie' where order_type='ticket' and pricategory is null and addtime>=to_date('2012-04-01','yyyy-mm-dd') and addtime<to_date('2012-08-01','yyyy-mm-dd');
update webdata.ticket_order set pricategory = 'movie' where order_type='ticket' and pricategory is null and addtime>=to_date('2012-08-01','yyyy-mm-dd') and addtime<to_date('2012-10-01','yyyy-mm-dd');
update webdata.ticket_order set pricategory = 'movie' where order_type='ticket' and pricategory is null and addtime>=to_date('2012-10-01','yyyy-mm-dd') and addtime<to_date('2013-01-01','yyyy-mm-dd');
update webdata.ticket_order set pricategory = 'movie' where order_type='ticket' and pricategory is null;


update webdata.ticket_order t set t.category=(select g.goods_type from webdata.goods g where g.recordid = t.relatedid) where t.order_type='goods';

update webdata.ticket_order set pricategory = 'activity' where order_type='goods' and category='activity' and pricategory is null;
update webdata.ticket_order set pricategory = 'sport' where order_type='goods' and category='sport' and pricategory is null;


update webdata.ticket_order t set t.pricategory = 'movie' 
	where t.order_type='goods' and (category='goods' or category is null) and exists(select * from webdata.goods g where g.recordid=t.relatedid and g.goods_type='goods' and tag='bmh') and t.pricategory is null; 
update webdata.ticket_order t set t.pricategory = 'sport' 
	where t.order_type='goods' and (category='goods' or category is null) and exists(select * from webdata.goods g where g.recordid=t.relatedid and g.goods_type='goods' and tag='bmh_sport') and t.pricategory is null; 
update webdata.ticket_order t set t.pricategory = 'drama' 
	where t.order_type='goods' and (category='goods' or category is null) and exists(select * from webdata.goods g where g.recordid=t.relatedid and g.goods_type='goods' and tag='bmh_theatre') and t.pricategory is null; 
update webdata.ticket_order t set t.pricategory = 'bar' 
	where t.order_type='goods' and (category='goods' or category is null) and exists(select * from webdata.goods g where g.recordid=t.relatedid and g.goods_type='goods' and tag='bmh_bar') and t.pricategory is null; 

update webdata.ticket_order t set t.pricategory = 'activity' 
	where t.order_type='goods' and (category='goods' or category is null) and exists(select * from webdata.goods g where g.recordid=t.relatedid and g.goods_type='goods' and tag='activity') and t.pricategory is null; 

update webdata.ticket_order t set t.pricategory = 'groupon' 
	where t.order_type='goods' and (category='goods' or category is null) and exists(select * from webdata.goods g where g.recordid=t.relatedid and g.goods_type='goods' and tag='groupon') and t.pricategory is null;
	
update webdata.ticket_order t set t.pricategory = 'point' 
	where t.order_type='goods' and (category='goods' or category is null) and exists(select * from webdata.goods g where g.recordid=t.relatedid and g.goods_type='goods' and tag='point') and t.pricategory is null; 

update webdata.ticket_order t set t.pricategory = 'goods' where t.order_type='goods' and t.addtime<to_date('2010-01-01','yyyy-mm-dd');
	
update webdata.ticket_order t set t.pricategory = 'drama' 
	where t.order_type='goods' and (category='ticket' or category is null) and exists(select * from webdata.goods g where g.recordid=t.relatedid and g.goods_type='ticket' and tag='theatre') and t.pricategory is null; 
update webdata.ticket_order set pricategory = 'movie' where order_type='ticket' and pricategory is null and addtime>sysdate-20;

alter table webdata.ticket_order modify pricategory not null;

update webdata.ticket_order t set t.pricategory = 'movie' 
where t.order_type='goods' and t.addtime>sysdate-1 and (category='goods' or category is null) and exists(select * from webdata.goods g where g.recordid=t.relatedid and g.goods_type='goods' and tag='bmh') and t.pricategory is null; 


---------------------------------------------2013-03-15-------------------------------------------------------------------------------------------------------------------------
create table webdata.goods_disquantity(
  recordid number(19) not null,
  data_version number(10) default 0 not null,
  goodsid number(19) not null,
  gspid number(19) not null,
  price number(5) not null,
  costprice number(5) not null,
  oriprice number(5),
  quantity number(9) not null,
  allownum number(5) not null
  sellordernum number(9) not null,
  addtime timestamp(6) not null,
  updatetime timestamp(6) not null,
  constraint pk_goods_disquantity primary key (recordid)
  using index (create unique index webdata.idxpk_goods_disquantity on webdata.goods_disquantity(recordid) tablespace tbs_index),
  CONSTRAINT uk_goods_disquantity unique (gspid,price,quantity)
   using index (CREATE UNIQUE index webdata.idxuk_goods_disquantity on webdata.goods_disquantity(gspid,price,quantity) TABLESPACE tbs_index)
);
grant select, insert, update, delete on webdata.goods_disquantity to shanghai;
create index webdata.idx_goods_disquantity_goodsid on webdata.goods_disquantity(goodsid) tablespace tbs_index;
create index webdata.idx_goods_disquantity_gspid on webdata.goods_disquantity(gspid) tablespace tbs_index;

alter table webdata.goods_price drop constraint uk_goods_price cascade;
---------------------------------------------2013-03-13-------------------------------------------------------------------------------------------------------------------------
update open_drama_item set expressid = 'YD001' 
where playtime>sysdate and status <> 'D' and 
recordid not in (103226073,110211403,101566728,101757767,106282718,104120790,106262288,110224067,107922014,110224620,106255892,107924396);
---------------------------------------------2013-03-05-------------------------------------------------------------------------------------------------------------------------
-- Add/modify columns 
alter table webdata.OPEN_DRAMA_ITEM add EXPRESSID varchar2(20);

create table webdata.goods_price(
	recordid number(19) not null,
	data_version number(10) default 0 not null,
	goodsid number(19) not null,
	pricelevel varchar2(6) not null,
	price number(5) not null,
	costprice number(5) not null,
	oriprice number(5) not null,
	status varchar2(10) not null,
	remark varchar2(300),
	quantity number(9) not null,
	allowaddnum number(9) not null,
	sellquantity number(9) not null,
	sellordernum number(9) not null,
	updatetime timestamp(6) not null,
	constraint pk_goods_price primary key (recordid)
	using index (create unique index webdata.idxpk_goods_price on webdata.goods_price(recordid) tablespace tbs_index),
	CONSTRAINT uk_goods_price unique (goodsid,price)
	 using index (CREATE UNIQUE index webdata.idxuk_goods_price on webdata.goods_price(goodsid,price) TABLESPACE tbs_index)
);
CREATE index webdata.idx_goods_price_goodsid on webdata.goods_price(goodsid) TABLESPACE tbs_index;
grant select, insert, update on webdata.goods_price to shanghai;

create table webdata.otherfee_detail(
  recordid number(19) not null,
  orderid number(19) not null,
  fee number(5) not null,
  quantity number(5) default 1 not null,
  reason varchar2(100) not null,
  feetype varchar2(10) not null,
  constraint pk_otherfee_detail primary key (recordid)
  using index (create unique index webdata.idxpk_otherfee_price on webdata.otherfee_detail(recordid) tablespace tbs_index),
  constraint uk_otherfee_detail unique (orderid,feetype) 
  using index (CREATE UNIQUE index webdata.idxuk_otherfee_detail on webdata.otherfee_detail(orderid,feetype) TABLESPACE tbs_index)
)
create index webdata.idx_otherfee_price_orderid on webdata.otherfee_detail(orderid) tablespace tbs_index;
grant select, insert, update on webdata.otherfee_detail to shanghai;

alter table webdata.goods add EXPRESSID varchar2(19);
alter table webdata.goods add category varchar2(10);
alter table webdata.goods add roomname varchar2(30);
alter table webdata.goods add roomid number(19);
alter table webdata.goods add language varchar2(10);
alter table webdata.goods add elecard varchar2(20);
alter table webdata.ticket_order add otherfee_remark varchar2(1000);
---------------------------2013-02-26----------------------------------------------------------------------------------------------------------------------------------------
create table webdata.express_config(
	recordid varchar2(50) not null,
	name varchar2(50) not null,
	expresstype varchar2(10) not null,
	addtime timestamp(6) not null,
	updatetime timestamp(6) not null,
	remark varchar2(1000),
	constraint pk_express_config primary key (recordid)
	using index (create unique index webdata.idxpk_express_config on webdata.express_config(recordid) tablespace tbs_index)
);
grant select, insert, update on webdata.express_config to shanghai;

create table webdata.express_province(
	recordid number(19) not null,
	name varchar2(50) not null,
	expressid varchar2(50) not null,
	provincecode varchar2(10) not null,
	provincename varchar2(30) not null,
	expressfee number(5) not null,
	freelimit number(5) not null,
	addtime timestamp(6) not null,
	updatetime timestamp(6) not null,
	constraint pk_express_province primary key (recordid)
	using index (create unique index webdata.idxpk_express_province on webdata.express_province(recordid) tablespace tbs_index),
	CONSTRAINT uk_express_province unique (expressid,provincecode)
	 using index (CREATE UNIQUE index webdata.idxuk_express_province on webdata.express_province(expressid,provincecode) TABLESPACE tbs_index)
);
grant select, insert, update, delete on webdata.express_province to shanghai;

create table webdata.order_address(
	tradeno varchar2(30) not null,
	usefuladdressid number(19) not null,
	realname varchar2(50) not null,
	postalcode varchar2(6),
	mobile varchar2(20) not null,
	provincecode varchar2(10) not null,
	provincename varchar2(30) not null,
	citycode varchar2(10) not null,
	cityname varchar2(30) not null,
	countycode varchar2(10) not null,
	countyname varchar2(30) not null,
	address varchar2(300) not null,
	expresstype varchar2(10) not null,
	addtime timestamp(6) not null,
	constraint pk_order_address primary key (tradeno)
	using index (create unique index webdata.idxpk_order_address on webdata.order_address(tradeno) tablespace tbs_index)
);
grant select, insert, update on webdata.order_address to shanghai;

alter table webdata.MEMBER_USEFULADDRESS add provincecode varchar2(10);
alter table webdata.MEMBER_USEFULADDRESS add provincename varchar2(30);
alter table webdata.MEMBER_USEFULADDRESS add citycode varchar2(10);
alter table webdata.MEMBER_USEFULADDRESS add cityname varchar2(30);
alter table webdata.MEMBER_USEFULADDRESS add countycode varchar2(10);
alter table webdata.MEMBER_USEFULADDRESS add countyname varchar2(30);

update webdata.ticket_order t set t.category=(select g.goods_type from webdata.goods g where g.recordid = t.relatedid) where t.order_type='goods';
---------------------------2013-01-28----------------------------------------------------------------------------------------------------------------------------------------
alter table webdata.pubsale add pubnumber number(5) default 0 not null;
alter table webdata.pubsale add goodsid number(19);
alter table webdata.pubsale add unitminute number(9);
alter table webdata.pubsale add dupprice varchar2(100);
alter table webdata.pubsale add pubperiod number(9);
alter table webdata.pubsale modify upprice null;
update webdata.pubsale set dupprice = upprice;
update webdata.pubsale set unitminute=120;
---------------------------2013-01-17----------------------------------------------------------------------------------------------------------------------------------------
alter table webdata.sport_profile modify takemethod null;
alter table webdata.sport_profile modify notifymsg1 null;
alter table webdata.sport_profile modify notifymsg2 null;
alter table webdata.sport_profile modify notifymsg3 null;
alter table webdata.sport_profile modify limitminutes null;
alter table webdata.sport_profile modify diaryid null;
alter table webdata.sport_profile modify tickettype null;
alter table webdata.sport_profile modify exitsreturn null;
alter table webdata.sport_profile modify returnminutes null;
alter table webdata.sport_profile modify returnmoneytype null;
alter table webdata.sport_profile modify returnmoney null;
alter table webdata.sport_profile modify returnsmallmoney null;
alter table webdata.sport_profile modify exitschange null;
alter table webdata.sport_profile modify changeminutes null;
alter table webdata.sport_profile modify changemoneytype null;
alter table webdata.sport_profile modify changemoney null;
alter table webdata.sport_profile modify changesmallmoney null;
alter table webdata.sport_profile add pretype varchar2(10);
alter table webdata.sport_profile add premessage varchar2(500);
create table webdata.order_extra(
  recordid number(19) not null,
  tradeno varchar2(30) not null,
  status varchar2(20) not null,
  addtime timestamp(6) not null,
  updatetime timestamp(6) not null,
  invoice varchar2(10) not null,
  memberid number(19) not null,
  partnerid number(19),
  ordertype varchar2(10) not null,
  expressnote varchar2(50),
  expresstype varchar2(10),
  constraint pk_order_extra primary key(recordid)
  using index (create unique index webdata.idxpk_order_extra on webdata.order_extra(recordid) tablespace tbs_index),
  constraint uk_order_extra unique (tradeno)
  using index (create unique index webdata.idxuk_order_extra on webdata.order_extra(tradeno) tablespace tbs_index)
);
CREATE index webdata.idx_order_extra_expressnote on webdata.order_extra(expressnote) TABLESPACE tbs_index;
grant select, insert, update, delete on webdata.ORDER_EXTRA to shanghai;
create table webdata.order_extra_his(
  recordid number(19) not null,
  tradeno varchar2(30) not null,
  status varchar2(20) not null,
  addtime timestamp(6) not null,
  updatetime timestamp(6) not null,
  invoice varchar2(10) not null,
  memberid number(19) not null,
  partnerid number(19),
  ordertype varchar2(10) not null,
  expressnote varchar2(50),
  expresstype varchar2(10),
  constraint pk_order_extra_his primary key(recordid)
  using index (create unique index webdata.idxpk_order_extra_his on webdata.order_extra_his(recordid) tablespace tbs_index),
  constraint uk_order_extra_his unique (tradeno)
  using index (create unique index webdata.idxuk_order_extra_his on webdata.order_extra_his(tradeno) tablespace tbs_index)
);
CREATE index webdata.idx_order_extra_his_exp on webdata.order_extra_his(expressnote) TABLESPACE tbs_index;
grant select, insert， update on webdata.order_extra_his to shanghai;
create table webdata.invoice_relate(
   orderid number(19) not null,
   invoiceid number(19) not null,
   memberid number(19) not null,
   constraint pk_invoice_relate primary key(orderid)
  using index (create unique index webdata.idxpk_invoice_relate on webdata.invoice_relate(orderid) tablespace tbs_index)
);
CREATE index webdata.idx_invoice_invoiceid on webdata.invoice_relate(invoiceid) TABLESPACE tbs_index;
CREATE index webdata.idx_invoice_memberid on webdata.invoice_relate(memberid) TABLESPACE tbs_index;
grant select, insert, update, delete on webdata.invoice_relate to shanghai;
insert into webdata.order_extra_his(recordid,tradeno,status,addtime,updatetime,invoice,memberid,partnerid,ordertype) 
select recordid,trade_no,status,addtime,addtime,(case when paymethod like 'umPay%' then 'F' else 'N' end),memberid,partnerid,order_type
from webdata.ticket_order where status='paid_success' and addtime<sysdate-180;

insert into webdata.order_extra(recordid,tradeno,status,addtime,updatetime,invoice,memberid,partnerid,ordertype) 
select recordid,trade_no,status,addtime,addtime,(case when paymethod like 'umPay%' then 'F' else 'N' end),memberid,partnerid,order_type
from webdata.ticket_order where status='paid_success' and addtime>=sysdate-180;
update webdata.user_invoice set invoicestatus='Y_AGAIN' where invoicestatus='Y_OPEN,Y_AGAIN';


-----------------------------------------------------------
alter table webdata.open_timetable modify PRATIO null;  --|
alter table webdata.open_timeitem modify PRATIO null;	--|
---------------------------2013-01-14----------------------------------------------------------------------------------------------------------------------------------------
create table webdata.express_order(
	recordid varchar2(50) not null,
	expressnote varchar2(50) not null,
	expresstype varchar2(10) not null,
	addtime timestamp(6) not null,
	updatetime timestamp(6) not null,
	status varchar2(10) not null,
	otherinfo varchar2(1000),
	constraint pk_express_order primary key (recordid)
	using index (create unique index webdata.idxpk_express_order on webdata.express_order(recordid) tablespace tbs_index),
	CONSTRAINT uk_express_order unique (expressnote,expresstype)
	 using index (CREATE UNIQUE index webdata.idxuk_express_order on webdata.express_order(expressnote,expresstype) TABLESPACE tbs_index)
);
update webdata.order_extra o set invoice='Y' where o.invoice='N' and exists(select * from invoice_relate i where i.orderid=o.recordid);
update webdata.order_extra_his o set invoice='Y' where o.invoice='N' and exists(select * from invoice_relate i where i.orderid=o.recordid);
---------------------------2013-01-04----------------------------------------------------------------------------------------------------------------------------------------
alter table webdata.cityprice add quantity number(9) default 0 not null;
alter table webdata.cityprice add c_quantity number(9) default 0 not null;
---------------------------2012-12-28----------------------------------------------------------------------------------------------------------------------------------------
alter table webdata.goods add sales number(5) default 0 not null;
update webdata.goods g set g.sales = (select sum(t.quantity) from webdata.ticket_order t where t.order_type='goods' and t.status='paid_success' and t.relatedid=g.recordid)
where exists(select * from webdata.ticket_order t where t.order_type='goods' and t.status='paid_success' and t.relatedid=g.recordid);
---------------------------2012-12-28----------------------------------------------------------------------------------------------------------------------------------------
alter table webdata.specialdiscount add extdiscount number(3) default 0 not null;
---------------------------2012-12-12----------------------------------------------------------------------------------------------------------------------------------------
update wala.c_comment  set body = regexp_replace(body,'#null#','\1') where body like '%#null#%';
---------------------------2012-11-23----------------------------------------------------------------------------------------------------------------------------------------
alter table webdata.movielist add constraint uk_movielist_seqno unique(seqno);
create table webdata.his_data(
	dkey varchar2(50),
	jsondata varchar2(1000) not null,
	validtime timestamp(6) not null,
	constraint pk_his_data primary key (dkey)
	using index (create unique index webdata.idxpk_his_data on webdata.his_data(dkey) tablespace tbs_index)
);
---------------------------2012-11-21----------------------------------------------------------------------------------------------------------------------------------------
update webdata.picture_new p set p.membertype='member' 
where p.membertype is null and exists(select * from webdata.member m where m.recordid=p.memberid);

update webdata.picture_new p set membertype='sso' 
where p.membertype is null and exists(select * from webdata.app_user m where m.id=p.memberid);

update webdata.picture_new p set memberid=1,membertype='member' where p.membertype is null;

update webdata.video v set v.membertype='sso' 
where v.membertype is null 
and exists(select * from webdata.app_user a where a.id=v.memberid);
---------------------------2012-11-16----------------------------------------------------------------------------------------------------------------------------------------
alter table webdata.ticket_order add category varchar2(50);
alter table webdata.failure_order add category varchar2(50);
update webdata.ticket_order t set t.category = (select o.opentype from webdata.open_playitem o where o.mpid = t.relatedid)
where exists(select * from webdata.open_playitem o where o.mpid = t.relatedid);

alter table webdata.picture_new add membertype varchar2(10);
alter table webdata.video add membertype varchar2(10);

alter table webdata.treasure modify relatedid number(19);
---------------------------2012-11-09----------------------------------------------------------------------------------------------------------------------------------------
alter table webdata.specialdiscount add bankname varchar2(50);
alter table webdata.specialdiscount add logo varchar2(500);
update webdata.specialdiscount set logo = regexp_replace(remark,'<img src="([^>]+)">.*','\1') where regexp_substr(remark,'<img src="([^>]+)">') is not null and logo is null;
update webdata.specialdiscount set logo = regexp_replace(enableRemark,'<img src="([^>]+)">.*','\1') where regexp_substr(enableRemark,'<img src="([^>]+)">') is not null and logo is null;
update webdata.specialdiscount set remark = regexp_replace(remark,'<img src="([^>]+)">','') where regexp_substr(remark,'<img src="([^>]+)">') is not null;
update webdata.specialdiscount set enableRemark = regexp_replace(enableRemark,'<img src="([^>]+)">','') where regexp_substr(enableRemark,'<img src="([^>]+)">') is not null;
update webdata.specialdiscount set recommendRemark = regexp_replace(recommendRemark,'<img src="([^>]+)">','') where regexp_substr(recommendRemark,'<img src="([^>]+)">') is not null;
------------------------------------------------------------2012-11-06-------------------------------------------------------------------------------------------------------
update webdata.open_playitem o set o.seqno = (select m.seqno from webdata.movielist m where m.recordid=o.mpid) where o.opentype='MTX' and o.seqno is null;
------------------------------------------------------------2012-11-06-------------------------------------------------------------------------------------------------------
update cinemaroom set roomtype=upper(roomtype);
alter table cinemaroom modify roomtype not null;
------------------------------------------------------------2012-11-01-------------------------------------------------------------------------------------------------------
alter table webdata.movielist add opentype varchar2(10);
update webdata.movielist set opentype='HFH' where seqno is not null and seqno not like 'MTX%';
------------------------------------------------------------2012-10-30-------------------------------------------------------------------------------------------------------
alter table webdata.specialdiscount add edition varchar2(100);
alter table webdata.cinemaprofile add opentype varchar2(10);
update webdata.cinemaprofile c set opentype='HFH' where exists(select * from webdata.hfh_cinema h where h.gcid=c.cinemaid);
------------------------------------------------------------2012-10-22-------------------------------------------------------------------------------------------------------
alter table webdata.goods add limitnum number(5) default 0 not null;
alter table webdata.cinemaprofile add direct varchar2(4) default N not null;
update webdata.cinemaprofile c set c.direct ='Y' where exists(select * from hfh_cinema h where h.gcid=c.cinemaid);
alter table webdata.cinemaprofile add prompting varchar2(100);

------------------------------------------------------------2012-10-09-------------------------------------------------------------------------------------------------------
update webdata.picture_new p 
set (p.tag,p.foreignid) = (select 'gym', g.gymid from gymroom g where g.recordid=p.foreignid) 
where p.tag='gymroom';

update webdata.picture_new p 
set (p.tag,p.foreignid) = (select 'sport', s.sportid from sportroom s where s.recordid=p.foreignid) 
where p.tag='sportroom';
------------------------------------------------------------2012-10-18-------------------------------------------------------------------------------------------------------
alter table webdata.picture_new add category varchar2(20);
alter table webdata.picture_new add categoryid number(19);

alter table webdata.video add category varchar2(20);
alter table webdata.video add categoryid number(19);
------------------------------------------------------------2012-09-19-----execute--------------------------------------------------------------------------------------------------
create table webdata.settle_order(
	recordid number(19),
	credentialsid number(19) not null,
	paytime timestamp(6) not null,
	constraint pk_settle_order primary key (recordid)
	using index (create unique index webdata.idxpk_settle_order on webdata.settle_order(recordid) tablespace tbs_index)
);
------------------------------------------------------------2012-09-18-------------------------------------------------------------------------------------------------------
alter table webdata.drama_play_item add dramastarid number(19);
alter table webdata.drama_play_item add remoteid number(19);
------------------------------------------------------------2012-09-14-------------------------------------------------------------------------------------------------------
alter table webdata.goods add manager varchar2(10);
update webdata.goods set manager='user';
alter table webdata.goods modify manager not null;

------------------------------------------------------------2012-09-03-------------------------------------------------------------------------------------------------------
alter table webdata.account_refund add paymethod varchar2(30);
update webdata.account_refund a set a.paymethod=
	(select t.paymethod from webdata.ticket_order t where t.trade_no = a.tradeno)
	where exists(select * from webdata.ticket_order t where t.trade_no = a.tradeno);
alter table webdata.account_refund modify paymethod not null;
------------------------------------------------------------2012-08-27-------------------------------------------------------------------------------------------------------
create table webdata.account_refund(
	recordid number(19),
	tradeno varchar2(30),
	origin varchar2(10) not null,
	reason varchar2(100),
	status varchar2(10) not null,
	amount number(5) default 0 not null,
	memberid number(19),
	mobile varchar2(15),
	dealtime timestamp(6) not null,
	addtime timestamp(6) not null,
	remark varchar2(500),
	applyuser number(19) not null,
	dealuser number(19),
	refund_version number(8) not null,
	CONSTRAINT pk_account_refund PRIMARY key (recordid)
	using index (CREATE UNIQUE INDEX webdata.idxpk_account_refund on webdata.account_refund(recordid) TABLESPACE tbs_index)
);
alter table account_refund modify dealtime null;
------------------------------------------------------------2012-08-22-------------------------------------------------------------------------------------------------------
insert into county(citycode,briefname,countycode,countyname) values('320500','工业园区','320599','工业园区');
insert into gewaconfig(recordid,description,updatetime,newcontent) values(999,'ticket',sysdate,'32369133,12');
------------------------------------------------------------2012-07-31-------------------------------------------------------------------------------------------------------
UPDATE webdata.open_timeitem i set (i.endhour,i.validtime)=(
  select '12:00',t.playdate + 11.5/24
  from webdata.open_timetable t 
  where t.recordid=i.ottid and
        t.sportid=315852 and
        t.playdate>=trunc(sysdate)
)
where i.sportid=315852 
and i.hour='06:30'
and exists(select * from webdata.open_timetable o 
     where o.recordid=i.ottid 
     and o.sportid=315852 
     and o.playdate>=trunc(sysdate)
);
------------------------------------------------------------2012-07-25-------------------------------------------------------------------------------------------------------
insert into county(citycode,briefname,countycode,countyname) values ('330183','富阳','33018301','富阳');
------------------------------------------------------------2012-07-17-------------------------------------------------------------------------------------------------------
create table webdata.cpcounter(
	recordid number(19),
	spcounterid number(19) not null,
	flag varchar2(10) not null,
	cpcode varchar2(500) not null,
	basenum number(10) default 0 not null,
	allownum number(10) default 0 not null,
	limitnum number(10),
	sellquantity number(10) default 0 not null,
	sellorder number(10) default 0 not null,
	addtime timestamp(6) not null,
	updatetime timestamp(6) not null,
	CONSTRAINT uk_cpcounter unique (spcounterid,flag,cpcode)
	 using index ( CREATE UNIQUE index webdata.idxuk_cpcounter on webdata.cpcounter(spcounterid,flag,cpcode) TABLESPACE tbs_index),
	CONSTRAINT pk_cpcounter PRIMARY key (recordid)
	 using index (CREATE UNIQUE INDEX webdata.idxpk_cpcounter on webdata.cpcounter(recordid) TABLESPACE tbs_index)
)TABLESPACE webdata;

update webdata.open_timeitem o set o.validtime=sysdate-1 where o.validtime is null and o.opentype='field';

update webdata.open_timeitem o set o.validtime=(
       select to_date(to_char(t.playdate,'yyyy-mm-dd')|| ' '||o.endhour,'yyyy-mm-dd hh24:mi')-1/1440*o.unitminute 
       from webdata.open_timetable t where t.recordid=o.ottid)
where o.opentype<>'field' and o.unittype='time' and o.validtime is null;

update webdata.open_timeitem o set o.validtime=(
       select to_date(to_char(t.playdate,'yyyy-mm-dd')|| ' '||o.endhour,'yyyy-mm-dd hh24:mi')-1/1440*30 
       from webdata.open_timetable t where t.recordid=o.ottid)
where o.opentype<>'field' and o.unittype='whole' and o.validtime is null;

alter table webdata.open_timeitem modify validtime not null;
alter table webdata.specialdiscount add fieldid varchar2(500);
------------------------------------------------------------2012-07-16-------------------------------------------------------------------------------------------------------
alter table webdata.open_timetable add unitminute number(4);
update webdata.open_timetable set unitminute=60 where opentype='field';
update webdata.open_timetable o set unitminute=(select max(i.unitminute) from webdata.open_timeitem i where i.ottid=o.recordid group by i.ottid)
	where o.opentype<>'field' and exists(select * from webdata.open_timeitem t where t.ottid=o.recordid);
update webdata.open_timetable o set o.unitminute=60  where o.unitminute is null and o.opentype<>'field'; 
alter table webdata.open_timetable modify unitminute not null;
------------------------------------------------------------2012-07-13-------------------------------------------------------------------------------------------------------
alter table webdata.spcounter add opentype varchar2(10);--not null
alter table webdata.spcounter add periodtime timestamp(6);--not null
alter table webdata.spcounter add periodminute number(19);--not null
alter table webdata.spcounter add allquantity number(15) default 0 not null;
alter table webdata.spcounter add allordernum number(15) default 0 not null;
alter table webdata.spcounter add basenum number(15) default 0 not null;
update webdata.spcounter set opentype='whole';
alter table webdata.spcounter modify opentype not null;
update webdata.spcounter sp set sp.allordernum = sp.sellordernum;
update webdata.spcounter sp set sp.allquantity = sp.sellquantity;
------------------------------------------------------------2012-07-11-------------------------------------------------------------------------------------------------------
alter  table webdata.member_usefuladdress add mobile varchar2(15);
alter  table webdata.member_usefuladdress add idcard varchar2(20);
------------------------------------------------------------2012-07-10-------------------------------------------------------------------------------------------------------
update webdata.sell_gym_card s set s.orderid=(SELECT max(t.recordid) as orderid from webdata.ticket_order t where t.movieid=s.recordid and t.order_type='gym' group by t.movieid)
where exists(select t.recordid from ticket_order t where t.movieid=s.recordid and t.order_type='gym');
------------------------------------------------------------2012-06-29-------------------------------------------------------------------------------------------------------
alter table WEBDATA.OPEN_TIMEITEM modify unithour number(6,1);
alter table WEBDATA.OPEN_TIMETABLE modify unithour number(6,1);
alter table WEBDATA.PROGRAM_ITEM_TIME modify unithour number(6,1);
alter table WEBDATA.SELL_TIME_TABLE modify unithour number(6,1);
alter table webdata.sell_time_table MODIFY sumhour NUMBER(6,1);

create index webdata.idx_stt_group on webdata.sell_time_table(ottid,starttime,validtime,status) tablespace tbs_index;
------------------------------------------------------------2012-06-29-------------------------------------------------------------------------------------------------------
select recordid,endhour from open_timeitem oti where oti.endhour like '24:%';
update open_timeitem oti set oti.endhour=replace(endhour,'24:','23:') where oti.endhour like '24:%';

update open_timeitem o set validtime= (
  select to_date(to_char(ott.playdate,'yyyy-mm-dd')||' '||oti.endhour,'yyyy-mm-dd hh24:mi')-1/48
  from open_timetable ott,open_timeitem oti
  where ott.recordid=oti.ottid and oti.unittype='whole' and ott.itemid=288699 and ott.opentype='period'
  and oti.recordid=o.recordid)
where o.unittype='whole' and o.itemid=288699;

update open_timeitem o set validtime= (
  select to_date(to_char(ott.playdate,'yyyy-mm-dd')||' '||oti.endhour,'yyyy-mm-dd hh24:mi')-1/24*oti.unithour
  from open_timetable ott,open_timeitem oti
  where ott.recordid=oti.ottid and oti.unittype='time' and ott.itemid=288699 and ott.opentype='period'
  and oti.recordid=o.recordid)
where o.unittype='time' and o.itemid=288699;

update open_timeitem oti set oti.endhour=replace(endhour,'24:','23:') where oti.endhour like '24:%' and itemid=288699;
update open_timeitem oti set oti.endhour=replace(endhour,'24:00','23:59') where oti.endhour like '24:%' and itemid!=288699;
------------------------------------------------------------2012-06-20-------------------------------------------------------------------------------------------------------
alter table webdata.open_timeitem add endhour varchar2(5) not null;
alter table webdata.open_timeitem add opentype varchar2(30) not null;
alter table webdata.open_timeitem add unithour number(3,1) not null;
alter table webdata.open_timeitem add unittype varchar2(20) not null;
alter table webdata.open_timeitem add minpoint number(5) not null;
alter table webdata.open_timeitem add maxpoint number(5) not null;
alter table webdata.open_timeitem add pratio number(5) not null;
alter table webdata.open_timeitem add elecard varchar2(50);
alter table webdata.open_timeitem add spflag varchar2(50);
alter table webdata.open_timeitem add remark varchar2(50);
alter table webdata.open_timeitem add otherinfo varchar2(1000);
alter table webdata.open_timeitem add quantity number(5) default 1;
alter table webdata.open_timeitem add sales number(5) default 0;

alter table webdata.program_item_time add fieldid number(19,0) not null;
alter table sell_time_table add fieldid number(19,0) not null;

alter table webdata.open_timetable add citycode varchar2(10) not null;
alter table webdata.open_timeitem add citycode varchar2(10) not null;
alter table webdata.sell_time_table add otiid number(19,0) not null;
------------------------------------------------------------2012-06-19-------------------------------------------------------------------------------------------------------
update webdata.sport_sportitem s set (minprice,maxprice,avgprice)=(
  SELECT nvl(t.minprice,5),nvl(t.maxprice,5),nvl(t.avgprice,5)
  FROM （
    select a.sportid as sportid,a.itemid as itemid,
           nvl(a.minprice,b.minprice) as minprice, 
           nvl(a.maxprice,b.maxprice) as maxprice, 
           nvl(a.avgprice,b.avgprice) as avgprice 
    from    
       (select sportid, itemid, max(price) as maxprice, min(price) as minprice,avg(price) as avgprice 
        from   open_timetable   
        group by   sportid,itemid) a 
   left join 
       (select sportid,itemid,max(price) as maxprice,min(price) as minprice,avg(price) as avgprice 
        from   open_timeitem 
        group by   sportid,itemid) b   
   on (a.sportid=b.sportid and   a.itemid=b.itemid) 
       ) t 
   where t.sportid=s.sportid and t.itemid=s.sportitemid)
;

UPDATE webdata.sport_sportitem set minprice=5 where minprice <5 or minprice is null;
UPDATE webdata.sport_sportitem set maxprice=5 where maxprice <5 or maxprice is null;
UPDATE webdata.sport_sportitem set avgprice=5 where avgprice <5 or avgprice is null;
------------------------------------------------------------2012-06-18-------------------------------------------------------------------------------------------------------
alter table sport_sportitem add minprice number(5,0) default 5;
alter table sport_sportitem add maxprice number(5,0) default 5;
alter table sport_sportitem add avgprice number(5,0) default 5;
------------------------------------------------------------2012-06-11-------------------------------------------------------------------------------------------------------
create table webdata.gymcourse_gymlabel(
	recordid number(19,0),
	courseid number(19,0) not null,
	labelid number(19,0) not null,
	labelname varchar2(20) not null,
	CONSTRAINT uk_gymcourse_gymlabel unique (courseid,labelid)
	 using index ( CREATE UNIQUE index webdata.idxuk_gymcourse_gymlabel on webdata.gymcourse_gymlabel(courseid,labelid) TABLESPACE tbs_index),
	CONSTRAINT pk_gymcourse_gymlabel PRIMARY key (recordid)
	 using index (CREATE UNIQUE INDEX webdata.idxpk_gymcourse_gymlabel on webdata.gymcourse_gymlabel(recordid) TABLESPACE tbs_index)
) TABLESPACE webdata;
------------------------------------------------------------2012-06-09-------------------------------------------------------------------------------------------------------
alter table opencard_item add cardtype varchar2(20);
alter table opencard_item add itemtype varchar2(20);
update webdata.opencard_item o set (cardtype,itemtype)= (select cardtype,itemtype from webdata.gymcard_item g where g.recordid=o.gci) where exists(select * from webdata.gymcard_item g where g.recordid=o.gci);
------------------------------------------------------------2012-06-08-------------------------------------------------------------------------------------------------------
alter table gym_special_schedule modify coachid number(19,0) null;
alter table course_booking modify coachid number(19,0) null;
------------------------------------------------------------2012-06-04-------------------------------------------------------------------------------------------------------
alter table SELL_TIME_TABLE modify unithour number(3,1);
alter table SELL_TIME_TABLE modify sumhour number(3,1);

alter table c_comment add pointx varchar2(50);
alter table c_comment add pointy varchar2(50);
alter table c_comment_hist add pointx varchar2(50);
alter table c_comment_hist add pointy varchar2(50);
------------------------------------------------------------2012-06-04-------------------------------------------------------------------------------------------------------
alter table theatre_room add seatmap clob;
------------------------------------------------------------2012-06-01-------------------------------------------------------------------------------------------------------
select roomid as roomid,wm_concat(seatline || ':' ||seatrank) as seatbody from roomseat where initstatus='C' group by roomid order by roomid;
------------------------------------------------------------2012-05-31-------------------------------------------------------------------------------------------------------
alter table open_timetable add unittype varchar2(10);
update open_timetable o set o.unittype=
	(select p.unittype from program_item_time p 
		where p.sportid=o.sportid and p.itemid=o.itemid and p.starttime=o.starttime and p.endtime=o.endtime and p.week=o.week) 
	where exists(
		select p.unittype from program_item_time p 
			where p.sportid=o.sportid and p.itemid=o.itemid and p.starttime=o.starttime and p.endtime=o.endtime and p.week=o.week) 
		and opentype='period';
alter table open_timetable add constraint chk_open_timetable_opentype check (opentype='period' and starttime is not null and endtime is not null and unithour is not null and unittype is not null);
------------------------------------------------------------2012-05-29-------------------------------------------------------------------------------------------------------
alter table program_item_time modify unithour number(3,1);
alter table open_timetable modify unithour number(3,1);
------------------------------------------------------------2012-05-28-------------------------------------------------------------------------------------------------------
insert into webdata.opencard_item_gymcourse(recordid,oci,gci,MEMBERID,courseid,specialid)
select webdata.HIBERNATE_SEQUENCE.NEXTVAL,a.oci,a.gci,a.memberid,a.course,a.specialcourse
from (
select recordid oci,gci,memberid,regexp_substr(courses,'\d+',1,&num) course,regexp_substr(specialcourses,'\d+',1,&num) specialcourse
from opencard_item 
where courses is not null and specialcourses is not null and MEMBERID is not null
) a
where a.course is not null and a.specialcourse is not null;

create table webdata.opencard_item_gymcourse(
       recordid number(19,0),
       oci number(19,0) not null,
       gci number(19,0) not null,
       specialid number(19,0) not null,
       courseid number(19,0) not null,
       memberid number(19,0) not null,
       CONSTRAINT pk_opencard_item_gymcourse PRIMARY KEY(recordid)
         using index (CREATE UNIQUE index idxpk_opencard_item_gymcourse on webdata.opencard_item_gymcourse(recordid) TABLESPACE tbs_index),
       CONSTRAINT uk_opencard_item_gymcourse unique(oci,gci,specialid,courseid,memberid)
         using index (CREATE UNIQUE INDEX idxuk_opencard_item_gymcourse on webdata.opencard_item_gymcourse(oci,gci,specialid,courseid,memberid) TABLESPACE tbs_index)
)
TABLESPACE webdata;
------------------------------------------------------------2012-05-24-------------------------------------------------------------------------------------------------------
update open_timetable set unithour=1 where opentype='period' and unithour is null;
------------------------------------------------------------2012-05-23-------------------------------------------------------------------------------------------------------
alter table webdata.GYMCARD_ITEM_GYMCOURSE DROP CONSTRAINT UK_GYMCARD_ITEM_GYMCOURSE;
ALTER TABLE webdata.GYMCARD_ITEM_GYMCOURSE add CONSTRAINT UK_GYMCARD_ITEM_GYMCOURSE unique(SPECIALID,GCI,COURSEID)
 using INDEX (CREATE UNIQUE INDEX webdata.IDXUK_GYMCARD_ITEM_GYMCOURSE on webdata.GYMCARD_ITEM_GYMCOURSE(SPECIALID,GCI,COURSEID) TABLESPACE tbs_index);

create table webdata.sell_gym_card(
	recordid number(19,0),
	seat_version number(5,0) not null,
	gid number(19,0) not null,
	rgid number(19,0),
	gymid number(19,0) not null,
	orderid number(19,0) not null,
	specialcourses varchar2(1000),
	rspecialcourses varchar2(1000),
	cardtype varchar2(30) not null,
	itemtype varchar2(30) not null,
	validday number(5,0) not null,
	enableday number(5,0) default 0 not null,
	startdate timestamp(6) not null,
	enddate timestamp(6) not null,
	price number(5,0) not null,
	costprice number(5,0) default 0,
	gymprice number(5,0) default 0,
	quantity number(5,0) default 1,
	status varchar2(30) not null,
	validtime timestamp(6) not null,
	remark varchar2(100),
	CONSTRAINT pk_sell_gym_card primary key(recordid)
	  using index (CREATE UNIQUE INDEX webdata.idxpk_sell_gym_card on webdata.sell_gym_card(recordid) TABLESPACE tbs_index)
)TABLESPACE webdata;
 
CREATE global temporary table t_t1(sportid NUMBER(19,0),itemid NUMBER(19,0)) on commit PRESERVE rows;
insert into t_t1
select DISTINCT b.sportid,b.itemid from webdata.open_timetable b
     where not exists (
       select c.recordid from webdata.sport_sportitem c
         where c.sportid=b.sportid and c.sportitemid=b.itemid)
;
insert into webdata.sport_sportitem a(sportid,sportitemid,recordid)
  select b.sportid,b.itemid,HIBERNATE_SEQUENCE.nextval from webdata.t_t1 b;
drop table t_t1; 
------------------------------------------------------------2012-05-21-------------------------------------------------------------------------------------------------------
alter table sport_sportitem add takemethod varchar2(20);
alter table sport_sportitem add notifymsg1 varchar2(200);
alter table sport_sportitem add notifymsg2 varchar2(200);
alter table sport_sportitem add notifymsg3 varchar2(200);
alter table sport_sportitem add overmsg varchar2(200);
alter table sport_sportitem add booking varchar2(10) default 'close';
alter table sport_sportitem add sortnum number(6,0) default 0 not null;
alter table sport_sportitem add limitminutes number(4,0) default 0 not null;
alter table sport_sportitem add exitsreturn char(1);
alter table sport_sportitem add returnminutes number(4,0) default 0;
alter table sport_sportitem add returnmoneytype char(1) default 'A';
alter table sport_sportitem add returnmoney number(6,2) default 0;
alter table sport_sportitem add exitschange char(1);
alter table sport_sportitem add changeminutes number(4,0) default 0;
alter table sport_sportitem add changemoneytype char(1) default 'A';
alter table sport_sportitem add changemoney number(6,2) default 0;
alter table sport_sportitem add diaryid number(19,0);
alter table sport_sportitem add tickettype char(1) default 'A' not null;
alter table sport_sportitem add return_money_min number(4,0) default 0;
alter table sport_sportitem add change_money_min number(4,0) default 0;

update sport_sportitem s set (
s.takemethod,
s.notifymsg1,
s.notifymsg2,
s.notifymsg3,
s.limitminutes,
s.exitsreturn,
s.returnminutes,
s.returnmoneytype,
s.returnmoney,
s.exitschange,
s.changeminutes,
s.changemoneytype,
s.changemoney,
s.diaryid,
s.tickettype,
s.return_money_min,
s.change_money_min,
s.booking)
=(SELECT p.takemethod,
p.notifymsg1,
p.notifymsg2,
p.notifymsg3,
p.limitminutes,
p.exitsreturn,
p.returnminutes,
p.returnmoneytype,
p.returnmoney,
p.exitschange,
p.changeminutes,
p.changemoneytype,
p.changemoney,
p.diaryid,
p.tickettype,
p.return_money_min,
p.change_money_min,
p.booking
from sport_profile p
where p.sportid=s.sportid)
where exists 
 (select p.sportid from sport_profile p
where p.sportid=s.sportid) and s.sportitemid=288732
------------------------------------------------------------2012-05-18-------------------------------------------------------------------------------------------------------
alter table gymcard_item add rspecialcourses varchar2(1000);
alter table opencard_item add rspecialcourses varchar2(1000);
alter table open_timetable add sportprice number(5,0);
alter table sell_time_table add validtime timestamp(6) not null;
alter table sell_time_table add ottid number(19,0) not null;
------------------------------------------------------------2012-05-16-------------------------------------------------------------------------------------------------------
alter table sell_time_table add sumhour number(2,1) not null;
alter table open_timetable modify week number(1,0);
update open_timetable set week = decode(to_number(to_char(playdate,'d'))-1,0,7,to_number(to_char(playdate,'d'))-1);
alter table open_timetable modify week number(1,0) not null;
------------------------------------------------------------2012-05-11--2012-05-15-------------------------------------------------------------------------------------------
alter table sport_sportitem add otherinfo varchar2(1000); 
alter table sportitem add opentype varchar2(30);
update c_comment c set c.generalmark=10 where c.generalmark>10;
update membermark m where m.markvalue=10 where m.markvalue>10;

create table webdata.program_item_time(
       recordid number(19,0),
       itemid number(19,0) not null,
       sportid number(19,0) not null,
       ottid number(19,0) not null,
       price number(5,0) not null,
       costprice number(5,0) default 0,
       sportprice number(5,0) default 0,
       unittype varchar2(10) not null,
       unithour varchar2(10) not null,
       opentype varchar2(30) not null,
       quantity number(5,0),
       starttime varchar2(10) not null,
       endtime varchar2(10) not null,
       week number(1,0) not null,
       addtime timestamp(6),
       CONSTRAINT pk_program_item_time primary key (recordid)
         using index (create unique index webdata.idxpk_program_item_time on webdata.program_item_time(recordid) TABLESPACE tbs_index),
       CONSTRAINT uk_program_item_time unique (itemid,sportid,week,starttime)
         using index (create unique index webdata.idxuk_program_item_time on webdata.program_item_time(itemid,sportid,week,starttime) TABLESPACE tbs_index)
)TABLESPACE webdata;

alter table open_timetable add opentype varchar2(30);
alter table open_timetable add unithour number(2,1);
alter table open_timetable add quantity number(5,0) default 0;
update open_timetable set opentype='field';
alter table open_timetable modify opentype varchar2(30) not null;

create table sell_time_table(
       recordid number(19,0),
       seat_version number(19,0) not null,
       starttime varchar2(10) not null,
       endtime varchar2(10) not null,
       price number(5,0) not null,
       costprice number(5,0) default 0,
       sportprice number(5,0) default 0,
       quantity number(5,0) default 0,
       unithour number(2,1) not null,
       sumhour number(2,1) not null,
       status varchar2(30) not null,
       remark varchar2(100),
       validtime timestamp(6),
       CONSTRAINT pk_sell_time_table primary key (recordid)
         using index (create unique index webdata.idxpk_sell_time_table on webdata.sell_time_table(recordid) TABLESPACE tbs_index)
)TABLESPACE webdata;
------------------------------------------------------------2012-05-09---------------------------------------------------------------------------------------------------------
alter table opencard_item add consumerid number(19,0);
------------------------------------------------------------2012-04-27---------------------------------------------------------------------------------------------------------
alter table course_booking modify status varchar2(30) not null;
------------------------------------------------------------2012-04-26---------------------------------------------------------------------------------------------------------
alter table course_booking add week varchar2(4) not null;
update opencard_item set validDay=0 where validDay is null;
alter table opencard_item modify validDay number(5,0) default 0 not null;
update opencard_item set enableDay=0 where enableDay is null;
alter table opencard_item modify enableDay number(5,0) default 0 not null;
update gymcard_item set validDay=0 where validDay is null;
alter table gymcard_item modify validDay number(5,0) default 0 not null;
update gymcard_item set enableDay=0 where enableDay is null;
alter table gymcard_item modify enableDay number(5,0) default 0 not null;
update gymcard_item set pratio=0 where pratio is null;
alter table webdata.gymcard_item modify pratio number(5,0) default 0 not null;

------------------------------------------------------------2012-04-24---------------------------------------------------------------------------------------------------------
alter table course_booking add scheduleid number(19,0) not null;
------------------------------------------------------------2012-04-20---------------------------------------------------------------------------------------------------------
alter table gym_special_schedule rename column startime to starttime;
alter table gym_special_schedule rename column coacheid to coachid;
alter table gymspecialcourse add briefname varchar2(30);
alter table gymspecialcourse rename column reservationnumber to quantity;
alter table gymcard_item add gymprice number(5,0) default 0;
------------------------------------------------------------2012-04-19---------------------------------------------------------------------------------------------------------
alter table orderorigin add addtime timestamp(6);
update orderorigin o set o.addtime=(select t.addtime from ticket_order t where t.trade_no=o.tradeno); 
alter table gymcard_item add validday number(5,0) default 0;
alter table gymcard_item add enableday number(5,0) default 0;
alter table opencard_item add validday number(5,0) default 0;
alter table opencard_item add enableday number(5,0) default 0;
------------------------------------------------------------2012-04-17---------------------------------------------------------------------------------------------------------
alter table opencard_item add cardno varchar(100);
insert into gym_profile(gymid,booking) select recordid,'N' from gym;
alter table gym add booking varchar2(10);
------------------------------------------------------------2012-04-16---------------------------------------------------------------------------------------------------------
alter table opencard_item add orderid number(19,0);
alter table opencard_item add userid number(19,0);
------------------------------------------------------------2012-04-10---------------------------------------------------------------------------------------------------------
alter table gymcard_item_gymcourse add specialid number(19,0);
alter table gymcard_item add validtime timestamp(6);
------------------------------------------------------------2012-04-05---------------------------------------------------------------------------------------------------------
insert into gewaconfig(recordid,description,updatetime,newcontent) values(81,'pause sport order',sysdate,'2012-04-09 00:00:00');
alter table gymcard_item add alltimes number(5,0) default 0 not null;

update cinema c set c.bpointx = c.pointx, c.bpointy = c.pointy;
update theatre t set c.bpointx = t.pointx, t.bpointy = t.pointy;
update sport s set s.bpointx = s.pointx, s.bpointy = s.pointy;
update bar b set b.bpointx = b.pointx, b.bpointy = b.pointy;
update gym g set g.bpointx = g.pointx, g.bpointy = g.pointy;
update ktv k set k.bpointx = k.pointx, k.bpointy = k.pointy;

------------------------------------------------------------2012-03-30---------------------------------------------------------------------------------------------------------
alter table gymcard_item add hotvalue number(10) default 0;

alter table gymschedule add gymid number(19,0);
alter table gymschedule add logo varchar2(200);
alter table gymschedule add timefrom varchar2(10);
alter table gymschedule add timeto varchar2(10);
alter table gymschedule add quantity number(5,0) default 0;
alter table gymschedule add sales number(5,0) default 0;

update gymschedule g set g.gymid = (select p.gymid from gymprogram p where p.recordid=g.programid) where exists(select p.gymid from gymprogram p where p.recordid=g.programid);

alter table gymspecialcourse add logo varchar2(200);
alter table gymspecialcourse add feature varchar2(200);
alter table gymspecialcourse add classhour number(5,0);
alter table gymspecialcourse add status varchar2(10);
------------------------------------------------------------2012-03-16---------------------------------------------------------------------------------------------------------
delete from mobile_buytimes;
insert into mobile_buytimes(mobile,buytimes,lasttime,order_type) select mobile, count(*), max(addtime), order_type from ticket_order t where t.status='paid_success' group by mobile, order_type;
delete from member_buytimes;
insert into member_buytimes(memberid,buytimes,lasttime,order_type) select memberid, count(*), max(addtime), order_type from ticket_order t where t.status='paid_success' and memberid<50000000 group by memberid,order_type;

---------------------------------------------------------------------------------------------------------------------------------------------------------------------
alter table mobile_buytimes add order_type varchar2(10);
update mobile_buytimes set order_type='ticket';

---------------------------------------------------------------------------------------------------------------------------------------------------------------------
insert into city values('330282','aaaaaaaaaa','aaaaaaaaaa','330000');
insert into county values('330282','aaaaaaaaaa','33028201','aaaaaaaaaa');
insert into county values('330282','aaaaaaaaaa','33028202','aaaaaaaaaa');
insert into county values('330282','aaaaaaaaaa','33028203','aaaaaaaaaa');
insert into county values('330282','aaaaaaaaaa','33028204','aaaaaaaaaa');

insert into county values('320583','aaaaaaaaaa','32058301','aaaaaaaaaa');
insert into county values('320583','aaaaaaaaaa','32058302','aaaaaaaaaa');
insert into county values('320583','aaaaaaaaaa','32058303','aaaaaaaaaa');
--------------------------------------------------------------------------------------------------------------------------------------------------------------------
alter table movieprice add type varchar2(10);

create table webdata.MOVIE_TIERPRICE(
	recordid number(19,0),
	movieid number(19,0) not null,
	type varchar2(10) not null,
	price number(5,0) default 0 not null,
	addtime timestamp
)
tablespace webdata;
alter table webdata.MOVIE_TIERPRICE add constraint pk_MOVIE_TIERPRICE primary key(recordid)
	using index (create unique index idxpk_MOVIE_TIERPRICE on webdata.MOVIE_TIERPRICE(recordid) tablespace tbs_index);
alter table webdata.MOVIE_TIERPRICE add constraint uk_MOVIE_TIERPRICE unique(movieid,type)
	using index (create unique index idxuk_MOVIE_TIERPRICE on webdata.MOVIE_TIERPRICE(movieid,type) tablespace tbs_index);
--------------------------------------------------------------------------------------------------------------------------------------------------------------------
update bindmobile set status='N' where status='n';
update bindmobile set status='Y' where status='y';

update memberinfo set source='app'  where source is null and otherinfo like '%openMember%';
update memberinfo set source='email'  where source is null and (otherinfo not like '%openMember%' or otherinfo is null);

alter table bindmobile add checkcount number(4,0) default 0;
alter table bindmobile modify memberid number(19,0) null;

alter table memberinfo add source varchar2(20);

alter table webdata.member modify email null;
alter table webdata.member add constraint chk_member_emailmobile check (not(email is null and mobile is null));

--------------------------------------------------------------------------------------------------------------------------------------------------------------------

update drama_play_item a set a.partner=(select b.partner from open_drama_item b where b.dpid=a.recordid)
where exists (select b.dpid from open_drama_item b where b.dpid=a.recordid);

alter table webdata.DRAMA_PLAY_ITEM add PARTNER char(1) default 'N' not null;
alter table webdata.DRAMA_PLAY_ITEM modify status varchar2(3);


delete from barkeywordcount b where not exists(select w.recordid from barkeywordcount w where w.recordid=b.recordid and (w.keyword like '%aaaaaaaa%'or w.keyword like '%aaaaaaaa%' or w.keyword like '%aaaaaaaa%' or w.keyword like '%aaaaaaaa%' or w.keyword like '%aaaaaaaa%'));

alter table theatre_seat_price add sales number(5,0) default 0;

update theatre_seat_price t 
set t.sales = (select count(*) from ticket_order o where o.relatedid=t.dpid and o.sno=t.sno and o.unitprice=t.price and o.order_type='drama' and status='paid_success')  
where exists(select o.recordid from ticket_order o where o.relatedid=t.dpid and o.sno=t.sno and o.unitprice=t.price and o.order_type='drama' and status='paid_success');

alter table ticket_order add totalcost number(8,0) default 0;

update ticket_order t set t.totalcost=t.unitprice*t.quantity where t.order_type='drama';

insert into theatre_room(recordid,theatreid,roomname,line_num,rank_num,seat_num,roomnum,logo) 
select r.recordid,m.theatreid,r.name,r.line_num,r.rank_num,r.seat_num,r.num,r.logo
from roomsection r,theatre_room m
where r.roomid=m.recordid;

create table webdata.MEMBER_USEFULADDRESS(
  recordid          number(19,0),
  memberid          number(19,0) not null,
  addtime           timestamp,
  realname          varchar2(30),
  address           varchar2(400),
  postalcode        varchar2(6)
) tablespace webdata;

alter table webdata.MEMBER_USEFULADDRESS add constraint pk_MEMBER_USEFULADDRESS 
  primary key (recordid)
     using index ( create unique index webdata.idxpk_MEMBER_USEFULADDRESS on webdata.MEMBER_USEFULADDRESS(recordid) tablespace tbs_index);

create index webdata.idxpk_MEMBER_USEFULADDR_memid on webdata.MEMBER_USEFULADDRESS(memberid) tablespace tbs_index;
create index webdata.idx_MEMBER_USEFULADDR_addtime on webdata.MEMBER_USEFULADDRESS(addtime) tablespace tbs_index;

update theatre_room_seat t set t.roomid=(select r.recordid from roomsection r where r.roomid=t.roomid and r.sno=t.sno) where exists(select t.recordid from roomsection r where r.roomid=t.roomid and r.sno=t.sno);
------------------------------------2013-05-23---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
alter table webdata.drama_remote_order modify otherinfo varchar2(4000);
alter table webdata.order_note modify description varchar2(4000);


alter table webdata.buyitem modify tag varchar2(20);
update webdata.buyitem b set (b.relatedid,b.tag) = (select o.dpid,'dramaplayitem' from webdata.open_drama_item o where o.recordid=b.relatedid)
where b.tag='odi' and exists(select * from webdata.open_drama_item o where o.recordid=b.relatedid) 
      and exists(select * from webdata.ticket_order t where t.recordid=b.orderid and t.order_type='drama');
      
update webdata.order_note r set r.smallitemtype='dramaplayitem' where exists(select * from webdata.ticket_order t where t.recordid=r.orderid
       and t.order_type='drama') and r.smallitemtype='goods';

update webdata.order_note b set (b.smallitemid,b.smallitemtype) = (select o.dpid,'dramaplayitem' from webdata.open_drama_item o where o.recordid=b.smallitemid)
where b.smallitemtype='odi' and exists(select * from webdata.open_drama_item o where o.recordid=b.smallitemid) 
      and exists(select * from webdata.ticket_order t where t.recordid=b.orderid and t.order_type='drama');
------------------------------------2013-05-27---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
alter table webdata.GPTBS_PROGRAM_PRICE add STADIUMID number(19) not null;
------------------------------------2013-05-29---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
alter table webdata.theatreprofile add notifyremark varchar2(200);
alter table webdata.open_drama_item modify notifyremark varchar2(200);

update webdata.open_drama_item set takemsg = notifyremark where notifyremark is not null;
update webdata.open_drama_item o set o.notifyremark = (select t.notifyremark from webdata.theatreprofile t where t.theatreid=o.theatreid)
 where exists(select * from webdata.theatreprofile t where t.theatreid=o.theatreid);
update webdata.open_drama_item o set o.notifyremark=o.notifymsg1 where expressid is not null;

alter table webdata.theatreprofile modify takemethodp null;
alter table webdata.theatreprofile modify takemethodw null;
alter table webdata.theatreprofile modify takemethoda null;
alter table webdata.theatreprofile modify takemethode null;
alter table webdata.theatreprofile modify takemsgp null;
alter table webdata.theatreprofile modify takemsgw null;
alter table webdata.theatreprofile modify takemsga null;
alter table webdata.theatreprofile modify takemsge null;

update webdata.theatreprofile set takemethod='A' where takemethod not in ('E','A');
update webdata.open_drama_item set takemethod='E' where expressid is not null and takemethod not like '%A%';
update webdata.open_drama_item set takemethod='A' where expressid is null;

alter table webdata.gptbs_place_field_area modify hotzone varchar2(2000);
alter table webdata.show_area modify hotzone varchar2(2000);
alter table webdata.theatre_room modify hotzone varchar2(2000);
alter table webdata.theatre_seat_area modify hotzone varchar2(2000);
alter table webdata.FIELD_AREA modify hotzone varchar2(2000);
------------------------------------2013-05-29---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------