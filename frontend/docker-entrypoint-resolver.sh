#!/bin/sh
# Extract the DNS resolver from /etc/resolv.conf and export it
# so nginx envsubst can inject it into the config template
RAW_RESOLVER=$(awk '/^nameserver/{print $2; exit}' /etc/resolv.conf)
# Wrap IPv6 addresses in brackets for nginx
if echo "$RAW_RESOLVER" | grep -q ':'; then
  export NGINX_RESOLVER="[$RAW_RESOLVER]"
else
  export NGINX_RESOLVER="$RAW_RESOLVER"
fi
echo "Using DNS resolver: $NGINX_RESOLVER"
