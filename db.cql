create keyspace dev with replication = {'class':'SimpleStrategy', 'replication_factor':1};

create table card_records( card_number int PRIMARY KEY, name text, address text);
