scoreboard objectives add parent dummy
scoreboard objectives add disco dummy
scoreboard objectives add X dummy
scoreboard objectives add Y dummy
scoreboard objectives add Z dummy
scoreboard objectives add XDiff dummy
scoreboard objectives add YDiff dummy
scoreboard objectives add ZDiff dummy
scoreboard objectives add h dummy
scoreboard objectives add g dummy
scoreboard objectives add value dummy
scoreboard objectives add ground dummy
scoreboard objectives add isExpanding dummy
scoreboard objectives add i dummy
scoreboard objectives add startI dummy
scoreboard objectives add Config dummy
scoreboard objectives add queueCount dummy
scoreboard objectives add expandStarted dummy
scoreboard objectives add algoRunning dummy
scoreboard objectives add showInfoTick dummy

scoreboard players set global ground 0
scoreboard players set global isExpanding 0
scoreboard players set global expandStarted 0
scoreboard players set global i -1
scoreboard players set global startI 26
scoreboard players set global algoRunning 0
scoreboard players set global showInfoTick 0
execute unless entity @e[name="checkFirstTime"] run summon armor_stand ~ -20 ~ {Invisible:1b, NoGravity:1b, CustomName:'"checkFirstTime"'}
forceload add 0 0
function astar:showconfig