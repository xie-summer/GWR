create table carddiscount_stat(
 BATCHID        NUMBER(19) NOT NULL,
 CINEMAID       NUMBER(19),
 MOVIEID        NUMBER(19),
 OPENDATE       VARCHAR2(10),
 ORDERCOUNT     NUMBER(10),
 QUANTITY       NUMBER(10),
 TOTALCOST      NUMBER(10),
 TOTALAMOUNT    NUMBER(10),
 TOTALDISCOUNT  NUMBER(10),
 CARDCOUNT      NUMBER(10),
 CARDDISCOUNT   NUMBER(10),
 CARDCOST       NUMBER(10),
 ACOUNT         NUMBER(10),
 AAMOUNT        NUMBER(10),
 BCOUNT         NUMBER(10),
 BAMOUNT        NUMBER(10),
 CCOUNT         NUMBER(10),
 CAMOUNT        NUMBER(10),
 DCOUNT         NUMBER(10),
 DAMOUNT        NUMBER(10)
) tablespace webdata;

-------------------------------------
to_char(m.opentime,'yyyy-mm-dd') opendate,--放映日期 
count(distinct m.mpid) 场次数量, 
sum(t.quantity) 总出票量, 
sum(t.costprice*t.quantity) 总成本, 
sum(t.amount) 总销售额,
sum(t.discount) 总折扣,
sum(s.cardcount) 卡数量,
sum(s.amount) 卡金额,

----建表，2011年前的数据------
insert into carddiscount_stat  
select s.batchid, t.cinemaid, t.movieid, to_char(m.opentime,'yyyy-mm-dd') opendate
count(t.recordid) ordercount,
sum(t.quantity) quantity, 
sum(t.costprice*t.quantity) totalcost, 
sum(t.amount) totalamount,
sum(t.discount) totaldiscount,
sum(s.cardcount) cardcount,
sum(s.amount) carddiscount,
sum(t.costprice * s.cardcount) cardcost,
sum(decode(s.cardtype,'A',1,0)) Acount,
sum(decode(s.cardtype,'A',s.amount,0)) Aamount,
sum(decode(s.cardtype,'B',1,0)) Bcount,
sum(decode(s.cardtype,'B',s.amount,0)) Bamount,
sum(decode(s.cardtype,'C',1,0)) Ccount,
sum(decode(s.cardtype,'C',s.amount,0)) Camount,
sum(decode(s.cardtype,'D',1,0)) Dcount,
sum(decode(s.cardtype,'D',s.amount,0)) Damount
from ticket_order t inner join open_playitem m on t.relatedid=m.mpid inner join (
  select ds.orderid, c.batchid, ds.cardtype, count(ds.recordid) as cardcount, sum(ds.amount) as amount 
  from discount_item ds inner join eleccard_hist c on c.recordid=ds.relatedid 
  where ds.tag='ecard' group by ds.orderid, c.batchid, ds.cardtype
) s on t.recordid=s.orderid 
where t.order_type='ticket' and t.discount > 0 and t.status='paid_success' and  m.opentime< to_date('2011-01-01','yyyy-MM-dd')
group by s.batchid, t.cinemaid, t.movieid, to_char(m.opentime,'yyyy-mm-dd');

