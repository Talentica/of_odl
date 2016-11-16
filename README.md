#Description
Captive portal on OpenDaylight(ODL) platform.
#Problem Statement
To develop a captive portal feature on ODL controller.
#Solution Approach
Captive portal is a networking solution which performs authentication of the users before granting them network access. It secures the network from unwanted and unauthorized access by providing a landing web page where all the browser's requests from an unauthenticated user are redirected and authentication is performed. Traditionally a captive portal solution is implemented on the networking device itself by enabling the device to redirect the browser's requests from unauthenticated users. In a Software Defined Networking (SDN) environment we can separate the logic from the device to a centralized controller platform and write our own applications for networking solutions. The idea behind this project is to develop a captive portal solution for SDN infrastructure. We are developing a captive portal feature on ODL platform. 
ODL is a Java based open source SDN controller platform. ODL provides a number of ready-to-install features for networking solutions and also provide a platform to develop our own features. A captive portal secures the network from unwanted and unauthorized access. Captive portal authenticates the users accessing the network against the database by providing a landing web page. When a new user tries to access the network, it is redirected to captive portal web page and is asked to login with valid credentials. User provides username and password information and/or accept terms and conditions. The user provided credentials are validated by authentication server against the database. After the user is authenticated, user authorizations are set and user is allowed to access the Internet or services in the network based on its type of authorization. Apart from this, a captive portal can also be used in a number of ways by the organizations such as for advertising etc.
![Application Design](https://cloud.githubusercontent.com/assets/12424727/20339904/dc6e0de6-ac04-11e6-954a-799e288e81f8.jpg)
#Deployment
##Starting authentication server
We are using MySql for saving users. First create a schema with name captive portal.
```
CREATE SCHEMA `captiveportal` ;
```
Clone the captive portal web application from our github and start the application:
```
git clone https://github.com/Talentica/captiveportal.git
cd captiveportal
mvn clean install
cd captiveportal-web/target
java -jar captiveportal-web-0.0.1-SNAPSHOT.jar
```
This will start the captive portal web application and 2 users will be added in database. Also the root node will be added which is used to communicate between base machine and mininet topology.
##Starting ODL controller 
Next, start the ODL controller with captive portal feature. Clone the of_odl application from our github and start the application:
```
git clone https://github.com/Talentica/of_odl.git
cd of_odl
mvn clean install
cd odl-distribution/odl-karaf/target/assembly
./bin/karaf
```
##For mininet topology with ovs switch
###Installing features
Install the captive portal feature on odl platform
```
>features:install captiveportal-ovs
```
###Starting Topology
Next, start mininet topology on base machine
```
sudo mn --topo tree,3 --mac --switch ovsk --controller=remote,ip=127.0.0.1 --nat
```
In new terminal execute following two scripts to add flows from switches to controller and creating qos, attaching the qos with specified ports on switches: 
```
cd of_odl/ops
./controllerAction_simpleTree_ovsk.sh
./queueToPort.sh


```
##For mininet topology with ofsoftswitch switch
###Installing features
Install the captive portal feature on odl platform 
```
>features:install captiveportal-ofsoftswitch
```
###Starting Topology
Next, start mininet topology on base machine
```
sudo mn --topo tree,3 --mac --switch user --controller=remote,ip=127.0.0.1 --nat
```
In new terminal execute following script to add flows from switches to controller.(We are implementing qos with meters in the topology with ofsoftswitch, so there is no need to create qos): 
```
cd of_odl/ops
./controllerAction_simpleTree.sh
```
##Testing
It will start a tree topology with seven switches and 8 hosts. Out of 8 hosts; mac addresses of 2 hosts(h1 and h2 with mac addresses 00:00:00:00:00:01 and 00:00:00:00:00:02 respectively having user privileges) are already saved in database while starting captive portal web application as mentioned earlier. So h1 and h2 can communicate with each other. Other hosts need to get authenticated in order to access the network.
Try to ping h1 and h2 from h3. You will not be able to ping.
open firefox in h3 and hit
```
http://10.0.0.1
```
You will be redirected to login page in captive portal web application running on base machine. Login using username/password:guest/guest123. Username/password:guest/guest123 sets guests privileges for the user. On successful login, accept terms and conditions and click on connect.
Now try pinging h1 and h2 from h3. You should be able to ping now as h3 is authenticated by captive portal and is saved in database as well.
Test with h5. After redirection, login using username/password:user/user123.
