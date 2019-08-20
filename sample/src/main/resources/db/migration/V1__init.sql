create table sports (
                        id integer primary key,
                        name character varying(50) unique not null
);

create table champs (
                        id integer primary key,
                        sport_id integer not null references sports(id) on delete cascade,
                        name character varying(50) not null,
                        unique (sport_id, name)
);
