--changeset em2021:1
create schema if not exists cloud_storage;
--rollback drop schema cloud_storage;

--changeset em2021:2
create table if not exists cloud_storage.users (
	id serial primary key unique,
	name varchar(255) constraint name_contains_only_letters check (name similar to '[A-z]*'),
	surname varchar(255) constraint surname_contains_only_letters check (surname similar to '[A-z]*'),
	age int constraint age_is_positive check (age > 0),
	phone_number varchar(50) constraint phone_number_contains_only_numbers check (phone_number similar to '[0-9]*') unique,
	login varchar(255) unique,
	password varchar(255) not null);
--rollback drop table cloud_storage.users;

--changeset em2021:3
create table if not exists cloud_storage.user_sessions (
    session_id varchar(255) primary key,
    user_id int not null references cloud_storage.users (id));
--rollback drop table cloud_storage.user_sessions;

--changeset em2021:4
create table if not exists cloud_storage.files (
    name varchar(255),
    size int not null,
    owner int not null references cloud_storage.users (id),
    primary key (name, owner));
--rollback drop table cloud_storage.files;