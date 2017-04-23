alter table SUBWAYSTATION add citycode varchar2(6);
update subwaystation m set citycode=(
select max(citycode) from subwayline t where recordid in (select s.lid from line2station s where s.lid=t.recordid  and m.recordid=s.sid)
);

insert into subwaystation(recordid,stationname,citycode) values(586,'堤角','420100');
insert into subwaystation(recordid,stationname,citycode) values(587,'新荣','420100');
insert into subwaystation(recordid,stationname,citycode) values(588,'丹水池','420100');
insert into subwaystation(recordid,stationname,citycode) values(589,'徐州新村','420100');
insert into subwaystation(recordid,stationname,citycode) values(590,'二七路','420100');
insert into subwaystation(recordid,stationname,citycode) values(591,'头道街','420100');
insert into subwaystation(recordid,stationname,citycode) values(592,'黄浦路','420100');
insert into subwaystation(recordid,stationname,citycode) values(593,'三阳路','420100');
insert into subwaystation(recordid,stationname,citycode) values(594,'大智路','420100');
insert into subwaystation(recordid,stationname,citycode) values(595,'循礼门','420100');
insert into subwaystation(recordid,stationname,citycode) values(596,'友谊路','420100');
insert into subwaystation(recordid,stationname,citycode) values(597,'利济北路','420100');
insert into subwaystation(recordid,stationname,citycode) values(598,'崇仁路','420100');
insert into subwaystation(recordid,stationname,citycode) values(599,'硚口路','420100');
insert into subwaystation(recordid,stationname,citycode) values(600,'太平洋','420100');
insert into subwaystation(recordid,stationname,citycode) values(601,'宗关','420100');
insert into subwaystation(recordid,stationname,citycode) values(602,'汉西一路','420100');
insert into subwaystation(recordid,stationname,citycode) values(603,'古田四路','420100');
insert into subwaystation(recordid,stationname,citycode) values(604,'古田三路','420100');
insert into subwaystation(recordid,stationname,citycode) values(605,'古田二路','420100');
insert into subwaystation(recordid,stationname,citycode) values(606,'古田一路','420100');
insert into subwaystation(recordid,stationname,citycode) values(607,'舵落口','420100');
insert into subwaystation(recordid,stationname,citycode) values(608,'额头湾','420100');
insert into subwaystation(recordid,stationname,citycode) values(609,'五环大道','420100');
insert into subwaystation(recordid,stationname,citycode) values(610,'东吴大道','420100');

insert into subwayline (recordid,citycode,linename,remark)
values(31,'420100','1号线','堤角-东吴大道');

insert into line2station(sid,lid,stationorder,recordid)
select recordid,31,recordid-585, recordid+10 from subwaystation where recordid>=586 and recordid<=610;

insert into subwaystation(recordid,stationname,citycode) values(611,'金银潭','420100');
insert into subwaystation(recordid,stationname,citycode) values(612,'常青花园','420100');
insert into subwaystation(recordid,stationname,citycode) values(613,'长港路','420100');
insert into subwaystation(recordid,stationname,citycode) values(614,'汉口火车站','420100');
insert into subwaystation(recordid,stationname,citycode) values(615,'范湖','420100');
insert into subwaystation(recordid,stationname,citycode) values(616,'王家墩东','420100');
insert into subwaystation(recordid,stationname,citycode) values(617,'青年路','420100');
insert into subwaystation(recordid,stationname,citycode) values(618,'中山公园','420100');
insert into subwaystation(recordid,stationname,citycode) values(619,'循礼门','420100');
insert into subwaystation(recordid,stationname,citycode) values(620,'江汉路','420100');
insert into subwaystation(recordid,stationname,citycode) values(621,'积玉桥','420100');
insert into subwaystation(recordid,stationname,citycode) values(622,'螃蟹岬','420100');
insert into subwaystation(recordid,stationname,citycode) values(623,'小龟山','420100');
insert into subwaystation(recordid,stationname,citycode) values(624,'洪山广场','420100');
insert into subwaystation(recordid,stationname,citycode) values(625,'中南路','420100');
insert into subwaystation(recordid,stationname,citycode) values(626,'宝通寺','420100');
insert into subwaystation(recordid,stationname,citycode) values(627,'街道口','420100');
insert into subwaystation(recordid,stationname,citycode) values(628,'广埠屯','420100');
insert into subwaystation(recordid,stationname,citycode) values(629,'虎泉','420100');
insert into subwaystation(recordid,stationname,citycode) values(630,'杨家湾','420100');
insert into subwaystation(recordid,stationname,citycode) values(631,'光谷广场','420100');

insert into subwayline (recordid,citycode,linename,remark)
values(32,'420100','2号线','金银潭-光谷广场');

insert into line2station(sid,lid,stationorder,recordid)
select recordid,32,recordid-610, recordid+10 from subwaystation where recordid>=611 and recordid<=631;

