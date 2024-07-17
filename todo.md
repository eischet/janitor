# Things to do

The API contains lots of stuff that would be better off in the implementation only.
The plan is to move legacy functionality of JString, JDate, JRegex etc. into dispatch tables which are then
looked up from the implementation. This allows an embedded to exert far more control over how these classes
behave and at the same time simplifies the API significantly.
Some shuffling around of functionality is required to make this happen.

The "Toolbox" contains some leftover code that is not actually used here, but by the original mother app. Remove.
