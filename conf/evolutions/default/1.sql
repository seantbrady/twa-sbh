# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table parking_event (
  id                        bigint not null,
  arrival_id                bigint,
  departure_id              bigint,
  constraint pk_parking_event primary key (id))
;

create table traffic_log (
  id                        bigint not null,
  aircraft                  varchar(255),
  date                      timestamp,
  time                      timestamp,
  date_time                 timestamp,
  type                      varchar(255),
  company                   varchar(255),
  parking_event_id          bigint,
  constraint pk_traffic_log primary key (id))
;

create sequence parking_event_seq;

create sequence traffic_log_seq;

alter table parking_event add constraint fk_parking_event_arrival_1 foreign key (arrival_id) references traffic_log (id) on delete restrict on update restrict;
create index ix_parking_event_arrival_1 on parking_event (arrival_id);
alter table parking_event add constraint fk_parking_event_departure_2 foreign key (departure_id) references traffic_log (id) on delete restrict on update restrict;
create index ix_parking_event_departure_2 on parking_event (departure_id);
alter table traffic_log add constraint fk_traffic_log_parkingEvent_3 foreign key (parking_event_id) references parking_event (id) on delete restrict on update restrict;
create index ix_traffic_log_parkingEvent_3 on traffic_log (parking_event_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists parking_event;

drop table if exists traffic_log;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists parking_event_seq;

drop sequence if exists traffic_log_seq;

