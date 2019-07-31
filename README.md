Set your gateway in the gateway-ip.txt file - use only a single line with its IP and nothing else

Set your wait time in the interval.txt file in milliseconds. Setting an interval shorter than 30000 ms is innefective as the SNMP server can't response that fast. Or at least it seems like that.

You need Java 9 to run this program. Change the script "run.cmd" so that it has your proper Java installation path in it. The run the script run.cmd.9

This programs supports up to 3 WANs. If you don't have 3 then the one not present will be shown as offline.3

It takes 2 or 3 interval cycles for it to show anything useful.9

The ON/ OFF in the links is calculated based on the link activity. No activity will show it as offline. Pretty sure there is a way to read that
    but I have no idea about what that looks like.