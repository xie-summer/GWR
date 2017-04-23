--- 给演出表增加评论数字段,给场馆表增加评论数和购票数字段
--ALTER	TABLE	DRAMA	ADD DIARYCOUNT	NUMBER(19) default 0;
--ALTER	TABLE	THEATRE	ADD DIARYCOUNT	NUMBER(19) default 0;
ALTER TABLE THEATRE ADD BOUGHTCOUNT NUMBER(19) default 0 not null;

update theatre t set t.boughtcount = (select nvl(sum(ti.quantity),0) from ticket_order ti where ti.cinemaid = t.recordid and ti.pricategory = 'drama');