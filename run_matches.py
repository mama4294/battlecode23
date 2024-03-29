from itertools import product
import re
import subprocess

emojiMode = True
emojiMap = {
    'Won': ':heavy_check_mark:',
    'Lost': ':x:',
    'Tied': ':grimacing:',
    'N/A': ':heavy_minus_sign:',
    'Error': ':heavy_exclamation_mark:'
}
errors = []
currentBot = 'learningBot'

bots = ['examplefuncsplayer']
#bots = ['manaOnly', 'sprintTesting1', 'sprintTesting2', 'sprintTesting3', 'sprintTesting5', 'sprintTesting8']
botsSet = set(bots)
#maps = ['PairedProgramming', 'Rewind', 'Turtle']
#maps = ['Rewind', 'Heart', 'LightWork', 'TreasureMap', 'HotAirBalloon', 'Movepls', 'Grievance', 'IslandHopping']
#maps = ['DefaultMap', 'AllElements', 'SmallElements', 'maptestsmall']
#maps = ['DefaultMap', 'AllElements', 'SmallElements', 'maptestsmall', 'generated_captain_america', 'generated_chalice', 'generated_charge', 'generated_chessboard', 'generated_cobra', 'generated_collaboration', 'generated_colosseum', 'generated_deer', 'generated_defenseless']


# sprint 1 maps
#maps = ['ArtistRendition', 'BatSignal', 'BowAndArrow', 'Cat', 'Clown', 'Diagonal', 'Eyelands', 'Frog', 'Grievance', 'Hah', 'Jail', 'KingdomRush', 'Minefield', 'Movepls', 'Orbit', 'Pathfind', 'Pit', 'Pizza', 'Quiet', 'Rectangle', 'Scatter', 'Sun', 'Tacocat']
#maps = ['Eyelands']

# sprint 2 maps
#maps = ['BattleSuns', 'Checkmate2', 'Cornucopia', 'Crossword', 'Cube', 'Divergence', 'FourNations', 'HideAndSeek', 'Lantern', 'Lines', 'Maze', 'Pakbot', 'Piglets', 'Risk', 'Sine', 'Snowflake', 'SomethingFishy', 'Spin', 'Spiral', 'Squares', 'Star', 'Sus', 'SweetDreams', 'TicTacToe', 'USA']

# us qual maps
#maps = ['AbsoluteW', 'Buggy', 'Cave', 'Cee', 'Heart', 'HotAirBalloon', 'IslandHoppingTwo', 'LightWork', 'MassiveL', 'Potions', 'Rainbow', 'Resign', 'Sneaky', 'Target', 'Tightrope']

#maps = ['ReverseFunnel', 'Swooshy']
#maps = ['Quiet', 'Spin', 'Diagonal']
maps = ['maptestsmall']

mapsSet = set(maps)

matches = set(product(bots, maps))

numWinsMapping = {
    0: 'Lost',
    1: 'Tied',
    2: 'Won',
}


def retrieveGameLength(output):
    startIndex = output.find('wins (round ')
    if startIndex == -1:
        return -1
    endIndex = output.find(')', startIndex)
    if endIndex == -1:
        return -1
    return output[startIndex + len('wins(round ') + 1:endIndex]

def run_match(bot, map):
    print("Running {} vs {} on {}".format(currentBot, bot, map))
    try:
        outputA = str(subprocess.check_output(['./gradlew', 'run', '-PteamA=' + currentBot, '-PteamB=' + bot, '-Pmaps=' + map]))
        outputB = str(subprocess.check_output(['./gradlew', 'run', '-PteamA=' + bot, '-PteamB=' + currentBot, '-Pmaps=' + map]))
        # for local windows testing
        #outputA = str(subprocess.check_output(['gradlew', 'run', '-PteamA=' + currentBot, '-PteamB=' + bot, '-Pmaps=' + map], shell=True))
        #outputB = str(subprocess.check_output(['gradlew', 'run', '-PteamA=' + bot, '-PteamB=' + currentBot, '-Pmaps=' + map], shell=True))
    except subprocess.CalledProcessError as exc:
        print("Status: FAIL", exc.returncode, exc.output)
        return 'Error'
    else:
        winAString = '{} (A) wins'.format(currentBot)
        winBString = '{} (B) wins'.format(currentBot)
        loseAString = '{} (B) wins'.format(bot)
        loseBString = '{} (A) wins'.format(bot)
        resignedString = 'resigned'
        
        numWins = 0
        
        gameLengthA = retrieveGameLength(outputA)
        gameAResigned = resignedString in outputA
        gameLengthB = retrieveGameLength(outputB)
        gameBResigned = resignedString in outputB

        flagRegex = "FLAG{[^{}]*}"
        gameAFlags = list(set(re.findall(flagRegex, outputA)))
        gameBFlags = list(set(re.findall(flagRegex, outputB)))
        gameAFlags = ", ".join([s[5:-1] for s in gameAFlags])
        gameBFlags = ", ".join([s[5:-1] for s in gameBFlags])
        if len(gameAFlags) > 0:
            gameAFlags = "{{{}}}".format(gameAFlags)
        if len(gameBFlags) > 0:
            gameBFlags = "{{{}}}".format(gameBFlags)

        gameAInfo = gameLengthA + ('*' if gameAResigned else '') + gameAFlags
        gameBInfo = gameLengthB + ('*' if gameBResigned else '') + gameBFlags
        
        if winAString in outputA:
            numWins += 1
        else:
            if not loseAString in outputA:
                return 'Error'
        if winBString in outputB:
            numWins += 1
        else:
            if not loseBString in outputB:
                return 'Error'
        return numWinsMapping[numWins] + ' (' + ', '.join([gameAInfo, gameBInfo]) + ')'


results = {}
# Run matches
for bot, map in matches:
    # Verify match is valid
    if not bot in botsSet or not map in mapsSet:
        errors.append('Unable to parse bot={}, map={}'.format(bot, map))
    # run run_match.py
    
    results[(bot, map)] = run_match(bot, map)

# Construct table
table = [[results.get((bot, map), 'N/A') for bot in bots] for map in maps]

def replaceWithDictionary(s, mapping):
    for a, b in mapping.items():
        s = s.replace(a, b)
    return s

if emojiMode:
    table = [[replaceWithDictionary(item, emojiMap) for item in row] for row in table]

# Write to file
with open('matches-summary.txt', 'w') as f:
    table = [[''] + bots, [':---:' for i in range(len(bots) + 1)]] + [[map] + row for map, row in zip(maps, table)]
    for line in table:
        f.write('| ')
        f.write(' | '.join(line))
        f.write(' |')
        f.write('\n')
    f.write('\n')
    for error in errors:
        f.write(error)
