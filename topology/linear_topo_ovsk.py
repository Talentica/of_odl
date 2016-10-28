#!/usr/bin/env python

from mininet.net import Mininet
from mininet.node import Controller, RemoteController, Host, Node
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.link import Link, Intf, TCLink
from mininet.topo import Topo
from mininet.util import dumpNodeConnections
from mininet.util import isShellBuiltin
from subprocess import call, check_output
from subprocess import Popen, PIPE, STDOUT
import logging
import os 
import time
import select
import re

logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger( __name__ )

class HugeTopo(Topo):
    logger.debug("Class HugeTopo")
    CoreSwitchList = []
    HostList = []
    iNUMBER = 0
    def __init__(self):
        logger.debug("Class HugeTopo init")
        iNUMBER = 1
        
        self.iNUMBER = iNUMBER
        self.iCoreLayerSwitch = iNUMBER
        self.iHost = self.iCoreLayerSwitch * 3 
    
    
        #Init Topo
        Topo.__init__(self)

    def createTopo(self):    
        logger.debug("Start create Core Layer Swich")
        self.createCoreLayerSwitch(self.iCoreLayerSwitch)
        logger.debug("Start create Host")
        self.createHost(self.iHost)

    """
    Create Switch and Host
    """

    def createCoreLayerSwitch(self, NUMBER):
        logger.debug("Create Core Layer")
        for x in range(1, NUMBER+1):
            PREFIX = "100"
            self.CoreSwitchList.append(self.addSwitch(PREFIX + str(x)))
    
    def createHost(self, NUMBER):
        logger.debug("Create Host")
        for x in range(1, NUMBER+1):
            PREFIX = "400"
            self.HostList.append(self.addHost(PREFIX + str(x))) 

    """
    Create Link 
    """
    def createLink(self):
        logger.debug("Create Edge to Host")
	self.addLink(self.CoreSwitchList[0], self.HostList[0])
	self.addLink(self.CoreSwitchList[0], self.HostList[1])
	self.addLink(self.CoreSwitchList[0], self.HostList[2])

    def enableSSH(self, net, inetIntf='eth0', subnet='10.0.0.0/24' ):
        print 'Enabling ssh'
        routes = [ '10.0.0.0/24' ]
        mac='00:00:00:00:00:17'
        ip='10.123.123.1/32'
        switch = self.CoreSwitchList[0]
        print switch
        switch = net [ switch ]
        root = Node( 'root', inNamespace=False )
        intf = Link( root, switch ).intf1
        root.setIP( ip, intf=intf )
        root.setMAC( mac, intf=intf )
        net.start()
        for route in routes:
            root.cmd( 'route add -net ' + route + ' dev ' + str( intf ) )
        # Identify the interface connecting to the mininet network
        localIntf =  root.defaultIntf()
        # Flush any currently active rules
        root.cmd( 'iptables -F' )
        root.cmd( 'iptables -t nat -F' )
        # Create default entries for unmatched traffic
        root.cmd( 'iptables -P INPUT ACCEPT' )
        root.cmd( 'iptables -P OUTPUT ACCEPT' )
        root.cmd( 'iptables -P FORWARD DROP' )
        # Configure NAT
        root.cmd( 'iptables -I FORWARD -i', localIntf, '-d', subnet, '-j DROP' )
        root.cmd( 'iptables -A FORWARD -i', localIntf, '-s', subnet, '-j ACCEPT' )
        root.cmd( 'iptables -A FORWARD -i', inetIntf, '-d', subnet, '-j ACCEPT' )
        root.cmd( 'iptables -t nat -A POSTROUTING -o ', inetIntf, '-j MASQUERADE' )
        # Instruct the kernel to perform forwarding
        root.cmd( 'sysctl net.ipv4.ip_forward=1' )

def pingTest(net):
    logger.debug("Start Test all network")
    net.pingAll()

def createTopo():
    logging.debug("LV1 Create HugeTopo")
    topo = HugeTopo()
    topo.createTopo() 
    topo.createLink()   
    logging.debug("LV1 Start Mininet")
    CONTROLLER_IP = "127.0.0.1"
    CONTROLLER_PORT = 6653
    net = Mininet(topo=topo, link=TCLink, controller=None, autoSetMacs = True, listenPort=6634)
    net.addController( 'controller',controller=RemoteController,ip=CONTROLLER_IP,port=CONTROLLER_PORT)
    topo.enableSSH(net)
    print "*** Establish routes from end hosts"
    rootip='10.123.123.1'
    subnet='10.0.0.0/24'
    for host in net.hosts:
        host.cmd( 'route add default gw', rootip )
    logger.debug("LV1 dumpNode")
    dumpNodeConnections(net.hosts)
    logger.debug("Sleeping before pingTest");
    CLI(net)
    

if __name__ == '__main__':
    setLogLevel('info')
    if os.getuid() != 0:
        logger.debug("You are NOT root")
    elif os.getuid() == 0:
        createTopo()

