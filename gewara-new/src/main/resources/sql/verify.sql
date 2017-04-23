delete from info_member_action WHERE c_member_id NOT IN (SELECT c_member_id from c_member);
select g.*,rowid from gym g where g.name like 'test%';
select g.*,rowid from gymprogram g where g.programname like 'test%';