---[20110101~20110401)----
insert into carddiscount_stat ( 
select s.batchid, t.cinemaid, t.movieid, to_char(m.opentime,'yyyy-mm-dd') opendate, 
count(t.recordid) ordercount,
sum(t.quantity) quantity, 
sum(t.costprice*t.quantity) totalcost, 
sum(t.amount) totalamount,
sum(t.discount) totaldiscount,
sum(s.cardcount) cardcount,
sum(s.amount) carddiscount,
sum(t.costprice * s.cardcount) cardcost,
sum(decode(s.cardtype,'A',1,0)) Acount,
sum(decode(s.cardtype,'A',s.amount,0)) Aamount,
sum(decode(s.cardtype,'B',1,0)) Bcount,
sum(decode(s.cardtype,'B',s.amount,0)) Bamount,
sum(decode(s.cardtype,'C',1,0)) Ccount,
sum(decode(s.cardtype,'C',s.amount,0)) Camount,
sum(decode(s.cardtype,'D',1,0)) Dcount,
sum(decode(s.cardtype,'D',s.amount,0)) Damount
from ticket_order t inner join open_playitem m on t.relatedid=m.mpid inner join (
  select ds.orderid, c.batchid, ds.cardtype, count(ds.recordid) as cardcount, sum(ds.amount) as amount 
  from discount_item ds inner join eleccard_hist c on c.recordid=ds.relatedid 
  where ds.tag='ecard' group by ds.orderid, c.batchid, ds.cardtype
) s on t.recordid=s.orderid 
where t.order_type='ticket' and t.discount > 0 and t.status='paid_success' 
and  m.opentime>= to_date('2011-01-01','yyyy-MM-dd') and  m.opentime< to_date('2011-04-01','yyyy-MM-dd')
group by s.batchid, t.cinemaid, t.movieid, to_char(m.opentime,'yyyy-mm-dd')
);
---[20110401~~~~)----
insert into carddiscount_stat ( 
select s.batchid, t.cinemaid, t.movieid, to_char(m.opentime,'yyyy-mm-dd') opendate, 
count(t.recordid) ordercount,
sum(t.quantity) quantity, 
sum(t.costprice*t.quantity) totalcost, 
sum(t.amount) totalamount,
sum(t.discount) totaldiscount,
sum(s.cardcount) cardcount,
sum(s.amount) carddiscount,
sum(t.costprice * s.cardcount) cardcost,
sum(decode(s.cardtype,'A',1,0)) Acount,
sum(decode(s.cardtype,'A',s.amount,0)) Aamount,
sum(decode(s.cardtype,'B',1,0)) Bcount,
sum(decode(s.cardtype,'B',s.amount,0)) Bamount,
sum(decode(s.cardtype,'C',1,0)) Ccount,
sum(decode(s.cardtype,'C',s.amount,0)) Camount,
sum(decode(s.cardtype,'D',1,0)) Dcount,
sum(decode(s.cardtype,'D',s.amount,0)) Damount
from ticket_order t inner join open_playitem m on t.relatedid=m.mpid inner join (
  select ds.orderid, c.batchid, ds.cardtype, count(ds.recordid) as cardcount, sum(ds.amount) as amount 
  from discount_item ds inner join eleccard_hist c on c.recordid=ds.relatedid 
  where ds.tag='ecard' group by ds.orderid, c.batchid, ds.cardtype
) s on t.recordid=s.orderid 
where t.order_type='ticket' and t.discount > 0 and t.status='paid_success' 
and  m.opentime>= to_date('2011-04-01','yyyy-MM-dd')
group by s.batchid, t.cinemaid, t.movieid, to_char(m.opentime,'yyyy-mm-dd')
);
-------------正式表数据-----------------
insert into carddiscount_stat ( 
select s.batchid, t.cinemaid, t.movieid, to_char(m.opentime,'yyyy-mm-dd') opendate, 
count(t.recordid) ordercount,
sum(t.quantity) quantity, 
sum(t.costprice*t.quantity) totalcost, 
sum(t.amount) totalamount,
sum(t.discount) totaldiscount,
sum(s.cardcount) cardcount,
sum(s.amount) carddiscount,
sum(t.costprice * s.cardcount) cardcost,
sum(decode(s.cardtype,'A',1,0)) Acount,
sum(decode(s.cardtype,'A',s.amount,0)) Aamount,
sum(decode(s.cardtype,'B',1,0)) Bcount,
sum(decode(s.cardtype,'B',s.amount,0)) Bamount,
sum(decode(s.cardtype,'C',1,0)) Ccount,
sum(decode(s.cardtype,'C',s.amount,0)) Camount,
sum(decode(s.cardtype,'D',1,0)) Dcount,
sum(decode(s.cardtype,'D',s.amount,0)) Damount
from ticket_order t inner join open_playitem m on t.relatedid=m.mpid inner join (
  select ds.orderid, c.batchid, ds.cardtype, count(ds.recordid) as cardcount, sum(ds.amount) as amount 
  from discount_item ds inner join eleccard c on c.recordid=ds.relatedid 
  where ds.tag='ecard' group by ds.orderid, c.batchid, ds.cardtype
) s on t.recordid=s.orderid 
where t.order_type='ticket' and t.discount > 0 and t.status='paid_success' 
and  m.opentime < to_date('2011-12-01','yyyy-MM-dd')
group by s.batchid, t.cinemaid, t.movieid, to_char(m.opentime,'yyyy-mm-dd')
);
----------------------1227执行-------------------------
insert into carddiscount_stat ( 
select s.batchid, t.cinemaid, t.movieid, to_char(m.opentime,'yyyy-mm-dd') opendate, 
count(t.recordid) ordercount,
sum(t.quantity) quantity, 
sum(t.costprice*t.quantity) totalcost, 
sum(t.amount) totalamount,
sum(t.discount) totaldiscount,
sum(s.cardcount) cardcount,
sum(s.amount) carddiscount,
sum(t.costprice * s.cardcount) cardcost,
sum(decode(s.cardtype,'A',1,0)) Acount,
sum(decode(s.cardtype,'A',s.amount,0)) Aamount,
sum(decode(s.cardtype,'B',1,0)) Bcount,
sum(decode(s.cardtype,'B',s.amount,0)) Bamount,
sum(decode(s.cardtype,'C',1,0)) Ccount,
sum(decode(s.cardtype,'C',s.amount,0)) Camount,
sum(decode(s.cardtype,'D',1,0)) Dcount,
sum(decode(s.cardtype,'D',s.amount,0)) Damount
from ticket_order t inner join open_playitem m on t.relatedid=m.mpid inner join (
  select ds.orderid, c.batchid, ds.cardtype, count(ds.recordid) as cardcount, sum(ds.amount) as amount 
  from discount_item ds inner join eleccard c on c.recordid=ds.relatedid 
  where ds.tag='ecard' group by ds.orderid, c.batchid, ds.cardtype
) s on t.recordid=s.orderid 
where t.order_type='ticket' and t.discount > 0 and t.status='paid_success' 
and  m.opentime >= to_date('2011-12-01','yyyy-MM-dd') and  m.opentime < to_date('2011-12-25','yyyy-MM-dd')
group by s.batchid, t.cinemaid, t.movieid, to_char(m.opentime,'yyyy-mm-dd')
);
-------------------------20120105------------------------------------------------
insert into carddiscount_stat ( 
select s.batchid, t.cinemaid, t.movieid, to_char(m.opentime,'yyyy-mm-dd') opendate, 
count(t.recordid) ordercount,
sum(t.quantity) quantity, 
sum(t.costprice*t.quantity) totalcost, 
sum(t.amount) totalamount,
sum(t.discount) totaldiscount,
sum(s.cardcount) cardcount,
sum(s.amount) carddiscount,
sum(t.costprice * s.cardcount) cardcost,
sum(decode(s.cardtype,'A',1,0)) Acount,
sum(decode(s.cardtype,'A',s.amount,0)) Aamount,
sum(decode(s.cardtype,'B',1,0)) Bcount,
sum(decode(s.cardtype,'B',s.amount,0)) Bamount,
sum(decode(s.cardtype,'C',1,0)) Ccount,
sum(decode(s.cardtype,'C',s.amount,0)) Camount,
sum(decode(s.cardtype,'D',1,0)) Dcount,
sum(decode(s.cardtype,'D',s.amount,0)) Damount
from ticket_order t inner join open_playitem m on t.relatedid=m.mpid inner join (
  select ds.orderid, c.batchid, ds.cardtype, count(ds.recordid) as cardcount, sum(ds.amount) as amount 
  from discount_item ds inner join eleccard c on c.recordid=ds.relatedid 
  where ds.tag='ecard' group by ds.orderid, c.batchid, ds.cardtype
) s on t.recordid=s.orderid 
where t.order_type='ticket' and t.discount > 0 and t.status='paid_success' 
and  m.opentime >= to_date('2011-12-25','yyyy-MM-dd') and  m.opentime < to_date('2012-01-01','yyyy-MM-dd')
group by s.batchid, t.cinemaid, t.movieid, to_char(m.opentime,'yyyy-mm-dd')
);

