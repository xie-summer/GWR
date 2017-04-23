--table
CREATE TABLE WEBDATA.REPEATING_PAYORDER (	
RECORDID vARCHAR2(50) NOT NULL, 
TRADE_NO VARCHAR2(25), 
PAYSEQNO vARCHAR2(25), 
PAYMETHOD VARCHAR2(30), 
NOTIFY_TIME TIMESTAMP (6), 
STATUS VARCHAR2(10 ), 
AMOUNT NUMBER(5), 
SUCCESS_PAYMETHOD VARCHAR2(30), 
CONFIRM_USER VARCHAR2(50 ), 
CONFIRM_TIME TIMESTAMP (6), 
CONSTRAINT REPEATING_PAYORDER_PK PRIMARY KEY (RECORDID) using index tablespace tbs_index
);

grant select,insert,update on REPEATING_PAYORDER to shanghai;
--view
CREATE OR REPLACE VIEW VIEW_REPEATING_PAYORDER AS
SELECT RECORDID,TRADE_NO,PAYSEQNO,PAYMETHOD,NOTIFY_TIME,STATUS,AMOUNT,SUCCESS_PAYMETHOD,CONFIRM_USER,CONFIRM_TIME 
FROM WEBDATA.REPEATING_PAYORDER;

grant select on VIEW_REPEATING_PAYORDER to baobiao;

--member id query
select recordid, mobile from member where mobile in ('13381971635','13818812026','13761015109','15021763765','18666130269','15821481750','13764478527','15201972328','18917707033','13482163621','13917901075','13661536594','13381151580','18917705859','13951059000','13916558593','18917608051','18917706588','15216715720','13581999919','15618677550','13621934630','13917302507','18710600512','13818799239','18101880926','18017766825','18621553979','13774451767','18616649980','13917808754','18917706566','18916666915','13761043477','13916461784','13918656639','13681911147','13611665266','13611681240','13482130003','13795454972','13917221477','13816896876','','13764213688','13761928015','13391122901','13764051427','18616902795','18621553996','13651886575','13918103604');


--table
CREATE TABLE WEBDATA.PAY_METHOD (	
PAYMETHOD vARCHAR2(30) NOT NULL, 
PAYMETHOD_TEXT vARCHAR2(50),
FLAG VARCHAR2(10 ),
CONSTRAINT PAY_METHOD_PK PRIMARY KEY (PAYMETHOD) using index tablespace tbs_index
);
grant select on PAY_METHOD to shanghai;

--pay_method init
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('gewaPay','格瓦余额');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('sysPay','系统');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('elecardPay','电子券');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('ccardPay','格瓦充值卡');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('lakalaPay','拉卡拉');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('abcPay','农行合作');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('wcanPay','微能科技积分兑换');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('partnerPay','合作商');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('okcardPay','联华OK');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('spsdoPay','盛大合作');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('directPay','支付宝PC端');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('pnrPay','汇付天下PC端');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('cmPay','移动手机支付PC端');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('tempusPay','腾付通PC端');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('spsdo2Pay','盛付通PC端');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('chinaPay','银联便民');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('china2Pay','ChinapayPC端');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('srcbPay','Chinapay农商行');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('unionPay','unionPay银联支付');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('unionPayFast','unionPayFast银联快捷支付');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('unionPayFast_activity_js','unionPayFast江苏活动');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('unionPayFast_activity_bj','银联认证2.0北京活动');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('unionPay_js','unionPay江苏');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('unionPay_activity','unionPay活动');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('unionPay_activity_js','unionPay江苏活动');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('bcPay','交行直连PC端');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('spdPay','浦发直连PC端');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('cmbPay','招行直连PC端');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('gdbPay','广发直连PC端');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('psbcPay','邮储直连PC端');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('hzbankPay','杭州银行');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('ccbposPay','建行直连PC端-信用卡');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('jsbChina','江苏银行直连PC端-信用卡');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('spdPay_activity','浦发直连PC端-活动');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('bocPay','中国银行直连PC端');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('bocWapPay','中国银行直连WAP端');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('bocAgrmtPay','中国银行协议支付');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('umPay','移动话费支付(联动优势)');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('umPay_sh','移动话费支付(联动优势)_上海');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('telecomPay','电信固话话费支付');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('telecomMobilePay','电信手机话费支付');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('yagaoPay','雅高卡支付(艾登瑞德)');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('onetownPay','一城卡支付(新华传媒)');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('aliwapPay','支付宝手机端-WAP支付');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('cmwapPay','移动手机支付手机端-WAP支付');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('cmbwapPay','招行直连手机端');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('spdWapPay','浦发直连手机端-WAP');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('cmSmartPay','移动手机支付安卓版');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('spdWapPay_activity','浦发直连手机端-活动');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('chinaSmartMobilePay','银联手机在线支付');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('chinaSmartJsPay','江苏银联手机端-江苏银商收单');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('aliSmartMobilePay','支付宝手机端-安全支付');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('hzwapPay','杭州银行WAP');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('yeePay','易宝支付PC端');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('payecoDNAPay','易联DNA支付');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('sdoPay','盛大积分');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('tenPay','财付通');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('ipsPay','环讯PC端-信用卡支付');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('bcwapPay','交通WAP');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('allinPay','通联');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('alibankPay','支付宝招商银行WAP');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('handwapPay','翰银WAP');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('handwebPay','翰银WEB');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('pnrfastPay','汇付快捷支付--华夏信用卡');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('pnrfastPay2','汇付快捷支付--建行信用卡');
insert into webdata.PAY_METHOD (PAYMETHOD,PAYMETHOD_TEXT) values('pnrfastabcPay','汇付快捷支付--农行信用卡');

insert into webdata.PAY_METHOD( PAYMETHOD,PAYMETHOD_TEXT) values('icbcPay','工商银行直连支付PC端');
insert into webdata.PAY_METHOD( PAYMETHOD,PAYMETHOD_TEXT) values('njcbPay','南京银行直连支付PC端');
insert into webdata.PAY_METHOD( PAYMETHOD,PAYMETHOD_TEXT) values('abchinaPay','农业银行直连支付PC端');
