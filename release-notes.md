# 0.9.2, work in progress

An older "class" implementation is removed and a newer "dispatch table" approach is used instead. This has the
benefit of allowing greater customisation of built-in types than before. It also allows us to move code from
the API into the implementation that is not really supposed to be part of the API. This enables things
like extension methods to be easily added by users, but comes with a certain cost when creating instances of
built in types, because those calls are more complicated now.


# Initial Release 0.9.1, 2024-07-17

This is the first internal pre-release, after the language grew within a bigger application for a couple of years.
