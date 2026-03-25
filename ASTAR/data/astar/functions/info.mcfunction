tellraw @a {"text":"A* path finding algorithm visualization by Ha Chi Hao","bold":true,"color":"blue"}
tellraw @a ""

tellraw @a {"text":"/function:astar/givetool","underlined": true, "color":"yellow", "clickEvent":{"action":"run_command","value":"/function astar:givetool"}}
tellraw @a "Get neccessary spawn eggs"
tellraw @a ""

tellraw @a {"text":"/function:astar/showconfig","underlined": true, "color":"yellow", "clickEvent":{"action":"run_command","value":"/function astar:showconfig"}}
tellraw @a "Show current chosen Dimension and if the path finding can fly"
tellraw @a ""

tellraw @a {"text":"/function:astar/hideconfig","color":"yellow","underlined": true, "clickEvent":{"action":"run_command","value":"/function astar:hideconfig"}}
tellraw @a "Hide Dimension and CanFly"
tellraw @a ""

tellraw @a {"text":"/function:astar/info","color":"yellow","underlined": true, "clickEvent":{"action":"run_command","value":"/function astar:info"}}
tellraw @a "Show this text again"
tellraw @a ""

tellraw @a {"text":"/function:astar/credit","color":"yellow","underlined": true, "clickEvent":{"action":"run_command","value":"/function astar:credit"}}
tellraw @a "Few words from author"
tellraw @a ""

tellraw @a {"text":"Check out how A* algorithm works! I learned it by watching this video","bold":true,"underlined":true,"color":"green","clickEvent":{"action":"open_url","value":"https://youtu.be/ySN5Wnu88nE"}}