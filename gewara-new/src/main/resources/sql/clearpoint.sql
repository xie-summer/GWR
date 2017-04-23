----------------------2013-01-14------------------------------
insert into point_hist select RECORDID, TO_MEMBERID, TAG, TAGID, POINTVALUE, REASON, ADMINID, ADDTIME, UNIQUETAG, STAT_FLAG 
from point where addtime<to_date('2012-01-01', 'yyyy-mm-dd');--1031156
delete from point where addtime<to_date('2012-01-01', 'yyyy-mm-dd');

insert into point_hist select RECORDID, TO_MEMBERID, TAG, TAGID, POINTVALUE, REASON, ADMINID, ADDTIME, UNIQUETAG, STAT_FLAG 
from point where addtime<to_date('2012-02-01', 'yyyy-mm-dd');--331927
delete from point where addtime<to_date('2012-02-01', 'yyyy-mm-dd');

insert into point_hist select RECORDID, TO_MEMBERID, TAG, TAGID, POINTVALUE, REASON, ADMINID, ADDTIME, UNIQUETAG, STAT_FLAG 
from point where addtime<to_date('2012-03-01', 'yyyy-mm-dd');--436103
delete from point where addtime<to_date('2012-02-15', 'yyyy-mm-dd');
delete from point where addtime<to_date('2012-03-01', 'yyyy-mm-dd');

insert into point_hist select RECORDID, TO_MEMBERID, TAG, TAGID, POINTVALUE, REASON, ADMINID, ADDTIME, UNIQUETAG, STAT_FLAG 
from point where addtime<to_date('2012-04-01', 'yyyy-mm-dd');--357487
delete from point where addtime<to_date('2012-03-15', 'yyyy-mm-dd');
delete from point where addtime<to_date('2012-04-01', 'yyyy-mm-dd');

insert into point_hist select RECORDID, TO_MEMBERID, TAG, TAGID, POINTVALUE, REASON, ADMINID, ADDTIME, UNIQUETAG, STAT_FLAG 
from point where addtime<to_date('2012-05-01', 'yyyy-mm-dd');--558873
delete from point where addtime<to_date('2012-04-15', 'yyyy-mm-dd');
delete from point where addtime<to_date('2012-05-01', 'yyyy-mm-dd');

insert into point_hist select RECORDID, TO_MEMBERID, TAG, TAGID, POINTVALUE, REASON, ADMINID, ADDTIME, UNIQUETAG, STAT_FLAG 
from point where addtime<to_date('2012-06-01', 'yyyy-mm-dd');--616931
delete from point where addtime<to_date('2012-05-15', 'yyyy-mm-dd');
delete from point where addtime<to_date('2012-06-01', 'yyyy-mm-dd');

insert into point_hist select RECORDID, TO_MEMBERID, TAG, TAGID, POINTVALUE, REASON, ADMINID, ADDTIME, UNIQUETAG, STAT_FLAG 
from point where addtime<to_date('2012-07-01', 'yyyy-mm-dd');--619891
delete from point where addtime<to_date('2012-06-15', 'yyyy-mm-dd');
delete from point where addtime<to_date('2012-07-01', 'yyyy-mm-dd');

insert into point_hist select RECORDID, TO_MEMBERID, TAG, TAGID, POINTVALUE, REASON, ADMINID, ADDTIME, UNIQUETAG, STAT_FLAG 
from point where addtime<to_date('2012-08-01', 'yyyy-mm-dd');
delete from point where addtime<to_date('2012-07-10', 'yyyy-mm-dd');
delete from point where addtime<to_date('2012-07-20', 'yyyy-mm-dd');
delete from point where addtime<to_date('2012-08-01', 'yyyy-mm-dd');

insert into point_hist select RECORDID, TO_MEMBERID, TAG, TAGID, POINTVALUE, REASON, ADMINID, ADDTIME, UNIQUETAG, STAT_FLAG 
from point where addtime<to_date('2012-09-01', 'yyyy-mm-dd');
delete from point where addtime<to_date('2012-08-10', 'yyyy-mm-dd');
delete from point where addtime<to_date('2012-08-20', 'yyyy-mm-dd');
delete from point where addtime<to_date('2012-09-01', 'yyyy-mm-dd');

