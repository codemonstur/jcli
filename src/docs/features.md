
Steal some features from here:
https://github.com/h908714124/jbock#features-overview
https://github.com/h908714124/jbock/blob/master/READ_MORE.md

PositionalArguments
Support the Optional class

add a map collector

add a lookup DB type thing that maps the argument to something in the DB:
- from a Map<String, Object> instance
- from a Map<String, String> instance
- from a ResourceBundle instance
- from a Properties instance 

escape sequence to disable CliOption's

allowPrefixedPositionalArguments

Builder
    .withIndent(2)                                                  // default is 7
    .withResourceBundle(ResourceBundle.getBundle("UserOpts"))       // default is none

Consider:
Currently the Boolean class works like the boolean primitive:
- It is a flag
- If the argument is there the value is set to true
- If the argument isn't there the value is set to false
The Boolean class could be considered an optional field. The behavior would be like this:
- The argument always requires a value
- If the value is false set to Boolean.FALSE
- If the value is true set to Boolean.TRUE
- If the argument isn't provided set to null
- All other values (or no value) throws exception