alter table C_COMMENT add APPTYPE varchar2(10);

alter table SPORT_PROFILE add COMPANY varchar2(50);

insert into apiuser (RECORDID, PARTNERKEY, CONTENT, PRIVATEKEY, PARTNERNAME, UPDATETIME, CLERK, STATUS, LOGINNAME, LOGINPASS, BRIEFNAME, ROLES, PARTNERPATH, OPENTYPE, CITYCODE, DEFAULTCITY)
values (50000880, 'sand', 'sand', 'sand20111201', 'sand', sysdate, 5, 'open', 'sand', 'password', 'sand', 'apiuser,queryOrder,addOrderApi,payOrderApi', 'sand', 'all', '310000', '310000');

qryurl = http://www.gewara.com/partner/sand/qryOrder.xhtml
addordeurl = http://payment.sandpay.com.cn/SandPayPort/GewaraServet
notifyurl = http://manage.gewara.com/pay/partnerPayNotify.xhtml
--���У��KEY  sand20111201

drop index UK_TREASURE;



insert into apiuser (RECORDID, PARTNERKEY, CONTENT, PRIVATEKEY, PARTNERNAME, UPDATETIME, CLERK, STATUS, LOGINNAME, LOGINPASS, BRIEFNAME, ROLES, PARTNERPATH, OPENTYPE, CITYCODE, DEFAULTCITY)
values (50000880, '962288', '962288', '96228820111201', '962288', sysdate, 5, 'open', '962288', 'password', '962288', 'apiuser,queryOrder,addOrderApi,payOrderApi', '962288', 'all', '310000', '310000');


insert into apiuser (RECORDID, PARTNERKEY, CONTENT, PRIVATEKEY, PARTNERNAME, UPDATETIME, CLERK, STATUS, LOGINNAME, LOGINPASS, BRIEFNAME, ROLES, PARTNERPATH, OPENTYPE, CITYCODE, DEFAULTCITY)
values (50000885, 'mobile_ticket', 'mobile_ticket', 'mobile_ticket20111201', 'mobile_ticket', sysdate, 5, 'open', 'mobile_ticket', 'password', 'mobile_ticket', 'apiuser,queryOrder,addOrderApi,payOrderApi', 'mobile_ticket', 'all', '310000', '310000');
