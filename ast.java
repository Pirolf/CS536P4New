import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a Mini program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Kids
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignNode          ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of kids, or
// internal nodes with a fixed number of kids:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of kids:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, ReadStmtNode,   WriteStmtNode   
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode{ 
	// every subclass must provide an unparse operation
	abstract public void unparse(PrintWriter p, int indent);
	// this method can be used by the unparse methods to do indenting
	protected void doIndent(PrintWriter p, int indent) {
		for (int k=0; k<indent; k++) p.print(" ");
	}
   public static SymTable symTbl;
}

// **********************************************************************
// ProgramNode,  DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode
// **********************************************************************

//ISSUES: Scoping, Error Handling
class ProgramNode extends ASTnode {
	public ProgramNode(DeclListNode L) {
		myDeclList = L;
	}

	public void unparse(PrintWriter p, int indent) {
		myDeclList.unparse(p, indent);
	}

	public void analyzeName(){
		symTbl = new SymTable();
		myDeclList.analyzeName(symTbl);
	}
	// 1 kid
	private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
	public DeclListNode(List<DeclNode> S) {
		myDecls = S;
	}

	public void unparse(PrintWriter p, int indent) {
		Iterator it = myDecls.iterator();
		try {
			while (it.hasNext()) {
            ((DeclNode)it.next()).unparse(p, indent);
			}
		} catch (NoSuchElementException ex) {
			System.err.println("unexpected NoSuchElementException in DeclListNode.print");
			System.exit(-1);
		}
	}

   public void analyzeName(SymTable tbl) {
      for (DeclNode n : myDecls)
         n.analyzeName(tbl);
   }
	// list of kids (DeclNodes)
	private List<DeclNode> myDecls;
}

class FormalsListNode extends ASTnode {
	public FormalsListNode(List<FormalDeclNode> S) {
		myFormals = S;
	}

	public void unparse(PrintWriter p, int indent) {
		Iterator<FormalDeclNode> it = myFormals.iterator();
		if (it.hasNext()) { // if there is at least one element
			it.next().unparse(p, indent);
			while (it.hasNext()) {  // print the rest of the list
				p.print(", ");
				it.next().unparse(p, indent);
			}
		} 
	}
	public void analyzeName(SymTable tbl){
		//scope entry
      for (FormalDeclNode n : myFormals)
         n.analyzeName(tbl);
	}

	public String getFormalTypes(){
		String formalsString = "";
		Iterator<FormalDeclNode> it = myFormals.iterator();
		if(it.hasNext()){
			formalsString += (it.next()).formalType();
			while(it.hasNext()){
				formalsString = formalsString + ", " + (it.next()).formalType();
			}
		}
		return formalsString;
	}
	// list of kids (FormalDeclNodes)
	private List<FormalDeclNode> myFormals;
}

class FnBodyNode extends ASTnode {
	public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
		myDeclList = declList;
		myStmtList = stmtList;
	}

	public void unparse(PrintWriter p, int indent) {
		myDeclList.unparse(p, indent);
		myStmtList.unparse(p, indent);
	}
	public void analyzeName(SymTable tbl){
		myDeclList.analyzeName(tbl);
		myStmtList.analyzeName(tbl);
	}
	// 2 kids
	private DeclListNode myDeclList;
	private StmtListNode myStmtList;
}

class StmtListNode extends ASTnode {
	public StmtListNode(List<StmtNode> S) {
		myStmts = S;
	}

	public void unparse(PrintWriter p, int indent) {
		Iterator<StmtNode> it = myStmts.iterator();
		while (it.hasNext()) {
			it.next().unparse(p, indent);
		}
	}
	public void analyzeName(SymTable tbl){
      for (StmtNode n : myStmts)
         n.analyzeName(tbl);
	}
	// list of kids (StmtNodes)
	private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
	public ExpListNode(List<ExpNode> S) {
		myExps = S;
	}

