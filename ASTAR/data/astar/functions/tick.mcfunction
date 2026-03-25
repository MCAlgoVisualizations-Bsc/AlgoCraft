execute if score global showInfoTick matches ..3 run scoreboard players add global showInfoTick 1
execute if score global showInfoTick matches 3 run function astar:info

execute if entity @e[type=bat, name="reset"] run function astar:reset
function astar:setconfig

function astar:priorityqueue/tagstartend
function astar:priorityqueue/valueas

# execute unless entity @e[name="removingqueue"] run execute if score global value matches 0.. run function astar:priorityqueue/notused/startpushing
# execute if entity @e[name="removingqueue"] run execute store result score global compare1 run data get storage minecraft:astar queue[0]
# execute if entity @e[name="removingqueue"] unless entity @e[name="puttingbackintoqueue"] run execute if score global compare1 < global value run function astar:priorityqueue/notused/smallerkeepgoing
# execute if entity @e[name="removingqueue"] unless entity @e[name="puttingbackintoqueue"] run execute unless data storage astar queue[0] run function astar:priorityqueue/notused/insertcorrectpos
# execute if entity @e[name="removingqueue"] unless entity @e[name="puttingbackintoqueue"] run execute if score global compare1 >= global value run function astar:priorityqueue/notused/insertcorrectpos
# execute if entity @e[name="puttingbackintoqueue"] run function astar:priorityqueue/notused/putbacktempintoqueue
# execute if entity @e[name="removingqueue"] if entity @e[name="puttingbackintoqueue"] run execute unless data storage minecraft:astar Temp[0] run function astar:priorityqueue/notused/tempemptyendpushing

execute if entity @e[name="src"] run function astar:algo/summonsrc
execute if entity @e[name="dest"] run function astar:algo/summondest

execute if entity @e[tag=src] if entity @e[tag=dest] run function astar:algo/startalgo
execute if score global algoRunning matches 1 if score global isExpanding matches 0 run function astar:algo/expand/dequeuetoexpand
execute if score global expandStarted matches 0 if score global isExpanding matches 1 at @e[tag=expanding] unless entity @e[tag=dest, distance=0] run function astar:algo/expand/startexpand

execute if score global expandStarted matches 1 if score global i matches 26 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~ ~1 ~ {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 25 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~1 ~1 ~ {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 24 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~-1 ~1 ~ {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 23 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~ ~1 ~1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 22 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~ ~1 ~-1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 21 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~1 ~1 ~1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 20 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~-1 ~1 ~1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 19 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~1 ~1 ~-1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 18 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~-1 ~1 ~-1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 17 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~ ~-1 ~ {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 16 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~1 ~-1 ~ {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 15 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~-1 ~-1 ~ {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 14 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~ ~-1 ~1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 13 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~ ~-1 ~-1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 12 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~1 ~-1 ~1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 11 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~-1 ~-1 ~1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 10 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~1 ~-1 ~-1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 9 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~-1 ~-1 ~-1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 8 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~1 ~ ~ {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 7 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~-1 ~ ~ {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 6 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~ ~ ~1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 5 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~ ~ ~-1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 4 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~1 ~ ~1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 3 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~-1 ~ ~1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 2 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~1 ~ ~-1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 1 if score global isExpanding matches 1 at @e[tag=expanding] run summon item_frame ~-1 ~ ~-1 {Tags:["checking"], NoGravity:1b, Invisible:1b}
function astar:algo/expand/directions/checksummondirection
execute if score global expandStarted matches 1 if score global i matches 0 if score global isExpanding matches 1 run scoreboard players set global i -1

execute if entity @e[tag=currentNeigh] run function astar:algo/expand/directions/initonedir

execute if score global expandStarted matches 1 if score global i matches -1 if score global isExpanding matches 1 run function astar:algo/expand/doneexpand

execute at @e[tag=expanding] if entity @e[tag=dest, distance=0] run function astar:algo/traceback/starttraceback
execute if entity @e[tag=backtrack] run function astar:algo/traceback/traceback
execute if entity @e[tag=backtrack, scores={parent=-1}] run function astar:algo/traceback/donetraceback