# Contributing to PartySystem
Thank you for considering contributing to PartySystem! Before you start, please take a moment to review these guidelines. They will help you understand how to contribute to this project.

## Table of Contents
* [Code Style](#code-style)
* [Annotations](#annotations)
* [Naming Conventions](#naming-conventions)
* [Variable and Method Arguments](#variable-and-method-arguments)
* [IDE Files](#ide-files)

## Code Style
Please follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) for all code contributions. This will help maintain consistency across the project.

## Annotations
Please always use `org.jetbrains.annotations.NotNull` and `java.util.Optional<T>`. If Optional is not an option, please use `org.jetbrains.annotations.Nullable`.

### Benefits of Using @NotNull, @Nullable, and Optional Annotations
Using these annotations helps to ensure null safety and prevent NullPointerExceptions. `@NotNull` indicates that a parameter or return value cannot be null, which can help to catch potential issues during compile time. Similarly, `@Nullable` indicates that a parameter or return value can be null, which helps to prevent accidental null dereferencing. `Optional<T>` can be used to indicate that a value may or may not be present, which can make code more expressive and less error-prone. By consistently using these annotations, code becomes more robust, easier to read and maintain.

## Naming Conventions
Please follow the following naming conventions:
* Getter and Setter methods should be named without the "get" and "set" prefixes.
* Method and variable names should be in camel case.
* Constants should be in uppercase and separated by underscores.
* Class names should be in UpperCamelCase.

## Variable and Method Arguments
Please always use the `final` keyword for method and variable arguments whenever possible.

### Why use final for method and variable arguments?
Using the `final` keyword for method and variable arguments has several benefits. First, it communicates to other developers that the value of the argument should not be changed during the method execution, which can help to prevent bugs and make the code easier to reason about. Second, it can improve code readability by making it clear which variables are inputs to a method and which are not. Finally, the use of `final` can also help the compiler optimize the code, as it knows that the value of the variable will not change and can perform certain optimizations based on that knowledge.

## IDE Files
Please ensure that your IDE files are excluded from Git and are set up globally on your system instead. This will prevent any potential conflicts or errors when working with others and ensure that your IDE settings do not interfere with the project.

Thank you for your contribution to PartySystem!
