#!/usr/bin/python3
import sys
import numpy as np
from scipy import stats
import matplotlib.pyplot as plt


def plot(*args):
    means = {}
    stan_errs = {}
    fig, ax = plt.subplots()
    for filename in args:
        print("Processing " + filename)
        v = np.genfromtxt(filename,names=True,delimiter=',',autostrip=True,skip_header=3)
        nGames = sum(v[0])  # number of games per row
        for agentName in v.dtype.names:
            if agentName not in means:
                means[agentName] = []
                stan_errs[agentName] = []
            win_rate = np.average(v[agentName]) / nGames
            means[agentName].append(win_rate)
            stand_err = stats.sem(v[agentName] / nGames)
            stan_errs[agentName].append(stand_err)
    ax.set_ylabel("Win rate")
    width = 0.2
    ind = np.arange(2)
    w = -width
    for agentName in means:
        ax.bar(ind+w, means[agentName], width, yerr=stan_errs[agentName],label=agentName,capsize=5)
        w += width
    print(stan_errs)
            
    #ax.bar(ind+w, win_rate,width, yerr=stan_err, capsize=5)
    #w += width
    ax.set_xlabel("Game")
    plt.xticks(ind, [f for f in args])
    # plt.show()
    ax.legend(loc="best")

    ax.xaxis.label.set_size(14)
    ax.yaxis.label.set_size(14)
    plt.legend(loc="best", fontsize=12)
    plt.tick_params(axis='both', which='both', labelsize=12)
    return fig


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("incorrect number of arguments.")
        sys.exit(1)
    fig = plot(*sys.argv[1:])
    fig.savefig("plot.pdf")
    sys.exit(0)
