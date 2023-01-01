#!/bin/env python3
# A simple plot of the captured event data
# HanishKVC, 2022

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import sys



def _vector_info(vdata: pd.Series, tag):
    print("{}: min {}, avg {}, Max {}".format(tag, vdata.min(), vdata.mean(), vdata.max()))

def vector_info(vdata: pd.Series, tag):
    _vector_info(vdata.array, tag)
    deltas = vdata.array[1:] - vdata.array[:-1]
    _vector_info(deltas, "{}-Deltas".format(tag))



df = pd.read_csv(sys.argv[1], sep=' ', names=range(16))
FiSensor = 0
FiTime = 1
FiX = 2
FiY = 3
FiZ = 4

sensorsList = df[FiSensor].unique()
print("NumOfSensors:", sensorsList.size)
print("Sensors:", sensorsList)

fig, ax = plt.subplots(sensorsList.size, 1)
axi = -1
for sensor in sensorsList:
    print("\nPlotting:", sensor)
    axi += 1
    # Extract data belonging to current sensor
    bdf = df[df[FiSensor] == sensor]
    print(bdf)
    # Extract the fields in the data
    dt = bdf[FiTime]
    dx = bdf[FiX]
    dy = bdf[FiY]
    dz = bdf[FiZ]
    vector_info(dt, "\tdt")
    vector_info(dx, "\tdx")
    vector_info(dy, "\tdy")
    vector_info(dz, "\tdz")
    # Plot the fields relative to captured+saved sequence
    ax[axi].plot(dx, label="x")
    ax[axi].plot(dy, label="y")
    ax[axi].plot(dz, label="z")
    ax[axi].set_title(sensor)
    ax[axi].legend()
plt.show()
