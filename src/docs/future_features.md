
## Potential future features

These are features that don't exist yet in the code, but they could be done.
Or exist already in other parsers.

### 1. map collectors

add a lookup DB type thing that maps the argument to something in the DB:
- from a Map<String, Object> instance
- from a Map<String, String> instance
- from a ResourceBundle instance
- from a Properties instance 

### 2. escape sequence

If we encounter '--' with nothing else we stop parsing the arguments

### 3. allowPrefixedPositionalArguments

A boolean flag on the Arguments class. 
If set we will not throw an error if we encounter an argument that starts with a '-' or '--'.
Instead, we will treat it as a positional argument.
And throw an error if none are defined.

### 4. Adding a resource bundle for generating errors and help

Builder.withResourceBundle(ResourceBundle.getBundle("UserOpts"))

### 5. Different way to treat Boolean's

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

### 6. Interactive setting

https://picocli.info/#_interactive_password_options

### 7. Listable flag options

https://github.com/remkop/picocli#example

### 8. More help features

https://picocli.info/#_usage_help_api

### 9. arity setting

https://github.com/remkop/picocli#example

### 10. Graal VM annotation processor

https://github.com/remkop/picocli
