# C3-PO's wall building strats!
---

![](https://cdn3.movieweb.com/i/article/9eIsTJMJ8mQ6rOqYYgfuv1G12wkn8v/1200:100/Star-Wars-9-Anthony-Daniels-C3po-Wrapped.jpg)
#### HQ Strat
The HQ is going to occasionally broadcast it's location so the landscapers know where they need to be bringing dirt. This 
will happen in response to a request from the landscaper for information. This broadcast neads to include at least 2 pieces of information:
 - The Location of the HQ
 - The current level of the wall<br>
 
Additionally, they will produce miners somewhat frequently

#### Miners
Miners primary goal is to occasionally build a design school.
Miners will enter a secondary state of trying to mine when they aren't looking to build a school

#### Design School
Builds Landscapers occasionally

#### Landscapers
Landscapers are going to attempt to raise the level of dirt around the HQ. Over time, they will be going to collect dirt, 
and then will be returning whenever they have a sizable inventory to increase the height of the wall. The wall will be built
in a slope going both in and out (for now). This means as the wall gets taller, a larger and larger "safe" area will be 
built inside the ring. This will continue outwards over time.<br>
<br>
As a landscaper comes back, they will generate a list of all the locations they want to place dirt. They will check these
spots to see if they need to place any pieces of dirt there.<br>
<br>
Messages:<br>
 - A bot may need to know where the HQ is, they will send a message to request this location
 - A bot may find that the wall has gotten higher, and that all spots are already at the desired height. If this is the 
 case, they will send a message that indicates that is needs to be moved up to the next level of wall height.
 - It may prove helpful to maintain the list of plant locations via messaging. If this is the case, plant location mangement
 will eventually get moved to the HQ (as this is heavier work we want to offload onto a unit with more bytecode).