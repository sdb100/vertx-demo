create keyspace if not exists dev with replication = {'class':'SimpleStrategy', 'replication_factor':1};
use dev;
create table if not exists name_value( name text PRIMARY KEY, value text);
insert into name_value (name, value) values ('test_name', 'test_value');

