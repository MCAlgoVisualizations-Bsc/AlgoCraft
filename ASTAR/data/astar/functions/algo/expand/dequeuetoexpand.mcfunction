function astar:priorityqueue/tagstartend
function astar:priorityqueue/valueas
execute at @e[tag=visited] if score @e[distance=0, tag=visited, limit=1] disco = @e[tag=start, limit=1] disco run tag @e[distance=0, tag=visited, limit=1] add expanding
data merge entity @e[tag=expanding,limit=1] {ArmorItems:[{},{},{},{id:"green_wool",Count:1b}]}

function astar:priorityqueue/findqueuecount
execute if score global queueCount matches 0 run function astar:priorityqueue/dequeueempty
execute if score global queueCount matches 1.. run function astar:priorityqueue/dequeue

scoreboard players set global isExpanding 1