	public void unparse(PrintWriter p, int indent) {
		Iterator<ExpNode> it = myExps.iterator();
		if (it.hasNext()) { // if there is at least one element
			it.next().unparse(p, indent);
			while (it.hasNext()) {  // print the rest of the list
				p.print(", ");
				it.next().unparse(p, indent);
			}
		} 
	}
	public void analyzeName(SymTable tbl){
      for (ExpNode n : myExps)
         n.analyzeName(tbl);
	}

	// list of kids (ExpNodes)
	private List<ExpNode> myExps;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
   abstract public void analyzeName(SymTable tbl);
}

class VarDeclNode extends DeclNode {
	public VarDeclNode(TypeNode type, IdNode id, int size) {
		myType = type;
		myId = id;
		mySize = size;
	}

	public void unparse(PrintWriter p, int indent) {
		doIndent(p, indent);
		myType.unparse(p, 0);
		p.print(" ");
		myId.unparse(p, 0);
		p.println(";");
	}

	public void analyzeName(SymTable tbl) {
      Sym s = new Sym(myType.getTypeNodeType());
      
      // Check for var decl'd as type "void"
      if ((s.getType()).equals("void")){
         ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Non-function declared void");
      }
      
      // Make sure var is of a struct type that's been declared
      if (myType instanceof StructNode) {
         Sym temp = symTbl.lookupGlobal(myType.getTypeNodeType());
         if(temp == null || !temp.getType().equals("struct")){
         	int ln = myId.getLineNum();
         	int cn = myId.getCharNum();
         	ErrMsg.fatal(ln, cn, "Invalid name of struct type");
         }else{
            // Make sure var has access to names in the struct type's symtbl
            s.setData(temp.getData());
         }
      }
      
      // Add id to symbol table
      try {
         tbl.addDecl(myId.toString(), s);
      } catch (DuplicateSymException e) {
         int ln = myId.getLineNum();
         int cn = myId.getCharNum();
         ErrMsg.fatal(ln, cn, "Multiply declared identifier");
      } catch (EmptySymTableException e) {
         int ln = myId.getLineNum();
         int cn = myId.getCharNum();
         ErrMsg.fatal(ln, cn, "okay, you really screwed up!");
      }
   }

	// 3 kids
	private TypeNode myType;
	private IdNode myId;
	private int mySize;  // use value NOT_STRUCT if this is not a struct type

	public static int NOT_STRUCT = -1;
}

class FnDeclNode extends DeclNode {
   public FnDeclNode(TypeNode type,
         IdNode id,
         FormalsListNode formalList,
         FnBodyNode body) {
      
      myType = type;
      myId = id;
      myFormalsList = formalList;
      myBody = body;
   }

   public void unparse(PrintWriter p, int indent) {
      doIndent(p, indent);
      myType.unparse(p, 0);
      p.print(" ");
      myId.unparse(p, 0);
      p.print("(");
      myFormalsList.unparse(p, 0);
      p.println(") {");
      myBody.unparse(p, indent+4);
      p.println("}\n");
   }
   public void analyzeName(SymTable tbl){
      String fnType = getFormalTypes() + "->" + myType.getTypeNodeType();
      Sym s = new Sym(myType.getTypeNodeType());
      s.setFnType(fnType);
      myId.setSym(s);
      try{
         tbl.print();
         tbl.addDecl(myId.toString(), s);
         tbl.print();
         System.out.println("successfully added " + myId.toString());
         System.out.println(myType.getTypeNodeType());
      }catch(DuplicateSymException e){
         int ln = myId.getLineNum();
         int cn = myId.getCharNum();
         ErrMsg.fatal(ln, cn, "Multiply declared identifier");
       }catch(EmptySymTableException e){
         int ln = myId.getLineNum();
         int cn = myId.getCharNum();
         ErrMsg.fatal(ln, cn, "okay, you really screwed up!");
       }

      tbl.addScope();
      myFormalsList.analyzeName(tbl);
      myBody.analyzeName(tbl);

      try {
         tbl.removeScope();
      } catch (EmptySymTableException e) {
         int ln = myId.getLineNum();
         int cn = myId.getCharNum();
         ErrMsg.fatal(ln, cn, "okay, you really screwed up!");
      }
   }

