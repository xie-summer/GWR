create table webdata.deal_express_order(
	recordid number(19) not null,
	orderid number(19) not null,
	tradeNo varchar2(30) not null,
	dealuser number(19) not null,
	nickname varchar2(200) not null,
	mobile varchar2(50),
	dealtype varchar2(10) not null,
	dealStatus varchar2(10) not null,
	addtime timestamp(6) not null,
	remark varchar2(100),
	constraint pk_deal_express_order primary key(recordid) using index tablespace tbs_index
);
grant select, insert on webdata.deal_express_order to shanghai;

create index webdata.idx_deal_express_order_orderid on webdata.deal_express_order(orderid) tablespace tbs_index;
create index webdata.idx_deal_express_order_tradeno on webdata.deal_express_order(tradeno) tablespace tbs_index;

alter table webdata.order_extra add EXPRESSSTATUS VARCHAR2(10) default 'new' not null;
alter table webdata.order_extra add DEALSTATUS VARCHAR2(10) default 'new' not null;
alter table webdata.order_extra add DEALUSER NUMBER(19);
alter table webdata.order_extra_his add EXPRESSSTATUS VARCHAR2(10) default 'new' not null;
alter table webdata.order_extra_his add DEALSTATUS VARCHAR2(10) default 'new' not null;
alter table webdata.order_extra_his add DEALUSER NUMBER(19);

alter table webdata.open_playitem add EXPRESSID VARCHAR2(20);