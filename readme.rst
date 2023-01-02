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

Usage
######

Plot
======

Each captured sensor data event corresponds to one entry along the x-axis.

Y-axis plots the sensed value wrt all the parameters captured by the sensor.
The y-axis is autoscaled based on the min and max value seen in the captured
data across all the fields/parameters.


CSV file
==========

THe logic by default saves/logs sensor data into csv file, at a granularity
of once every 1000 records have been captured. So for now remember to capture
atleast 1000 records wrt any sensor, before switching them, if you want to
save the corresponding sensor data into csv file.

The CSV file is maintained in the external/emulated storage area alloted
for the android application wrt its files.


GUI Interaction
=================

Double tapping the Plot area toggles between FullScreen and inbetween mode
wrt the plot.

Pressing back button, when a sensor is already selected, will clear the
sensor selection. Pressing back button once again will quit the app.

