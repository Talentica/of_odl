#Description
Captiveportal on OpenDaylight(ODL) platorm - A Network Access Control(NAC) solution
#Problem Statement
To develop a captive portal feature on OpenDaylight controller.
#Solution Approach
Captive portal is a networking solution which performs authentication of the users before granting them network access. It secures the network from unwanted and unauthorized access by providing a landing web page where all the browser's requests from an unauthenticated user are redirected and authentication is performed. Traditionally a captive portal solution is implemented on the networking device itself by enabling the device to redirect the browser's requests from unauthenticated users. In a Software Defined Networking (SDN) environment we can separate the logic from the device to a centralized controller platform and write our own applications for networking solutions. The idea behind this project is to develop a captive portal solution for SDN infrastructure. We are developing a captive portal feature on OpenDaylight(ODL) platform. 
ODL is a Java based open source SDN controller platform. ODL provides a number of ready-to-install features for networking solutions and also provide a platform to develop our own features. A captive portal secures the network from unwanted and unauthorized access. Captive portal authenticates the users accessing the network against the database by providing a landing web page. When a new user tries to access the network, it is redirected to captive portal web page and is asked to login with valid credentials. User provides user name and password information and/or accept terms and conditions. The user provided credentials are validated by authentication serer against the database. After the user is authenticated, user authorizations are set and user is allowed to access the Internet or services in the network based on its type of authorization. Apart from this, a captive portal can also be used in a number of ways by the organizations such as advertising etc.
#Deployment
##Starting authentication server
We are using MySql for saving users. First create a schema with name captiveportal.
```
CREATE SCHEMA `captiveportal` ;
```
Clone the captive portal web application from our github and start the application:
```
git clone https://github.com/nkkize/captiveportal.git
cd captiveportal
mvn clean install
cd captiveportal-web/target
java -jar captiveportal-web-0.0.1-SNAPSHOT.jar
```
This will start the captiveportal web application and 2 users will be added in database. Also the root node will be added which is used to communicate between base machine and mininet topology.
##Starting ODL controller 
Next, start the ODL controller with captiveportal feature. Clone the odl application from our github and start the application:
```
git clone https://github.com/nkkize/of_odl.git
cd of_odl
mvn clean install
cd odl-distribution/odl-karaf/target/assembly
./bin/karaf
```
##For mininet topology with ovs switch
###Installing features
Install the captiveportal feature on odl platform
```
>features:install captiveportal-ovs
```
###Starting Topology
Next, start mininet topology on base machine
```
sudo mn --topo tree,3 --mac --switch ovsk --controller=remote,ip=127.0.0.1 --nat
```
In new terminal execute following two scrpits to add flows from switches to controller and creating qos and attaching the qos with specified ports on switches: 
```
cd of_odl/ops
./controllerAction_simpleTree_ovsk.sh
./queueToPort.sh

```
##For mininet topology with ofsoftswitch switch
###Installing features
Install the captiveportal feature on odl platform 
```
>features:install captiveportal-ofsoftswitch
```
###Starting Topology
Next, start mininet topology on base machine
```
sudo mn --topo tree,3 --mac --switch user --controller=remote,ip=127.0.0.1 --nat
```
In new terminal execute following scrpit to add flows from switches to controller.(We are implementing qos with meters in the topology with ofsoftswitch, so there is not need to create queues): 
```
cd of_odl/ops
./controllerAction_simpleTree.sh
```
##Testing
It will start a tree topology with seven switch and 8 hosts. Out of 8 hosts mac addresses of 2 hosts(h1 and h2 with mac addresses 00:00:00:00:00:01 and 00:00:00:00:00:02 respectively and normal user priviledges) are already saved in database via flydb on starting captiveportal web application as mentioned earlier. So these host can communicate with each other. Other hosts need to get authenticated in order to access the network.
Try to ping h1 and h2 from h3. You will not be able to ping.
open firefox in h3 and hit
```
http://10.0.0.1
```
You will be redirected to login page in captiveportal web application running on base machine. Login using username/password:guest/guest123. Username/password:guest/guest123 sets guests priveledges for the user. On successful login, accept terms and conditions and click on connect.
Now try pinging h1 and h2 from h3. You should be able to ping now as h3 is authenticated by captiveportal and is saved in database as well.
Test with h5. After redirection, login using username/password:user/user123.