	public String getFormalTypes(){
		return myFormalsList.getFormalTypes();
	}
	// 4 kids
	private TypeNode myType;
	private IdNode myId;
	private FormalsListNode myFormalsList;
	private FnBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
	public FormalDeclNode(TypeNode type, IdNode id) {
		myType = type;
		myId = id;
	}

	public void unparse(PrintWriter p, int indent) {
		myType.unparse(p, 0);
		p.print(" ");
		myId.unparse(p, 0);
	}
	public void analyzeName(SymTable tbl){    
      try {
      	if((myType.getTypeNodeType()).equals("void")){
      		int ln = myId.getLineNum();
         	int cn = myId.getCharNum();
      		ErrMsg.fatal(ln, cn, "Non-function declared void");
      	}else{
      		tbl.addDecl(myId.toString(), new Sym(myType.getTypeNodeType()));
      	}
         
      } catch (DuplicateSymException e) {
         int ln = myId.getLineNum();
         int cn = myId.getCharNum();
         ErrMsg.fatal(ln, cn, "Multiply declared identifier");
      } catch (EmptySymTableException e) {
         int ln = myId.getLineNum();
         int cn = myId.getCharNum();
         ErrMsg.fatal(ln, cn, "okay, you really screwed up!");
      }
         
		//we don't have to deal with struct here because it is not allowed to be a param
	}
	public String formalType(){
		return myType.getTypeNodeType();
	}
	// 2 kids
	private TypeNode myType;
	private IdNode myId;
}

class StructDeclNode extends DeclNode {
	public StructDeclNode(IdNode id, DeclListNode declList) {
		myId = id;
		myDeclList = declList;
	}

	public void unparse(PrintWriter p, int indent) {
		doIndent(p, indent);
		p.print("struct ");
		myId.unparse(p, 0);
		p.println("{");
		myDeclList.unparse(p, indent+4);
		doIndent(p, indent);
		p.println("};\n");

	}
	public void analyzeName(SymTable tbl){
		//should also add to symtbl
		//scope entry
		SymTable strctTable = new SymTable();
      myDeclList.analyzeName(strctTable);
		//tbl.lookupLocal(myId.toString());
      Sym s = new Sym("struct");
      s.setData(strctTable);
      try {
         tbl.addDecl(myId.toString(), s);
      } catch (DuplicateSymException e) {
         int ln = myId.getLineNum();
         int cn = myId.getCharNum();
         ErrMsg.fatal(ln, cn, "Multiply declared identifier");
      } catch (EmptySymTableException e) {
         int ln = myId.getLineNum();
         int cn = myId.getCharNum();
         ErrMsg.fatal(ln, cn, "okay, you really screwed up!");
      }
	}
	// 2 kids
	private IdNode myId;
	private DeclListNode myDeclList;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    abstract public String getTypeNodeType();
}

class IntNode extends TypeNode {
	public IntNode() {
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("int");
	}

	public String getTypeNodeType(){
		return "int";
	}
}

class BoolNode extends TypeNode {
	public BoolNode() {
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("bool");
	}
	public String getTypeNodeType(){
		return "bool";
	}

}

class VoidNode extends TypeNode {
	public VoidNode() {
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("void");
	}

	public String getTypeNodeType(){
		return "void";
	}
}

class StructNode extends TypeNode {
	public StructNode(IdNode id) {
		myId = id;
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("struct ");
		myId.unparse(p, 0);
	}
	public void analyzeName(SymTable tbl){
      if (tbl.lookupGlobal(myId.toString()) == null)
         ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Invalid name of struct type");
	}

