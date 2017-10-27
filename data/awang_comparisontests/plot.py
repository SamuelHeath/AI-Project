import sys
import numpy as np
from scipy import stats
import matplotlib.pyplot as plt

def plot(filename, agentName):
    v = np.genfromtxt(filename, names=True, delimiter=',', autostrip=True)
    # Then calculate the mean winrate.
    # Then calculate the standard error winrate.
    nGames   = sum(v[0]) # number of games per row
    win_rate = np.average(v[agentName])/nGames
    stan_err = stats.sem(v[agentName])/nGames
    
    
    
if __name__ == "__main__":
    if len(sys.argv) is not 3:
        print("Incorrect number of arguments.")
        print("Usage: " + sys.argv[0] + "filename agentName")
        sys.exit(1)
    else:
        plot(sys.argv[1], sys.argv[2])
