#!/bin/bash

# List of ODroids"
ODROIDS="192.168.1.107
192.168.1.113
192.168.1.112
192.168.1.100
192.168.1.101
192.168.1.110
192.168.1.108
192.168.1.119
192.168.1.114
192.168.1.115
192.168.1.105"

echo "Attempt to reboot all ODroids"
for entry in $ODROIDS; do
  echo "Reboot $entry"
  ssh -i ~/.ssh/odroid root@$entry "reboot"
done
echo "Rebooting done"