	public String getTypeNodeType(){
		return myId.toString();
	}
	//TODO
	// 1 kid
	private IdNode myId;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void analyzeName(SymTable tbl);
}

class AssignStmtNode extends StmtNode {
	public AssignStmtNode(AssignNode assign) {
		myAssign = assign;
	}

	public void unparse(PrintWriter p, int indent) {
		doIndent(p, indent);
		myAssign.unparse(p, -1); // no parentheses
		p.println(";");
	}
	public void analyzeName(SymTable tbl){
		myAssign.analyzeName(tbl);
	}
	// 1 kid
	private AssignNode myAssign;
}

class PostIncStmtNode extends StmtNode {
	public PostIncStmtNode(ExpNode exp) {
		myExp = exp;
	}

	public void unparse(PrintWriter p, int indent) {
		doIndent(p, indent);
		myExp.unparse(p, 0);
		p.println("++;");
	}
	public void analyzeName(SymTable tbl){
		myExp.analyzeName(tbl);
	}
	// 1 kid
	private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
	public PostDecStmtNode(ExpNode exp) {
		myExp = exp;
	}

	public void unparse(PrintWriter p, int indent) {
		doIndent(p, indent);
		myExp.unparse(p, 0);
		p.println("--;");
	}
	public void analyzeName(SymTable tbl){
		myExp.analyzeName(tbl);
	}
	// 1 kid
	private ExpNode myExp;
}

class ReadStmtNode extends StmtNode {
	public ReadStmtNode(ExpNode e) {
		myExp = e;
	}

	public void unparse(PrintWriter p, int indent) {
		doIndent(p, indent);
		p.print("cin >> ");
		myExp.unparse(p, 0);
		p.println(";");
	}
	public void analyzeName(SymTable tbl){
		myExp.analyzeName(tbl);
	}
	// 1 kid (actually can only be an IdNode or an ArrayExpNode)
	private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
	public WriteStmtNode(ExpNode exp) {
		myExp = exp;
	}

