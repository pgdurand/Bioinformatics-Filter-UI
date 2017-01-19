#Bioinformatics Filtering Engine - User Interface API

[![Build Status](https://travis-ci.org/pgdurand/Bioinformatics-Filter-UI.svg?branch=master)](https://travis-ci.org/pgdurand/Bioinformatics-Filter-UI)
[![License AGPL](https://img.shields.io/badge/license-Affero%20GPL%203.0-blue.svg)](https://www.gnu.org/licenses/agpl-3.0.txt)

##Introduction

This package contains the User Interface library used by [BLAST Filter Tool](https://github.com/pgdurand/BLAST-Filter-Tool). 

##Requirements

Use a [Java Virtual Machine](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 1.7 (or above) from Oracle. 

*Not tested with any other JVM providers but Oracle... so there is no guarantee that the software will work as expected if not using Oracle's JVM.*

##Library uses

Since this package is a library, its primary purpose targets a use within other softwares. You can see how to use the library by having a look at:

* [test](https://github.com/pgdurand/Bioinformatics-Filter-UI/tree/master/src/test) package in this project: it contains sample source code; 
* [Blast Filter Tool](https://github.com/pgdurand/BLAST-Filter-Tool) source code: a full example of a running application relying on the BLAST Data Filtering UI

More on the [Wiki](https://github.com/pgdurand/Bioinformatics-Filter-UI/wiki) of this project.

##License and dependencies

"Bioinformatics Filtering Engine - User Interface API" itself is released under the GNU Affero General Public License, Version 3.0. [AGPL](https://www.gnu.org/licenses/agpl-3.0.txt)

It depends on several thrid-party libraries as stated in the NOTICE.txt file provided with this project.

--
(c) 2006-2016 - Patrick G. Durand
