#!/bin/bash

set -e

if [[ -z $1 ]]; then
  echo
  echo "Missing required arguments."
  echo
  echo "    Usage: bootstrap.sh <prefix>"
  echo
  exit 1
fi

PREFIX=$1

echo "Runing benchmarker..."
mkdir -p ~/workspace/$PREFIX
cd ~/benchmarker/workflow
pipenv run python bootstrap.py -p $PREFIX > ~/workspace/$PREFIX/benchmark.log 2>&1
echo "  [Done]"

echo "Uploading logs..."
cd ~/workspace/$PREFIX
if [[ -f logs.tar.gz ]]; then
  gunzip logs.tar.gz
  tar -uf logs.tar benchmark.log
  gzip -f logs.tar
  ossutil cp -f logs.tar.gz oss://middlewarerace2018/$(cat .osspath)/logs.tar.gz
  echo "  [Done]"
fi

echo "Cleaning up..."
rm -f benchmark.log
rm -f logs.tar.gz
rm -f .osspath
echo "  [Done]"