insert into point_hist select RECORDID, TO_MEMBERID, TAG, TAGID, POINTVALUE, REASON, ADMINID, ADDTIME, UNIQUETAG, STAT_FLAG 
from point where addtime<to_date('2012-10-01', 'yyyy-mm-dd');
delete from point where addtime<to_date('2012-09-10', 'yyyy-mm-dd');
delete from point where addtime<to_date('2012-09-20', 'yyyy-mm-dd');
delete from point where addtime<to_date('2012-10-01', 'yyyy-mm-dd');

---------------2013-11-06--------------------------------------------
---------------before point:17668821, point_hist:8462134-------------
insert into point_hist select RECORDID, TO_MEMBERID, TAG, TAGID, POINTVALUE, REASON, ADMINID, ADDTIME, UNIQUETAG, STAT_FLAG
from point where addtime<to_date('2012-11-01', 'yyyy-mm-dd');--896458 rows created.
delete from point where addtime<to_date('2012-10-10', 'yyyy-mm-dd');--274875 rows deleted.
delete from point where addtime<to_date('2012-10-20', 'yyyy-mm-dd');--275184 rows deleted.
delete from point where addtime<to_date('2012-11-01', 'yyyy-mm-dd');--346399 rows deleted.

insert into point_hist select RECORDID, TO_MEMBERID, TAG, TAGID, POINTVALUE, REASON, ADMINID, ADDTIME, UNIQUETAG, STAT_FLAG
from point where addtime<to_date('2012-12-01', 'yyyy-mm-dd');--1000153
delete from point where addtime<to_date('2012-11-10', 'yyyy-mm-dd');--268030 rows deleted.
delete from point where addtime<to_date('2012-12-01', 'yyyy-mm-dd');--732123

insert into point_hist select RECORDID, TO_MEMBERID, TAG, TAGID, POINTVALUE, REASON, ADMINID, ADDTIME, UNIQUETAG, STAT_FLAG
from point where addtime<to_date('2013-01-01', 'yyyy-mm-dd');--1290910
delete from point where addtime<to_date('2012-12-06', 'yyyy-mm-dd');--187265
delete from point where addtime<to_date('2012-12-16', 'yyyy-mm-dd');--380939
delete from point where addtime<to_date('2012-12-21', 'yyyy-mm-dd');--220928
delete from point where addtime<to_date('2012-12-26', 'yyyy-mm-dd');--248908
delete from point where addtime<to_date('2013-01-01', 'yyyy-mm-dd');--252870

insert into point_hist select RECORDID, TO_MEMBERID, TAG, TAGID, POINTVALUE, REASON, ADMINID, ADDTIME, UNIQUETAG, STAT_FLAG
from point where addtime<to_date('2013-02-01', 'yyyy-mm-dd');--1050353
delete from point where addtime<to_date('2013-01-11', 'yyyy-mm-dd');--315275
delete from point where addtime<to_date('2013-01-21', 'yyyy-mm-dd');--342929
delete from point where addtime<to_date('2013-02-01', 'yyyy-mm-dd');--392149

insert into point_hist select RECORDID, TO_MEMBERID, TAG, TAGID, POINTVALUE, REASON, ADMINID, ADDTIME, UNIQUETAG, STAT_FLAG
from point where addtime<to_date('2013-03-01', 'yyyy-mm-dd');--1027704
delete from point where addtime<to_date('2013-02-11', 'yyyy-mm-dd');--319063
delete from point where addtime<to_date('2013-02-21', 'yyyy-mm-dd');--391009
delete from point where addtime<to_date('2013-03-01', 'yyyy-mm-dd');--317632

create table point_dayidx as 
select to_char(addtime,'yyyy-mm-dd') as adddate, min(recordid) as minid, max(recordid) as maxid, count(*) as totalnum from webdata.point_hist group by to_char(addtime,'yyyy-mm-dd');
---------2013-11-11-------------------------------------------------------------
create table pointhis130301 as select * from point where addtime<to_date('2013-03-02','yyyy-mm-dd');--44181
delete from point where addtime<to_date('2013-03-02','yyyy-mm-dd');--44181

insert into pointhis130301 select * from point where addtime<to_date('2013-04-01', 'yyyy-mm-dd');--1190910
delete from point where addtime<to_date('2013-03-11', 'yyyy-mm-dd');--323510
delete from point where addtime<to_date('2013-04-01', 'yyyy-mm-dd');--867400

insert into pointhis130301 select * from point where addtime<to_date('2013-05-01', 'yyyy-mm-dd');--1429109
delete from point where addtime<to_date('2013-04-16', 'yyyy-mm-dd');--638579
delete from point where addtime<to_date('2013-05-01', 'yyyy-mm-dd');--790530