	public void unparse(PrintWriter p, int indent) {
		doIndent(p, indent);
		p.print("cout << ");
		myExp.unparse(p, 0);
		p.println(";");
	}
	public void analyzeName(SymTable tbl){
		myExp.analyzeName(tbl);
	}
	// 1 kid
	private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
	public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
		myDeclList = dlist;
		myExp = exp;
		myStmtList = slist;
	}

	public void unparse(PrintWriter p, int indent) {
		doIndent(p, indent);
		p.print("if (");
		myExp.unparse(p, 0);
		p.println(") {");
		myDeclList.unparse(p, indent+4);
		myStmtList.unparse(p, indent+4);
		doIndent(p, indent);
		p.println("}");
	}
	public void analyzeName(SymTable tbl){
		myExp.analyzeName(tbl);
		//scope entry
		tbl.addScope();
      myDeclList.analyzeName(tbl);
      myStmtList.analyzeName(tbl);
		//scope exit
      try {
         tbl.removeScope();
      } catch (EmptySymTableException e) {
         int ln = 0;
         int cn = 0;
         ErrMsg.fatal(ln, cn, "okay, you really screwed up!");
      }
	}
	// e kids
	private ExpNode myExp;
	private DeclListNode myDeclList;
	private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
	public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
			StmtListNode slist1, DeclListNode dlist2,
			StmtListNode slist2) {
		myExp = exp;
		myThenDeclList = dlist1;
		myThenStmtList = slist1;
		myElseDeclList = dlist2;
		myElseStmtList = slist2;
	}

	public void unparse(PrintWriter p, int indent) {
		doIndent(p, indent);
		p.print("if (");
		myExp.unparse(p, 0);
		p.println(") {");
		myThenDeclList.unparse(p, indent+4);
		myThenStmtList.unparse(p, indent+4);
		doIndent(p, indent);
		p.println("}");
		doIndent(p, indent);
		p.println("else {");
		myElseDeclList.unparse(p, indent+4);
		myElseStmtList.unparse(p, indent+4);
		doIndent(p, indent);
		p.println("}");        
	}
	public void analyzeName(SymTable tbl){
		myExp.analyzeName(tbl);
		//scope entry
		tbl.addScope();
      myThenDeclList.analyzeName(tbl);
		myThenStmtList.analyzeName(tbl);
		//scope exit: if
      try {
         tbl.removeScope();
      } catch (EmptySymTableException e) {
         int ln = 0;
         int cn = 0;
         ErrMsg.fatal(ln, cn, "okay, you really screwed up!");
      }
		//scope entry
		tbl.addScope();
		myElseDeclList.analyzeName(tbl);
		myElseStmtList.analyzeName(tbl);
		//scope exit: else
      try {
         tbl.removeScope();
      } catch (EmptySymTableException e) {
         int ln = 0;
         int cn = 0;
         ErrMsg.fatal(ln, cn, "okay, you really screwed up!");
      }
	}
	// 5 kids
	private ExpNode myExp;
	private DeclListNode myThenDeclList;
	private StmtListNode myThenStmtList;
	private StmtListNode myElseStmtList;
	private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
	public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
		myExp = exp;
		myDeclList = dlist;
		myStmtList = slist;
	}

	public void unparse(PrintWriter p, int indent) {
		doIndent(p, indent);
		p.print("while (");
		myExp.unparse(p, 0);
		p.println(") {");
		myDeclList.unparse(p, indent+4);
		myStmtList.unparse(p, indent+4);
		doIndent(p, indent);
		p.println("}");
	}
	public void analyzeName(SymTable tbl){
		myExp.analyzeName(tbl);
      tbl.addScope();
      myDeclList.analyzeName(tbl);
      myStmtList.analyzeName(tbl);
		try {
         tbl.removeScope();
      } catch (EmptySymTableException e) {
         int ln = 0;
         int cn = 0;
         ErrMsg.fatal(ln, cn, "okay, you really screwed up!");
      }
	}
	// 3 kids
	private ExpNode myExp;
	private DeclListNode myDeclList;
	private StmtListNode myStmtList;
}

class CallStmtNode extends StmtNode {
	public CallStmtNode(CallExpNode call) {
		myCall = call;
	}

	public void unparse(PrintWriter p, int indent) {
		doIndent(p, indent);
		myCall.unparse(p, indent);
		p.println(";");
	}
	public void analyzeName(SymTable tbl){
		myCall.analyzeName(tbl);
	}
	// 1 kid
	private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
	public ReturnStmtNode(ExpNode exp) {
		myExp = exp;
	}

	public void unparse(PrintWriter p, int indent) {
		doIndent(p, indent);
		p.print("return");
		if (myExp != null) {
			p.print(" ");
			myExp.unparse(p, 0);
		}
		p.println(";");
	}
	public void analyzeName(SymTable tbl){
		if(myExp == null){
			return;
		}
		myExp.analyzeName(tbl);
	}
	// 1 kid
	private ExpNode myExp; // possibly null
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    abstract public void analyzeName(SymTable tbl);
}

class IntLitNode extends ExpNode {
	public IntLitNode(int lineNum, int charNum, int intVal) {
		myLineNum = lineNum;
		myCharNum = charNum;
		myIntVal = intVal;
	}

	public void unparse(PrintWriter p, int indent) {
		p.print(myIntVal);
	}
	public void analyzeName(SymTable tbl){
		return;
	}
	private int myLineNum;
	private int myCharNum;
	private int myIntVal;
}

class StringLitNode extends ExpNode {
	public StringLitNode(int lineNum, int charNum, String strVal) {
		myLineNum = lineNum;
		myCharNum = charNum;
		myStrVal = strVal;
	}

