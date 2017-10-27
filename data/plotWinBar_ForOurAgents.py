#!/usr/bin/python3
import sys
import numpy as np
from scipy import stats
import matplotlib.pyplot as plt
from os.path import basename

def plot(agentNames, *args):
    fig, ax = plt.subplots()
    ind = 0;
    win_rates = {}
    stan_errs = {}
    for filename in args:
        print("Processing " + filename)
        v = np.genfromtxt(filename, names=True, delimiter=',', autostrip=True)
        # get agent
        thisAgent = None
        for n in v.dtype.names:
            if n in agentNames:
                thisAgent = n
                break
        if thisAgent is None:
            sys.exit(1)
        if thisAgent not in win_rates:
            win_rates[thisAgent] = {}
            stan_errs[thisAgent] = {}

        nGames   = sum(v[0]) # number of games per row
        win_rate = np.average(v[thisAgent])/nGames
        win_rates[thisAgent][basename(filename)] = win_rate
        stan_err = stats.sem(v[thisAgent]/nGames)
        stan_errs[thisAgent][basename(filename)] = stan_err
    width = 0.4
    ind = np.arange(2)
    w=-0.2
    for n in agentNames:
        print(win_rates[n].values())
        print([win_rates[n].keys()])
        ax.bar(ind+w, list(win_rates[n].values()), width, yerr=stan_errs[n].values(),label=n,capsize=5)
        w+=width
    ax.set_ylabel("Win rate")
    ax.set_xlabel("Opponents")
    ax.legend(loc="best")
    plt.xticks(ind, ['Greedy', 'Random'])

    ax.xaxis.label.set_size(14)
    ax.yaxis.label.set_size(14)
    plt.legend(loc="best", fontsize=12)
    plt.tick_params(axis='both', which='both', labelsize=12)
    plt.show()
    return fig
    
if __name__ == "__main__":
    ourAgents = ["SOISMCTS", "MOISMCTS"]
    folder_awang = "awang_comparisontests"
    folder_sheath = "sheath_comparisontests"
    folders = [folder_awang, folder_sheath]
    g = "greedyAgent.dat"
    r = "randomAgent.dat"
    opponents = [g, r]
    filenames = []
    for f in folders:
        for o in opponents:
            filenames.append(f + "/" + o)

    fig = plot(ourAgents, *filenames)
    fig.savefig("plot.pdf")
    sys.exit(0)
