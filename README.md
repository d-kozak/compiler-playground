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

I've implemented enough infrastructure to parse the [fibonacci example](./programs/source/fib.prog) into AST,
transform it to lower level IR and interpret it. Now I am considering what to do next.

## Next steps

- [x] IR - Transform the AST into more low level IR on which basic optimization passes can be implemented.
- [x] IR interpreter - Write an interpreter for the IR - mainly for performance comparisons with other forms of
  interpretation/compilation. And also for fun :) I could of course do an AST interepreter, but I've already implemented
  some in the past, so I would rather try something new.
- [x] Control flow analysis - Extract basic blocks and create control-flow-graphs. Will be useful both for optimizations
  and visualizations.
- [x] Graph visualizations for the CFG
- [ ] Bytecode interpreter - Once there is a low level IR which is executable, the next logical step seems to be a
  bytecode interpreter.
  I'm interested in the performance differences compared to the IR interpreter.
- [ ] Semantic analysis - At least differentiate void and int functions.

## Other Ideas and Side Tasks

- [ ] Support for constants directly in the IR. The extra variables like $i = 1 just for $j = $k + $i are not necessary
  make the code longer
- [ ] Get rid of unnecessary NOT ops before conditionals. Conditionals can be inverted instead.
- [ ] Restructure main and provide proper option handling for various modes of the compiler.
- [ ] Command line build command and launcher.
- [ ] Design and implement appropriate testing infrastructure that would help you fix bugs and prevent from regressions.
  But make sure it is as loosely coupled with the actual compiler and language as possible,
  because anything from internal representation all the way to the syntax can change.
- [ ] Support for arrays. Once arrays are in place, basic algorithms can be written as test programs :)
- [ ] AST interpreter - I've already implemented some, so it is not a priority at the moment. I'd rather focused on
  later
  stages of compilation.
- [ ] Minimize the amount of boolean instructions for comparisons. Currently, I have all of them, but it is of course
  not necessary. I could choose a minimal subset and convert the others to it. 