update diary d
   set replycount=(select count(0) from diarycomment c  where c.diaryid  = d.recordid);

delete hfh_qryresponse t where t.updatetime<sysdate - 1;
delete hfh_show t where t.playdate<sysdate -7;
delete hfh_showupdate t where t.playdate< sysdate-7;

create materialized view memberpoint as 
select s.to_memberid memberid, sum(s.pointvalue) pointvalue 
from point s group by s.to_memberid;


select t.recordid, t.pointvalue, s.pointvalue 
from member t inner join memberpoint s on t.recordid=s.memberid
where t.pointvalue!=s.pointvalue;

update member m set m.pointvalue=(
      select n.pointvalue from memberpoint n where m.recordid=n.memberid)
where exists (
      select t.memberid from memberpoint t
      where t.memberid = m.recordid and t.pointvalue!=m.pointvalue
)



update eleccard c set possessor = (select memberid from ticket_order t where t.recordid=c.orderid) where c.status='U' and c.possessor is null;
update eleccard_hist c set possessor = (select memberid from ticket_order t where t.recordid=c.orderid) where c.status='U' and c.possessor is null;