#!/bin/bash

# Example of Linux regain start script

# Option 1: install jdic-libs (Ubuntu/Debian e.g. sudo apt-get install libjdic-bin)
# Preferred
java -Djava.library.path=/usr/lib/jni -jar regain.jar

# Option 2: use provided (Ubuntu 32bit, 10.10.) jdic-libs
            at which the path (absolut!) points to the installation dir
# java -Djava.library.path=/usr/local/regain/ -jar regain.jar




