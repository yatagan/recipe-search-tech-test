## Recipe search

We have to give users the ability to search for recipes. We have some text files containing recipe descriptions written in English. We would like to be able to search over the set of these text files to find recipes given at a minimum a single word e.g. `tomato`.

Example text files: [recipes.zip](https://media.riverford.co.uk/downloads/hiring/sse/recipes.zip)

We would like a program that can provide a search function over these files, each search returning a small number (e.g around 1-10) relevant recipes if possible.

The text files are of differing sizes, and are encoded as utf-8. New text files are coming in all the time, so we should not assume a static set of recipes.

The name of each file is considered the id of the recipe.

Our requirements have been listed by a key business stakeholder:

## Essential requirements:

-	Search results should be relevant, e.g. a search for broccoli stilton soup should return at least broccoli stilton soup.
-	Searches should complete quickly so users are not kept waiting – this tool needs to serve many users so lower latency will mean we can serve more concurrent searches - ideally searches will take < 10ms.

Ideally the results will be sorted so that the most relevant result is first in the result list.

## Technical Notes

The program can be a command line app, a set of functions for REPL usage or a web app. It can be written in any language you want.

The goal of this test is to appraise your programming ability, that is to say – how simple, readable, efficient and correct your solution is. In addition good comments, good function/interface design and good naming will be looked for.

We have left the level of sophistication up to you, we find it is easier to make a naïve solution obviously simple, but as things get more sophisticated, keeping it simple is more challenging. We are interested in how you can do ‘hard’ things in a simple way.

-	Returning relevant results is fairly open ended and subjective, so there is a lot of room for increasing sophistication. 
-	Performance / Resource utilisation is another area where you can apply more sophistication to the solution.
-	Degree to which the solution is generalized without loss of clarity is another.

Because we are trying to appraise your programming ability and initiative, be reasonable in your use of libraries or existing search tools – In this test we really *do* want you to reinvent the wheel as far as the core problem is concerned! So just using `grep` will not win you many points. Feel free to use established techniques and algorithms, in fact this is encouraged.

You can spend as long or as little as you would like on the test, it does not have to be perfect! I think anywhere from 2-6 hours would be appropriate.

Please host the program source on github.com (e.g by forking this repo), gitlab.com or provide a link to a zip file on google drive. Let me know you’ve done it by sending a link to danielstone@riverford.co.uk

Good luck!
