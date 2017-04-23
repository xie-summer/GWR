create table open_playitem_his as select * from open_playitem where playtime<to_date('2012-01-01','yyyy-mm-dd');

delete from open_playitem where playtime<to_date('2011-01-01','yyyy-mm-dd');--145598
delete from open_playitem where playtime<to_date('2011-04-01','yyyy-mm-dd');--87056
delete from open_playitem where playtime<to_date('2011-07-01','yyyy-mm-dd');--108731
delete from open_playitem where playtime<to_date('2011-10-01','yyyy-mm-dd');--153960
delete from open_playitem where playtime<to_date('2011-11-01','yyyy-mm-dd');--71271
delete from open_playitem where playtime<to_date('2012-01-01','yyyy-mm-dd');--158959
------------------ total: 725575-----------------------------------------------------

alter table sellseat_hist drop column oseatid;
alter table sellseat_hist drop column MEMBERID;
alter table sellseat_hist modify remark varchar2(1500);
insert into sellseat_hist (select * from sellseat where mpid in (select mpid from open_playitem_his)) --317106
delete from sellseat where mpid in (select mpid from open_playitem_his);  --317106
-------------------------------------------------------------------------------------

update open_playitem set language=trim(language) where language!=trim(language);--35
update open_playitem set language='英语' where language='英';
update open_playitem set language='英语' where language='English/中文字幕';
update open_playitem set language='西班牙语' where language='西班牙语 西班牙语';
update open_playitem set language='国语' where language='配音';
update open_playitem set language='国语' where language='国语*';
update open_playitem set language='国语' where language='国语#';
update open_playitem set language='国语' where language='数字';
update open_playitem set language='国语' where language='无';
update open_playitem set language='国语' where language='普通话';
update open_playitem set language='国语' where language='国话';
update open_playitem set language='国语' where language='中文';
update open_playitem set language='赛德克语' where language='赛德克';
update open_playitem set language='韩语' where language='韩文';
update open_playitem set language='英语' where language='英文';
update open_playitem set language='英语' where language='原声';
update open_playitem set language='国语' where language='国语国语';
update open_playitem set language='日语' where language='日文';
update open_playitem set language='国语' where length(language)=1;
update open_playitem set language='波兰语' where language='波兰';
update open_playitem set language='英语' where language='影院';
update open_playitem set language='法语' where language='法国';
update open_playitem set language='德语' where language='德国';
update open_playitem set language='泰语' where language='泰国';
-----乱码
update open_playitem set language='国语' where length(language)=2 and language not in ('泰语','法国','粤语','英语','波兰','原版','希腊','德国','泰国','国语','法语','德语','俄语','日语','韩语');


update open_playitem set edition='2D' where edition is null;

update cinema set partner=null, pcid=null where partner='HFH' and recordid in (100597908,47149892,47150108,56127143,77177135,37545712,82455999,82457021,7,250349,91505022);

delete from hfh_cinema where hcid in ('31182301','31081901','31113101','31113201','31152201','31112901','31163401','31100901','31090801','31050801','31193701');
delete from UPDATE_RECORDER where pcid in ('31182301','31081901','31113101','31113201','31152201','31112901','31163401','31100901','31090801','31050801','31193701');


