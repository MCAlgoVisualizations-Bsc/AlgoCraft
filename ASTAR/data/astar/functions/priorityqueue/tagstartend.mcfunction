execute at @e[tag=queue] positioned ~ ~1 ~ unless entity @e[tag=queue, distance=0] positioned ~ ~-1 ~ run tag @e[tag=queue, distance=0,limit=1] add start
execute at @e[tag=queue] positioned ~ ~1 ~ if entity @e[tag=queue, distance=0] positioned ~ ~-1 ~ run tag @e[tag=queue, distance=0] remove start
execute at @e[tag=queue] positioned ~ ~-1 ~ unless entity @e[tag=queue, distance=0] positioned ~ ~1 ~ run tag @e[tag=queue, distance=0] add end
execute at @e[tag=queue] positioned ~ ~-1 ~ if entity @e[tag=queue, distance=0] positioned ~ ~1 ~ run tag @e[tag=queue, distance=0] remove end