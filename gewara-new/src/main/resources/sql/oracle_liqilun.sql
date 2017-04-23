create table AS_CONFIG
(
  RECORDID      NUMBER(19) not null,
  PARTNERID     NUMBER(19) not null,
  APPSOURCE     VARCHAR2(20) not null,
  PAYMETHOD     VARCHAR2(1000) not null,
  SPECIALMETHOD VARCHAR2(500),
  APP_VERSION   VARCHAR2(20)
);

alter table AS_CONFIG add constraint PK_AS_CONFIG unique (RECORDID) using index tablespace tbs_index;
create index IDX_AS_CONFIG_APPSOURCE on AS_CONFIG (APPSOURCE) tablespace tbs_index;
grant select, insert, update,delete on AS_CONFIG to SHANGHAI;


create table WINNERINFO_HIS
(
  RECORDID   NUMBER(19) not null,
  ACTIVITYID NUMBER(19) not null,
  MEMBERID   NUMBER(19),
  NICKNAME   VARCHAR2(200),
  PRIZEID    NUMBER(19),
  MOBILE     VARCHAR2(15),
  ADDTIME    TIMESTAMP(6) not null,
  STATUS     VARCHAR2(20),
  TAG        VARCHAR2(20),
  RELATEDID  NUMBER(19),
  REMARK     VARCHAR2(150),
  IP         VARCHAR2(30)
);



create index IDX_TREASURE_MEMBERID on TREASURE (member_id) tablespace TBS_INDEX;

create index IDX_SPDISCOUNT_TIMETO on SPECIALDISCOUNT (timeto) tablespace TBS_INDEX;
create index IDX_SPDISCOUNT_PTNIDS on SPECIALDISCOUNT (PTNIDS)tablespace TBS_INDEX;

alter table APIUSER_EXTRA modify SPECIALMETHOD VARCHAR2(1000);

ALTER TABLE machine ADD COLUMN openfire_name character varying(30);

alter table APIUSER_EXTRA add OTHERINFO varchar2(500);

alter table webdata.phoneadvertisement add sharefriend varchar2(5);

insert into webdata.pay_method(paymethod,paymethod_text) values('wxWCPay','微信公众号支付');


insert into webdata.county(citycode,briefname,countycode,countyname) values('350181','玉屏街道','35018101','玉屏街道');
update webdata.specialdiscount s set s.ptnids=s.partnerid;
update webdata.specialdiscount s set s.ptnids='50000010,50000020,50000070' where opentype='W' and tag='movie';
update webdata.specialdiscount s set s.ptnids='50000010,50000035,50000030' where opentype='W' and tag='sport';
update webdata.specialdiscount s set s.ptnids='50000010,50000080,50000081' where opentype='W' and tag='drama';
insert into webdata.specialdiscount_extra(recordid,applycity,applydept,applytype,maxallowance,orderallowance,unitallowance) select s.recordid,s.applycity,s.applydept,s.applytype,s.maxallowance,s.orderallowance,s.unitallowance from webdata.specialdiscount s where s.recordid>138732383;



insert into webdata.county(citycode,briefname,countycode,countyname) values('350181','音西街道','35018102','音西街道');
insert into webdata.county(citycode,briefname,countycode,countyname) values('350181','龙山街道','35018103','龙山街道');
insert into webdata.county(citycode,briefname,countycode,countyname) values('350181','龙江街道','35018104','龙江街道');
insert into webdata.county(citycode,briefname,countycode,countyname) values('350181','宏路街道','35018105','宏路街道');
insert into webdata.county(citycode,briefname,countycode,countyname) values('350181','石竹街道','35018106','石竹街道');
insert into webdata.county(citycode,briefname,countycode,countyname) values('350181','阳下街道','35018107','阳下街道');



alter table SPECIALDISCOUNT add PTNIDS varchar2(100);
alter table SPECIALDISCOUNT add EXTRA_INFO char(1) default 'Y' not null;
update webdata.specialdiscount s set s.ptnids=s.partnerid;
update webdata.specialdiscount s set s.ptnids='50000010,50000020,50000070' where opentype='W' and tag='movie';
update webdata.specialdiscount s set s.ptnids='50000010,50000035,50000030' where opentype='W' and tag='sport';
update webdata.specialdiscount s set s.ptnids='50000010,50000080,50000081' where opentype='W' and tag='drama';

