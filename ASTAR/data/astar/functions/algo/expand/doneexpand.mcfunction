tag @e[tag=expanding] add expanded
tag @e[tag=expanding] remove expanding
execute if score global isExpanding matches 1 run scoreboard players set global isExpanding 0
scoreboard players set global expandStarted 0