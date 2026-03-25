tag @e[tag=backtrack] add path
execute at @e[tag=backtrack, scores={parent=0..}] run data merge entity @e[tag=backtrack,limit=1] {ArmorItems:[{},{},{},{id:"orange_wool",Count:1b}]}
execute at @e[tag=visited] if score @e[tag=visited,distance=0,limit=1] disco = @e[tag=backtrack, limit=1] parent run tag @e[tag=visited, distance=0] add newBacktrack
tag @e[tag=backtrack] remove backtrack
tag @e[tag=newBacktrack] add backtrack
tag @e[tag=newBacktrack] remove newBacktrack
