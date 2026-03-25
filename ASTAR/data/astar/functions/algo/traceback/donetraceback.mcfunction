execute as @e[tag=backtrack, scores={parent=-1}] run kill @e[tag=visited, tag=!path, tag=!dest, scores={parent=0..}]
execute as @e[tag=backtrack, scores={parent=-1}] run kill @e[tag=queue]
execute at @e[scores={parent=-1},limit=1] run summon minecraft:armor_stand ~ ~ ~ {Tags:["visited"], ArmorItems:[{},{},{},{id:"orange_wool",Count:1b}], NoGravity:1b, Small:1b, Invisible:1b}
tag @e[tag=backtrack] remove backtrack
scoreboard players set global algoRunning 0