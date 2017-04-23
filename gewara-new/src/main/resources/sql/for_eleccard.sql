alter table ELECCARD_BATCH modify ADDTIME null;
alter table ELECCARD_BATCH modify ADDUSERID null;
alter table ELECCARD_BATCH modify MERCHANTID null;

create table ELECCARD_EXTRA as select 
   RECORDID AS BATCHID, PID, CHANNEL,ISSUERID,ADDTIME,ADDUSERID,SOLDTIME,SELLERID,SELLPRICE,
   CATEGORY1,CATEGORY2,APPLYCITY,APPLYDEPT,APPLYTYPE,MERCHANTID 
FROM ELECCARD_BATCH;
alter table ELECCARD_EXTRA add CARDCOUNT NUMBER(10);
alter table ELECCARD_EXTRA add USEDCOUNT NUMBER(10);
alter table ELECCARD_EXTRA add DELCOUNT NUMBER(10);
alter table ELECCARD_EXTRA add NEWCOUNT NUMBER(11);
alter table ELECCARD_EXTRA add SOLDCOUNT NUMBER(11);
alter table ELECCARD_EXTRA add MINCARDNO VARCHAR2(15);
alter table ELECCARD_EXTRA add MAXCARDNO VARCHAR2(15);
alter table ELECCARD_EXTRA add STATSTIME TIMESTAMP;
alter table ELECCARD_EXTRA modify MINCARDNO VARCHAR2(20);
alter table ELECCARD_EXTRA modify MAXCARDNO VARCHAR2(20);
alter table ELECCARD_EXTRA add STATUS varchar2(10);

create table ELECCARD_HIS_STATUS AS 
SELECT BATCHID, COUNT(*) AS CARDCOUNT, MAX(CARDNO) AS MAXCARDNO, MIN(CARDNO) AS MINCARDNO,
	SUM(CASE STATUS WHEN 'N' THEN 1 ELSE 0 END) AS NEWCOUNT, 
	SUM(CASE STATUS WHEN 'Y' THEN 1 ELSE 0 END) AS SOLDCOUNT, 
	SUM(CASE STATUS WHEN 'D' THEN 1 ELSE 0 END) AS DELCOUNT, 
	SUM(CASE STATUS WHEN 'U' THEN 1 ELSE 0 END) AS USEDCOUNT,
	SUM(CASE STATUS WHEN 'L' THEN 1 ELSE 0 END) AS LOCKCOUNT
FROM ELECCARD_HIST GROUP BY BATCHID;

update eleccard_extra set status='data_now' where status is null;

insert into ELECCARD_EXTRA s (batchid,pid,channel,issuerid,addtime,adduserid,soldtime,sellerid,sellprice,
category1,category2,applycity,applydept,applytype,merchantid) (
select RECORDID AS BATCHID, PID, CHANNEL,ISSUERID,ADDTIME,ADDUSERID,SOLDTIME,SELLERID,SELLPRICE,
   CATEGORY1,CATEGORY2,APPLYCITY,APPLYDEPT,APPLYTYPE,MERCHANTID 
FROM ELECCARD_BATCH t where t.recordid not in (select m.batchid from eleccard_extra m)
);
-------------------20120922-----------------------------------------------------------------------------------
update eleccard_extra s set status='data_his' where exists (select recordid from eleccard_batch t where t.recordid=s.batchid and t.timeto<sysdate - 30 and t.daynum=0) and s.status !='data_his';
--1157 rows updated.

select count(*) from eleccard where batchid in (select batchid from eleccard_extra s where s.status ='data_his')
--2152830
insert into eleccard_hist select * from eleccard where batchid in (select batchid from eleccard_extra s where s.status ='data_his');
--2152830 rows created.
--eleccard count:4745922, after delete:2593092
delete from eleccard where batchid in (select batchid from eleccard_extra s where s.status ='data_his') and rownum < 100000;
--99999*4,200000*8,152834

