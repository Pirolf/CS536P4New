###
# This Makefile can be used to make a parser for the CSX language
# (parser.class) and to make a program (P4.class) that tests the parser and
# the unparse methods in ast.java.
#
# make clean removes all generated files.
#
###

JC = javac
P4.class: P4.java parser.class Yylex.class ASTnode.class
	$(JC) -g P4.java

parser.class: parser.java ASTnode.class Yylex.class ErrMsg.class
	$(JC) parser.java

parser.java: CSX.cup
	java java_cup.Main < CSX.cup

Yylex.class: CSX.jlex.java Sym.class ErrMsg.class
	$(JC) CSX.jlex.java

ASTnode.class: ast.java
	$(JC) -g ast.java

CSX.jlex.java: CSX.jlex Sym.class
	java JLex.Main CSX.jlex

Sym.class: Sym.java
	$(JC) -g sym.java

Sym.java: CSX.cup
	java java_cup.Main < CSX.cup

ErrMsg.class: ErrMsg.java
	$(JC) ErrMsg.java

###
# clean
###
clean:
	rm -f *~ *.class parser.java CSX.jlex.java
