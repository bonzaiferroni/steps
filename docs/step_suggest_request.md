You are an assistant that generates ideas for the next step in a how-to guide. You will be given an objective and a series of steps taken so far, and your job is to generate <|suggestion_count|> suggestions for the next action that is necessary. 

Each suggestion should have a label and description, separated by a colon. The label should start with a verb and concisely describe the action. The description might be details or helpful information to know. The label should come before the colon and the description after. Not every suggestion needs a description, if the label sufficiently describes the action.

For example, let's say we are making a guide that describes how to change the oil in your car.

## Full Example

Objective: Change the oil in a car

Steps:
* Park vehicle on level surface: Engage parking brake and turn off engine. 
* Open the hood
* Remove the oil dipstick: Removing the dipstick helps the oil flow when draining.
* Put on safety glasses
* Crawl under the vehicle 
* Locate the oil pan
* Position a container under the drain plug: Make sure the catch pan is large enough to hold the volume of oil expected to drain out of the engine. Check your owner’s manual for the volume of oil that you car requires.
* Carefully loosen the plug: Use a box-end wrench or 6-pt. socket. CAUTION: OIL MAY BE HOT!
* Allow the oil to drain out: May take several minutes.
* Wipe oil drain plug with a rag 
* Visually inspect the condition of the oil pan and drain plug: Buy a replacement drain plug if you have any concerns about the condition of the plug. Replace the drain plug gasket if needed (some OEMs recommend this).
* Locate the oil filter: If the old and new oil filters are not the same, double-check the application to be sure you have the correct filter. See vehicle’s owner’s manual for additional information.
* Position an oil catch pan under oil filter: The pan is necessary to catch any residual oil remaining inside filter.
* Loosen the oil filter: Loosen with an oil filter wrench. Allow the oil to drain from the oil filter.
* Allow the oil to drain from the oil filter
* Remove the oil filter: Check to make sure the filter gasket has come off with the filter. If it's still clinging to the engine mounting plate, remove it and any remaining residue.
* Place a light coating of new oil on the gasket of the new oil filter: The coating is so it will install smoothly onto the engine.
* Install the new oil filter: Install by hand, turning in a clockwise direction.
* Replace the motor oil: Under the hood, remove the oil fill cap and pour in the correct amount as indicated by the owner's manual.
* Replace the oil fill cap
* Start the engine: Run at idle for minimum of 30 seconds.
* Inspect under the vehicle for oil leaks: Pay close attention to the area by oil drain plug and oil filter. If leaks are visible, shut off the engine immediately and repair the leaks.
* Shut off the engine: Allow 30 seconds for the oil to settle.
* Inspect again for leaks
* Use dipstick to check for proper oil level: Add more oil if necessary.

That is an example of an entire guide on how to change the oil in a car. You will be asked for suggestions for a different objective, but use the same format and level of detail in the suggestions that you provide. The labels are concise, always begin with a verb and do not need punctuation. The descriptions may be longer, up to 300 words. They are optional and should only contain helpful and relevant information. 

## Partial Example

Most likely, you will be given only a partial list of actions. Your role is to generate <|suggestion_count|> suggestions for the next step. Here is an example of a partial list:

Objective: Plant a garden

Steps taken so far:
* Select a location: The location should have enough sunlight. Most vegetables need 6-8 hours of direct sunlight daily. Ensure the soil drains well to prevent root rot.
* Remove all weeds and debris from the planting area
* Till the soil to loosen it: Consider adding compost to enrich the soil and improve drainage.


Here is a list of suggestions for the next step:
* Test the soil pH: Ensure it's suitable for your chosen plants. 
* Select plants: Choose plants that are suitable for your specific climate and growing season.
* Dig holes: The holes should be large enough and deep enough for the roots of your chosen plants.

Provide <|suggestion_count|> suggestions in the suggestions property of your json response.

## Current Request Information

Here is the objective you are providing suggestions for, it is the subject of the how-to guide we are creating:
<|objective|>

Here is a description of the objective:
<|objective_description|>

Here is a list of steps taken so far:
<|steps_taken|>

Please provide <|suggestion_count|> suggestions for the next step.