create database if not exists `rss`;
use `rss`;
create table if not exists `url_table`(
    `url_id` int(10) not null auto_increment,
    `url_content` varchar(510) not null,
    `url_level` int(10) not null,
    primary key(`url_id`)
)engine=InnoDB default charset=utf8;

create table if not exists `store_message`(
    `message_id` int(10) not null auto_increment,
    `message_content` text not null ,
    `message_url` varchar(255) not null,
    primary key (`message_id`)
)engine=InnoDB default charset=utf8;
