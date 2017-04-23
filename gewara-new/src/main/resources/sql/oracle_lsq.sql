-- 更新项目属性
update webdata.sport_sportitem  set otherInfo =  (select  t2.otherinfo from  (
 select t1.recordid as sportid,
        (case
          when t1.heightvenue is null and t1.flooring is null and t1.sitecount is null then '{}'
         else
           '{"roommaterial":"' || t1.flooring || '","roomaddress":"","roomnumber":"' || t1.sitecount ||'","roomheight":"' || t1.heightvenue || '"}'
         end) as otherInfo 
   from (select recordid, 
                regexp_substr(regexp_substr(otherinfo,'heightvenue":"\w+'),'\w+$') as heightvenue,
                regexp_substr(regexp_substr(otherinfo,'flooring":"\w+'),'\w+$') as flooring,
                regexp_substr(regexp_substr(otherinfo,'sitecount":"\w+'),'\w+$') as sitecount
                from webdata.Sport) t1 
         )t2 where t2.sportid=webdata.sport_sportitem.sportId )
    where  exists (select 1 from webdata.sportitem where recordid=webdata.sport_sportitem.sportitemid and opentype='field') and otherInfo is null;