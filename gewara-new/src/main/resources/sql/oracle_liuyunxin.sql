--网友添加影片表字段添加(海报，主演，导演)
alter table tempmovie 
  add (LOGO VARCHAR2 (500),ACTORS VARCHAR2(200),DIRECTOR VARCHAR(100))