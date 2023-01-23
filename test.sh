#!/bin/bash

java \
  -cp target/*:target/dependencies/* \
  org.devopology.test.engine.TestEngine