create table SPECIALDISCOUNT_EXTRA
(
  RECORDID        NUMBER(19) not null,
  APPLYCITY       VARCHAR2(6),
  APPLYDEPT       VARCHAR2(6),
  APPLYTYPE       VARCHAR2(6),
  ALLOWANCETYPE   VARCHAR2(20),
  PARTNERID       NUMBER(19),
  PARTNERNAME     VARCHAR2(50),
  ORDERALLOWANCE  NUMBER(10,2) default 0 not null,
  UNITALLOWANCE   NUMBER(10,2) default 0 not null,
  MAXALLOWANCE    NUMBER(10,2) default 0 not null,
  PARTNERID1      NUMBER(19),
  PARTNERNAME1    VARCHAR2(50),
  ORDERALLOWANCE1 NUMBER(10,2) default 0 not null,
  UNITALLOWANCE1  NUMBER(10,2) default 0 not null,
  MAXALLOWANCE1   NUMBER(10,2) default 0 not null,
  PARTNERID2      NUMBER(19),
  PARTNERNAME2    VARCHAR2(50),
  ORDERALLOWANCE2 NUMBER(10,2) default 0 not null,
  UNITALLOWANCE2  NUMBER(10,2) default 0 not null,
  MAXALLOWANCE2   NUMBER(10,2) default 0 not null
);
alter table SPECIALDISCOUNT_EXTRA add constraint PK_SPECIALDISCOUNT_EXTRA primary key (RECORDID) using index tablespace tbs_index;
grant select, insert, update on SPECIALDISCOUNT_EXTRA to SHANGHAI;

insert into webdata.specialdiscount_extra(recordid,applycity,applydept,applytype,maxallowance,orderallowance,unitallowance) select s.recordid,s.applycity,s.applydept,s.applytype,s.maxallowance,s.orderallowance,s.unitallowance from webdata.specialdiscount s;


update webdata.gewa_city set cityname='福清' where cityname='福清峡';
update webdata.gewa_city set provincename=substr(provincename, 0, length(provincename)-1) where provincename like '%省';
delete from webdata.gewa_city where cityname='台山';

