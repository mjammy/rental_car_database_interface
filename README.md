# README #

## OVERVIEW ##

* If you are reading this, you're checking out my github... yay!!

* This set of files were made for a project done in my databases class, in which I designed a high level approach to making a rent-a-car database and subsequently implemented it with a commandline customer interface... Enjoy!

### DATABASE DESIGN ###

* The ER diagram is included in a PDF, but just to summarize there are 4 basic important entities:
	1) Customer
	2) Rental
	3) Vehicle
	4) Location
	
* The construction of the SQL database can be found in tables.sql

### DATA GENERATION ###

* In datageneration.java you will see that I wrote a significant amount of code that took different information and randomized SQL insert statements for me, which can be found in inserts.sql 

* There were 3 methods that I wrote to generate data, each taking a parameter for how many rows of insert statements I needed (You can make as many as you want). The thing that needs more explanation is how I got random names and addresses. 

* What I did was to initially create the insert statements with “TEMPLATE” as the name and address if it needed them, and then I updated it with a set of statements generated from the website generatedata.com, which updated using statements ending with “where id = 1,2,3 etc.” That is why I created an identity generating “id” attribute in the customer table. I simply executed the update statements and then after they finished I dropped that attribute. Kind of unorthodox but it worked.

*Note: I kept address separate from state so it would be easier to check to see if an address was input in a valid way. 

### INTERFACE ###

* My command-line interface is designed for a customer, and it allows that customer to do 2 things:

1) Register with an account if they weren’t already registered
2) Check out the cars available and rent one if they wish

* The biggest aspect of the interface was walking customers through the steps and making sure I had appropriate input validation before actually changing anything in my tables

*Note: When inputing a new address, I couldn’t cover all my bases to check for validity but I did make sure that if you’re trying to enter a P.O. box or an Apartment, you must follow the same format that generatedata.com did.

### CLOSING ###

* Unfortunately this was all run on an Oracle SQL database hosted by my professor which is no longer live so it cannot be run in its current state, but everything about the process is included in here

* Hope you enjoy!
