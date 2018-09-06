#!/usr/bin/env bash
docker run --name mysql -e MYSQL_ROOT_PASSWORD=root --net host -d mysql:5