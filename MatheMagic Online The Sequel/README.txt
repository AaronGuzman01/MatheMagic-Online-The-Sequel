GitHub Repository: 
https://github.com/AaronGuzman01/MatheMagic-Online-The-Sequel.git

INSTRUCTIONS:
To run this program successfully you have to first start the server by running the 
MatheMagicServer.java file. Once the server is running, which can be verified by
looking at the server's command console where a server started message should appear 
you then start the MatheMagicClient.java file and make ensure that the command console 
on the client side has established a connection with the server (A message will display 
in the console indicating a successful connection). Also, it is possible to run multiple
clients on this server, so you can run the multiple MatheMagicClient.java file multiple
times.


COMMANDS IMPLEMENTED:
This project implements the following server Protocols:

1. LOGOUT - This request will verify the clients' identity and indicate a successful or 
unsuccessful login attempt. A list of authorized users can be found in the login.txt file.
This command follows the following format: LOGIN *username *password.

2. SOLVE - This request will receive a geometry problem from the client and return a 
solution or error message to the client. These interactions will also be recorded for 
each user in a corresponding file. This command can take input for a circle or rectangle 
problem using a flag in the request.
This command follows the following formats (n indicates numerical value): 
SOLVE -c n
SOLVE -r n
SOLVE -r n n

3. LIST - This request will return a list of interactions (from the SOLVE request)
that the user has had with the server. This request can also be used with a special 
flag format to return all interactions from all users. This flag format, however, will
require the user to be the root user, which this protocol will verify.
This command follows the following formats:
LIST
LIST -all

4. MESSAGE - This request will send a message entered by the requesting user to a specified 
logged in user. This request can also be used with a special flag format to broadcast a
message to all users logged into the server. This flag format, however, will require the 
user to be the root user, which this protocol will verify.
This commad following the following formats:
MESSAGE *user *Message
Message -all *Message

5. SHUTDOWN - This request will close the all client connections to the server and will 
close the server down.
This command follows the following format: SHUTDOWN

6. LOGOUT - This request will logout the user and clear all client information for that
connection. The connection to the server will still remain but the user will not be able
to interact with the server until they login again.
This command follows the following format: LOGOUT


PROBLEMS & BUGS:
One problem I found in my implementation was that when you entered an invalid character 
after the command it would not work. For example if I entered SHUTDOWN 5 or LOGOUT  3, it 
would not execute the command. I accounted for whitespaces so whitespaces after a correctly
formatted command would not affect a command but an extra character would. This also happened
when I entered an extra number in the SOLVE command, so commands like SOLVE -c 4 4 & SOLVE -r 5 5 5
would not work. This really isn't a bug since the protocols account for the correct format,
but I do feel that this was a limitation in my implementation.

Something else I found with my implementation was that a user is able to send a message to
themselves. This will not affect the protocol or server in anyway but it does introduce
a limitation in my implementation, since it does not prevent the user from sending a 
message to themselves.

SAMPLE RUN:
Both clients were ran simultaneously and were connected to the server at the same time.


First Client:

MathMagic connection established
Start entering a command:

LOGIN root root22
SUCCESS 

SOLVE -c 56
Circle’s circumference is 351.86 and area is 9852.03

SOLVE -r 350 34
Rectangle’s perimeter is 768.00 and area is 11900.00

MESSAGE -all Hello!
 
Message from qiang: 
Hello back!

LIST -all
root:
	radius 56: Circle’s circumference is 351.86 and area is 9852.03
	sides 350 34: Rectangle’s perimeter is 768.00 and area is 11900.0
john:
	No interactions yet
sally:
	No interactions yet
qiang:
	radius 6: Circle’s circumference is 37.70 and area is 113.10
	sides 5 5: Rectangle’s perimeter is 20.00 and area is 25.00
	sides 2 6: Rectangle’s perimeter is 16.00 and area is 12.0

SHUTDOWN
200 OK 


Second Client:

MathMagic connection established
Start entering a command:

LOGIN qiang qiang22
SUCCESS 

SOLVE -c 6
Circle’s circumference is 37.70 and area is 113.10

SOLVE -r 5
Rectangle’s perimeter is 20.00 and area is 25.00

SOLVE -r 2 6
Rectangle’s perimeter is 16.00 and area is 12.00

LIST
qiang:
	radius 6: Circle’s circumference is 37.70 and area is 113.10
	sides 5 5: Rectangle’s perimeter is 20.00 and area is 25.00
	sides 2 6: Rectangle’s perimeter is 16.00 and area is 12.0

Message from root: 
Hello!

MESSAGE root Hello back!

LOGOUT
200 OK 

