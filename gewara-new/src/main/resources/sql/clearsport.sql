delete from webdata.open_timeitem i 
where exists(select t.recordid from webdata.open_timetable t where t.playdate<to_date('2013-01-01','yyyy-MM-dd') and i.ottid=t.recordid)