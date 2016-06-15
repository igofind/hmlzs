CREATE TABLE `hmlzs_article` (
  `id`            INT NOT NULL,
  `creator`       VARCHAR(255),
  `author`        VARCHAR(255),
  `source`        VARCHAR(255),
  `updater`       VARCHAR(255),
  `serial`        INT NOT NULL,
  `title`         VARCHAR(255),
  `depict`        VARCHAR(255),
  `templateId`    INT,
  `url`           VARCHAR(255),
  `content`       VARCHAR(255),
  `categoryId`    INT NOT NULL,
  `treeId`        INT NOT NULL,
  `focus`         INT,
  `headLine`      INT,
  `headLineOrder` INT,
  `audit`         INT,
  `auditorId`     INT,
  `status`        INT,
  `createDate`    DATETIME,
  `updateDate`    DATETIME,
  `publishDate`   DATETIME,
  `headLineDate`  DATETIME,
  `deleteDate`    DATETIME,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `hmlzs_template` (
  `id`         INT NOT NULL,
  `name`       VARCHAR(255),
  `filename`   VARCHAR(255),
  `path`       VARCHAR(255),
  `status`     INT,
  `createDate` DATETIME,
  `updateDate` DATETIME,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `hmlzs_subarticle` (
  `id`         INT NOT NULL,
  `articleId`  INT NOT NULL,
  `seq`        INT NOT NULL,
  `content`    VARCHAR(255),
  `createDate` DATETIME,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


CREATE TABLE `hmlzs_media` (
  `id`         INT NOT NULL,
  `creator`    VARCHAR(255),
  `title`      VARCHAR(255),
  `depict`     VARCHAR(255),
  `category`   INT NOT NULL,
  `ukey`       VARCHAR(255),
  `ftype`      VARCHAR(255),
  `hash`       VARCHAR(255),
  `handle`     VARCHAR(255),
  `size`       INT,
  `width`      INT,
  `height`     INT,

  `status`     INT,
  `createDate` DATETIME,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `hmlzs_mediaarticle` (
  `id`            INT NOT NULL,
  `mediaId`       INT NOT NULL,
  `articleId`     INT NOT NULL,
  `mediaCategory` INT,
  `createDate`    DATETIME,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `hmlzs_category` (
  `id`         INT NOT NULL,
  `name`       VARCHAR(255),
  `depict`     VARCHAR(255),
  `parentId`   INT,
  `hasChild`   TINYINT,
  `status`     INT,
  `updateDate` DATETIME,
  `createDate` DATETIME,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `hmlzs_tree` (
  `id`         INT NOT NULL,
  `name`       VARCHAR(255),
  `parentId`   INT,
  `hasChild`   TINYINT,
  `treeroad`   VARCHAR(255),
  `storepath`  VARCHAR(255),
  `status`     INT,
  `updateDate` DATETIME,
  `createDate` DATETIME,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `cms_user` (
  `id`            INT NOT NULL,
  `account`       VARCHAR(255) DEFAULT NULL,
  `name`          VARCHAR(255) DEFAULT NULL,
  `depict`        VARCHAR(255) DEFAULT NULL,
  `pwd`           CHAR(32),
  `phone`         VARCHAR(255) DEFAULT NULL,
  `mail`          VARCHAR(255) DEFAULT NULL,
  `status`        INT,
  `manager`       TINYINT,
  `createDate`    DATETIME,
  `updateDate`    DATETIME,
  `lastloginDate` DATETIME,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

-- +++++++++++++++++++++++++++++ the core tables +++++++++++++++++++++++++++++
CREATE TABLE `cms_keygen` (
  `table_name`   VARCHAR(255) NOT NULL,
  `last_used_id` INT          NOT NULL,
  PRIMARY KEY (`table_name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

-- The right table.
CREATE TABLE `cms_right` (
  `id`         INT NOT NULL,
  `md5`        CHAR(32)     DEFAULT NULL,
  `name`       VARCHAR(255) DEFAULT NULL,
  `depict`     VARCHAR(255) DEFAULT NULL,
  `status`     INT,
  `createDate` DATETIME,
  PRIMARY KEY (`id`),
  UNIQUE (`md5`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

-- A role will has many right.
CREATE TABLE `cms_role` (
  `id`         INT NOT NULL,
  `md5`        CHAR(32)     DEFAULT NULL,
  `name`       VARCHAR(255) DEFAULT NULL,
  `depict`     VARCHAR(255) DEFAULT NULL,
  `status`     INT,
  `createDate` DATETIME,
  PRIMARY KEY (`id`),
  UNIQUE (`md5`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

-- This table will be stored those who have special right.
-- An user might has one or many role.
CREATE TABLE `cms_admin` (
  `id`            INT NOT NULL,
  `account`       VARCHAR(255) DEFAULT NULL,
  `name`          VARCHAR(255) DEFAULT NULL,
  `depict`        VARCHAR(255) DEFAULT NULL,
  `pwd`           CHAR(32),
  `phone`         VARCHAR(255) DEFAULT NULL,
  `mail`          VARCHAR(255) DEFAULT NULL,
  `status`        INT,
  `createDate`    DATETIME,
  `updateDate`    DATETIME,
  `lastloginDate` DATETIME,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

-- The core log.
CREATE TABLE `cms_log` (
  `id`         INT NOT NULL,
  `userId`     INT NOT NULL,
  `name`       VARCHAR(255)  DEFAULT NULL,
  `action`     VARCHAR(255)  DEFAULT NULL,
  `content`    VARCHAR(2000) DEFAULT NULL,
  `ip`         VARCHAR(255)  DEFAULT NULL,
  `status`     INT,
  `createDate` DATETIME,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


CREATE TABLE `cms_roleright` (
  `id`         INT NOT NULL,
  `roleid`     INT NOT NULL,
  `rightid`    INT NOT NULL,
  `createDate` DATETIME
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


CREATE TABLE `cms_userrole` (
  `id`         INT NOT NULL,
  `userid`     INT NOT NULL,
  `roleid`     INT NOT NULL,
  `createDate` DATETIME,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `cms_session` (
  `id`         INT NOT NULL,
  `sessionKey` CHAR(32),
  `userId`     INT NOT NULL,
  `account`    VARCHAR(255),
  `status`     INT,
  `loginDate`  DATETIME,
  `expireDate` DATETIME,

  PRIMARY KEY (`id`),
  UNIQUE (`sessionKey`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

# admin account
INSERT INTO cms_admin (id, account, name, depict, pwd, phone, mail, status, createDate)
VALUES (1, 'admin', '超级管理员', '拥有所有权限', 'E10ADC3949BA59ABBE56E057F20F883E', 17090412945, '664006332@qq.com', 1, now());

# category
INSERT INTO hmlzs_category (id, name, depict, parentId, hasChild, status, updateDate, createDate) VALUES
  (1, 'navbanner', '导航和banner', 0, 0, -2, NULL, now()),
  (2, 'news', '新闻法讯', 0, 0, 1, NULL, now()),
  (3, 'life', '生活禅', 0, 0, 1, NULL, now()),
  (4, 'ziyunfoguo', '紫云佛国', 0, 0, 1, NULL, now()),
  (5, 'corpus', '慧公文集', 0, 0, 1, NULL, now()),
  (6, 'knowledge', '佛教常识', 0, 0, 1, NULL, now()),
  (7, 'audio', '梵音喧流', 0, 0, 1, NULL, now()),
  (8, 'zhuyingposuo', '竹影婆娑', 0, 0, 1, NULL, now()),
  (9, 'law', '紫云法务', 0, 0, 1, NULL, now()),
  (10, 'futian', '广种福田', 0, 0, 1, NULL, now()),
  (11, 'notice', '活动通知', 0, 0, 1, NULL, now()),
  (12, 'contact', '联系我们', 0, 0, 1, NULL, now());

# the article contains nav and banner
INSERT INTO hmlzs_article (id, categoryId, treeId, templateId, serial, focus, headLine, headLineOrder, creator, title, content, status, createDate)
VALUES
  (1, 1, 0, 0, 0, 0, 0, 0, 'admin', '导航和banner', '', -2, now()),
  (2, 10, 0, 0, 0, 0, 0, 0, 'admin', '广种福田',
   '壹贰叁肆伍陆柒捌玖拾壹贰叁肆伍陆柒捌玖拾壹贰叁肆伍陆柒捌玖拾壹贰叁肆伍陆柒捌玖拾壹贰叁肆伍陆柒捌玖拾壹贰叁肆伍陆柒捌玖拾壹贰叁肆伍陆柒捌玖拾壹贰叁肆伍陆柒捌玖拾壹贰叁肆伍陆柒捌玖拾壹贰叁肆伍陆柒捌玖拾壹贰叁肆伍陆柒捌玖拾壹贰叁肆伍陆柒捌玖拾壹贰叁肆伍陆柒捌玖拾壹贰叁肆伍陆柒捌玖拾@2供养寺院助刊11@中国建设银行湖北省分行黄梅支行营业部@湖北黄梅老祖寺@4200 16766 8080 5300 1018',
   -2, now());

# 导航(主)
# 首页,http://hmlzs.com/article/index.html;
# 新闻法讯,http://list.hmlzs.com/list/c-2-1.html;
# 生活禅,http://list.hmlzs.com/list/c-3-1.html;
# 紫云佛国,http://list.hmlzs.com/list/c-4-1.html;
# 慧公文集,http://list.hmlzs.com/list/c-5-1.html;
# 佛教常识,http://list.hmlzs.com/list/c-6-1.html;
# 梵音宣流,http://list.hmlzs.com/list/c-7-1.html;
# 竹影婆娑,http://list.hmlzs.com/list/c-8-1.html;
# 导航(次)
# 紫云法务,http://list.hmlzs.com/list/c-9-1.html;
# 广种福田,http://www.hmlzs.com/index.html;
# 联系我们,http://www.hmlzs.com/index.html;
# banner
# http://hmlzs.com/article/static/lzs/images/banner-1.jpg;
# http://hmlzs.com/article/static/lzs/images/banner-2.jpg

# template
INSERT INTO hmlzs_template (id, name, filename, path, status, createDate) VALUES
  (1, '基本模板_index', 'index.vm', 'base\\index.vm', -2, now()),
  (2, '基本模板_head', 'head.vm', 'base\\head.vm', -2, now()),
  (3, '基本模板_nav', 'nav.vm', 'base\\nav.vm', -2, now()),
  (4, '基本模板_notice', 'notice.vm', 'base\\notice.vm', -2, now()),
  (5, '基本模板_banner', 'banner.vm', 'base\\banner.vm', -2, now()),
  (6, '基本模板_script', 'script.vm', 'base\\script.vm', -2, now()),
  (7, '基本模板_fawu', 'fawu.vm', 'base\\fawu.vm', -2, now()),
  (8, '基本模板_futian', 'futian.vm', 'base\\futian.vm', -2, now()),
  (9, '基本模板_foot', 'foot.vm', 'base\\foot.vm', -2, now()),
  (10, '普通文章列表模板', 'articleList.vm', 'base\\articleList.vm', -2, now()),
  (11, '图片文章列表模板', 'photoList.vm', 'base\\photoList.vm', -2, now()),
  (12, '文章模板一(法务栏居右侧)', 'article1.vm', 'base\\article1.vm', 1, now()),
  (13, '文章模板二(法务栏居底部)', 'article2.vm', 'base\\article2.vm', 1, now()),
  (14, '站内搜索列表模板', 'search.vm', 'base\\search.vm', -2, now())
