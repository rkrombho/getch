[![Build Status](https://api.travis-ci.org/rkrombho/getch.png?branch=master)](https://travis-ci.org/rkrombho/getch)
Getch
=====

Getch is a hierarchical configuration store queryable over a 
simple HTTP interface.

Project Status
==============
This project is in a very early protptype state. Please don't use it anywhere near a productive environment.


The tool is meant to be used by organization who don't have a fully fledged configuration 
management system (like Chef, Puppet etc.) deployed but still want to benefit from a central
hierarchical configuration store.

Additionally because everything is stored in a directory structure in plain text files you can manage the whole configuration hierarchy in the version control system of your choice (passwords can by stored in a encrypted format).


Concept
=======
The idea is to build a directory tree structure representing the structure of your IT organization and services.

This structure can be queried with any HTTP client (be it a browser or a command-line tool like  ``curl`` or ``wget``).
Clients can query either for a single key in order to receive single value or filenames in order to download the complete file.

Getch reacts to queries by searching thorugh its configured directory tree until it finds a directory with the same name that the querying ``host`` has. Starting from this directory it will scan all files downwards **and** upwards in the directory tree.

In case a singley ``key`` was queried, all found files (on all levels of the tree in the path identifies with the hostname) fill be scanned to see if any contains the queried ``key``. The corresponding ``value`` of the ``key`` is then returned as plain text to inside the HTTP response body.
The identified tree path is scanned bottom-up and therefore more concrete (lower level) values are preffered over more commonly defined (higher level) values.

In case a ``filename`` was queried Getch will scan all levels of the tree (again only the path to the querying hostname directory) in order to find a file with the same name. If it found one it will send the file through an internal template engine before it is returned as downloadable file back to the HTTP client. The templating engine will have all ``key=value`` pairs from all configuration files of all layers thorugh the complete path of the hostname in it's context. Effectively this way you can use configuration values from anywhere in the hierarchy as variables (referenced by their key) in configuration files that you store in Getch.

Below a sample directory structure that Getch may work with. How you layout your hierarchy is completely up to you.

```bash
  .
  └── myorg/
      ├── departmentA/
      │   ├── common.properties
      │   ├── serviceA/
      │   │   ├── preprod/
      │   │   │   ├── app/
      │   │   │   │   └── hostname5/
      │   │   │   │       └── tomcat/
      │   │   │   │           ├── datasources.yaml
      │   │   │   │           └── instance.properties
      │   │   │   ├── db/
      │   │   │   │   └── hostname6/
      │   │   │   │       └── mysql/
      │   │   │   │           └── my.cnf
      │   │   │   └── web/
      │   │   │       └── hostname4/
      │   │   │           └── apache/
      │   │   │               └── webserver_details.properties
      │   │   ├── prod/
      │   │   │   ├── app/
      │   │   │   │   └── hostname2/
      │   │   │   │       ├── hostconfig.properties
      │   │   │   │       └── tomcat/
      │   │   │   │           ├── datasources.yaml
      │   │   │   │           └── instance.properties
      │   │   │   ├── db/
      │   │   │   │   └── hostname3/
      │   │   │   │       ├── hostconfig.properties
      │   │   │   │       └── mysql/
      │   │   │   │           └── my.cnf
      │   │   │   └── web/
      │   │   │       └── hostname1/
      │   │   │           ├── apache/
      │   │   │           │   └── webserver_details.properties
      │   │   │           └── hostconfig.properties
      │   │   ├── serviceA.yaml
      │   │   └── test/
      │   │       ├── ...
      │   └── serviceB/
      │       ├── preprod/
      │       │   ├── app/
      │       │   │   ├── hostname5/
      │       │   │   │   └── tomcat/
      │       │   │   │       ├── datasources.yaml
      │       │   │   │       └── instance.properties
      │       │   │   └── hostname14/
      │       │   │       └── tomcat/
      │       │   ├── db/
      │       │   │   ├── hostname6/
      │       │   │   │   └── mysql/
      │       │   │   │       └── my.cnf
      │       │   │   └── hostname15/
      │       │   │       └── mysql/
      │       │   └── web/
      │       │       ├── hostname4/
      │       │       │   └── apache/
      │       │       │       └── webserver_details.properties
      │       │       └── hostname13/
      │       │           └── apache/
      │       ├── prod/
      │       │   ├── app/
      │       │   │   ├── hostname2/
      │       │   │   │   ├── hostconfig.properties
      │       │   │   │   └── tomcat/
      │       │   │   │       ├── datasources.yaml
      │       │   │   │       └── instance.properties
      │       │   │   └── hostname11/
      │       │   │       └── tomcat/
      │       │   ├── db/
      │       │   │   ├── hostname3/
      │       │   │   │   ├── hostconfig.properties
      │       │   │   │   └── mysql/
      │       │   │   │       └── my.cnf
      │       │   │   └── hostname12/
      │       │   │       └── mysql/
      │       │   └── web/
      │       │       ├── hostname1/
      │       │       │   ├── apache/
      │       │       │   │   └── webserver_details.properties
      │       │       │   └── hostconfig.properties
      │       │       └── hostname10/
      │       │           └── apache/
      │       ├── serviceB.yaml
      │       └── test/
      │           ├── ...
      ├── departmentB/
      └── org_conventions.properties

```

Supported file types
===================
Currently Getch supports only YAML and .properties files as key-value stores for single value queries.
JSON and XML are planned to be implemented soon.

For full file queries Getch support all text based configuration files.
Note that when templating is activated you may need to escape all ``$`` characters in your files with a backslash 
because this is used by Groovys [SimpleTemplateEngine](http://groovy.codehaus.org/Groovy+Templates).


Installation
============
//TODO: document this

List values
=======
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
