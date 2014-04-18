import java.util.*;
public class Sym {
    private String type;
    private Object myObj;
    //for functions 
    private String retType;
    private LinkedList<String> formalTypes;
    private String fnType;

    public Sym(String type) {
        this.type = type;
        retType = "";
        formalTypes = new LinkedList<String>();
        fnType = "nonfunc";
    }
    
    public String getType() {
        return type;
    }
    public String getRetType(){
        return retType;
    }
    public String toString() {
        return type;
    }
    
    public void setData(Object o) {
      myObj = o;
    }

    public Object getData() {
      return myObj;
    }
    public void setFnType(String fnType){
        this.fnType = fnType;
    }
    public void setRetType(String retType){
        this.retType = retType;
    }
    public String getFnType(){
        return fnType;
    }
}
