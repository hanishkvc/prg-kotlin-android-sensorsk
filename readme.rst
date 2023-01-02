###########
SensorsK
###########

HanishKVC, 2022

Overview
##########

Allows one to get the list of sensors in a android device and inturn select
one of them for monitoring live in GUI, as well as log into a csv file for
later analysis.

Parallely the GPS location info is also saved into the csv file.

Android app color codes upto 3 parameters wrt any given sensor with R, G, B.
However if there are more than 3 parameters being monitored by a sensor, then
it will plot the remaining parameters with black color.

There is also a python helper script to plot the captured sensor data. If
the user had switched sensors in a given run of the android app, then the
csv file would have data about multiple sensors, the logic will plot data
wrt each sensor seperately.

A long time back, I remember there being a google's android app for monitoring
sensors in a android device or so, now I am not able to find/recall the same,
so created this, for some experimentation I was doing.
