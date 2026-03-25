execute if entity @e[name="fly"] run scoreboard players set global ground 0
tp @e[name="fly"] ~ -100 ~
kill @e[name="fly"]
execute if entity @e[name="walk"] run scoreboard players set global ground 1
tp @e[name="walk"] ~ -100 ~
kill @e[name="walk"]
execute if score global ground matches 0 run scoreboard players set CanFly Config 1
execute if score global ground matches 1 run scoreboard players set CanFly Config 0

execute if entity @e[name="2D"] run scoreboard players set global startI 8
tp @e[name="2D"] ~ -100 ~
kill @e[name="2D"]
execute if entity @e[name="3D"] run scoreboard players set global startI 26
tp @e[name="3D"] ~ -100 ~
kill @e[name="3D"]
execute if score global startI matches 8 run scoreboard players set Dimension Config 2
execute if score global startI matches 26 run scoreboard players set Dimension Config 3