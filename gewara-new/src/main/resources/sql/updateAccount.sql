select count(distinct memberid) from payment where status='paid_success' and paymethod in ('lakalaPay','telecomPay','directPay','pnrPay') 
and memberid not in (
select distinct memberid from payment where status='paid_success' and paymethod in('ccardPay','sysPay')
)

select count(distinct memberid) from payment where status='paid_success' and paymethod in('ccardPay','sysPay')
and memberid not in (
select distinct memberid from payment where status='paid_success' and paymethod in ('lakalaPay','telecomPay','directPay','pnrPay') 
)


select count(distinct memberid) from payment where status='paid_success' and paymethod in('ccardPay','sysPay')
and memberid in (
select distinct memberid from payment where status='paid_success' and paymethod in ('lakalaPay','telecomPay','directPay','pnrPay') 
)


使用网银充值：   14787 
只使用网银充值： 11121 有余额：5043

使用券及返利：   37356
只使用券及返利： 33690 有余额：33392

使用网银+券返利：3666  有余额：1217, 网银>券返利：921，网银<=券返利:225

不平账号（banlance!=0 and bankcharge+othercharge!=banlance）： 40362   更新后： 2299

select count(*) from member_account where banlance!=0 and bankcharge+othercharge!=banlance

select count(*) from member_account where banlance!=0 and memberid in (select distinct memberid from payment where status='paid_success' and paymethod in ('lakalaPay','telecomPay','directPay','pnrPay') 
and memberid not in (select distinct memberid from payment where status='paid_success' and paymethod in('ccardPay','sysPay'))
)

update member_account set bankcharge=banlance,othercharge=0 where banlance!=0 and memberid in (select distinct memberid from payment where status='paid_success' and paymethod in ('lakalaPay','telecomPay','directPay','pnrPay') 
and memberid not in (select distinct memberid from payment where status='paid_success' and paymethod in('ccardPay','sysPay'))
);

select count(*) from member_account where banlance!=0 and memberid in (
select memberid from payment where status='paid_success' and paymethod in('ccardPay','sysPay')
and memberid not in (select distinct memberid from payment where status='paid_success' and paymethod in ('lakalaPay','telecomPay','directPay','pnrPay'))
)

update member_account set othercharge=banlance,bankcharge=0 where banlance!=0 and memberid in (
select memberid from payment where status='paid_success' and paymethod in('ccardPay','sysPay')
and memberid not in (select distinct memberid from payment where status='paid_success' and paymethod in ('lakalaPay','telecomPay','directPay','pnrPay'))
);

select count(*) from member_account where banlance!=0 and memberid in (
select memberid from payment where status='paid_success' and paymethod in('ccardPay','sysPay')
and memberid in (select distinct memberid from payment where status='paid_success' and paymethod in ('lakalaPay','telecomPay','directPay','pnrPay'))
)


create table upgrade_account as select distinct memberid from payment where status='paid_success' and paymethod in('ccardPay','sysPay')
and memberid in (select distinct memberid from payment where status='paid_success' and paymethod in ('lakalaPay','telecomPay','directPay','pnrPay'));




update member_account t set bankcharge=banlance,othercharge=0 where banlance!=0 and memberid in (select memberid from upgrade_account)
and (select sum(TOTAL_FEE) from payment s where s.memberid=t.memberid and s.status='paid_success' and s.paymethod in ('lakalaPay','telecomPay','directPay','pnrPay')) >
(select sum(TOTAL_FEE) from payment m where m.memberid=t.memberid and m.status='paid_success' and m.paymethod in ('ccardPay','sysPay'));

update member_account t set othercharge=banlance,bankcharge=0 where banlance!=0 and memberid in (select memberid from upgrade_account)
and (select sum(TOTAL_FEE) from payment s where s.memberid=t.memberid and s.status='paid_success' and s.paymethod in ('lakalaPay','telecomPay','directPay','pnrPay')) <=
(select sum(TOTAL_FEE) from payment m where m.memberid=t.memberid and m.status='paid_success' and m.paymethod in ('ccardPay','sysPay'));


update member_account t set bankcharge=banlance,othercharge=0 where banlance!=0 and bankcharge+othercharge!=banlance and not exists(
select s.recordid from payment s where status='paid_success' and s.memberid=t.memberid
);




update member_account set bankcharge=banlance,othercharge=0 where banlance!=0 and bankcharge+othercharge!=banlance and memberid in (
select distinct memberid from payment where status='paid_success' and paymethod in ('lakalaPay','telecomPay','directPay','pnrPay') 
and memberid not in (select distinct memberid from payment where status='paid_success' and paymethod in('ccardPay','sysPay'))
);

update member_account set othercharge=banlance,bankcharge=0 where banlance!=0 and bankcharge+othercharge!=banlance and memberid in (
select memberid from payment where status='paid_success' and paymethod in('ccardPay','sysPay')
and memberid not in (select distinct memberid from payment where status='paid_success' and paymethod in ('lakalaPay','telecomPay','directPay','pnrPay'))
);

update member_account t set bankcharge=banlance,othercharge=0 where banlance!=0 and bankcharge+othercharge!=banlance and memberid in (select memberid from upgrade_account)
and (select sum(TOTAL_FEE) from payment s where s.memberid=t.memberid and s.status='paid_success' and s.paymethod in ('lakalaPay','telecomPay','directPay','pnrPay')) >
(select sum(TOTAL_FEE) from payment m where m.memberid=t.memberid and m.status='paid_success' and m.paymethod in ('ccardPay','sysPay'));

update member_account t set othercharge=banlance,bankcharge=0 where banlance!=0 and bankcharge+othercharge!=banlance and memberid in (select memberid from upgrade_account)
and (select sum(TOTAL_FEE) from payment s where s.memberid=t.memberid and s.status='paid_success' and s.paymethod in ('lakalaPay','telecomPay','directPay','pnrPay')) <=
(select sum(TOTAL_FEE) from payment m where m.memberid=t.memberid and m.status='paid_success' and m.paymethod in ('ccardPay','sysPay'));


update  member_account set bankcharge=0, othercharge=0  where banlance=0 and (bankcharge!=0 or othercharge!=0);

update ticketorder set paymethod='gewaPay' where paymethod='card' and alipaid=0 and status like 'paid%';

update member_account set bankcharge=270 where memberid=38771507 and banlance=270;
update member_account set bankcharge=45 where memberid=38826315 and banlance=45;
update member_account set bankcharge=0 where memberid=39203459 and banlance=0;
----------------------------------------------------------






