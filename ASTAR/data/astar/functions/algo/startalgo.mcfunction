scoreboard players set global disco 0
scoreboard players set @e[tag=src] g 0
scoreboard players operation @e[tag=src] disco = global disco
tag @e[tag=src] add visited
scoreboard players set @e[tag=src] parent -1
scoreboard players add global disco 1
tag @e[tag=src] add expanding
tag @e[tag=src] remove src
scoreboard players set global isExpanding 1
scoreboard players set global algoRunning 1