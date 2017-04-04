#!/bin/sh
set -e
# check to see if protobuf folder is empty
if [ ! -d "$HOME/protobuf/lib" ]; then
  wget https://github.com/google/protobuf/releases/download/v3.0.0/protobuf-java-3.0.0.zip
  unzip -o protobuf-java-3.0.0
  cd protobuf-java-3.0.0 && ./configure --prefix=$HOME/protobuf && make && make install
else
  echo "Using cached directory."
fi

export PATH=$HOME/protobuf/bin:$PATH