delete from subwaystation where recordid not in (select sid from line2station);
--去除站
update subwaystation set stationname=substring(stationname,1,len(stationname)-1) where stationname like '%站' and recordid not in (7702792,170149,43,100,2,110,133,148,161,164,203,477,500,170203,60,533,558,614);

insert into subwaystation(recordid,stationname,citycode) values(632,'金运路','310000');
insert into subwaystation(recordid,stationname,citycode) values(633,'江桥','310000');
insert into subwaystation(recordid,stationname,citycode) values(634,'丰庄','310000');
insert into subwaystation(recordid,stationname,citycode) values(635,'祁连山南路','310000');
insert into subwaystation(recordid,stationname,citycode) values(636,'真北路','310000');
insert into subwaystation(recordid,stationname,citycode) values(637,'大渡河路','310000');
insert into subwaystation(recordid,stationname,citycode) values(640,'武宁路','310000');
insert into subwaystation(recordid,stationname,citycode) values(642,'江宁路','310000');
insert into subwaystation(recordid,stationname,citycode) values(644,'自然博物馆','310000');
insert into subwaystation(recordid,stationname,citycode) values(646,'淮海中路','310000');
insert into subwaystation(recordid,stationname,citycode) values(649,'卢浦大桥','310000');
insert into subwaystation(recordid,stationname,citycode) values(650,'世博大道','310000');
insert into subwaystation(recordid,stationname,citycode) values(653,'东明路','310000');
insert into subwaystation(recordid,stationname,citycode) values(654,'六里','310000');
insert into subwaystation(recordid,stationname,citycode) values(655,'下南路','310000');
insert into subwaystation(recordid,stationname,citycode) values(656,'北蔡','310000');
insert into subwaystation(recordid,stationname,citycode) values(657,'绿川新村','310000');
insert into subwaystation(recordid,stationname,citycode) values(658,'莲溪路','310000');
insert into subwaystation(recordid,stationname,citycode) values(659,'华夏中路','310000');
insert into subwaystation(recordid,stationname,citycode) values(660,'中科路','310000');
insert into subwaystation(recordid,stationname,citycode) values(661,'哥白尼路','310000');
insert into subwaystation(recordid,stationname,citycode) values(662,'张江路','310000');

insert into subwayline (recordid,citycode,linename,remark)
values(33,'310000','13号线','金运路-张江路');

update line2station set STATIONORDER=1 where recordid=441;
update line2station set STATIONORDER=2 where recordid=440;
update line2station set STATIONORDER=5 where recordid=439;
update line2station set STATIONORDER=6 where recordid=59;
update line2station set STATIONORDER=7 where recordid=163;
update line2station set STATIONORDER=8 where recordid=162;
update line2station set STATIONORDER=9 where recordid=147;
update line2station set STATIONORDER=10 where recordid=146;
update line2station set STATIONORDER=11 where recordid=145;
update line2station set STATIONORDER=12 where recordid=144;
update line2station set STATIONORDER=13 where recordid=143;
update line2station set STATIONORDER=14 where recordid=142;
update line2station set STATIONORDER=16 where recordid=141;
update line2station set STATIONORDER=17 where recordid=161;
update line2station set STATIONORDER=20 where recordid=239;
update line2station set STATIONORDER=23 where recordid=261;
update line2station set STATIONORDER=24 where recordid=159;
update line2station set STATIONORDER=26 where recordid=140;
update line2station set STATIONORDER=27 where recordid=139;
update line2station set STATIONORDER=28 where recordid=138;
update line2station set STATIONORDER=29 where recordid=137;
update line2station set STATIONORDER=30 where recordid=136;
update line2station set STATIONORDER=32 where recordid=58;
update line2station set STATIONORDER=22 where recordid=160;

update subwaystation set stationname='龙华中路',citycode='310000' where recordid=160;
update subwaystation set stationname='后滩' where recordid=240847;

insert into line2station(stationorder, lid, sid, recordid) values(31,240837,170188,693);
insert into line2station(stationorder, lid, sid, recordid) values(21,240837,170224,694);
insert into line2station(stationorder, lid, sid, recordid) values(15,240837,102,695);
insert into line2station(stationorder, lid, sid, recordid) values(31,240837,240817,696);
insert into line2station(stationorder, lid, sid, recordid) values(19,240837,170154,697);
insert into line2station(stationorder, lid, sid, recordid) values(18,240837,170179,698);
insert into line2station(stationorder, lid, sid, recordid) values(25,240837,170248,699);



insert into subwaystation(recordid,stationname,citycode) values(663,'刘行','310000');
insert into subwaystation(recordid,stationname,citycode) values(664,'潘广路','310000');

--(\d+)\t(\d+)\t(\d+)\t(\d+)
--insert into line2station(stationorder, lid, sid, recordid) values(\1,\2,\3,\4);
--(\d+)\t(.*)\t(\d+)
--insert into subwaystation(recordid,stationname,citycode) values(\1,'\2','\3');

