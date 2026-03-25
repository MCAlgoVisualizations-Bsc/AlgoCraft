execute at @e[type=bat,name="src"] run summon minecraft:armor_stand ~ ~ ~ {Tags:["src"], NoGravity:1b}
execute at @e[type=bat,name="src"] run summon minecraft:item_frame ~ ~ ~ {Tags:["centerBlock"], NoGravity:1b, Invisible:1b}
execute at @e[tag=centerBlock] positioned ~ ~-0.96875 ~ unless entity @e[distance=0, tag=src] run function astar:algo/centersrc
kill @e[tag=centerBlock]
tp @e[name="src"] ~ -100 ~
kill @e[name="src", type=bat]