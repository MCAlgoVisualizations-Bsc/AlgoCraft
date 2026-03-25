execute store result storage minecraft:astar temp int 1 run scoreboard players get global value
data modify storage minecraft:astar queue prepend from storage minecraft:astar temp
execute store result storage minecraft:astar temp int 1 run scoreboard players get global disco
data modify storage minecraft:astar disco prepend from storage minecraft:astar temp
#scoreboard players add global disco 1
summon bat ~ -20 ~ {CustomName:'"puttingbackintoqueue"'}