#!/usr/bin/python3
import sys
import numpy as np
from scipy import stats
import matplotlib.pyplot as plt

def plot(agentName, *args):
    fig, ax = plt.subplots()
    SKIP = 3
    for filename in args:
        print("Processing " + filename)
        v = np.genfromtxt(filename, names=True, delimiter=',', autostrip=True,skip_header=3)
        summaryStats = []
        nGames = sum(v[0]) - v[0]["exploration"]  # yikes
        for x in np.unique(v["exploration"]):
            slice = v[np.where(v["exploration"] == x)][agentName]
            summaryStats.append([x, np.average(slice)/nGames, stats.sem(slice/nGames)])
        print(filename)
        for x in summaryStats:
            print(x)
        xVals = [row[0] for row in summaryStats]
        yVals = [row[1] for row in summaryStats]
        stdErr = [row[2] for row in summaryStats]
        ax.errorbar(xVals, yVals, yerr=stdErr, capsize=3, label=filename)
    ax.set_ylabel("Win rate")
    ax.set_xlabel("Exploration coefficient")
    ax.xaxis.label.set_size(16)
    ax.yaxis.label.set_size(16)
    plt.legend(loc="best", fontsize=14)
    return fig

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("incorrect number of arguments.")
        print("use: python3 " + sys.argv[0] + " agentName filename0 [filename1...]")
        sys.exit(1)
    fig = plot(sys.argv[1], *sys.argv[2:])
    fig.savefig("plot.pdf")
    sys.exit(0)
