# Academic Recursive Descent Parser

## Description
This project implements a **recursive descent parser** in **Java**.  
It was developed for educational purposes as part of my Computer Engineering studies.  
The parser analyzes input programs and validates them according to a specified grammar.

---

## Features
- Recursive descent parsing
- Grammar validation
- Tokenization and error reporting
- Easy to extend for new grammars

---

## Grammar

program ::= program [decl-list] begin stmt-list end

decl-list ::= decl {decl}
decl ::= type ":" ident-list ";"
ident-list ::= identifier {"," identifier}
type ::= int | float | char

stmt-list ::= stmt {";" stmt}
stmt ::= assign-stmt | if-stmt | while-stmt | repeat-stmt | read-stmt | write-stmt

assign-stmt ::= identifier "=" simple_expr

if-stmt ::= if condition then [decl-list] stmt-list end
| if condition then [decl-list] stmt-list else decl-list stmt-list end

condition ::= expression

repeat-stmt ::= repeat [decl-list] stmt-list stmt-suffix
stmt-suffix ::= until condition

while-stmt ::= stmt-prefix [decl-list] stmt-list end
stmt-prefix ::= while condition do

read-stmt ::= in "(" identifier ")"
write-stmt ::= out "(" writable ")"
writable ::= simple-expr | literal

expression ::= simple-expr | simple-expr relop simple-expr
simple-expr ::= term | simple-expr addop term
term ::= factor-a | term mulop factor-a
factor-a ::= factor | ! factor | "-" factor
factor ::= identifier | constant | "(" expression ")"

relop ::= "==" | ">" | ">=" | "<" | "<=" | "!="
addop ::= "+" | "-" | "||"
mulop ::= "*" | "/" | "&&"

constant ::= integer_const | float_const | char_const

---

## Folder Structure
├── src/ # Source code

├── README.md # Project description

├── LICENSE # License file

---

## Prerequisites
- Java JDK 11 or higher
- Any Java IDE or command-line environment

---

## How to Run
1. Clone the repository:
git clone https://github.com/waguinhofilhow/academic-recursive-descent-parser.git

2. Compile the Java files:
cd src
javac *.java

3. Run the parser:
java Main

## Example
**Input (`exemplo1.txt`):**

```text
program
 int: a,b,c;
 float: result;
 char: ch;

begin
 out("Digite o valor de a:");
 in (a);
 out("Digite o valor de c:");
 in (ch);
 b = 10;
 result = (a * ch)/(b*5 - 345.27);
 out("O resultado e: ");
 out(result);
 result = result + ch
end
```

**Output:**

```text
Erro semântico: Na linha 16: Tipos incompatíveis em expressão aritmética: float e char
```

## Author

Wagner Augusto – Bachelor in Computer Engineering (in progress) – CEFET-MG
GitHub: https://github.com/waguinhofilhow

## License

This project is licensed under the MIT License – see the LICENSE file for details.
