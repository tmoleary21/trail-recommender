#!/bin/bash

tmux send-keys -t trail-flask-server "^C"
tmux kill-session -t trail-flask-server

