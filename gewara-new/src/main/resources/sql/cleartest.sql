---------gym-------
delete from GYMPROGRAM where gymid in(select g.recordid from gym g where g.name like '%test%');
delete from GYM_GYMCOACH where gymid in(select g.recordid from gym g where g.name like '%test%');
delete from activity a where a.tag='gym' and a.relatedid in(select g.recordid from gym g where g.name like '%test%');
delete from diary a where a.tag='gym' and a.relatedid in(select g.recordid from gym g where g.name like '%test%');
delete from C_COMMENT a where a.tag='gym' and a.relatedid in(select g.recordid from gym g where g.name like '%test%');
delete from GEWAQUESTION a where a.tag='gym' and a.relatedid in(select g.recordid from gym g where g.name like '%test%');
delete from gym where recordid in(select g.recordid from gym g where g.name like '%test%');


---------bar--------
delete from activity a where a.tag='bar' and a.relatedid in(select g.recordid from bar g where g.name like '%test%');
delete from diary a where a.tag='bar' and a.relatedid in(select g.recordid from bar g where g.name like '%test%');
delete from C_COMMENT a where a.tag='bar' and a.relatedid in(select g.recordid from bar g where g.name like '%test%');
delete from GEWAQUESTION a where a.tag='bar' and a.relatedid in(select g.recordid from bar g where g.name like '%test%');
delete from bar where recordid in(select g.recordid from bar g where g.name like '%test%');


delete from SPORTPRICETABLE  where sportid in(select g.recordid from sport g where g.name like '%test%');
delete from activity a where a.tag='sport' and a.relatedid in(select g.recordid from sport g where g.name like '%test%');
delete from diary a where a.tag='sport' and a.relatedid in(select g.recordid from sport g where g.name like '%test%'); 
delete from C_COMMENT a where a.tag='sport' and a.relatedid in(select g.recordid from sport g where g.name like '%test%'); 
delete from GEWAQUESTION a where a.tag='sport' and a.relatedid in(select g.recordid from sport g where g.name like '%test%');
delete from sport where recordid in(select g.recordid from sport g where g.name like '%test%');
 



