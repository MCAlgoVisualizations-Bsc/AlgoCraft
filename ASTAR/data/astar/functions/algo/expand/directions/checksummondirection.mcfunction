#not safe -> next direction (i--)
execute at @e[tag=checking] unless block ~ ~-0.96875 ~ air unless block ~ ~-0.96875 ~ cave_air run scoreboard players remove global i 1
execute at @e[tag=checking] positioned ~ ~-0.96875 ~ if entity @e[type=armor_stand,tag=visited,distance=0, tag=!currentNeigh] run scoreboard players remove global i 1
execute if score global ground matches 1 at @e[tag=checking] if block ~ ~-0.96875 ~ air if block ~ ~-1.96875 ~ air run scoreboard players remove global i 1
execute if score global ground matches 1 at @e[tag=checking] if block ~ ~-0.96875 ~ air if block ~ ~-1.96875 ~ cave_air run scoreboard players remove global i 1
execute if score global ground matches 1 at @e[tag=checking] if block ~ ~-0.96875 ~ cave_air if block ~ ~-1.96875 ~ air run scoreboard players remove global i 1
execute if score global ground matches 1 at @e[tag=checking] if block ~ ~-0.96875 ~ cave_air if block ~ ~-1.96875 ~ cave_air run scoreboard players remove global i 1

#safe -> summon currentNeigh
execute if score global ground matches 0 at @e[tag=checking] if block ~ ~-0.96875 ~ air positioned ~ ~-0.96875 ~ unless entity @e[type=armor_stand,tag=visited,distance=0] run summon armor_stand ~ ~ ~ {Tags:["visited","currentNeigh"], Small:1b,ArmorItems:[{},{},{},{id:"light_blue_wool",Count:1b}], Invisible:1b, NoGravity:1b, Pose:{Head:[180f,0f,0f]}}
execute if score global ground matches 0 at @e[tag=checking] if block ~ ~-0.96875 ~ cave_air positioned ~ ~-0.96875 ~ unless entity @e[type=armor_stand,tag=visited,distance=0] run summon armor_stand ~ ~ ~ {Tags:["visited","currentNeigh"], Small:1b,ArmorItems:[{},{},{},{id:"light_blue_wool",Count:1b}], Invisible:1b, NoGravity:1b, Pose:{Head:[180f,0f,0f]}}
execute if score global ground matches 1 at @e[tag=checking] if block ~ ~-0.96875 ~ air positioned ~ ~-0.96875 ~ unless entity @e[type=armor_stand,tag=visited,distance=0] unless block ~ ~-1 ~ air unless block ~ ~-1 ~ cave_air run summon armor_stand ~ ~ ~ {Tags:["visited","currentNeigh"], Small:1b,ArmorItems:[{},{},{},{id:"light_blue_wool",Count:1b}], Invisible:1b, NoGravity:1b, Pose:{Head:[180f,0f,0f]}}
execute if score global ground matches 1 at @e[tag=checking] if block ~ ~-0.96875 ~ cave_air positioned ~ ~-0.96875 ~ unless entity @e[type=armor_stand,tag=visited,distance=0] unless block ~ ~-1 ~ air unless block ~ ~-1 ~ cave_air run summon armor_stand ~ ~ ~ {Tags:["visited","currentNeigh"], Small:1b,ArmorItems:[{},{},{},{id:"light_blue_wool",Count:1b}], Invisible:1b, NoGravity:1b, Pose:{Head:[180f,0f,0f]}}

kill @e[tag=checking]