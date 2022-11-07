alter table `page` modify `content` mediumtext  character set utf8mb4;
alter table `page` add index `IX_page_path` (`path`(128) asc) visible;

insert into field (name, selector, weight) values ('title', 'title', 1.0);
insert into field (name, selector, weight) values ('body', 'body', 0.8);
insert into field (name, selector, weight) values ('h1', 'h1', 0.1);
