INSERT INTO WEBDATA.PAY_METHOD(PAYMETHOD, PAYMETHOD_TEXT) VALUES('bfbPay', '百度钱包支付');

INSERT INTO WEBDATA.PAY_METHOD(PAYMETHOD, PAYMETHOD_TEXT) VALUES('bfbWapPay', '百度钱包wap支付');

INSERT INTO WEBDATA.PAY_METHOD(PAYMETHOD, PAYMETHOD_TEXT) VALUES('bestPay', '翼支付');

INSERT INTO WEBDATA.PAY_METHOD(PAYMETHOD, PAYMETHOD_TEXT) VALUES('oneClickTenPay', '财付通移动终端一键支付');

---------------------------------------------------------------
INSERT INTO WEBDATA.PAY_METHOD(PAYMETHOD, PAYMETHOD_TEXT) VALUES('ccbWapPay', '建行手机wap支付');

-----------------2013-09-22-----------------------
INSERT INTO WEBDATA.PAY_METHOD(PAYMETHOD, PAYMETHOD_TEXT) VALUES('wxAppTenPay', '财付通微信支付（APP间）');
INSERT INTO WEBDATA.PAY_METHOD(PAYMETHOD, PAYMETHOD_TEXT) VALUES('wxScanTenPay', '财付通微信支付（扫码）');


------热门影院统计数据
create table HOTSPOT_CINEMA
(
  CINEMAID    NUMBER(19) not null,
  BUYQUANTITY NUMBER(10) not null
)
-- Create/Recreate primary, unique and foreign key constraints 
alter table HOTSPOT_CINEMA
  add constraint PK_HOTSPOT_CINEMA primary key (CINEMAID)
  using index tablespace tbs_index;
  
  
grant select,insert,update on webdata.HOTSPOT_CINEMA to shanghai;
