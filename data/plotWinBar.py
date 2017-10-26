#!/usr/bin/python3
import sys
import numpy as np
from scipy import stats
import matplotlib.pyplot as plt

def plot(agentName, *args):
    fig, ax = plt.subplots()
    ind = 0;
    for filename in args:
        print("Processing " + filename)
        v = np.genfromtxt(filename, names=True, delimiter=',', autostrip=True)
        nGames   = sum(v[0]) # number of games per row
        win_rate = np.average(v[agentName])/nGames
        stan_err = stats.sem(v[agentName])/nGames
        ax.bar(ind, win_rate, yerr=stan_err, capsize=5)
        #ax.set_xticks(ind+0.4) # centre
        ind += 1
    #ax.set_xticklabels([f for f in args])
    ax.set_ylabel("Win rate")
    ax.set_xlabel("Opponents")
    plt.xticks(range(ind), [f for f in args])
    #plt.show()
    return fig
    
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("incorrect number of arguments.")
        print("use: python3 " + sys.argv[0] + " agentName filename0 [filename1...]")
        sys.exit(1)
    fig = plot(sys.argv[1], *sys.argv[2:])
    fig.savefig("plot.pdf")
    sys.exit(0)