	public void unparse(PrintWriter p, int indent) {
		p.print(myStrVal);
	}
	public void analyzeName(SymTable tbl){
		return;
	}
	private int myLineNum;
	private int myCharNum;
	private String myStrVal;
}

class TrueNode extends ExpNode {
	public TrueNode(int lineNum, int charNum) {
		myLineNum = lineNum;
		myCharNum = charNum;
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("true");
	}
	public void analyzeName(SymTable tbl){
		return;
	}
	private int myLineNum;
	private int myCharNum;
}

class FalseNode extends ExpNode {
	public FalseNode(int lineNum, int charNum) {
		myLineNum = lineNum;
		myCharNum = charNum;
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("false");
	}
	public void analyzeName(SymTable tbl){
		return;
	}
	private int myLineNum;
	private int myCharNum;
}

class IdNode extends ExpNode {
	public IdNode(int lineNum, int charNum, String strVal) {
		myLineNum = lineNum;
		myCharNum = charNum;
		myStrVal = strVal;
	}

	public void unparse(PrintWriter p, int indent) {
	  p.print(myStrVal);
      if (mySym != null){
      	if(mySym.getFnType().equals("nonfunc")){
      		//non function
      		p.print("("+ mySym.getType() + ")");
      	}
      }      	
	}

   // Returns sym table for struct type id, null for primitives & undecl'd
   public SymTable getTbl(SymTable tbl) {
      // Check that it's declared in scope
      this.analyzeName(tbl);
      if (mySym == null){
         return null;
      }

      // Check it's a struct type
      Sym s = symTbl.lookupGlobal(mySym.getType());
      SymTable table;
      table = (SymTable) mySym.getData();

      // return this id's symtable (will be null in case of nonstruct)
      return table;
   }

	public void analyzeName(SymTable tbl){
      mySym = tbl.lookupGlobal(myStrVal);
		if (mySym == null)
         ErrMsg.fatal(myLineNum, myCharNum, "Undeclared identifier");
	}

	public String toString(){
      return myStrVal;
   }
	
	public Sym getSym(){
		return mySym;
	}

	public void setSym(Sym s){
		mySym = s;
	}

   public int getLineNum() {
      return myLineNum;
   }

   public int getCharNum() {
      return myCharNum;
   }
	private int myLineNum;
	private int myCharNum;
	private String myStrVal;
	private Sym mySym;//to link the node with the corresponding symbol-table entry
}
/*
 * A bad struct access happens when
 * either the left-hand side of the dot-access is not a name already declared to be of a struct type
 * or the right-hand side of the dot-access is not the name of a field for the appropriate type of struct
 */
class DotAccessExpNode extends ExpNode {
	public DotAccessExpNode(ExpNode loc, IdNode id) {
		myLoc = loc;    
		myId = id;
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("(");
		myLoc.unparse(p, 0);
		p.print(").");
		myId.unparse(p, 0);
	}
   
   // A slightly different recursive approach...
   public SymTable getTbl (SymTable tbl) {
      // Base case, an invalid access has already happened
      if (tbl == null)
         return null;
      
      SymTable table = null;
      // Base case, myLoc is an IdNode
      if (myLoc instanceof IdNode){
         table = ((IdNode)myLoc).getTbl(symTbl);
         // Tried to do a dot access of a nonstruct
         if (table == null) {
            int ln, cn;
            ln = ((IdNode) myLoc).getLineNum();
            cn = ((IdNode) myLoc).getCharNum();
            ErrMsg.fatal(ln, cn, "Dot-access of non-struct type");
            return null;
         }
      // Recursive case, is a dot access node (needs myLoc's sym table 1st!)
      }else if (myLoc instanceof DotAccessExpNode) {
         table = ((DotAccessExpNode) myLoc).getTbl(tbl);   
         // Tried to do a dot access of nonstruct
         if (table == null) {
            int ln, cn;
            ln = ((DotAccessExpNode) myLoc).getId().getLineNum();
            cn = ((DotAccessExpNode) myLoc).getId().getCharNum();
            ErrMsg.fatal(ln, cn, "Dot-access of non-struct type");
            return null;
         }
      }
      
      Sym s = table.lookupGlobal(myId.toString());
      // errmsg invalid structfield name
      if (s == null){
         int ln = myId.getLineNum();
         int cn = myId.getCharNum();
         ErrMsg.fatal(ln, cn, "Invalid struct field name");
         return null;
      }
      return myId.getTbl(table); 
   }