insert into webdata.county(citycode,briefname,countycode,countyname) values('410200','鼓楼区','410212','鼓楼区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('410400','新华区','410412','新华区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('469002','琼海市','46900201','琼海市');
insert into webdata.county(citycode,briefname,countycode,countyname) values('140400','城区','140422','城区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('429005','潜江市','42900501','潜江市');
insert into webdata.county(citycode,briefname,countycode,countyname) values('511100','市中区','511114','市中区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('511000','市中区','511012','市中区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('330900','普陀区','330903','普陀区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('130500','内丘县','130538','内丘县');
insert into webdata.county(citycode,briefname,countycode,countyname) values('231000','西安区','231005','西安区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('220300','铁东区','220325','铁东区');

insert into webdata.county(citycode,briefname,countycode,countyname) values('220300','铁西区','220324','铁西区');

alter table SPECIALDISCOUNT add PERIODTYPE char(1) default 'A' not null;





-- Create/Recreate indexes 
create index IDX_GOODSGIFT_GOODSID on GOODSGIFT (goodsid) tablespace tbs_index;;
create index IDX_GOODSGIFT_CINEMAID on GOODSGIFT (cinemaid) tablespace tbs_index;


alter table SYNCH add MONITOR char(1) default 'Y' not null;
alter table SYNCH add NEWSYS char(1) default 'N' not null;


insert into webdata.county(citycode,briefname,countycode,countyname) values('510800','利州区','510813','利州区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('231000','西安区','231004','西安区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('360100','开发区','360113','开发区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('210200','开发区','210214','开发区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('340100','巢湖','340124','巢湖');

delete from webdata.gewa_city where citycode='341400';
delete from webdata.gewa_city where citycode='430481';
delete from webdata.gewa_city where citycode='321088';

update webdata.cinema c set c.citycode='340100',countycode='340124' where cinemaid in(132134474,134734801);
update webdata.movielist m set m.citycode='340100' where cinemaid in(132134474,134734801);

insert into webdata.county(citycode,briefname,countycode,countyname) values('320300','鼓楼区','320311','鼓楼区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('130700','桥东区','130707','桥东区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('533100','芒市','533104','芒市');
insert into webdata.county(citycode,briefname,countycode,countyname) values('620100','城关区','620112','城关区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('371000','经济技术开发区','371003','经济技术开发区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('450100','防城港','450128','防城港');
insert into webdata.county(citycode,briefname,countycode,countyname) values('350182','吴航街道','35018201','吴航街道');
insert into webdata.county(citycode,briefname,countycode,countyname) values('429004','仙桃市','42900401','仙桃市');
insert into webdata.county(citycode,briefname,countycode,countyname) values('130523','内丘县','13052301','内丘县');
insert into webdata.county(citycode,briefname,countycode,countyname) values('460200','河西','46020001','河西');



insert into webdata.subwaystation(recordid, stationname) values(834,'中华艺术宫');
insert into webdata.LINE2STATION(recordid,sid,lid) values(894,834,170247);
insert into webdata.subwaystation(recordid, stationname) values(835,'育新站');
insert into webdata.LINE2STATION(recordid,sid,lid) values(895,835,23);

delete from webdata.gewa_city where citycode='430481';
delete from webdata.gewa_city where citycode='321088';


ningde|luoyang|haikou|sanya|sanmenxia|huaibei|yunfu|fuqing|xingyi|kaili|leiyang|jiangdu

insert into webdata.city(citycode,cityname,provincecode) values('321088','江都市', '320000');
insert into webdata.city(citycode,cityname,provincecode) values('430481','耒阳市', '430000');
insert into webdata.city(citycode,cityname,provincecode) values('522601','凯里市', '520000');
insert into webdata.city(citycode,cityname,provincecode) values('522301','兴义市', '520000');
insert into webdata.city(citycode,cityname,provincecode) values('350181','福清市', '350000');


insert into webdata.gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) values ('411200', '三门峡', '410000', '河南省', 'N', 'N', 'N', 10, 350, 'sanmenxia', 'smx', 'auto');
insert into webdata.gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) values ('340600', '淮北', '340000', '安徽省', 'N', 'N', 'N', 161, 150, 'huaibei', 'hb', 'auto');
insert into webdata.gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) values ('445300', '云浮', '440000', '广东省', 'N', 'N', 'N', 90, 70, 'yunfu', 'yf', 'auto');
insert into webdata.gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) values ('350181', '福清峡', '350000', '福建省', 'N', 'N', 'N', 121, 130, 'fuqing', 'fq', 'auto');
insert into webdata.gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) values ('522301', '兴义', '520000', '贵州省', 'N', 'N', 'N', 10, 390, 'xingyi', 'xy', 'auto');
insert into webdata.gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) values ('522601', '凯里', '520000', '贵州省', 'N', 'N', 'N', 10, 390, 'kaili', 'kl', 'auto');
insert into webdata.gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) values ('430481', '耒阳', '430000', '湖南省', 'N', 'N', 'N', 236, 230, 'leiyang', 'ly', 'auto');
insert into webdata.gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) values ('321088', '江都', '320000', '江苏省', 'N', 'N', 'N', 40, 30, 'jiangdu', 'jd', 'auto');


insert into webdata.county(citycode,briefname,countycode,countyname) values('210200','高新区','210284','高新区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('140300','城区','140312','城区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('140300','矿区','140313','矿区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('620500','麦积区','620504','麦积区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('531000','思茅区','531010','思茅区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('430400','高新区','430413','高新区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('220100','朝阳区','220113','朝阳区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('360100','红谷滩新区','360112','红谷滩新区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('320800','清河区','320812','清河区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('420607','襄城区','42060701','襄城区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('420607','樊城区','42060702','樊城区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('420607','襄州区','42060703','襄州区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('420607','南漳县','42060704','南漳县');
insert into webdata.county(citycode,briefname,countycode,countyname) values('420607','谷城县','42060705','谷城县');
insert into webdata.county(citycode,briefname,countycode,countyname) values('420607','保康县','42060706','保康县');
insert into webdata.county(citycode,briefname,countycode,countyname) values('420607','老河口市','42060707','老河口市');
insert into webdata.county(citycode,briefname,countycode,countyname) values('420607','枣阳市','42060708','枣阳市');
insert into webdata.county(citycode,briefname,countycode,countyname) values('420607','宜城市','42060709','宜城市');


insert into webdata.gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) values ('350900', '宁德', '350000', '福建省', 'N', 'N', 'N', 120, 130, 'ningde', 'nd', 'auto');
insert into webdata.gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) values ('410300', '洛阳', '410000', '河南省', 'N', 'N', 'N', 10, 350, 'luoyang', 'ly', 'auto');
insert into webdata.gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) values ('460100', '海口', '460000', '海南省', 'N', 'N', 'N', 10, 480, 'haikou', 'hk', 'auto');
insert into webdata.gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) values ('460200', '三亚', '460000', '海南省', 'N', 'N', 'N', 10, 480, 'sanya', 'sy', 'auto');

alter table webdata.cooper_user add MOBILE varchar2(20);
alter table webdata.cooper_user add REALNAME varchar2(20);

alter table webdata.cooper_user add TAG varchar2(20);
alter table webdata.cooper_user add RELATEDIDS varchar2(200);
alter table webdata.cooper_user add CATEGORY varchar2(20);
alter table webdata.cooper_user add CATEGORYIDS varchar2(200);
alter table webdata.COOPER_USER modify PARTNERIDS null;
alter table webdata.ORDER_NOTE add OTHERINFO varchar2(500) default '{}' not null;


update webdata.gewa_city set pinyin='changle',py='cl' where citycode='350182';
update webdata.gewa_city set pinyin='bengbu' where citycode='340300';
update webdata.gewa_city set pinyin='xiamen',py='xm' where citycode='350200';
update webdata.gewa_city set pinyin='changsha',py='cs' where citycode='430100';
update webdata.gewa_city set pinyin='changchun',py='cc' where citycode='220100';


insert into webdata.county(citycode,briefname,countycode,countyname) values('220100','高新区','220184','高新区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('630100','城中区','630106','城中区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('610100','长安区','610105','长安区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('610100','新城区','610106','新城区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('371300','河东区','371330','河东区');

insert into webdata.city(citycode,cityname,provincecode) values('531000','普洱市', '530000');
insert into gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) 
values ('531000', '普洱', '530000', '云南省', 'N', 'N', 'Y', 10, 9, 'puer', 'pe', 'auto');

insert into gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) 
values ('330300', '温州', '330000', '浙江省', 'N', 'N', 'Y', 10, 9, 'wenzhou', 'wz', 'auto');

insert into gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) 
values ('210800', '营口', '210000', '辽宁省', 'N', 'N', 'Y', 10, 9, 'yingkou', 'yk', 'auto');

insert into gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) 
values ('220200', '吉林', '220000', '吉林省', 'N', 'N', 'Y', 10, 9, 'jilin', 'jl', 'auto');

insert into gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) 
values ('140300', '阳泉', '140000', '山西省', 'N', 'N', 'Y', 10, 9, 'yangquan', 'yq', 'auto');

