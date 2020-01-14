# Droideka

![](https://vignette.wikia.nocookie.net/starwars/images/9/9d/Droideka-SWE.png/revision/latest?cb=20160910210418)

#### Overall Strategy
- Droideka behaves very similarly to C3-PO
- Droideka sends a late game rush at the enemy's turtle. It sometimes can get units onto their wall and attacks them there.
- In order to acheive a late game rush, this strategy sacrifices some Landscaper to wall-segment ratio. This bot is able to
achieve a ratio of 15:16.

#### HQ Strategy
- This bot attempted to build miners (in a limited capacity) until about round 400, at which point it would build one miner
to it's south that would attempt to build the base

#### Mining Strategy
- All early miners functioned as miners
- They would search for resources and deposit them.
- If they weren't close to the HQ or another refinery, the miners would build a refinery in order to deposit their materials
- Basic Pathing was included to allow the bots to more consistently reach their destinations
- Miners also had basic communication implemented so they would move to other locations that had been discovered by other miners.

#### Building Strategy
The Building strats were very similar as well to those of C3-PO, however they were more efficient and worked in a way designed
to improve consistence and efficiency of bot production. The following was the build pattern (in relation to the HQ):
1. Design School to the East
2. Vaporator to the North West
3. Net Gun to the North East
4. Vaporator to the North
5. Fulfillment Center to the South West
6. Net Gun to the West

#### Design School Strategies
- Attempted to build a Landscaper to the East
- If it couldn't it attempted to build it to the south.

#### Fullfillment Center Strats
- Attempted to build a Delivery Drone to it's East

#### Landscaper Strategies (Landscapers on top of the wall)
These would attempt to run around the wall in a circle until a critical number of landscapers was reached. After this,
they would stop running in a specific spot that would leave a permanent opening for the drones to leave through. The landscapers
in this version were also smart enough to try to average the wall size as they built it up.

#### Landscaper Strategies (On the Enemy's Walls)
These bots had a priority list of objectives that they would attempt to achieve
1. If they were adjacent to the enemy's HQ they would bury the enemy's HQ
2. If there were any buildings inside or on the turtle, the landscapers would attempt to bury those
3. If the landscapers couldn't do anything else, they would try to dig a hole in the wall. This would result in the enemy 
base eventually getting flooded.

#### Drone Strategies
Drones had 2 Priorities
1. Put Landscapers on our wall.
2. Carry Landscapers to the enemy
    - When doing this the Drones would first attempt to put their unit on the wall. If they could not, they would drop the
    unit they were carrying and would attempt to grab the enemy's units and drop them in the water.
    - Using this strategy, a swarm of around 20-30 drones could typically put about 5 units on their wall
    
#### Ongoing Problems
- Many small bugs
- At this point, we are getting consistently smashed by the rush
- Strategy feels a little inefficient and un-effective at the level we are playing against with the current meta.