	public void analyzeName(SymTable tbl){
      SymTable table = null;
      if (myLoc instanceof DotAccessExpNode){ 
         table = ((DotAccessExpNode) myLoc).getTbl(tbl);
         // Some problem already caught
         if (table == null)
            return;
      }else {
         table = ((IdNode) myLoc).getTbl(tbl);
         // dotaccess of nonstruct
         if (table == null) {
            int ln = ((IdNode) myLoc).getLineNum();
            int cn = ((IdNode) myLoc).getCharNum();
            ErrMsg.fatal(ln, cn, "Dot-access of non-struct type");
            return;
         }
      }
      Sym s = table.lookupGlobal(myId.toString());
      // Very last id isn't in very last symbol table
      if (s == null) {
         int ln = myId.getLineNum();
         int cn = myId.getCharNum();
         ErrMsg.fatal(ln, cn, "Invalid struct field name");
         return;
      }
      myId.analyzeName(table);   
   }
   public IdNode getId() {
      return (IdNode) myId;
   }
	// 2 kids
	private ExpNode myLoc;    
	private IdNode myId;
   
}
class AssignNode extends ExpNode {
	public AssignNode(ExpNode lhs, ExpNode exp) {
		myLhs = lhs;
		myExp = exp;
	}

	public void unparse(PrintWriter p, int indent) {
		if (indent != -1)  p.print("(");
		myLhs.unparse(p, 0);
		p.print(" = ");
		myExp.unparse(p, 0);
		if (indent != -1)  p.print(")");
	}
	public void analyzeName(SymTable tbl){
		myLhs.analyzeName(tbl);
		myExp.analyzeName(tbl);
	}
	// 2 kids
	private ExpNode myLhs;
	private ExpNode myExp;
}

class CallExpNode extends ExpNode {
	public CallExpNode(IdNode name, ExpListNode elist) {
		myId = name;
		myExpList = elist;
	}

	public CallExpNode(IdNode name) {
		myId = name;
		myExpList = new ExpListNode(new LinkedList<ExpNode>());
	}

	// ** unparse **
	public void unparse(PrintWriter p, int indent) {
		myId.unparse(p, 0);
		Sym funcSym = symTbl.lookupGlobal(myId.toString());
		
		p.print("(" +funcSym.getFnType() + ")");
		p.print("(");
		if (myExpList != null) {
			myExpList.unparse(p, 0);
		}
		p.print(")");
	}
	public void analyzeName(SymTable tbl){
		myId.analyzeName(tbl);
		if(myExpList != null){
			myExpList.analyzeName(tbl);  
		}

	}
	// 2 kids
	private IdNode myId;
	private ExpListNode myExpList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode {
	public UnaryExpNode(ExpNode exp) {
		myExp = exp;
	}

	// one child
	protected ExpNode myExp;
	public void analyzeName(SymTable tbl){
		myExp.analyzeName(tbl);
	}
}

abstract class BinaryExpNode extends ExpNode {
	public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
		myExp1 = exp1;
		myExp2 = exp2;
	}
	// two kids
	protected ExpNode myExp1;
	protected ExpNode myExp2;
   public void analyzeName(SymTable tbl) {
      myExp1.analyzeName(tbl);
      myExp2.analyzeName(tbl);
   }
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
	public UnaryMinusNode(ExpNode exp) {
		super(exp);
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("(-");
		myExp.unparse(p, 0);
		p.print(")");
	}
}

