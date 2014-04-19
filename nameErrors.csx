// include bad and multiply declared names 
//for all of the different kinds of names,
// and in all of the different places 
//that declarations can appear. 
//It should also include uses of undeclared names
// in all kinds of statements and expressions 
//as well as bad struct accesses.

//Multiply declared identifier: same type and name
int m1;
int m1;
//Multiply declared identifier: same name, diff type
int m2;
bool m2;

int m3;
//Non-function declared void: var
void v;

//Non-function declared void: param
int badFunc(void c){
	return;
}
bool a;
//multiple decl for struct
struct multDecl{
	int m;
};
//with same fields
struct multDecl{
	int m;
};
//with diff fields
struct multDecl{
	int n;
};
//decl struct and var with same name
struct multDeclWithVar{
	bool p;
};
int multDeclWithVar;
//mult var decl within structs
struct multVarDecl{
	int v;
	int v;
	bool v;
};
//set up for bad struct tests
struct oh{
	int h;
};
struct ts{
	bool t;
	struct oh aw;
	//Dot-access of non-struct type
	struct whoknows wk;
};
//This returns an Invalid Name of A Struct Type
struct recursiveStruct{
	struct recursiveStruct rs;
};
struct testStruct{
	int x;
	int y;
	//decl void struct field
	void z;
	struct ts testStruct2;
};
//Dup decl for struct
struct testStruct s;
struct nonExistStruct s;
//Invalid name of struct type
struct nonExistStruct nes;
//Invalid name of struct type and multiply declared id
struct nonExistStruct nes;
//Multi decl: diff types: struct and var
void badStructDecl(){
	int bla;
	struct testStruct bla;
	//multiple decl of struct fields
	struct testStruct bla;
	struct oh bla;
}
//Multiply Declared ID for funcs
void foo(int a){
}
void foo(int a){
}
void foo(int a, int b){
	//Invalid name of struct type
	struct nonExistStruct1 oops;
	//Dot-access of non-struct type
	a.x = 3;
	//Invalid struct field name
	s.xyzInvalid.t.p = 2;
	s.testStruct2.aw.hInvalid = 3;
	//assignment to undeclared id
	m1 = invalidid;
	//assignment to undeclared id in a chain
	m1 = yyy = ppp;
}
void miao(int a, bool b, int c){
	//multi var decl
 	int a;
	int b;
	//multi struct decl
	struct oh ho;
	struct ts ho;
	//undecl func
	foofoo(1, 2);
	//undecl var
	mmm = 4;
	
}
//undelcared func
int badRet0(){
	return bad();
}
int badRet1(){
	//undeclared id in return stmt
	return k;
}
//multiple decl in formals
bool badFormalDecl(int a, int a, int b, bool b, int c){
	//redeclare actual within funcBody
	bool c;
	//declare void var
	void d;
	//func call with undeclared actual
	miao(ggg, jjj, kkk);
}



