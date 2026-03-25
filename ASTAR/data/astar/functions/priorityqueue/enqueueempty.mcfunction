summon minecraft:armor_stand 0 255 0 {Tags:["queue","start","end","pushing"], NoGravity:1b,Invisible:1b}

execute as @e[tag=currentNeigh] run scoreboard players operation @e[tag=pushing] value = @s g
execute as @e[tag=currentNeigh] run scoreboard players operation @e[tag=pushing] disco = @s disco
execute as @e[tag=currentNeigh] run scoreboard players operation @e[tag=pushing] value += @s h