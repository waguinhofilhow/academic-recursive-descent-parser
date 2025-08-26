package semantic;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.HashMap;
import java.util.Deque;

public class SymbolTable {
    private Deque<Map<String, Symbol>> scopes = new ArrayDeque<>();
    private int scopeLevel = 0;

    public SymbolTable(){
        this.scopes = new ArrayDeque<>();
        enterScope();
    }
    
    public void enterScope() {
        scopes.push(new HashMap<>());
        scopeLevel++;
    }

    public void exitScope() {
        scopes.pop();
        scopeLevel--;
    }

    public boolean add(String name, Symbol symbol) {
        Map<String, Symbol> currentScope = scopes.peek();
        if (currentScope.containsKey(name)) return false;
        symbol.SetcopeLevel(scopeLevel);
        currentScope.put(name, symbol);
        return true;
    }

    public Symbol lookup(String name) {
        for (Map<String, Symbol> scope : scopes) {
            if (scope.containsKey(name)) return scope.get(name);
        }
        return null; // símbolo não encontrado
    }
}