insert into gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) 
values ('620500', '天水', '620000', '山甘省', 'N', 'N', 'Y', 10, 9, 'tianshui', 'ts', 'auto');

delete from webdata.gewa_city where cityname in('德清','东阳','义乌','海宁','宁海','余姚','慈溪','上虞','富阳','昆山','常熟');
ahfy|anshan|baise|baishan|bangbu|baoding|baotou|beihai|beijing|binzhou|bozhou|changzhou|chaohu|chaozhou|chengdou|chizhou|chuzhou|dalian|daqing|datong|dehong|dezhou|dongguan|dongying|eerduosi|enshi|foshan|fushun|fuzhou|ganzhou|guangyuan|guangzhou|guilin|guiyang|guyuan|haerbin|hangzhou|hanzhong|hefei|hengyang|heyuan|hezhou|huaian|huangshi|huhehaote|huizhou|huludao|hulunbeier|huzhou|jian|jiangmen|jiangyin|jiaxing|jieyang|jilin|jinan|jincheng|jingdezhen|jingmen|jingzhou|jinhua|jining|jiujiang|jiuquan|jstz|kunming|laiwu|langfang|lanzhou|lianyungang|linfen|linyi|liuan|liupanshui|liuzhou|longyan|luohe|maanshan|maoming|meizhou|mianyang|mudanjiang|nanchang|nanjing|nanning|nanping|nantong|nanyang|ningbo|puer|putian|puyang|qiandongnanmiaozudongzuzhou|qingdao|qingyuan|qinhuangdao|qinzhou|qiqihaer|quanzhou|quzhou|rizhao|sanming|shamen|shanghai|shangqiu|shangrao|shanwei|shaoguan|shaoxing|shenyang|shenzhen|shijiazhuang|shiyan|suihua|suqian|suzhou|taishan|taiyuan|taizhou|tangshan|tianjin|tianshui|tieling|weifang|weihai|wenzhou|wuhai|wuhan|wuhu|wuxi|wuzhou|xian|xiangyang|xianning|xiantao|xiaogan|xingtai|xining|xinxiang|xinyang|xuchang|xuzhou|yaan|yanbian|yancheng|yangjiang|yangquan|yangzhou|yantai|yichang|yichun|yinchuan|yingkou|yiyang|yueyang|yulin|yuncheng|yuxi|zhangchun|zhangjiakou|zhangle|zhangsha|zhangzhou|zhanjiang|zhaoqing|zhengzhou|zhenjiang|zhongqing|zhongshan|zhuzhou|zibo|zunyi






