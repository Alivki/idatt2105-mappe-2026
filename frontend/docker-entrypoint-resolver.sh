#!/bin/sh
# Extract the DNS resolver from /etc/resolv.conf and export it
# so nginx envsubst can inject it into the config template
export NGINX_RESOLVER=$(awk '/^nameserver/{print $2; exit}' /etc/resolv.conf)
echo "Using DNS resolver: $NGINX_RESOLVER"
