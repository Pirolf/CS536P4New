// exercises all of the name-analysis methods 
//that you wrote for the different AST nodes. 
//This means that it should include (good) declarations 
//of all of the different kinds of names in all of 
//the places that names can be declared 
//and it should include (good) uses of names
// in all kinds of statements and expressions.

//struct decl
struct kitty{
	int  numWiskers;
	bool eatTuna;
};
//bool decl
bool b;
//int decl
int i;
//struct field decl within struct
struct ts{
	int p;
	int q;
	bool isSleeping;
	struct kitty fluffy;
};
struct testStruct{
	int x;
	int y;
	struct ts testStruct2;
};
//struct decls, also setup for following struct tests
struct testStruct s;
struct ts t;
struct kitty k;
//fnDecl of type void
void foo(){
	//int assignment
	i = 1;	//to intVal
	i = i + 2; //to Exp
	//bool assignment
	b = false; //to bool val
	b = i > 3; //to Exp
	//struct assignment
	s.x = 3; //to int val
	s.y = -s.x - 1;//to exp
	s.testStruct2 = t; //struct assign: to struct
	s.testStruct2.fluffy = k; //deeper level assignment
	s.testStruct2.q = 0; //3rd level assignment to val
	s.testStruct2.p = s.testStruct2.q - 3 - s.x; //3rd level assignment to exp
	s.testStruct2.fluffy.eatTuna = true;//4th level assignment to bool val
	s.testStruct2.fluffy.eatTuna = s.testStruct2.p / 4 > 2; //4th level assignment to exp
}
//fnDecl of type int with args
int foo1(int a, bool b, int c){
	//using actuals
	return a + c && b;
}
//fnDecl of type bool
bool foo2(){
	//call previous func
	foo();
	//call previous func with accessing struct fields and using exps
	foo1(1, true, i*2 + s.x);
}
//return with nothing
void goodRetFunc1(int c){
	return;
}
//return bool val
bool goodRetFunc2(int d){
	return true;
}
//return int val
int goodRetFunc3(bool c){
	return -3;
}
//decl formal with same name as gloabl var (int b and bool b)
int func0(int a, int b, bool c){
	//decl local var with same names as struct fields
	int x;
	int y;
	bool z;
	bool t;
	x = 2;
	y = 3;
	//using ++, --
	x++;
	y--;
	x = y + 3;
	z = 5 > 3 && x!= 4;
	t = !z;
	//chaining assignemnt
	a = b = c;
	goodRetFunc1();
	//using local vars as actuals to call func
	func0(x, x*y, (x == y|| x> 3));
	//return exp
	return (1 + 2 + 3) * 4 + a + x*y;
}
bool func1(){
	//return: an exp of other func call in an exp
	return func0(1, i, i>2) + 2;
}
