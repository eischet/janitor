# JSONTestSuite

`test_parsing/` and `LICENSE` are copied unchanged from [nst/JSONTestSuite](https://github.com/nst/JSONTestSuite),
commit `1ef36fa01286573e846ac449e8683f8833c5b26a` (2024-11-22). Files starting with `y_` must be accepted, `n_` must be
rejected, `i_` are up to the parser. `JsonTestSuiteTestCase` runs them through every JSON stream implementation.

`.gitattributes` keeps Git from changing line endings, as some files test exact bytes.
