--1、订单金额问题

update ticket_order set alipaid=AMOUNT where alipaid=amount*100 and trade_no in ('1120309200726972','1111228114532353','1111227215951292','1120314203551176','1120316110942211','1120309112332972','1120309165115078','1120309111914335','1120322113123507','1120328225327240','1120327150301967','1120326145413008','1120325095521909','1120405154334963','1120401160748612','1120403151608518','1120406133430856','1120403102134430','1120402144140458','1120406095201768','1120408112537529','1120405161105169','1120405160201061','1120410180807960','1120410134056821')
update Bill_Record set amount = amount/100 where  tradeno in ('1120309200726972','1111228114532353','1111227215951292','1120314203551176','1120316110942211','1120309112332972','1120309165115078','1120309111914335','1120322113123507','1120328225327240','1120327150301967','1120326145413008','1120325095521909','1120405154334963','1120401160748612','1120403151608518','1120406133430856','1120403102134430','1120402144140458','1120406095201768','1120408112537529','1120405161105169','1120405160201061','1120410180807960','1120410134056821')

--2、折扣有问题
select recordid, trade_no, amount, discount, alipaid,gewapaid, discount_reason, description2,addtime from ticket_order where status='paid_success' and amount-discount<0 and addtime<sysdate-30 order by addtime desc

update discount_item d set amount=(select amount/quantity from ticket_order t where t.recordid=d.orderid) where orderid in(
63610446,63606723,63571143,63563275,63534208,63519761,63517498,63516246,63498390,63488534,63485444,63475461,63463866,63411229,63396633,63368184,63295171,63287278,63276630,63275759,63257713,63256390,63246117,63245283,
63237441,63232886,63228100,63223932,63218722,63136847,63103032,63083525,63081624,63066019,63058858,63057715,63056209,63042750,63030038,62939378,62907449,62904742,62901278,62896481,62888728,62887136,62872080,62870175,
62861975,62825780,62824508,62788593,62767696,62726387,62718231,62686215,62651417,62601250,62597535,62488382,62487636,62486690,62486009,62485134,62484820,62484799,62482275,62481362,62478923,62476883,62473738,62472584,
62467111,62466095,62434213,62434001,62429573,62429073,62403373,62397548,62386192,62384468,62384451,62383504,62383237,62383080,62382616,62375555,62349449,60059800,59720876,59694249,59672892,59314310,59277963,59205014,
59028962,59014417,59005267,58937764,58933075,58819265,58760454,58550454,58545472,58437880,58319909,58284456,58121848,58026047,57798034,57697450,57658015,57502094,57462928,57431004,57011089,56723853,56718230,44474784,
43955000,43412299,43271570,43264534,42793388,42651232,42620973,42620831,42620731,42620635,42620519,42620478,42620415,42620358,42620243,42620181,42620082,42619888,42612509)



update ticket_order t set unitprice=amount/quantity, t.discount=t.amount where t.recordid in(
63610446,63606723,63571143,63563275,63534208,63519761,63517498,63516246,63498390,63488534,63485444,63475461,63463866,63411229,63396633,63368184,63295171,63287278,63276630,63275759,63257713,63256390,63246117,63245283,
63237441,63232886,63228100,63223932,63218722,63136847,63103032,63083525,63081624,63066019,63058858,63057715,63056209,63042750,63030038,62939378,62907449,62904742,62901278,62896481,62888728,62887136,62872080,62870175,
62861975,62825780,62824508,62788593,62767696,62726387,62718231,62686215,62651417,62601250,62597535,62488382,62487636,62486690,62486009,62485134,62484820,62484799,62482275,62481362,62478923,62476883,62473738,62472584,
62467111,62466095,62434213,62434001,62429573,62429073,62403373,62397548,62386192,62384468,62384451,62383504,62383237,62383080,62382616,62375555,62349449,60059800,59720876,59694249,59672892,59314310,59277963,59205014,
59028962,59014417,59005267,58937764,58933075,58819265,58760454,58550454,58545472,58437880,58319909,58284456,58121848,58026047,57798034,57697450,57658015,57502094,57462928,57431004,57011089,56723853,56718230,44474784,
43955000,43412299,43271570,43264534,42793388,42651232,42620973,42620831,42620731,42620635,42620519,42620478,42620415,42620358,42620243,42620181,42620082,42619888,42612509);


update ticket_order t set unitprice=amount/quantity, t.discount=t.amount where t.recordid in(
63523769,63492631,63224011,63213198,63092905,62876476,62823193,62651338,62642172,62437921,62434969,58480884,58431775,56704060);

update discount_item d set amount=(select amount/quantity from ticket_order t where t.recordid=d.orderid) where orderid in(
63523769,63492631,63224011,63213198,63092905,62876476,62823193,62651338,62642172,62437921,62434969,58480884,58431775,56704060);