------------删除异常数据-----------------------
delete from carddiscount_stat where BATCHID is null or CINEMAID is null or MOVIEID is null or OPENDATE is null or MPICOUNT is null 
or QUANTITY is null or TOTALCOST is null or TOTALAMOUNT is null or TOTALDISCOUNT is null or CARDCOUNT is null or CARDDISCOUNT is null 
or CARDCOST is null or ACOUNT is null or AAMOUNT is null or BCOUNT is null or BAMOUNT is null or CCOUNT is null or CAMOUNT is null 
or DCOUNT is null or DAMOUNT is null;

--------------修正语句--------------------------
alter table carddiscount_stat add cardtype varchar2(2);
alter table carddiscount_stat drop column Acount;
alter table carddiscount_stat drop column Aamount;
alter table carddiscount_stat drop column Bcount;
alter table carddiscount_stat drop column Bamount;
alter table carddiscount_stat drop column Ccount;
alter table carddiscount_stat drop column Camount;
alter table carddiscount_stat drop column Dcount;
alter table carddiscount_stat drop column Damount;

insert into carddiscount_stat ( 
select s.batchid, t.cinemaid, t.movieid, to_char(m.opentime,'yyyy-mm-dd') opendate, 
count(t.recordid) ordercount,
sum(t.quantity) quantity, 
sum(t.costprice*t.quantity) totalcost, 
sum(t.amount) totalamount,
sum(t.discount) totaldiscount,
sum(s.cardcount) cardcount,
sum(s.amount) carddiscount,
sum(t.costprice * s.cardcount) cardcost, s.cardtype 
from ticket_order t inner join open_playitem m on t.relatedid=m.mpid inner join (
  select ds.orderid, c.batchid, ds.cardtype, count(ds.recordid) as cardcount, sum(ds.amount) as amount 
  from discount_item ds inner join eleccard c on c.recordid=ds.relatedid 
  where ds.tag='ecard' group by ds.orderid, c.batchid, ds.cardtype
) s on t.recordid=s.orderid 
where t.order_type='ticket' and t.discount > 0 and t.status='paid_success' 
and  m.opentime >= to_date('2011-12-25','yyyy-MM-dd') and  m.opentime < to_date('2012-01-01','yyyy-MM-dd')
group by s.batchid, s.cardtype, t.cinemaid, t.movieid, to_char(m.opentime,'yyyy-mm-dd')
);
-----0205-----
insert into carddiscount_stat ( 
select s.batchid, t.cinemaid, t.movieid, to_char(m.opentime,'yyyy-mm-dd') opendate, 
count(t.recordid) ordercount,
sum(t.quantity) quantity, 
sum(t.costprice*t.quantity) totalcost, 
sum(t.amount) totalamount,
sum(t.discount) totaldiscount,
sum(s.cardcount) cardcount,
sum(s.amount) carddiscount,
sum(t.costprice * s.cardcount) cardcost, s.cardtype
from ticket_order t inner join open_playitem m on t.relatedid=m.mpid inner join (
  select ds.orderid, c.batchid, ds.cardtype, count(ds.recordid) as cardcount, sum(ds.amount) as amount 
  from discount_item ds inner join eleccard c on c.recordid=ds.relatedid 
  where ds.tag='ecard' group by ds.orderid, c.batchid, ds.cardtype
) s on t.recordid=s.orderid 
where t.order_type='ticket' and t.discount > 0 and t.status='paid_success' 
and  m.opentime >= to_date('2012-01-01','yyyy-MM-dd') and  m.opentime < to_date('2012-02-01','yyyy-MM-dd')
group by s.batchid, s.cardtype, t.cinemaid, t.movieid, to_char(m.opentime,'yyyy-mm-dd')
);

