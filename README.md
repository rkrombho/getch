[![Build Status](https://api.travis-ci.org/rkrombho/getch.png?branch=master)](https://travis-ci.org/rkrombho/getch)
Getch
=====

Getch is a hierarchical configuration store queryable over a 
simple HTTP interface.

Project Status
==============
This project is in a very early protptype state. Please don't use it anywhere near a productive environment.


Quick Start
===========
```bash
GETCH_VERSION="x.x.x"
HIERARCHY_BASE=<path_to_getch_hierarchy_base_dir>
HOSTNAME=<dns_resolable_hostname>
BIND_PORT=8080

# download the standalone jar
curl -O https://github.com/rkrombho/getch/releases/download/0.0.1/getch-$GETCH_VERSION.jar
# create a config file
cat <<EOF
getch.base.directory = '<path_to_getch_hierarchy_base_dir>'
getch.encryption.password = '<password_for_encryption>'
EOF > ~/.getch.groovy
# run the embedded servlet container
java -Dgetch.config.location=~/.getch.groovy -jar getch-$GETCH_VERSION.jar port=$BIND_PORT host=$HOSTNAME
# create a sample hierarchy 
mkdir -p $HIERARCHY_BASE/myorg/myservice/myenvironment/mytier/HOSTNAME
# create a sample key=value file
echo "testkey=testvalue" > $HIERARCHY_BASE/myorg/myservice/myenvironment/mytier/$HOSTNAME/config.properties
# query the value we just created
curl -i <dnsresolvable_interface_name> -X GET http://$HOSTNAME:$BIND_PORT/getch/testkey
# list all values
curl -i <dnsresolvable_interface_name> -X GET http://$HOSTNAME:$BIND_PORT/getch/list

``` 

Documentation
=============
For more detailed documentation please browse the [Getch wiki](https://github.com/rkrombho/getch/wiki)
