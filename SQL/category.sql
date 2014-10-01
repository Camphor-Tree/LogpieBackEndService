
CREATE TABLE activity_category_first_level
(
  acpid serial NOT NULL,
  category_cn character varying NOT NULL,
  category_us character varying NOT NULL,
  CONSTRAINT "PK_act_cate_parent" PRIMARY KEY (acpid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "act_cate_parent"
  OWNER TO postgres;

-- Table: act_cate_child

-- DROP TABLE act_cate_child;

CREATE TABLE activity_category_second_level
(
  accid serial NOT NULL,
  category_cn character varying NOT NULL,
  category_us character varying NOT NULL,
  parent integer NOT NULL,
  CONSTRAINT "PK_act_cate_child" PRIMARY KEY (accid),
  CONSTRAINT "FK_act_cate_child" FOREIGN KEY (parent)
      REFERENCES act_cate_parent (acpid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE act_cate_child
  OWNER TO postgres;

INSERT INTO act_cate_parent (category_cn,category_us) values ("运动","Sports");
INSERT INTO act_cate_parent (category_cn,category_us) values ("学术","Academy");
INSERT INTO act_cate_parent (category_cn,category_us) values ("休闲","Leisure");
INSERT INTO act_cate_parent (category_cn,category_us) values ("旅游","Tourism");
INSERT INTO act_cate_parent (category_cn,category_us) values ("公益","Public Service");
INSERT INTO act_cate_parent (category_cn,category_us) values ("其他","Others");

INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("足球","Soccer",1);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("篮球","Basketball",1);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("羽毛球","Badminton",1);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("游泳","Swimming",1);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("网球","Tennis",1);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("保龄球","Bowling",1);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("乒乓球","Pingpang",1);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("台球","Billiards",1);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("瑜伽","Yoga",1);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("健身","Fitness",1);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("滑雪","Skiing",1);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("滑冰","Skating",1);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("攀岩","Climbing",1);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("极限运动","Extreme Sports",1);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("其他","Others",1);

INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("信息技术","Information Technology",2);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("艺术","Fine Arts",2);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("生命科学","Bio-Science",2);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("商业管理","Business",2);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("经济金融","Economy&Finance",2);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("教育","Education",2);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("法律","Law",2);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("工程","Engineering",2);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("社会","Society",2);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("人文","Culture",2);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("基础科学","Basic Science",2);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("统计","Statistics",2);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("其他","Others",2);

INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("桌游","Board Games",3);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("KTV","KTV",3);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("聚会","Party",3);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("演唱会","Concert",3);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("棋牌","Chess&Cards",3);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("酒吧","Bar",3);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("电影","Movies",3);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("书友会","Book Club",3);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("摄影","Photography",3);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("购物","Shopping",3);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("烧烤","Barbecue",3);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("其他","Others",3);

INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("自然风光","Natural Scenery",4);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("城市游","City Tour",4);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("古朴小镇","Quaint Town",4);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("海外旅游","Overseas Travel",4);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("徒步","Hiking",4);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("自驾游","Road Trip",4);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("露营","Camping",4);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("其他","Others",4);

INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("慈善","Charity",5);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("志愿者","Volunteer",5);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("公益演讲","Charity Speech",5);
INSERT INTO act_cate_parent (category_cn,category_us, parent) values ("其他","Charity Speech",5);

