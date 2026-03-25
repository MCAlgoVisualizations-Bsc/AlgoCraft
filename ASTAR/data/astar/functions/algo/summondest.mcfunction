execute at @e[type=bat,name="dest"] run summon minecraft:armor_stand ~ ~ ~ {Tags:["dest"], NoGravity:1b}
execute at @e[type=bat,name="dest"] run summon minecraft:item_frame ~ ~ ~ {Tags:["centerBlock"], NoGravity:1b, Invisible:1b}
execute at @e[tag=centerBlock] positioned ~ ~-0.96875 ~ unless entity @e[distance=0, tag=dest] run function astar:algo/centerdest
kill @e[tag=centerBlock]
tp @e[name="dest"] ~ -100 ~
kill @e[name="dest", type=bat]