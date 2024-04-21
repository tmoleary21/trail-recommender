#! /bin/bash

tmux new-session -d -s trail-flask-server
tmux send-keys -t trail-flask-server "source ./env/bin/activate" C-m "python3 server.py" C-m
tmux attach-session -t trail-flask-server

