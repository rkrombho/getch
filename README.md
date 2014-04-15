Getch
=====

Getch is a hierarchical configuration store queryable over a 
simple HTTP interface.

Why?
====
Getch is insipired by [Hiera](https://github.com/puppetlabs/hiera) and copies the concept
of a queryble hierarchical datastore. 
It is a purely server side implementation which uses HTTP to offer a queryable interface
to hosts in your DataCenter (or anywhere else as long as you can establish a TCP connection).

The tool is meant to be used by organization who don't have a fully fledged configuration 
management system (like Chef, Puppet etc.) deployed but still want to benefit from a central
hierarchical configuration store.


Concept
=======
The idea is to build a directory tree structure representing the
organizational and technical structure of your IT services.

When a query arrives at the server it will do a reverse DNS lookup of 
the querying host, try to find a directory inside the tree structure
with the name of querying host and than search for the queried 'key' 
upwards in the directory tree (queries from other hosts are 
possible but additionally require to explicitly query for a specific 'host').
You can place all sorts of different configuration files on all layers
of your directory tree structure and the query will find the first occurance
of the 'key' by traversing through all files upwards in the tree.

If it finds a property / yaml entry / xml element / xml attribute 
somewhere in the tree it will return the corresponding value.

Furthermore if the key represnets a filename within the directory structure
(e.g. config.xml) it will return the whole file (with the correct MIME type setted) 
as a response to the HTTP request.
Before the file is returned the server will first run the file through 
a templating engine in order to resolve any potentially defined 
varialbes used in that file. The template engine will then resolve all 
variables with the values that it are available upwards in the tree.


Sample structure
================
Here is a simple sample of a directory & file hierarchy:
```bash
common
├── config.properties
└── dc1
    ├── config.properties
    └── mydepartment
        ├── config.properties
        └── myproduct
            ├── app
            │   ├── config.properties
            │   └── hostname2
            │       └── config.properties
            ├── config.properties
            └── web
                ├── config.properties
                └── hostname1
                    └── config.properties
```
_**note:** in this sample contains structure only single .properties files 
but you can have as many files on every level of the tree as you want.
Additionally you can use [.properties](http://en.wikipedia.org/wiki/.properties) (key=value), [yaml](http://www.yaml.org/), [json](http://www.json.org/) or [xml](http://www.w3schools.com/xml/xml_whatis.asp) files
to store configuration values in._

Query single values
===================
Below is a sample CURL command to query a key called `mykey` from a Getch 
server:
```bash
curl -X GET http://mygetchhost:port/getch/mykey
```

With hierarchy setup as desceived above, if the query arrives 
from `hostname1`, Getch will search through the content of 
the following files in that order:
```bash
common/mydatacenter/mydepartment/myproduct/web/hostname1/config.properties
common/mydatacenter/mydepartment/myproduct/web/config.properties
common/mydatacenter/mydepartment/myproduct/config.properties
common/mydatacenter/mydepartment/config.properties
common/mydatacenter/config.properties
common/config.properties
```
In case the searched 'key' does not exist in any of the files a HTTP 404 
is going to be returned.
If a value is found it is returned as a response to the HTTP request
within the response body. The MIME type for this response it going to be `text/plain`.

Query complete configuration files
==================================
//TODO: document this