update webdata.gewa_city set provincecode='450000' where citycode='450100';
update webdata. gewa_city set cityname='呼伦贝尔',pinyin='hulunbeier',py='hlbe' where citycode='150700';

insert into apiuser (RECORDID, PARTNERKEY, CONTENT, PRIVATEKEY, PARTNERNAME, UPDATETIME, CLERK, STATUS,  BRIEFNAME, ROLES, PARTNERPATH, CITYCODE, DEFAULTCITY)
values (50000975, 'huashu', 'huashu', 'afb749faba7b095e3a41d20ff8a0e1f8', 'huashu', sysdate, 5, 'open', 'huashu', 'apiuser', 'huashu', '330100', '330100');


alter table OPENMEMBER add VALIDTIME timestamp;

insert into gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) values ('630100', '西宁', '630000', '青海省', 'N', 'N', 'Y', 10, 9, 'xining', 'xn', 'auto');
insert into gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) values ('360100', '南昌', '360000', '江西省', 'N', 'N', 'Y', 10, 9, 'nanchang', 'nc', 'auto');
insert into gewa_city (CITYCODE, CITYNAME, PROVINCECODE, PROVINCENAME, SHOWHOT, SHOWIDX, SHOWADM, CITYSORT, PROVINCESORT, PINYIN, PY, MANMETHOD) values ('360200', '景德镇', '360000', '江西省', 'N', 'N', 'Y', 10, 9, 'jingdezhen', 'jdz', 'auto');

create table GEWA_CITY
(
  CITYCODE     VARCHAR2(6) not null,
  CITYNAME     VARCHAR2(30) not null,
  PROVINCECODE VARCHAR2(6) not null,
  PROVINCENAME VARCHAR2(20) not null,
  SHOWHOT      CHAR(1) default 'N' not null,
  SHOWIDX      CHAR(1) default 'N' not null,
  SHOWADM      CHAR(1) default 'Y' not null,
  CITYSORT     NUMBER(3) default 0 not null,
  PROVINCESORT NUMBER(3) default 0 not null,
  PINYIN       VARCHAR2(60) not null,
  PY           VARCHAR2(10) not null,
  MANMETHOD    VARCHAR2(10)
);
alter table GEWA_CITY add constraint PK_GEWA_CITY primary key (CITYCODE) using index tablespace tbs_index;
alter table GEWA_CITY add constraint UK_GEWA_CITY_CITYNAME unique (CITYNAME) using index tablespace tbs_index;
alter table GEWA_CITY add constraint UK_GEWA_CITY_PINYIN unique (PINYIN) using index tablespace tbs_index;
grant select, insert, update on GEWA_CITY to SHANGHAI;



insert into webdata.city(citycode,cityname,provincecode) values('420607','襄阳市', '420000');
insert into webdata.city(citycode,cityname,provincecode) values('350182','长乐市', '350000');
insert into webdata.county(citycode,briefname,countycode,countyname) values('440700','蓬江','440786','蓬江区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','石碣','441905','石碣');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','石龙','441906','石龙');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','茶山','441907','茶山');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','石排','441908','石排');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','企石','441909','企石');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','横沥','441910','横沥');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','桥头','441911','桥头');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','谢岗','441912','谢岗');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','东坑','441913','东坑');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','常平','441914','常平');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','寮步','441915','寮步');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','大朗','441916','大朗');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','黄江','441917','黄江');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','清溪','441918','清溪');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','塘厦','441919','塘厦');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','凤岗','441920','凤岗');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','长安','441921','长安');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','虎门','441922','虎门');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','厚街','441923','厚街');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','沙田','441924','沙田');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','道滘','441925','道滘');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','洪梅','441926','洪梅');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','麻涌','441927','麻涌');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','中堂','441928','中堂');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','高埗','441929','高埗');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','樟木头','441930','樟木头');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','大岭山','441931','大岭山');
insert into webdata.county(citycode,briefname,countycode,countyname) values('441900','望牛墩','441932','望牛墩');




