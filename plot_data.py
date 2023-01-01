#!/bin/env python3
# A simple plot of the captured event data
# HanishKVC, 2022

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import sys



def _vector_info(vdata: pd.Series, tag):
    print("{}: min {:32.12f}, avg {:32.12f}, Max {:32.12f}".format(tag, vdata.min(), vdata.mean(), vdata.max()))

def vector_info(vdata: pd.Series, tag):
    _vector_info(vdata.array, "\n{}-RawDat".format(tag))
    deltas = vdata.array[1:] - vdata.array[:-1]
    _vector_info(deltas, "{}-Deltas".format(tag))


FNSensor = 'sensor'
FNTime = 'time'
FNames = [ FNSensor, FNTime ]
ValueFieldsCnt = 20
for i in range(ValueFieldsCnt):
    FNames.append("F{}".format(i))

df = pd.read_csv(sys.argv[1], sep=' ', names=FNames)

sensorsList = df[FNSensor].unique()
print("NumOfSensors:", sensorsList.size)
print("Sensors:", sensorsList)

fig, ax = plt.subplots(sensorsList.size, 1)
fig.set_size_inches(10, 6*sensorsList.size)
axi = -1
for sensor in sensorsList:
    print("\nPlotting:", sensor)
    axi += 1
    # Extract data belonging to current sensor
    bdf = df[df[FNSensor] == sensor]
    print(bdf)
    # Extract the fields in the data
    dt = bdf[FNTime]
    dv = []
    for i in range(2,len(FNames)):
        cv = bdf[FNames[i]]
        if bdf[cv.notna()].size == 0:
            break
        dv.append(cv)
    # Show info including Plot the fields relative to captured+saved sequence
    vector_info(dt, "\tdt")
    for i in range(len(dv)):
        cv = dv[i]
        vector_info(cv, "\t{}".format(cv.name))
        ax[axi].plot(cv, label=cv.name)
    ax[axi].set_title(sensor)
    ax[axi].legend()
fig.savefig("/tmp/plotdata.png")
plt.show()
