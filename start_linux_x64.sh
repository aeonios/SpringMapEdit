#!/bin/sh

arch=x64
maxmem=2048m
lib=lib_lin_$arch

java -cp springmapedit.jar:$lib/gluegen-rt.jar:$lib/jogl.jar:$lib/swt.jar -Xms512m -Xmx$maxmem -Djava.library.path=$lib application.SpringMapEditApplication
