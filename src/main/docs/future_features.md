
## Potential future features

These are features that don't exist yet in the code, but they could be done.
Or exist already in other parsers.

### 1. Support the Optional class

Optional isn't supported as a type
If the value doesn't exist set to Optional.empty()
Else convert to its type and set that.

### 2. map collectors

add a lookup DB type thing that maps the argument to something in the DB:
- from a Map<String, Object> instance
- from a Map<String, String> instance
- from a ResourceBundle instance
- from a Properties instance 

### 3. escape sequence

If we encounter '--' with nothing else we stop parsing the arguments

### 4. allowPrefixedPositionalArguments

a boolean flag on the Arguments class. 
If set we will not throw an error if we encounter an argument that starts with a '-' or '--'.
Instead, we will treat it as a positional argument.
And throw an error is none are defined.

### 5. Setting the help tab size

Builder.withIndent(2)

### 6. Adding a resource bundle for generating errors and help

Builder.withResourceBundle(ResourceBundle.getBundle("UserOpts"))

### 7. Different way to treat Boolean's

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