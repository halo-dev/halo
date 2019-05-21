#!/bin/bash
PACKAGE_VERSION=$(sed -nE 's/^\s*"version": "(.*?)",$/\1/p' package.json)
echo "Halo admin version $PACKAGE_VERSION"
zip -r dist/halo-admin-$PACKAGE_VERSION.zip dist

export PACKAGE_VERSION