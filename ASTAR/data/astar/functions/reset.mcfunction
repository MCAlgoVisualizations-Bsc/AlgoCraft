#execute at @e[type=minecraft:armor_stand] if block ~ ~ ~ oak_sign run setblock ~ ~ ~ air replace
scoreboard players set global isExpanding 0
scoreboard players set global algoRunning 0
scoreboard players set global i -1
scoreboard players set global expandStarted 0
kill @e[tag=visited]
kill @e[tag=queue]
kill @e[tag=dest]
kill @e[tag=src]
tp @e[type=bat] ~ -100 ~
kill @e[type=bat]