delete from ELECCARD_HIS_STATUS;
insert into ELECCARD_HIS_STATUS
SELECT BATCHID, COUNT(*) AS CARDCOUNT, MAX(CARDNO) AS MAXCARDNO, MIN(CARDNO) AS MINCARDNO,
	SUM(CASE STATUS WHEN 'N' THEN 1 ELSE 0 END) AS NEWCOUNT, 
	SUM(CASE STATUS WHEN 'Y' THEN 1 ELSE 0 END) AS SOLDCOUNT, 
	SUM(CASE STATUS WHEN 'D' THEN 1 ELSE 0 END) AS DELCOUNT, 
	SUM(CASE STATUS WHEN 'U' THEN 1 ELSE 0 END) AS USEDCOUNT,
	SUM(CASE STATUS WHEN 'L' THEN 1 ELSE 0 END) AS LOCKCOUNT
FROM ELECCARD_HIST GROUP BY BATCHID;
---注意部分未开户的券不要更新：status=N-----
update eleccard_extra s set status='data_now' where batchid=41339559;
select count(*) from eleccard where batchid=41339559
insert into eleccard select * from eleccard_hist where batchid=41339559;
delete eleccard_hist where batchid=41339559;
delete ELECCARD_HIS_STATUS where batchid=41339559;

insert into eleccard select * from eleccard_hist where batchid=46205456;
delete eleccard_hist where batchid=46205456;
delete ELECCARD_HIS_STATUS where batchid=46205456;
update eleccard_extra set status='data_now' where  batchid=46205456;

---2012-11-30---
insert into eleccard select * from eleccard_hist where batchid=46419545;
delete eleccard_hist where batchid=46419545;
delete ELECCARD_HIS_STATUS where batchid=46419545;
update eleccard_extra set status='data_now' where  batchid=46419545;

---2012-12-17---
insert into eleccard select * from eleccard_hist where batchid=44177924 and cardno>='G1107141555005174' and cardno<='G1107141555005200';
delete eleccard_hist where batchid=44177924 and cardno>='G1107141555005174' and cardno<='G1107141555005200';
delete ELECCARD_HIS_STATUS where batchid=44177924;
insert into ELECCARD_HIS_STATUS
SELECT BATCHID, COUNT(*) AS CARDCOUNT, MAX(CARDNO) AS MAXCARDNO, MIN(CARDNO) AS MINCARDNO,
	SUM(CASE STATUS WHEN 'N' THEN 1 ELSE 0 END) AS NEWCOUNT, 
	SUM(CASE STATUS WHEN 'Y' THEN 1 ELSE 0 END) AS SOLDCOUNT, 
	SUM(CASE STATUS WHEN 'D' THEN 1 ELSE 0 END) AS DELCOUNT, 
	SUM(CASE STATUS WHEN 'U' THEN 1 ELSE 0 END) AS USEDCOUNT,
	SUM(CASE STATUS WHEN 'L' THEN 1 ELSE 0 END) AS LOCKCOUNT,
	SUM(NVL2(GAINER, 1, 0)) AS ISSUECOUNT
FROM ELECCARD_HIST where batchid=44177924 GROUP BY BATCHID;
update eleccard_extra set status='data_now' where  batchid=44177924;

---2012-12-24---
insert into eleccard select * from eleccard_hist where batchid=43392337;
delete eleccard_hist where batchid=43392337;
delete ELECCARD_HIS_STATUS where batchid=43392337;
update eleccard_extra set status='data_now' where  batchid=43392337;  
---2013-01-17---
update eleccard_extra set STATUS='data_now' where batchid=39829157;
insert into eleccard select * from eleccard_hist where batchid=39829157;
delete eleccard_hist where batchid=39829157;
delete ELECCARD_HIS_STATUS where batchid=39829157;

