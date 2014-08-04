[![Build Status](https://api.travis-ci.org/rkrombho/getch.png?branch=master)](https://travis-ci.org/rkrombho/getch)
[![Stories in Ready](https://badge.waffle.io/rkrombho/getch.png?label=ready&title=Ready)](https://waffle.io/rkrombho/getch)
Getch
=====
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
```

## Use Getch
```bash
# create a sample hierarchy 
mkdir -p $HIERARCHY_BASE/myorg/myservice/myenvironment/mytier/HOSTNAME
# create a sample key=value file
echo "testkey=testvalue" > $HIERARCHY_BASE/myorg/myservice/myenvironment/mytier/$HOSTNAME/config.properties
# query the value we just created
curl -i <dnsresolvable_interface_name> -X GET http://$HOSTNAME:$BIND_PORT/getch/testkey
> testvalue
# list all values
curl -i <dnsresolvable_interface_name> -X GET http://$HOSTNAME:$BIND_PORT/getch/list
> textkey=testvalue
# create a template file
echo "MyDirective ${teskey}" > $HIERARCHY_BASE/myorg/myservice/my.conf
# query the templated file
curl -i <dnsresolvable_interface_name> -X GET -O http://$HOSTNAME:$BIND_PORT/getch/my.conf
cat my.conf
> MyDirective testvalue
``` 

Documentation
=============
For a more detailed documentation and description of all features please browse the [Getch wiki](https://github.com/rkrombho/getch/wiki)
