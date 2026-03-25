execute at @e[tag=end] run summon minecraft:armor_stand ~ ~-1 ~ {Tags:["pushing", "queue"], NoGravity:1b, Invisible:1b}

execute as @e[tag=currentNeigh] run scoreboard players operation @e[tag=pushing] value = @s g
execute as @e[tag=currentNeigh] run scoreboard players operation @e[tag=pushing] disco = @s disco
execute as @e[tag=currentNeigh] run scoreboard players operation @e[tag=pushing] value += @s h