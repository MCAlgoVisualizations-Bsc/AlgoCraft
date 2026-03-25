scoreboard players operation @e[tag=currentNeigh] g = @e[tag=expanding] g
execute at @e[tag=currentNeigh] run scoreboard players add @e[type=minecraft:armor_stand, distance=0] g 1
execute at @e[tag=currentNeigh] run scoreboard players operation @e[tag=visited, distance=0] parent = @e[tag=expanding] disco
execute at @e[tag=currentNeigh] run scoreboard players operation @e[tag=visited, distance=0] disco = global disco
scoreboard players add global disco 1

#calculate h
function astar:algo/finddestpos

execute as @e[tag=currentNeigh] run execute store result score @s X run data get entity @s Pos[0]
execute as @e[tag=currentNeigh] run execute store result score @s Y run data get entity @s Pos[1]
execute as @e[tag=currentNeigh] run execute store result score @s Z run data get entity @s Pos[2]

execute as @e[tag=currentNeigh] if score @s X >= @e[tag=dest,limit=1] X run scoreboard players operation @s XDiff = @s X
execute as @e[tag=currentNeigh] if score @s X >= @e[tag=dest,limit=1] X run scoreboard players operation @s XDiff -= @e[tag=dest,limit=1] X
execute as @e[tag=currentNeigh] if score @s Y >= @e[tag=dest,limit=1] Y run scoreboard players operation @s YDiff = @s Y
execute as @e[tag=currentNeigh] if score @s Y >= @e[tag=dest,limit=1] Y run scoreboard players operation @s YDiff -= @e[tag=dest,limit=1] Y
execute as @e[tag=currentNeigh] if score @s Z >= @e[tag=dest,limit=1] Z run scoreboard players operation @s ZDiff = @s Z
execute as @e[tag=currentNeigh] if score @s Z >= @e[tag=dest,limit=1] Z run scoreboard players operation @s ZDiff -= @e[tag=dest,limit=1] Z

execute as @e[tag=currentNeigh] if score @s X < @e[tag=dest,limit=1] X run scoreboard players operation @s XDiff = @e[tag=dest,limit=1] X
execute as @e[tag=currentNeigh] if score @s X < @e[tag=dest,limit=1] X run scoreboard players operation @s XDiff -= @s X
execute as @e[tag=currentNeigh] if score @s Y < @e[tag=dest,limit=1] Y run scoreboard players operation @s YDiff = @e[tag=dest,limit=1] Y
execute as @e[tag=currentNeigh] if score @s Y < @e[tag=dest,limit=1] Y run scoreboard players operation @s YDiff -= @s Y
execute as @e[tag=currentNeigh] if score @s Z < @e[tag=dest,limit=1] Z run scoreboard players operation @s ZDiff = @e[tag=dest,limit=1] Z
execute as @e[tag=currentNeigh] if score @s Z < @e[tag=dest,limit=1] Z run scoreboard players operation @s ZDiff -= @s Z

execute as @e[tag=currentNeigh] run scoreboard players operation @s h = @s XDiff
execute as @e[tag=currentNeigh] run scoreboard players operation @s h += @s YDiff
execute as @e[tag=currentNeigh] run scoreboard players operation @s h += @s ZDiff

#done calculating h
function astar:priorityqueue/tagstartend
function astar:priorityqueue/valueas

function astar:priorityqueue/findqueuecount
execute if score global queueCount matches 1.. run function astar:priorityqueue/enqueue
execute if score global queueCount matches 0 run function astar:priorityqueue/enqueueempty

function astar:priorityqueue/sort

tag @e[tag=pushing] remove pushing
tag @e[tag=currentNeigh] remove currentNeigh
scoreboard players remove global i 1