select playtime,mpid from webdata.open_playitem where playtime>sysdate-10 and roomid in (select recordid from WEBDATA.CINEMAROOM where roomname like '%删%')
select count(1) from webdata.roomseat where roomid in (select recordid from WEBDATA.CINEMAROOM where roomname like '%删%');
delete from webdata.roomseat where roomid in (select recordid from WEBDATA.CINEMAROOM where roomname like '%删%');
delete from webdata.cinemaroom where roomname like '%删%';

---2013-09-02----
create table cinemasettle_mtx_20130902 as 
select * from cinemasettle where cinemaid in 
(3298455,32113697,37089563,37795693,37948332,37948936,37949418,37986315,37994559,38021503,38022990,38023004,38023722,38024723,38040362,38041521,38044933,38790928,38792095,38792339,38792379,38792733,38793798,38794022,38794776,38796987,38811863,40354327,46208349,47835652,51016374,51854768,53285998,53889783,57928991,57930793,60083225,61140516,65363331,67601750,67602144,67699989,70577934,70578148,71377180,72097892,72259719,72458627,78050341,78051264,95345078,100795733,100798453,101156329,101566601,103405032,119151654,120635806,121024351,127992453,129252086,130349048);

delete from cinemasettle where cinemaid in 
(3298455,32113697,37089563,37795693,37948332,37948936,37949418,37986315,37994559,38021503,38022990,38023004,38023722,38024723,38040362,38041521,38044933,38790928,38792095,38792339,38792379,38792733,38793798,38794022,38794776,38796987,38811863,40354327,46208349,47835652,51016374,51854768,53285998,53889783,57928991,57930793,60083225,61140516,65363331,67601750,67602144,67699989,70577934,70578148,71377180,72097892,72259719,72458627,78050341,78051264,95345078,100795733,100798453,101156329,101566601,103405032,119151654,120635806,121024351,127992453,129252086,130349048);--56 rows deleted.

delete from cinemasettle where recordid=132939149;
update cinemasettle set status='N' where recordid=129339953;



---2013-08-30----
insert into smsrecord_his select * from smsrecord where sendtime<to_date('2013-08-01','yyyy-mm-dd'); --1225025 rows created.
delete from smsrecord where sendtime<to_date('2012-01-01','yyyy-mm-dd');--14307 rows deleted.
delete from smsrecord where sendtime<to_date('2012-07-01','yyyy-mm-dd');--147484 rows deleted.
delete from smsrecord where sendtime<to_date('2013-01-01','yyyy-mm-dd');--345980 rows deleted.
delete from smsrecord where sendtime<to_date('2013-03-01','yyyy-mm-dd');--91322 rows deleted.
delete from smsrecord where sendtime<to_date('2013-04-01','yyyy-mm-dd');--56834 rows deleted.
delete from smsrecord where sendtime<to_date('2013-05-01','yyyy-mm-dd');--89676 rows deleted.
delete from smsrecord where sendtime<to_date('2013-06-01','yyyy-mm-dd');--252890 rows deleted.
delete from smsrecord where sendtime<to_date('2013-08-01','yyyy-mm-dd');--226532 rows deleted.



---2013-06-05----
insert into diary_hist select * from diary where sorttime<to_date('2012-01-01','yyyy-mm-dd');
delete from diary where sorttime<to_date('2012-01-01','yyyy-mm-dd');

create table gewacommend_20130605 as select * from gewacommend;
delete from gewacommend where addtime<to_date('2012-01-02','yyyy-mm-dd'); --4251 rows deleted.
delete from WEBDATA.GEWACOMMEND where signname in (
'dramaindex_beforesell',
'sportrookie',
'sportmaster',
'index_sportlist',
'index_partner_activity',
'index_recommend_news',
'index_recommend_activity',
'index_recommend_movie',
'index_recommend_commu',
'index_info_movie',
'index_info_bk',
'index_info_gs',
'index_opicinema',
'index_cg',
'index_barktv_activity',
'index_gymsport_item',
'index_itemdiary',
'index_recommend_topic',
'index_recommend_people',
'index_recommend_hotsearch',
'index_order_drama',
'index_order_movie',
'index_order_sport',
'index_order_move_info',
'index_order_drama_info',
'index_order_sport_info',
'movieindex_search',
'movieindex_buyticket',
'movieindex_diary',
'classicmovie',
'movieindex_mobile',
'index_citymovie_header',
'ktv_headinfo',
'bar_headinfo',
'gymindex_search_gym',
'gymindex_search_card',
'gymindex_search_course',
'gymindex_search_coach',
'gymindex_mate_activity',
'gymindex_diary',
'gymindex_commu',
'gymindex_gym',
'gymindex_special_activity',
'gymindex_together_activity',
'gymindex_nexttogether_activity',
'gymindex_referrals_card',
'gymindex_hot_course',
'gymindex_gym_video',
'gymindex_news',
'gymindex_dync_news',
'gymindex_coach',
'gymindex_diary_course',
'gymindex_referrals_course',
'gymindex_gym_carditem');

delete from gewacommend where signname in ('bnews','knews')

-----2013-08-07---------------------------------------------------
drop table FIELD;
drop table FIELD_AREA;
drop table FIELD_AREA_SEAT;
drop table GPTBS_DRAMA;
drop table GPTBS_THEATRE;
drop table GPTBS_PLACE_FIELD;
drop table GPTBS_PLACE_FIELD_AREA;
drop table GPTBS_PROGRAM;
drop table GPTBS_PROGRAM_PRICE;
drop table GPTBS_SCHEDULE;
drop table GPTBS_SCHEDULE_AREA;
drop table GPTBS_SCHEDULE_LOGGER;
drop table GPTBS_STADIUM;
drop table SHOW_AREA;
drop table SHOW_ITEM;
drop table SHOW_PRICE;
drop table SHOW_SEAT;

--TODO:DRAMA_REMOTE_ORDER;

drop table BEFOREPROCESS;
drop table DETECT_MOBILE;

create table WINNERINFO_HIS as select w.* from webdata.winnerinfo w 
where exists(select d.recordid from webdata.DRAWACTIVITY d where d.endtime<to_date('2011-01-01','yyyy-mm-dd') and d.recordid=w.activityid);--38356

delete from webdata.winnerinfo w webdata.winnerinfo w 
where exists(select d.recordid from webdata.DRAWACTIVITY d where d.endtime<to_date('2011-01-01','yyyy-mm-dd') and d.recordid=w.activityid);--38356

insert into WINNERINFO_HIS select * from webdata.winnerinfo w 
where exists(select d.recordid from webdata.DRAWACTIVITY d where d.endtime<to_date('2012-01-01','yyyy-mm-dd') and d.recordid=w.activityid);--39936

delete from webdata.winnerinfo w  
where exists(select d.recordid from webdata.DRAWACTIVITY d where d.endtime<to_date('2012-01-01','yyyy-mm-dd') and d.recordid=w.activityid);--38356

insert into WINNERINFO_HIS select * from webdata.winnerinfo w 
where exists(select d.recordid from webdata.DRAWACTIVITY d where d.endtime<to_date('2013-01-01','yyyy-mm-dd') and d.recordid=w.activityid);--68430

delete from webdata.winnerinfo w  
where exists(select d.recordid from webdata.DRAWACTIVITY d where d.endtime<to_date('2013-01-01','yyyy-mm-dd') and d.recordid=w.activityid);--68430

insert into WINNERINFO_HIS select * from webdata.winnerinfo w where w.activityid=37585911; --640831
delete from webdata.winnerinfo w where w.activityid=37585911; --640831


