
-- Auto run script when application is getting started
-- Created "Text Table" only for data readability purpose otherwise it has little performance hit


drop table event if exists;
create text table event(
	id varchar(255) not null,
	alert varchar(255),
	duration bigint,
	end_time bigint,
	host varchar(255),
	start_time bigint,
	type varchar(255),
	primary key (id)
);
SET TABLE event SOURCE "eventfile";