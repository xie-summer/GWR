-------------------------------------------2012-10-16-----start-----------------------------------------
alter table webdata.SYNCH add SYNCHKEY varchar2(100);
-------------------------------------------2012-10-16-----end-------------------------------------------
-------------------------------------------2012-09-13-------------------------------------------------
alter table webdata.theatre_room add synchtime timestamp(6);
alter table webdata.THEATRE_SEAT_PRICE add UPDATETIME timestamp(6);--¼Û¸ñ±í

update webdata.theatre_room set  synchtime=systimestamp;
update webdata.THEATRE_SEAT_PRICE set updatetime=systimestamp;
-------------------------------------------2012-09-13-------------------------------------------------

-------------------------------------------2012-08-10-------------------------------------------------
alter table webdata.memberinfo add IP varchar2(15);
-------------------------------------------2012-08-10-------------------------------------------------

-------------------------------------------2012-08-08--------------------------------------------------
alter table webdata.activity add IP varchar2(15);  
alter table webdata.GEWAQUESTION add IP varchar2(15);
alter table webdata.QUESTION add IP varchar2(15);
alter table webdata.GEWAANSWER add IP varchar2(15);
alter table webdata.DIARY add IP varchar2(15);
alter table webdata.DIARY_HIST add IP varchar2(15);
alter table webdata.DIARYCOMMENT add IP varchar2(15);
alter table webdata.COMMU add IP varchar2(15);

alter table webdata.c_comment add IP varchar2(15);

-------------------------------------------2012-08-08--------------------------------------------------

-------------------------------------------2012-07-02--------------------------------------------------
create table webdata.cityprice(
RECORDID NUMBER(19) NOT NULL,
TAG VARCHAR2(20) NOT NULL,
CITYCODE VARCHAR2(6) NOT NULL,
RELATEDID NUMBER(19) NOT NULL,
ADDTIME TIMESTAMP NOT NULL,
UPDATETIME TIMESTAMP NOT NULL,
AVGPRICE NUMBER(4) DEFAULT 0 NOT NULL,
MINPRICE NUMBER(4) DEFAULT 0 NOT NULL,
MAXPRICE NUMBER(4) DEFAULT 0 NOT NULL,
constraint pk_cityprice primary key (recordid)
 using index(create unique index webdata.idxpk_cityprice on webdata.cityprice(recordid) tablespace tbs_index),
constraint uk_cityprice unique (relatedid, citycode, tag)
 using index(create unique index webdata.idxuk_cityprice on webdata.cityprice(tag, relatedid, citycode) tablespace tbs_index)
) tablespace webdata;
------------------------------------------2012-07-02----------------------------------------------------

-------------------------------------------2012-05-22--------------------------------------------------
alter table webdata.GYM_SPECIAL_SCHEDULE ADD REMOTEID NUMBER(19) ;
alter table webdata.GYM_SPECIAL_SCHEDULE add CONSTRAINT uk_GYM_SPECIAL_SCHEDULE_REMOTEID unique(REMOTEID)
using index (create unique index webdata.idxuk_GYM_SPECIAL_SCHEDULE_REMOTEID on webdata.GYM_SPECIAL_SCHEDULE(REMOTEID) TABLESPACE tbs_index);
 
alter table webdata.GYM_SPECAIL_PROGRAM ADD REMOTEID NUMBER(19) ;
alter table webdata.GYM_SPECAIL_PROGRAM add CONSTRAINT uk_GYM_SPECAIL_PROGRAM_REMOTEID unique(REMOTEID)
using index (create UNIQUE index webdata.idxuk_GYM_SPECAIL_PROGRAM_REMOTEID on webdata.GYM_SPECAIL_PROGRAM(REMOTEID) TABLESPACE tbs_index);
 
alter table webdata.GYMSPECIALCOURSE ADD REMOTEID NUMBER(19) 
alter table webdata.GYMSPECIALCOURSE ADD ADDTIME TIMESTAMP;
alter table webdata.GYMSPECIALCOURSE add CONSTRAINT uk_GYMSPECIALCOURSE_REMOTEID UNIQUE(REMOTEID)
using index (CREATE UNIQUE index webdata.idxuk_GYMSPECIALCOURSE_REMOTEID on webdata.GYMSPECIALCOURSE(REMOTEID) TABLESPACE tbs_index);

alter table webdata.GYMCOACH add REMOTEID NUMBER(19) ;
alter table webdata.GYMCOACH ADD GYMID NUMBER(19) ;
alter table webdata.GYMCOACH ADD STATUS VARCHAR2(2);
alter table webdata.GYMCOACH add CONSTRAINT uk_GYMCOACH_REMOTEID UNIQUE(REMOTEID)
using index (create UNIQUE index webdata.idxuk_GYMCOACH_REMOTEID on webdata.GYMCOACH(REMOTEID) TABLESPACE tbs_index);
-------------------------------------------2012-05-22--------------------------------------------------

alter table tempcoach add OTHERINFO VARCHAR2(1000);
alter table place add OTHERINFO VARCHAR2(1000); 
alter table CORRECTION add OTHERINFO VARCHAR2(1000);
alter table TEMPMOVIE add REASON VARCHAR2(500);

ALTER TABLE webdata.DRAMA ADD CITYCODE VARCHAR2(6) DEFAULT '310000' not null;

ALTER TABLE webdata.DRAMA_PLAY_ITEM ADD CITYCODE VARCHAR2(6) DEFAULT '310000' not null;

ALTER TABLE webdata.OPEN_DRAMA_ITEM ADD CITYCODE VARCHAR2(6) DEFAULT '310000' not null;