update eleccard_extra set STATUS='data_now' where batchid=79933161;
---2013-01-18---
update eleccard set status='Y' where status='N' and (cardno>='G1212202143153401' and cardno<='G1212202143153418' or cardno>='G1212202143157836' and cardno<='G1212202143157861' or cardno>='G1212202143157687' and cardno<='G1212202143157698' or cardno>='G1212202143157771' and cardno<='G1212202143157782');
---2013-02-06---
update eleccard_extra set STATUS='data_now' where batchid=39828722;
insert into eleccard select * from eleccard_hist where batchid=39828722;
delete eleccard_hist where batchid=39828722;
update eleccard_batch set timeto=to_date('2013-12-31','yyyy-mm-dd') where batchid=39828722;
---2013-03-11---
update eleccard_extra set STATUS='data_now' where batchid=37059116;
insert into eleccard select * from eleccard_hist where batchid=37059116;
delete eleccard_hist where batchid=37059116;
delete ELECCARD_HIS_STATUS where batchid=37059116;
---2013-03-12---
update eleccard_extra set STATUS='data_now' where batchid=46205460;
insert into eleccard select * from eleccard_hist where batchid=46205460;
delete eleccard_hist where batchid=46205460;
delete ELECCARD_HIS_STATUS where batchid=46205460;

------2013-04-09-------------------------
----电子券同一批次多种类型（运营改过）-----
/*
RECORDID	cur	old	genid
33144569	D	A	33144568
42571165	D	A	42571164
43186287	D	A	43186286
43187337	D	A	43187336
43187493	D	A	43187492
49748067	D	A	49748066
56833272	B	A	56833271
57793508	B	A	57793507
57794556	B	A	57794555
57797896	A	B	57797895
58038630	B	A	58038629
61141482	A	D	61141481
62132105	A	B	62132104
*/
insert into webdata.eleccard_batch(RECORDID, PID, PRICE, REMARK, WEEKTYPE, VALIDCINEMA, TIMEFROM, TIMETO, CARDTYPE, VALIDMOVIE, VALIDITEM, NOTIFYMSG, DAYNUM, TAG, CITYCODE, OPENTIME, CLOSETIME, VALIDPARTNER, BINDPAY, BINDGOODS, BINDRATIO, COSTTYPE, COSTNUM, ADDTIME1, ADDTIME2, ADDWEEK, CITYPATTERN, EDITION, LIMITDESC, ACTIVATION, EXCHANGETYPE, CHANNELINFO, APPOINT, COSTNUM3D) 
select RECORDID-1, PID, PRICE, REMARK, WEEKTYPE, VALIDCINEMA, TIMEFROM, TIMETO, CARDTYPE, VALIDMOVIE, VALIDITEM, NOTIFYMSG, DAYNUM, TAG, CITYCODE, OPENTIME, CLOSETIME, VALIDPARTNER, BINDPAY, BINDGOODS, BINDRATIO, COSTTYPE, COSTNUM, ADDTIME1, ADDTIME2, ADDWEEK, CITYPATTERN, EDITION, LIMITDESC, ACTIVATION, EXCHANGETYPE, CHANNELINFO, APPOINT, COSTNUM3D
from webdata.eleccard_batch
where recordid in (49748067,42571165,33144569,43187493,57794556,61141482,62132105,58038630,57797896,56833272,57793508,43187337,43186287);

insert into webdata.eleccard_extra(BATCHID, PID, CHANNEL, ISSUERID, ADDTIME, ADDUSERID, SOLDTIME, SELLERID, SELLPRICE, CATEGORY1, CATEGORY2, APPLYCITY, APPLYDEPT, APPLYTYPE, MERCHANTID, CARDCOUNT, USEDCOUNT, DELCOUNT, NEWCOUNT, SOLDCOUNT, MINCARDNO, MAXCARDNO, STATSTIME, STATUS , LOCKCOUNT, ISSUECOUNT)
select batchid-1, PID, CHANNEL, ISSUERID, ADDTIME, ADDUSERID, SOLDTIME, SELLERID, SELLPRICE, CATEGORY1, CATEGORY2, APPLYCITY, APPLYDEPT, APPLYTYPE, MERCHANTID, CARDCOUNT, USEDCOUNT, DELCOUNT, NEWCOUNT, SOLDCOUNT, MINCARDNO, MAXCARDNO, STATSTIME, STATUS , LOCKCOUNT, ISSUECOUNT 
from webdata.eleccard_extra
where BATCHID in (49748067,42571165,33144569,43187493,57794556,61141482,62132105,58038630,57797896,56833272,57793508,43187337,43186287);

