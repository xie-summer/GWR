ALTER TABLE MEMBER DROP CONSTRAINT CHK_MEMBER_EMAILMOBILE;

ALTER TABLE WEBDATA.MEMBER_ACCOUNT ADD CERTTYPE NUMBER(2,0) DEFAULT 0;
ALTER TABLE WEBDATA.MEMBER_ACCOUNT ADD EMCONTACT VARCHAR2(30 BYTE);
ALTER TABLE WEBDATA.MEMBER_ACCOUNT ADD EMMOBILE VARCHAR2(11 BYTE);
UPDATE WEBDATA.MEMBER_ACCOUNT SET CERTTYPE = 0 WHERE IDCARD IS NOT NULL AND CERTTYPE IS NULL;

ALTER TABLE WEBDATA.CINEMAROOM ADD CHARACTERISTIC VARCHAR2(20 BYTE);