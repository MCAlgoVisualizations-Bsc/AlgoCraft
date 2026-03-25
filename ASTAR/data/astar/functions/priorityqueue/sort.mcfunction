# move bigger values back
execute at @e[tag=valueAS] positioned ~1 ~ ~ as @e[tag=queue, tag=!pushing, distance=0] if score @s value > @e[tag=pushing, limit=1] value run tp @s ~ ~-1 ~

# put pushing into the right position
execute at @e[tag=queue] positioned ~ ~-1 ~ unless entity @e[tag=queue,distance=0] positioned ~ ~-1 ~ if entity @e[tag=queue,distance=0] positioned ~ ~1 ~ run tp @e[tag=pushing] ~ ~ ~
execute positioned 0 255 0 unless entity @e[tag=queue,distance=0] run tp @e[tag=pushing] ~ ~ ~