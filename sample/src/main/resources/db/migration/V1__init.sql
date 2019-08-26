create table Sports (
                        id int4 not null,
                        name varchar(255),
                        primary key (id)
);
create table champs (
                        id integer primary key,
                        sport_id integer not null references sports(id) on delete cascade,
                        name character varying(50) not null,
                        unique (sport_id, name)
);

create table ChampsTest (
                            id int8 not null,
                            name varchar(255),
                            sport_id int4,
                            primary key (id)
);



create table TestEntity (
                            id int8 not null,
                            name varchar(255),
                            sport_id int4,
                            primary key (id)
);