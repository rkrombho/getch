[![Build Status](https://api.travis-ci.org/rkrombho/getch.png?branch=master)](https://travis-ci.org/rkrombho/getch)
[![Stories in Ready](https://badge.waffle.io/rkrombho/getch.png?label=ready&title=Ready)](https://waffle.io/rkrombho/getch)

![alt text](https://raw.githubusercontent.com/rkrombho/getch/master/logo/getch_logo.png "Getch")

Getch is a very minimalistic and simple hierarchical configuration store and template-engine queryable via HTTP.

Project Status
==============
The project is still in a very early stage but all existing features are stable and tested in a productive environment.

Quick Start
===========
## Run Getch
```bash
GETCH_VERSION="x.x.x"
HIERARCHY_BASE=<path_to_getch_hierarchy_base_dir>
HOSTNAME=<dns_resolable_hostname>
ENCRYPTION_PASSWORD=<secret>
BIND_PORT=8080

# download the standalone jar
curl -L -O https://github.com/rkrombho/getch/releases/download/$GETCH_VERSION/getch-$GETCH_VERSION.jar
# create a config file
mkdir ~/.getch
(
cat <<EOF                     
getch.base.directory = '$HIERARCHY_BASE'
getch.encryption.password = '$ENCRYPTION_PASSWORD'
getch.trusted.proxies = [ '127.0.0.1' ]
getch.feature.templating.enabled = true
EOF
) > ~/.getch/getch.groovy
# run the embedded servlet container
java -jar getch-$GETCH_VERSION.jar port=$BIND_PORT host=$HOSTNAME
```

## Use Getch
```bash
HIERARCHY_BASE=<path_to_getch_hierarchy_base_dir>
HOSTNAME=<dns_resolable_hostname>
BIND_PORT=8080
# create a sample hierarchy 
mkdir -p $HIERARCHY_BASE/myorg/myservice/myenvironment/mytier/$HOSTNAME
# create a sample key=value file
echo "testkey=testvalue" > $HIERARCHY_BASE/myorg/myservice/myenvironment/mytier/$HOSTNAME/config.properties
# query the value we just created (note: your default network interface must DNS resolve to $HOSTNAME)
curl -X GET http://$HOSTNAME:$BIND_PORT/testkey
> testvalue
# list all values
curl -X GET http://$HOSTNAME:$BIND_PORT/list
> textkey=testvalue
# create a template file
echo "MyDirective \${testkey}" > $HIERARCHY_BASE/myorg/myservice/my.conf
# query the templated file
curl -X GET -O http://$HOSTNAME:$BIND_PORT/my.conf
cat my.conf
> MyDirective testvalue
``` 

*Note:* There is also a command line client available for Getch called [getchctl](https://github.com/rkrombho/getchctl) which supports the majority of the features that Getch pffers. It's available for Windows and Linux, for i386 and amd64 architectures. 


Documentation
=============
For a more detailed documentation and description of all features please browse the [Getch wiki](https://github.com/rkrombho/getch/wiki)
