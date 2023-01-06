# Create icon for the program
# HanishKVC, 2022

import matplotlib.pyplot as plt
import numpy as np

b1 = np.arange(0,np.pi*2*8,0.01)
a1=np.linspace(0,8,int(b1.size/2))
a1r = a1[-1:0:-1]
a2=np.append(a1,a1r)
a2n=a2/a2.max()
r1=np.exp(a2n)*np.sin(b1)[:5025]
r2=np.exp(a2)*np.sin(b1)[:5025]
r2adj = r2.max()/r1.max()
plt.plot(r1,'r')
plt.plot(r2/r2adj,'g')
plt.text(0.02,0.88,"SensorsK", fontsize=36, transform=plt.gca().transAxes)
plt.xticks([])
plt.yticks([])
fig = plt.gcf()
fig.set_figwidth(5.12)
fig.set_figheight(5.12)
plt.savefig("data/icon.png")
plt.show()