update eleccard_batch set cardtype='A' WHERE RECORDID=33144568;
update eleccard_batch set cardtype='A' WHERE RECORDID=42571164;
update eleccard_batch set cardtype='A' WHERE RECORDID=43186286;
update eleccard_batch set cardtype='A' WHERE RECORDID=43187336;
update eleccard_batch set cardtype='A' WHERE RECORDID=43187492;
update eleccard_batch set cardtype='A' WHERE RECORDID=49748066;
update eleccard_batch set cardtype='A' WHERE RECORDID=56833271;
update eleccard_batch set cardtype='A' WHERE RECORDID=57793507;
update eleccard_batch set cardtype='A' WHERE RECORDID=57794555;
update eleccard_batch set cardtype='B' WHERE RECORDID=57797895;
update eleccard_batch set cardtype='A' WHERE RECORDID=58038629;
update eleccard_batch set cardtype='D' WHERE RECORDID=61141481;
update eleccard_batch set cardtype='B' WHERE RECORDID=62132104;
---------------------------------------------------------------------
update webdata.eleccard_hist set batchid=33144568 where recordid in (select RELATEDID from WEBDATA.DISCOUNT_ITEM where batchid=33144568);--6
update webdata.eleccard_hist set batchid=42571164 where recordid in (select RELATEDID from WEBDATA.DISCOUNT_ITEM where batchid=42571164);--2
update webdata.eleccard_hist set batchid=43186286 where recordid in (select RELATEDID from WEBDATA.DISCOUNT_ITEM where batchid=43186286);--6
update webdata.eleccard_hist set batchid=43187336 where recordid in (select RELATEDID from WEBDATA.DISCOUNT_ITEM where batchid=43187336);--11
update webdata.eleccard_hist set batchid=43187492 where recordid in (select RELATEDID from WEBDATA.DISCOUNT_ITEM where batchid=43187492);--7
update webdata.eleccard_hist set batchid=49748066 where recordid in (select RELATEDID from WEBDATA.DISCOUNT_ITEM where batchid=49748066);--4
update webdata.eleccard_hist set batchid=56833271 where recordid in (select RELATEDID from WEBDATA.DISCOUNT_ITEM where batchid=56833271);--14972
update webdata.eleccard_hist set batchid=57793507 where recordid in (select RELATEDID from WEBDATA.DISCOUNT_ITEM where batchid=57793507);--14616
update webdata.eleccard_hist set batchid=57794555 where recordid in (select RELATEDID from WEBDATA.DISCOUNT_ITEM where batchid=57794555);--12365
update webdata.eleccard_hist set batchid=57797895 where recordid in (select RELATEDID from WEBDATA.DISCOUNT_ITEM where batchid=57797895);--59
update webdata.eleccard_hist set batchid=58038629 where recordid in (select RELATEDID from WEBDATA.DISCOUNT_ITEM where batchid=58038629);--0
update webdata.eleccard_hist set batchid=61141481 where recordid in (select RELATEDID from WEBDATA.DISCOUNT_ITEM where batchid=61141481);--1
update webdata.eleccard_hist set batchid=62132104 where recordid in (select RELATEDID from WEBDATA.DISCOUNT_ITEM where batchid=62132104);--2
----------------------------------------------------------------------
update webdata.eleccard set batchid=58038629 where recordid in (select RELATEDID from WEBDATA.DISCOUNT_ITEM where batchid=58038629);--66
--其他为0-----
delete ELECCARD_HIS_STATUS 
insert into ELECCARD_HIS_STATUS
SELECT BATCHID, COUNT(*) AS CARDCOUNT, MAX(CARDNO) AS MAXCARDNO, MIN(CARDNO) AS MINCARDNO,
	SUM(CASE STATUS WHEN 'N' THEN 1 ELSE 0 END) AS NEWCOUNT, 
	SUM(CASE STATUS WHEN 'Y' THEN 1 ELSE 0 END) AS SOLDCOUNT, 
	SUM(CASE STATUS WHEN 'D' THEN 1 ELSE 0 END) AS DELCOUNT, 
	SUM(CASE STATUS WHEN 'U' THEN 1 ELSE 0 END) AS USEDCOUNT,
	SUM(CASE STATUS WHEN 'L' THEN 1 ELSE 0 END) AS LOCKCOUNT,
	SUM(NVL2(GAINER, 1, 0)) AS ISSUECOUNT
