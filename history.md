
# Technical History

For a software project I work on, I needed a simple scripting language that allows users to customize and enhance
the core product. Important: These users are not required to be professional software developers.
User produce two kinds of code in this context: expressions and scripts. Expressions are simplistic statements
like "value.x > 15" that can filter a number of records from a set of records, or "'This is ' + value.name" that
pluck values from records. Scripts are more like classic programs, with loops, conditionals, and function definitions,
and are used to e.g. drive data imports and exports.

Stage 1: First, we tried Groovy for a couple of months. Groovy is easily embedded, and expressions worked just great, 
but scripts turned out to be very brittle  because they tended to import Java classes from the core application all 
over the place. 
Whenever a change was made  to the core app, scripts tended to break or start misbehaving in various unpredictable ways. 
A way around this would, of course, have been to define very strict interfaces between the app side and the scripting 
side and keep them stable. One might argue that such an approach would inhibit rapid changes to the core app, which is not what we want.
Also, users were unfamiliar with the Java-based-and-even-more-complex Groovy syntax, so in the end I wrote most 
non-trivial scripts myself anyway.

Stage 2: Rhino, the JavaScript engine for Java. I felt that Javascript was a much better fit for the users, and experiments
proved that to be true. However, embedding Rhino in a satisfying way turned out to be a challenge. I feel that Rhino
is nice for writing JavaScript applications that use Java libraries, but not for extending a Java app with e.g. JavaScript
expressions and callbacks. It was especially challenging to implement simple callbacks, like where a user writes
"2 * x + 1" and things "just work". I found myself diving deep into the Rhino code to understand how they implement
objects. etc., and finally decided to give up because I figured that understanding the Rhino code deeply enough to
make it work like I want it to would involve more lines of code than starting from scratch with a small and simple
language.

I feel that both Groovy and Rhino/JavaScript are great tools, but they try to be stand-alone languages that happen
to run on the JVM rather than being embedded scripting engines for Java applications.

Therefore, Stage 3: take JavaScript-like syntax, strip it down to the bare essentials, and embed it in such a way that
I don't have to fight against the scripting engine all the time. 
Even though it looks a lot like JavaScript, Janitor started out with the official ANTLR4 grammar for Java by Parr et al., 
found at [the ANTLR repository](https://github.com/antlr/grammars-v4/tree/master/java). From this grammar, all 
"unnecessary" parts were removed. Base on user feedback, constructs like "&&" and "||" look quite alien to a lot of 
people, so that's why I took "and" and "or" from Python and SQL. (The C-like syntax is actually supported, too, at least
for now.)

