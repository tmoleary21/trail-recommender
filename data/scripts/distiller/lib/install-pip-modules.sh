#!/bin/bash
if [[ "$OSTYPE" == "darwin"* ]]; then
    export PY=python3
else
    export PY=python3.9
fi

if [ ! -d ./venv ]
then
    $PY -m venv venv
fi

source ./venv/bin/activate

echo "Installing pip packages"
$PY -m pip install -r requirements.txt
