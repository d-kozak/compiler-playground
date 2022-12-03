# Compiler Playground

The goal of this project is to play around with various compilation techniques.
Real languages tend to be very complex and their compilers then have to
deal with a lot of complexity. Here, I am trying to keep the language simple.
Various features can be gradually added, but only if:

* they are necessary for the experiment I am working on at the moment or
* I want to implement them as an exercise :)

## Language

The goal here is NOT a language design, at least not at the moment. There is nothing really special about the language
right now. It is a fairly minimal subset of C with:

* only integer types
* basic arithmetic operations
* if and for statements
* functions
* no semicolons :)

## Current status

I've just implemented enough lexing and parsing to parse the [fibonacci example](./programs/source/fib.prog) into AST.
Now I am considering what to do next.

## Next steps
- [x] IR - Transform the AST into more low level IR on which basic optimization passes can be implemented.
- [ ] IR interpreter - Write an interpreter for the IR - mainly for performance comparisons with other forms of
 interpretation/compilation. And also for fun :) I could of course do an AST interepreter, but I've already implemented some in the past, so I would rather try something new.
- [ ] Bytecode interpreter - Once there is a low level IR which is executable, the next logical step seems to be a bytecode interpreter. 
I'm interested in the performance differences compared to the IR interpreter.

## Other Ideas and Side Tasks
- [ ] AST interpreter - I've already implemented some, so it is not a priority at the moment. I'd rather focused on later
 stages of compilation.