alter table THEATRE_SEAT_AREA rename column GPHOTZONE to MOBILEHOTZONE;
alter table THEATRE_FIELD rename column GPLOGO to MOBILELOGO;

alter table webdata.theatre_field add gplogo varchar2(100);
alter table webdata.theatre_seat_area add GPHOTZONE VARCHAR2(4000);

insert into webdata.county(citycode,briefname,countycode,countyname) values('210100','浑南新区','210182','浑南新区');

insert into webdata.city(citycode,cityname,provincecode) values('440781','台山市', '440000');
insert into webdata.county(citycode,briefname,countycode,countyname) values('120000','和平','120116','和平区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('130500','桥东','130536','桥东区');
insert into webdata.county(citycode,briefname,countycode,countyname) values('130500','桥西','130537','桥西区');

insert into webdata.city(citycode,cityname,provincecode) values('330521','德清', '330000');
insert into webdata.city(citycode,cityname,provincecode) values('330783','东阳', '330000');
insert into webdata.city(citycode,cityname,provincecode) values('330481','海宁', '330000');
insert into webdata.city(citycode,cityname,provincecode) values('330226','宁海', '330000');
insert into webdata.city(citycode,cityname,provincecode) values('330682','上虞', '330000');
insert into webdata.city(citycode,cityname,provincecode) values('330782','义乌', '330000');
alter table webdata.openmember add category varchar2(20);
alter table webdata.openmember add nickname varchar2(50);
alter table webdata.apiuser_extra add SOURCEMETHOD VARCHAR2(500);

alter table WEBDATA.TICKET_ORDER add EXPRESS char(1) default 'N' not null;
alter table WEBDATA.ORDER_NOTE add EXPRESS char(1) default 'N' not null;

create table API_USER_BUSINESS
(
  RECORDID       NUMBER(19) not null,
  SHOWMODEL      VARCHAR2(20) not null,
  COOPMODEL      CHAR(1) not null,
  MONEYTO        VARCHAR2(20) not null,
  GEWABUSUSER    VARCHAR2(400),
  GEWATECUSER    VARCHAR2(400),
  PARTNERBUSUSER VARCHAR2(400),
  PARTNERTECUSER VARCHAR2(400),
  ONTIME         TIMESTAMP(6) not null,
  OFFTIME        TIMESTAMP(6),
  WEBSITE        VARCHAR2(400),
  REMARK         VARCHAR2(2000)
);
alter table API_USER_BUSINESS add constraint PK_API_USER_BUSINESS primary key (RECORDID) using index tablespace tbs_index
grant select,update,insert on webdata.api_user_business to shanghai;

update ticket_order t set t.express='Y' where t.express='N' and t.addtime>sysdate-120 and exists(select o.orderid from webdata.OTHERFEE_DETAIL o where o.orderid=t.recordid and o.feetype='E');
update WEBDATA.ORDER_NOTE t set t.express='Y' where t.express='N' and t.addtime>sysdate-120 and exists(select o.orderid from webdata.OTHERFEE_DETAIL o where o.orderid=t.orderid and o.feetype='E');




-- Create table
create table MEMBER_CARD_INFO
(
  RECORDID       NUMBER(19) not null,
  TYPEID         NUMBER(19) not null,
  MEMBERID       NUMBER(19) not null,
  MEMBERCARDCODE VARCHAR2(50) not null,
  NAME           VARCHAR2(50),
  SEX            VARCHAR2(20),
  MOBILE         VARCHAR2(11) not null,
  VALIDTIME      TIMESTAMP(6),
  OVERMONEY      NUMBER(5) not null,
  CARDSTATUS     VARCHAR2(20) not null,
  FITITEM        VARCHAR2(500),
  ADDTIME        TIMESTAMP(6) not null,
  CARDTYPEUKEY   VARCHAR2(50) not null,
  BELONGVENUE    VARCHAR2(500) not null,
  TRADENO        VARCHAR2(30),
  TYPETITLE      VARCHAR2(50),
  BINDSTATUS     CHAR(1) default 'N' not null
);

alter table MEMBER_CARD_INFO add constraint PK_MEMBER_CARD_INFO primary key (RECORDID)  using index tablespace tbs_index;
alter table MEMBER_CARD_INFO add constraint UK_MEMBER_CARD_INFO_MCI unique (MEMBERCARDCODE) using index tablespace tbs_index;
create index IDX_MEMBER_CARD_INFO_MEMBERID on MEMBER_CARD_INFO (MEMBERID) tablespace tbs_index;
grant select, insert, update on MEMBER_CARD_INFO to SHANGHAI;


create table MEMBER_CARD_TYPE
(
  CARDTYPEUKEY VARCHAR2(60) not null,
  CARDTYPECODE VARCHAR2(60) not null,
  CARDTYPE     CHAR(1) not null,
  MONEY        NUMBER(5) not null,
  OVERNUM      NUMBER(5) not null,
  RESERVE      NUMBER(5) not null,
  DESCRIPTION  VARCHAR2(2000),
  VALIDTIME    NUMBER(5) not null,
  PRICE        NUMBER(5) not null,
  DISCOUNT     NUMBER(5) not null,
  FITITEM      VARCHAR2(500),
  BELONGVENUE  VARCHAR2(500) not null,
  BUSINESSID   NUMBER(19) not null,
  SALES        NUMBER(5) not null,
  COSTRATE     NUMBER(3),
  MINGAIN      NUMBER(5) not null,
  STATUS       VARCHAR2(20) not null,
  OPENTIME     TIMESTAMP(6) not null,
  CLOSETIME    TIMESTAMP(6) not null,
  SPFLAG       VARCHAR2(30),
  MINPOINT     NUMBER(5) not null,
  MAXPOINT     NUMBER(5) not null,
  ELECARD      VARCHAR2(20),
  REMARK       VARCHAR2(500),
  OTHERINFO    VARCHAR2(500) not null,
  RECORDID     NUMBER(19) not null,
  ADDTIME      TIMESTAMP(6) not null,
  GEWAPRICE    NUMBER(5),
  NOTIFYMSG    VARCHAR2(100)
);

alter table MEMBER_CARD_TYPE add constraint PK_MEMBER_CARD_TYPE primary key (RECORDID) using index tablespace tbs_index;
alter table MEMBER_CARD_TYPE add constraint UK_MEMBER_CARD_TYPE unique (CARDTYPEUKEY) using index tablespace tbs_index;
grant select, insert, update on MEMBER_CARD_TYPE to SHANGHAI;

-- Create table
create table MEMBER_CARD_TYPE_PLACE
(
  RECORDID NUMBER(19) not null,
  PLACEID  NUMBER(19) not null,
  MCTID    NUMBER(19) not null
);

alter table MEMBER_CARD_TYPE_PLACE add constraint PK_MEMBER_CARD_TYPE_PLACE primary key (RECORDID) using index tablespace tbs_index;
alter table MEMBER_CARD_TYPE_PLACE add constraint UK_MEMBER_CARD_TYPE_PLACE unique (PLACEID, MCTID) using index tablespace tbs_index;
grant select, insert, update, delete on MEMBER_CARD_TYPE_PLACE to SHANGHAI;















alter table webdata.orderorigin add APPTYPE VARCHAR2(30);
alter table webdata.orderorigin add OSTYPE VARCHAR2(30);
alter table webdata.orderorigin add APPVERSION VARCHAR2(10);
alter table webdata.orderorigin add MOBILETYPE VARCHAR2(50);

insert into apiuser (RECORDID, PARTNERKEY, CONTENT, PRIVATEKEY, PARTNERNAME, UPDATETIME, CLERK, STATUS, LOGINNAME, LOGINPASS, BRIEFNAME, ROLES, PARTNERPATH, CITYCODE, DEFAULTCITY)
values (50000080, 'androdidrama', 'androdidrama', 'dbed06e7e046d2b43cbfbdcb5773646b', 'androdidrama', sysdate, 5, 'open', 'androdidrama', 'dsfsd', 'androdidrama', 'apiuser', 'androdidrama', '000000', '310000');


grant select,update,insert,delete on webdata.member_card_type_place to shanghai

insert into webdata.gewaconfig(recordid,description,updatetime,newcontent) values(84,'stop membercard',sysdate,'2013-01-01 00:00:00')

grant select,update,insert on webdata.member_card_type to shanghai;

insert into apiuser (RECORDID, PARTNERKEY, CONTENT, PRIVATEKEY, PARTNERNAME, UPDATETIME, CLERK, STATUS, LOGINNAME, LOGINPASS, BRIEFNAME, ROLES, PARTNERPATH, CITYCODE, DEFAULTCITY)
values (50000964, 'unionpayterminal', 'unionpayterminal', '51b35b065706a24b251dfabb02b252fb', 'unionpayterminal', sysdate, 5, 'open', 'unionpayterminal', 'dsfsd', 'unionpayterminal', 'apiuser', 'unionpayterminal', '000000', '310000');


insert into apiuser (RECORDID, PARTNERKEY, CONTENT, PRIVATEKEY, PARTNERNAME, UPDATETIME, CLERK, STATUS, LOGINNAME, LOGINPASS, BRIEFNAME, ROLES, PARTNERPATH, CITYCODE, DEFAULTCITY)
values (50000961, 'tv189', 'tv189', 'bd7381cf6d777028c2af7071b633be53', 'tv189', sysdate, 5, 'open', 'tv189', 'dsfsd', 'tv189', 'apiuser', 'tv189', '000000', '310000');

insert into apiuser (RECORDID, PARTNERKEY, CONTENT, PRIVATEKEY, PARTNERNAME, UPDATETIME, CLERK, STATUS, BRIEFNAME, ROLES, PARTNERPATH, CITYCODE, DEFAULTCITY)
values (50000962, 'zhongsou', 'zhongsou', '2f568cff793d0abc964aa7c4f360809e', 'zhongsou', sysdate, 5, 'open', 'zhongsou', 'apiuser', 'zhongsou', '000000', '310000');

insert into apiuser (RECORDID, PARTNERKEY, CONTENT, PRIVATEKEY, PARTNERNAME, UPDATETIME, CLERK, STATUS, LOGINNAME, LOGINPASS, BRIEFNAME, ROLES, PARTNERPATH, CITYCODE, DEFAULTCITY)
values (50000963, 'daohang2345', 'daohang2345', 'acdbec6f96c6fb8fca5c6b33feb39cd2', 'daohang2345', sysdate, 5, 'open', 'daohang2345', 'dsfsd', 'daohang2345', 'apiuser', 'daohang2345', '000000', '310000');





update webdata.theatre_seat_price t set t.seller='GPTBS' where exists (select d.recordid from webdata.drama_play_item d where d.recordid=t.dpid and d.seller='GPTBS')
and t.seller='GEWA';


alter table OPEN_DRAMA_ITEM add PRINT char(1);
update webdata.open_drama_item set print='Y' where print is null;

alter table DISQUANTITY add DISTYPE char(1);
update webdata.disquantity d set d.distype='P' where distype is null; 
update webdata.disquantity d set d.distype='G' where dpid in(110283616,110283620,110283589); 

alter table webdata.cooper_user add origin varchar(50);


alter table SYNCH add GSUCTIME timestamp(6);
alter table SYNCH add GSYNTIME timestamp(6);
alter table SYNCH add GTICKETNUM varchar2(50);
update webdata.synch s set s.gsuctime=s.successtime where s.gsuctime is null;
update webdata.synch s set s.gsyntime=s.synchtime where s.gsyntime is null;


insert into webdata.cooper_user(recordid, loginname,loginpass,name,partnerid,partnerids,appsource,status,usertype,roles)
values(1,'91120cps','37c5064221bca0f43246f0a9a8735f53','91120cps', 50000020,'50000020', 'AS120', 'OPEN', 'ticket', 'apiuser,queryOrder');
insert into webdata.cooper_user(recordid, loginname,loginpass,name,partnerid,partnerids,appsource,status,usertype,roles)
values(2,'xiaomi','8d2ef0b6bfe34cb6aa2b284bf22603e5','xiaomi', 50000020,'50000020', 'AS114', 'open', 'ticket', 'apiuser,appSourceQry');


insert into webdata.cooper_user(recordid, loginname,loginpass,name,partnerid,partnerids,appsource,status,usertype,roles)
values(3,'lenovocps','abdc4d917ce6b290ce800f12578fbe02','lenovocps', 50000020,'50000020', 'AS113', 'open', 'ticket', 'apiuser,appSourceQry');

