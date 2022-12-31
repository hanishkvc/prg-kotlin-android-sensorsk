#!/bin/env python3
# A simple plot of the captured event data
# HanishKVC, 2022

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import sys


df = pd.read_csv(sys.argv[1], sep=' ', names=['sensor', 'time', 'x', 'y', 'z'])
dtime = df['time']
dx = df['x']
dy = df['y']
dz = df['z']

plt.plot(dx, label="x")
plt.plot(dy, label="y")
plt.plot(dz, label="z")
plt.legend()
plt.show()