update ticket_order t set unitprice=amount/quantity, t.discount=t.amount where t.recordid in(
63492388,63492158,62484089,62434118,38778094,38598621,37852849,35008283,33710659,19014927);


update discount_item d set amount=(select amount/quantity from ticket_order t where t.recordid=d.orderid) where orderid in(
63492388,63492158,62484089,62434118,38778094,38598621,37852849,35008283,33710659,19014927);


update ticket_order set discount=0 where recordid in (38002408,31841594,31527418,31405060,31405031,31374229,31374100,31363367,31325461,31252902,31249989,31249853,29157706);
delete from discount_item where orderid in  (38002408,31841594,31527418,31405060,31405031,31374229,31374100,31363367,31325461,31252902,31249989,31249853,29157706);

--3、泰久问题
select recordid, trade_no, amount, discount, alipaid, discount_reason, description2 from ticket_order 
where discount>0  and partnerid=50000560 and status like 'paid%' and amount<discount+alipaid order by addtime desc

update ticket_order set alipaid=amount-discount where  discount>0  and partnerid=50000560 and status like 'paid%' and amount<discount+alipaid;

select * from bill_record where TRADENO in(select trade_no from ticket_order 
where discount>0  and partnerid=50000560 and status like 'paid%' and amount<discount+alipaid)

update bill_record s set amount=(select alipaid from ticket_order m where s.tradeno=m.trade_no) 
where tradeno in (
 select trade_no from ticket_order t where discount>0 and partnerid=50000560 and status like 'paid%'
) and amount!=(select alipaid from ticket_order t where s.tradeno=t.trade_no)

--4、系统支付订单
select recordid, trade_no, amount, discount, gewapaid, alipaid, remark from ticket_order 
where status like 'paid%' and paymethod='sysPay' and amount<gewapaid+alipaid order by addtime desc

update ticket_order set gewapaid=0 where recordid in (
select recordid from ticket_order where status like 'paid%' and paymethod='sysPay' and amount<gewapaid+alipaid
)

--5、积分商城问题
select recordid, trade_no, amount, discount, alipaid, discount_reason, description2 from ticket_order 
where discount>0  and partnerid=50000670 and status like 'paid%' and amount<discount+alipaid order by addtime desc

--6、上海热线问题
select recordid, trade_no, amount, discount, alipaid, discount_reason, description2 from ticket_order 
where discount>0  and partnerid=50000150 and status like 'paid%' and amount<discount+alipaid order by addtime desc

update ticket_order set alipaid=amount-discount where  discount>0  and partnerid=50000150 and status like 'paid%' and amount<discount+alipaid;

update bill_record s set amount=(select alipaid from ticket_order m where s.tradeno=m.trade_no) 
where tradeno in (
 select trade_no from ticket_order t where discount>0 and partnerid=50000150 and status like 'paid%'
) and amount!=(select alipaid from ticket_order t where s.tradeno=t.trade_no)

update ticket_order t set unitprice=amount/quantity, t.discount=t.amount where t.recordid in(63728755,63730188,63734762,63765156,63836312,63837424);
update discount_item d set amount=(select amount/quantity from ticket_order t where t.recordid=d.orderid) where orderid in(63728755,63730188,63734762,63765156,63836312,63837424);

--7、绑定套餐问题（2013-04-03）
update ticket_order t set alipaid=0, unitprice=0 where paymethod='sysPay' and relatedid=56215066;
update ticket_order t set alipaid=0, unitprice=0 where paymethod='sysPay' and relatedid=56150851;
update ticket_order t set alipaid=0, unitprice=0 where paymethod='sysPay' and relatedid=54178393;
update ticket_order t set alipaid=0, unitprice=0 where paymethod='sysPay' and relatedid=40384495;
update ticket_order t set alipaid=0, unitprice=0 where paymethod='sysPay' and trade_no in ('3120209195630004','3120205151256842','3120205150840080');

update ticket_order t set amount=0 where paymethod='sysPay' and relatedid in (56215066,56150851,54178393,40384495);
update ticket_order t set amount=alipaid where paymethod='sysPay' and relatedid=28083108;
update ticket_order t set amount=0 where paymethod='sysPay' and trade_no in ('3120209195630004','3120205151256842','3120205150840080');

update ticket_order set amount=28,alipaid=28 where trade_no='3110820151148487';
update ticket_order set amount=17,alipaid=17 where trade_no='3110820153239305';
update ticket_order set amount=28,alipaid=28 where trade_no='3110820185505892';
update ticket_order set amount=72,alipaid=72,unitprice=36 where trade_no='3111217154532346';



