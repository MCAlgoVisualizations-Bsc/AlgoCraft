#execute at @e[tag=valueAS] positioned ~1 ~ ~ run execute if entity @e[tag=queue, distance=0] run setblock ~-1 ~ ~ minecraft:oak_sign[rotation=4] replace
#execute at @e[tag=valueAS] positioned ~1 ~ ~ run execute if entity @e[tag=queue, distance=0] run data merge block ~-1 ~ ~ {Text2:"{\"score\":{\"name\":\"@e[tag=queue,sort=nearest,limit=1]\",\"objective\":\"value\"}}",id:"Sign"}
#execute at @e[tag=valueAS] positioned ~1 ~ ~ run execute unless entity @e[tag=queue, distance=0,limit=1] run setblock ~-1 ~ ~ air
execute at @e[tag=valueAS] positioned ~1 ~ ~ run execute unless entity @e[tag=queue,distance=0] run execute positioned ~-1 ~ ~ run kill @e[tag=valueAS, distance=0]
execute at @e[tag=queue] positioned ~-1 ~ ~ unless entity @e[tag=valueAS, distance=0] run summon minecraft:armor_stand ~ ~ ~ {Tags:["valueAS"], Invisible:1b}

#execute at @e[tag=valueAS] positioned ~1 ~ ~ run execute if entity @e[tag=start, distance=0] run data merge block ~-1 ~ ~ {Text3:"{\"text\":\"start\"}"}
#execute at @e[tag=valueAS] positioned ~1 ~ ~ run execute if entity @e[tag=end, distance=0] run data merge block ~-1 ~ ~ {Text4:"{\"text\":\"end\"}"}
#execute at @e[tag=valueAS] positioned ~1 ~ ~ run execute if entity @e[tag=!start, distance=0] run data merge block ~-1 ~ ~ {Text3:"{\"text\":\"\"}"}
#execute at @e[tag=valueAS] positioned ~1 ~ ~ run execute if entity @e[tag=!end, distance=0] run data merge block ~-1 ~ ~ {Text4:"{\"text\":\"\"}"}