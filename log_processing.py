import sys

def main(fileName):
    print("Processing input file: {}".format(fileName))
    logFile = open(fileName, "r")

    totalTSTime  = 0
    totalTJTime  = 0
    queryCount = 0

    nsTime = logFile.readline()

    while nsTime:

        TSTJtime = nsTime.split(",")

        TSmsTime = float(TSTJtime[0])/1000000
        TJmsTime = float(TSTJtime[1])/1000000

        totalTSTime += TSmsTime
        totalTJTime += TJmsTime
        queryCount  += 1

        nsTime = logFile.readline()


    TSaverage = totalTSTime/queryCount
    TJaverage = totalTJTime/queryCount

    print("Average TS in ms is: {}ms".format(TSaverage))
    print("Average TJ in ms is: {}ms".format(TJaverage))


if __name__ == "__main__":
    if len(sys.argv) > 1:
        main(sys.argv[1])