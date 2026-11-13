create user me identified by 'password';
-- drop database starbase;

create database starbase default character set 'UTF8';
grant all on starbase.* to me;

use starbase;

create table users
(
	number        bigint unique not null auto_increment,
	email         varchar(200) not null unique,
	password      varchar(200) not null,
	first_name    varchar(200) not null,
	last_name     varchar(200) not null,

	administrator char         not null default 'F',
	staff         char         not null default 'F',

	status        varchar(200) not null default 'typical',
	language      varchar(200) not null default 'en-us',
	signature     varchar(200) not null unique,
	identifier    bigint       not null unique,
	theme         varchar(200) not null default '#EE5577',
	created       datetime     not null default (utc_timestamp())
);

-- users.status can be 'typical' or 'banned'
-- 
-- or create a new table, e.g. realtime_statuses
-- 'typical'
-- 'busy'
-- 'calling'
-- 'banned'
-- 'removed'
-- 'deleted'


insert into users(email, password, first_name, last_name, signature, identifier)
values('user@email.com', sha2('User1234', 512), 
'System', 'Administrator', 'user', 12340001);

update users set administrator='T', staff='T'
where email='user@email.com';

create table languages
(
	number        bigint unique not null auto_increment,
	alias         varchar( 200) unique not null,
	name          varchar( 200) not null,
	icon          varchar( 200)
);

insert into languages(alias, name) values('en-us', 'English (United State)');
insert into languages(alias, name) values('en-uk', 'English (United Kingdom)');
insert into languages(alias, name) values('en-au', 'English (Australia)');
insert into languages(alias, name) values('en-in', 'English (India)');

-- insert into languages(alias, name) values('en-ph', 'English (Philippines)');
-- insert into languages(alias, name) values('en-ca', 'English (Canada)');
-- insert into languages(alias, name) values('en-sg', 'English (Singapore)');
-- insert into languages(alias, name) values('en-nz', 'English (New Zealand)');

insert into languages(alias, name) values('de-de', 'Deutsch (Deutschland)');
insert into languages(alias, name) values('de-ch', 'Deutsch (Switzerland)');
insert into languages(alias, name) values('de-at', 'Deutsch (Austria)');

insert into languages(alias, name) values('th-th', 'Thai (Thailand)');

create table currencies
(
	number        bigint unique not null auto_increment,
	alias         varchar( 200) unique not null,          -- USD, AUD, THB
	name          varchar( 200) not null,
	symbol        varchar( 200) not null default '$',
	icon          varchar( 200)
);

insert into currencies(alias, name, symbol) values('USD', 'US Dollar', '$');
insert into currencies(alias, name, symbol) values('THB', 'Thai Baht', '฿');

create table settings
(
	number        bigint unique not null auto_increment,
	name          varchar(200) unique not null,
	value         varchar(200) not null default ''
);

insert into settings(name, value) values('default-language', 'en-us');

insert into settings(name, value) values('full-web-site', 'https://online.com');
insert into settings(name, value) values('support-email', 'support@email.com');
insert into settings(name, value) values('email-server',  'mail.email.com');
insert into settings(name, value) values('email-sender',  'support@email.com');

insert into settings(name, value) values('email-port',        '587');
insert into settings(name, value) values('email-sender-name', 'Administrator');
insert into settings(name, value) values('email-password',    '');
insert into settings(name, value) values('email-protocol',    'TLSv1.2');
insert into settings(name, value) values('email-auth',        'true');
insert into settings(name, value) values('email-tls',         'true');

-- prevent password hacking
create table password_trials
(
	number      bigint unique not null auto_increment,
	user        bigint,
	happened    datetime default (utc_timestamp())
);

-- select * from password_trials 
-- where happened >= utc_timestamp() - interval 60 minute;

create table translations
(
	number        bigint unique not null auto_increment,
	code          varchar(200),
	language      varchar(200),
	text          varchar(2000)
);

-- insert into translations(language,code,text) value('en-us', 'home', 'Home');
-- insert into translations(language,code,text) value('th-th', 'home', 'หน้าแรก');


-- The End