class NotNode extends UnaryExpNode {
	public NotNode(ExpNode exp) {
		super(exp);
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("(!");
		myExp.unparse(p, 0);
		p.print(")");
	}
}

// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************

class PlusNode extends BinaryExpNode {
	public PlusNode(ExpNode exp1, ExpNode exp2) {
		super(exp1, exp2);
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("(");
		myExp1.unparse(p, 0);
		p.print(" + ");
		myExp2.unparse(p, 0);
		p.print(")");
	}
}

class MinusNode extends BinaryExpNode {
	public MinusNode(ExpNode exp1, ExpNode exp2) {
		super(exp1, exp2);
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("(");
		myExp1.unparse(p, 0);
		p.print(" - ");
		myExp2.unparse(p, 0);
		p.print(")");
	}
}

class TimesNode extends BinaryExpNode {
	public TimesNode(ExpNode exp1, ExpNode exp2) {
		super(exp1, exp2);
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("(");
		myExp1.unparse(p, 0);
		p.print(" * ");
		myExp2.unparse(p, 0);
		p.print(")");
	}
}

class DivideNode extends BinaryExpNode {
	public DivideNode(ExpNode exp1, ExpNode exp2) {
		super(exp1, exp2);
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("(");
		myExp1.unparse(p, 0);
		p.print(" / ");
		myExp2.unparse(p, 0);
		p.print(")");
	}
}

class AndNode extends BinaryExpNode {
	public AndNode(ExpNode exp1, ExpNode exp2) {
		super(exp1, exp2);
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("(");
		myExp1.unparse(p, 0);
		p.print(" && ");
		myExp2.unparse(p, 0);
		p.print(")");
	}
}

class OrNode extends BinaryExpNode {
	public OrNode(ExpNode exp1, ExpNode exp2) {
		super(exp1, exp2);
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("(");
		myExp1.unparse(p, 0);
		p.print(" || ");
		myExp2.unparse(p, 0);
		p.print(")");
	}

}

class EqualsNode extends BinaryExpNode {
	public EqualsNode(ExpNode exp1, ExpNode exp2) {
		super(exp1, exp2);
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("(");
		myExp1.unparse(p, 0);
		p.print(" == ");
		myExp2.unparse(p, 0);
		p.print(")");
	}
}

class NotEqualsNode extends BinaryExpNode {
	public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
		super(exp1, exp2);
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("(");
		myExp1.unparse(p, 0);
		p.print(" != ");
		myExp2.unparse(p, 0);
		p.print(")");
	}
}

class LessNode extends BinaryExpNode {
	public LessNode(ExpNode exp1, ExpNode exp2) {
		super(exp1, exp2);
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("(");
		myExp1.unparse(p, 0);
		p.print(" < ");
		myExp2.unparse(p, 0);
		p.print(")");
	}
}

class GreaterNode extends BinaryExpNode {
	public GreaterNode(ExpNode exp1, ExpNode exp2) {
		super(exp1, exp2);
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("(");
		myExp1.unparse(p, 0);
		p.print(" > ");
		myExp2.unparse(p, 0);
		p.print(")");
	}
}

class LessEqNode extends BinaryExpNode {
	public LessEqNode(ExpNode exp1, ExpNode exp2) {
		super(exp1, exp2);
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("(");
		myExp1.unparse(p, 0);
		p.print(" <= ");
		myExp2.unparse(p, 0);
		p.print(")");
	}
}

class GreaterEqNode extends BinaryExpNode {
	public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
		super(exp1, exp2);
	}

	public void unparse(PrintWriter p, int indent) {
		p.print("(");
		myExp1.unparse(p, 0);
		p.print(" >= ");
		myExp2.unparse(p, 0);
		p.print(")");
	}
}
