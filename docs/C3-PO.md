# C3-PO

![](https://cdn3.movieweb.com/i/article/9eIsTJMJ8mQ6rOqYYgfuv1G12wkn8v/1200:100/Star-Wars-9-Anthony-Daniels-C3po-Wrapped.jpg)

#### Overall Strategies
- This was a major bot rewrite. This bot gives a much better attempt at building a wall
- This bot was able to achieve a Landscaper to Wall-Segment Ratio of 1:1
- This bot was also the first bot to attempt to build an economy

#### HQ Strategies
- This bot attempted to build miners (in a limited capacity) until about round 400, at which point it would build one miner
to it's south that would attempt to build the base

#### Miners Strategies
- Miners fall into two categories here. There were Mining Miners, and Building Miners
- Mining Miners would attempt to find resources and collect them. This was fairly inefficient, however it worked.
- The Building miner would attempt to build the interior of a base. The base had the following static design (locations 
notated in relation to the HQ)
    - Design School to the east of the HQ
    - Fulfillment Center to the South-West of the HQ
    - Vaporator to the North-West of the HQ
    - Net Gun to the North of the HQ

#### Design School Strategies
- Attempted to build a Landscaper to the East
- If it couldn't it attempted to build it to the south.

#### Landscapers Strategies
- Landscapers would attempt to run in circles around the wall. This wasn't the best strategy, however they did a fairly good
job of balancing the wall over time, and it could typically get this wall up.
- If the landscaper had not been spawned on the wall, it would wait until it had been picked up by a delivery drone, which would
place it on the wall

#### Fulfillment Center Strategies
- Attempted to build a Delivery Drone to it's East

#### Delivery Drone Strategies
- The Delivery Drone would wait until it could pick up a Landscaper from it's right. It would pick up this landscaper and
then place it on the wall.
- It did this by moving in and out of the wall in a method that was fairly inefficient.

#### Ongoing Problems
- By this time, the rush meta had started to become evident in a lot of the stronger teams. This bot did not hold up well
to those strategies
- The bot didn't wall earlier enough to beat some teams
- This bot had numerous bugs