FROM ELECCARD_HIST GROUP BY BATCHID;
--做一次统计------
update eleccard_extra set status='data_now' where batchid in (33144568,42571164,43186286,43187336,43187492,49748066,56833271,57793507,57794555,57797895,61141481,62132104);
--完成统计后恢复--
update eleccard_extra set status='data_his' where batchid in (33144568,42571164,43186286,43187336,43187492,49748066,56833271,57793507,57794555,57797895,61141481,62132104);



update WEBDATA.DISCOUNT_ITEM set batchid=batchid-1 where 
batchid=33144569 and cardtype='A' or
batchid=42571165 and cardtype='A' or
batchid=43186287 and cardtype='A' or
batchid=43187337 and cardtype='A' or
batchid=43187493 and cardtype='A' or
batchid=49748067 and cardtype='A' or
batchid=56833272 and cardtype='A' or
batchid=57793508 and cardtype='A' or
batchid=57794556 and cardtype='A' or
batchid=57797896 and cardtype='B' or
batchid=58038630 and cardtype='A' or
batchid=61141482 and cardtype='D' or
batchid=62132105 and cardtype='B';
--42122 rows updated.
update eleccard_batch set timeto = to_date('2010-11-10 23:59:00','yyyy-mm-dd HH24:mi:ss') where recordid=25188691;
-----2013-04-15-----
update eleccard_extra set status='data_now' where batchid =39827861;
update eleccard set status='Y' where status='D' and cardno in ('G1205251649078435','G1205251649078436','G1205251649078437')

-----2013-05-02--刘彬---
update eleccard_extra set status='data_now' where batchid =39827861;
insert into eleccard select * from eleccard_hist where batchid=39827861;
delete eleccard_hist where batchid=39827861;
delete ELECCARD_HIS_STATUS where batchid=39827861;

update eleccard_extra set status='data_now' where batchid =59892902;
insert into eleccard select * from eleccard_hist where batchid=59892902;
delete eleccard_hist where batchid=59892902;
delete ELECCARD_HIS_STATUS where batchid=59892902;
----2013-05-06----
update eleccard_extra set status='data_now' where batchid =53154328;
insert into eleccard select * from eleccard_hist where batchid=53154328;
delete eleccard_hist where batchid=53154328;
delete ELECCARD_HIS_STATUS where batchid=53154328;

update eleccard_extra set status='data_now' where batchid =51876606;
insert into eleccard select * from eleccard_hist where batchid=51876606;
delete eleccard_hist where batchid=51876606;
delete ELECCARD_HIS_STATUS where batchid=51876606;

update eleccard_extra set status='data_now' where batchid =43698878;
insert into eleccard select * from eleccard_hist where batchid=43698878;
delete eleccard_hist where batchid=43698878;
delete ELECCARD_HIS_STATUS where batchid=43698878;

update eleccard_extra set status='data_now' where batchid =40266672;
insert into eleccard select * from eleccard_hist where batchid=40266672;
delete eleccard_hist where batchid=40266672;
delete ELECCARD_HIS_STATUS where batchid=40266672;

--解冻45433342  G1107141734104001~G1107141734104400
select count(1) from eleccard where batchid=45433342;
46419545
select batchid,cardno from eleccard where cardno >='G1107141734104001' and cardno<='G1107141734104400';

create table scalper_cardbatch as  
select batchid, possessor, count(1) as total from veleccard 
where possessor is not null group by batchid,possessor having count(1) >= 8;

grant select on scalper_cardbatch to shanghai;

  