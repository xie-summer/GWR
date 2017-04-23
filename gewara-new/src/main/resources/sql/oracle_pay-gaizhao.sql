ALTER TABLE WEBDATA.TICKET_ORDER ADD ( GATEWAY_CODE VARCHAR2(30) NULL ) ;
ALTER TABLE WEBDATA.TICKET_ORDER ADD ( MERCHANT_CODE VARCHAR2(30) NULL ) ;

/*==============================================================*/
/* Table: pay_gateway                                         */
/*==============================================================*/
create table webdata.pay_gateway 
(
   recordid           NUMBER(19,0)         not null,
   gateway_code       VARCHAR2(30)         not null,
   gateway_name       VARCHAR2(90)         not null,
   support_bank       VARCHAR2(10)         not null,
   gateway_type       VARCHAR2(20)         not null,
   status             VARCHAR2(10)         not null,
   bank_type_key      VARCHAR2(200),
   update_time        TIMESTAMP            not null,
   route_status       VARCHAR2(10)         not null,
   modify_time        TIMESTAMP,
   modify_user        VARCHAR2(30),
   constraint PK_PAY_GATEWAY primary key (recordid)
   using index tablespace tbs_index
);

comment on table webdata.pay_gateway is
'支付配置表';

comment on column webdata.pay_gateway.gateway_code is
'支付网关';

comment on column webdata.pay_gateway.gateway_name is
'支付网关名称';

comment on column webdata.pay_gateway.support_bank is
'是否支持银行：Y：支持；N：不支持';

comment on column webdata.pay_gateway.gateway_type is
'类型：PLATFORM：支付平台；BANK：银行直连；CARD：卡支付';

comment on column webdata.pay_gateway.status is
'状态：NO_USE：未启用；IN_USE：使用中；DESUETUDE：废弃；';

comment on column webdata.pay_gateway.bank_type_key is
'银行类型key，这里只放特殊的，形如{C:信用卡,KJ:快捷信用卡支付}';

comment on column webdata.pay_gateway.update_time is
'更新时间';

comment on column webdata.pay_gateway.route_status is
'商户号路由状态：OPEN：开启；CLOSE：关闭；默认：CLOSE';

comment on column webdata.pay_gateway.modify_time is
'修改时间';

comment on column webdata.pay_gateway.modify_user is
'修改人';

/*==============================================================*/
/* Index: UK_PAYGATEWAY_GWCODE                                  */
/*==============================================================*/
create unique index UK_PAYGATEWAY_GWCODE on webdata.pay_gateway (
   gateway_code ASC
);




/*==============================================================*/
/* Table: pay_merchant                                        */
/*==============================================================*/
create table webdata.pay_merchant 
(
   recordid           NUMBER(19,0)         not null,
   gateway_id         NUMBER(19,0)         not null,
   merchant_code      VARCHAR2(30)         not null,
   city_code          VARCHAR2(10),
   acquiring_bank     VARCHAR2(60),
   description        VARCHAR2(90),
   status             VARCHAR2(10)         not null,
   update_time        TIMESTAMP            not null,
   is_default         VARCHAR2(10),
   modify_time        TIMESTAMP,
   modify_user        VARCHAR2(30),
   constraint PK_PAY_MERCHANT primary key (recordid)
   using index tablespace tbs_index
);

comment on table webdata.pay_merchant is
'商户号表';

comment on column webdata.pay_merchant.gateway_id is
'网关配置ID';

comment on column webdata.pay_merchant.merchant_code is
'商户号标识';

comment on column webdata.pay_merchant.city_code is
'城市';

comment on column webdata.pay_merchant.acquiring_bank is
'收单行';

comment on column webdata.pay_merchant.description is
'商户号说明';

comment on column webdata.pay_merchant.status is
'商户号状态：NO_USE：未启用；IN_USE：使用中；DESUETUDE：废弃；';

comment on column webdata.pay_merchant.update_time is
'更新时间';

comment on column webdata.pay_merchant.is_default is
'是否是是否默认：Y ：默认；';

comment on column webdata.pay_merchant.modify_time is
'修改时间';

comment on column webdata.pay_merchant.modify_user is
'修改人';

/*==============================================================*/
/* Index: UK_MERCHANT_CODE                                      */
/*==============================================================*/
create unique index UK_MERCHANT_CODE on webdata.pay_merchant (
   merchant_code ASC
);



