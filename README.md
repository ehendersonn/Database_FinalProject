# Database_FinalProject
This project is an Electronic Voting System developed from an ER-diagram all the way to a usable voter and manager interface. It includes the ER-diagram, relational schema, SQL DDL, SQL commands to populate the database, normal form analysis of the relational schema, and two interactive user applications: Voter and Manager. 

**Usage:**

The file entitled “ER_Diagram.drawio” is the ER-diagram used to develop the database used in this project. 

The file entitled “Relational_Schema.drawio” is the translated version of the above-mentioned ER-diagram as a relational schema. 

The file entitled “evs.sql.txt” contains the SQL DDL CREATE statements used to create the tables of the database documented in the relational schema and can be used to understand the layout and constraints of the database.

The file entitled “evs_insert.sql.txt” contains the insert statements used to populate the database with voters, candidates, contests, and their related attributes. 

The file entitled “univ.db” is the database used in the voter and manager applications to present contests to voters based on their location and record their votes in each contest, as well as keep track of the candidates running in each contest and display each candidate’s vote tally in every contest they ran in as a function of the manager application. 

The file entitled “NormalForm_Analysis” contains the normal form analysis of each relation in the relational schema to 1NF, 3NF, 4NF, and BCNF. 

The file entitled “OverviewDoc” contains overview narratives for the ER-diagram, relational schema, voter and manager application implementation, and SQL DDL. 

The folder entitled “Database Project” contains the implementation of both the voter and manager GUI applications in the src. 
