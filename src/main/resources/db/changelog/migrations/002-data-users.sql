--changeset em2021:1
insert into cloud_storage.users (name, surname, age, phone_number, login, password)
values ('John', 'Black', 18, '3333333', 'jblack@test.com', '123');

insert into cloud_storage.users (name, surname, age, phone_number, login, password)
values ('Tom', 'Green', 15, '4444444', 'greent@test.com', '331');

insert into cloud_storage.users (name, surname, age, phone_number, login, password)
values ('Ann', 'White', 25, '5555555', 'aw@test.com', '192');
--rollback truncate table cloud_service.users;