/*==============================================================*/
/* Table: pay_gateway_bank                                    */
/*==============================================================*/
create table webdata.pay_gateway_bank 
(
   recordid           NUMBER(19,0)         not null,
   gateway_id         NUMBER(19,0)         not null,
   gwra_bank_code     VARCHAR2(30)         not null,
   bank_name          VARCHAR2(60),
   bank_type          VARCHAR2(20)         not null,
   update_time        TIMESTAMP            not null,
   constraint PK_PAY_GATEWAY_BANK primary key (recordid)
   using index tablespace tbs_index
);

comment on table webdata.pay_gateway_bank is
'支付平台银行表';

comment on column webdata.pay_gateway_bank.gateway_id is
'网关配置ID';

comment on column webdata.pay_gateway_bank.gwra_bank_code is
'格瓦银行代码';

comment on column webdata.pay_gateway_bank.bank_name is
'银行名称';

comment on column webdata.pay_gateway_bank.bank_type is
'银行类型，少数支付平台银行代码不一样，如支付宝，默认值为：DEFAULT';

comment on column webdata.pay_gateway_bank.update_time is
'更新时间';



/*==============================================================*/
/* Table: pay_city_merchant                                   */
/*==============================================================*/
create table webdata.pay_city_merchant 
(
   recordid           NUMBER(19,0)         not null,
   gateway_id         NUMBER(19,0)         not null,
   area_code          VARCHAR2(10)         not null,
   area_type          VARCHAR2(10)         not null,
   merchant_code      VARCHAR2(30)         not null,
   modify_time        TIMESTAMP            not null,
   modify_user        VARCHAR2(30)         not null,
   constraint PK_PAY_CITY_MERCHANT primary key (recordid)
   using index tablespace tbs_index
);

comment on table webdata.pay_city_merchant is
'商户号城市路由配置表';

comment on column webdata.pay_city_merchant.gateway_id is
'网关配置ID';

comment on column webdata.pay_city_merchant.area_code is
'区域代码：省份或者城市代码';

comment on column webdata.pay_city_merchant.area_type is
'区域类型：P：省；C：城市；';

comment on column webdata.pay_city_merchant.merchant_code is
'商户号标识';

comment on column webdata.pay_city_merchant.modify_time is
'修改时间';

comment on column webdata.pay_city_merchant.modify_user is
'修改人';

/*==============================================================*/
/* Index: UIDX_AREA_MERCHANT                                    */
/*==============================================================*/
create unique index UIDX_AREA_MERCHANT on webdata.pay_city_merchant (
   area_code ASC,
   area_type ASC,
   merchant_code ASC
);



/*==============================================================*/
/* Table: pay_interface_switch                                */
/*==============================================================*/
create table webdata.pay_interface_switch 
(
   gateway_code       VARCHAR2(30)         not null,
   interface_type     VARCHAR2(10)         not null,
   add_time           TIMESTAMP            not null,
   modify_time        TIMESTAMP            not null,
   modify_user        VARCHAR2(30)         not null,
   constraint PK_PAY_INTERFACE_SWITCH primary key (gateway_code)
   using index tablespace tbs_index
);

comment on table webdata.pay_interface_switch is
'支付接口转换';

comment on column webdata.pay_interface_switch.gateway_code is
'支付网关';

comment on column webdata.pay_interface_switch.interface_type is
'接口类型：OLD：老；NEW：新';

comment on column webdata.pay_interface_switch.add_time is
'新增时间';

comment on column webdata.pay_interface_switch.modify_time is
'修改时间';

comment on column webdata.pay_interface_switch.modify_user is
'修改人';


grant select,insert,update,delete on webdata.pay_gateway to shanghai;
grant select,insert,update,delete on webdata.pay_merchant to shanghai;
grant select,insert,update,delete on webdata.pay_gateway_bank to shanghai;
grant select,insert,update,delete on webdata.pay_city_merchant to shanghai;
grant select,insert,update,delete on webdata.pay_interface_switch to shanghai;


ALTER TABLE WEBDATA.PAYMENT ADD ( GATEWAY_CODE VARCHAR2(30) NULL ) ;
ALTER TABLE WEBDATA.PAYMENT ADD ( MERCHANT_CODE VARCHAR2(30) NULL ) ;


