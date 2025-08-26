package semantic;

public class Symbol {
    public String name;
    public String type; // "int", "float", "char"
    public int scopeLevel;

    public Symbol(String name, String type) {
        this.name = name.toLowerCase(); // linguagem não é case-sensitive
        this.type = type;
    }

    public void SetcopeLevel(int scopeLevel){
        this.scopeLevel = scopeLevel;
    }

    public String getName(){
        return this.name;
    }

    public String getType(){
        return this.type;
    }
}