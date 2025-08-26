package sintatic;

import lexical.*;

import java.util.ArrayList;
import java.util.List;
import semantic.*;

public class Parser {
    private List<Token> tokens;
    private int currentIndex;
    private Token current;
    private SymbolTable symbolTable;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currentIndex = 0;
        this.current = tokens.get(0);
    }

    private void advance() {
    if (current.type != TokenType.EOF) {
        currentIndex++;
        current = tokens.get(currentIndex);
    }
    // Se já for EOF, não faz nada: current continua apontando para EOF
    }

    private void eat(TokenType expected) {
        if (current.type == expected) {
            advance();
        } else {
            error(": Na linha "+current.line + ": Esperado: " + expected + ", mas encontrado: " + current.type);
        }
    }

    private void error(String msg) {
        throw new RuntimeException("Erro sintático: " + msg);
    }

    private void semanticError(String msg){
        throw new RuntimeException("Erro semântico: " + msg);
    }

    // program ::= PROGRAM opt-decl-list BEGIN stmt-list END
    public void parseProgram() {
        eat(TokenType.PROGRAM);

        this.symbolTable = new SymbolTable(); 
        parseOptDeclList();

        eat(TokenType.BEGIN);
        parseStmtList();
        eat(TokenType.END);
    }

    // opt-decl-list ::= decl-list | ε
    private void parseOptDeclList() {
        if (isType(current.type)) {
            parseDeclList();
        }
    }

    // decl-list ::= decl decl-list'
    private void parseDeclList() {
        parseDecl();
        parseDeclListPrime();
    }

    // decl-list' ::= decl decl-list' | ε
    private void parseDeclListPrime() {
        while (isType(current.type)) {
            parseDecl();
        }
    }

    // decl ::= type ":" ident-list ";"
    private void parseDecl() {
        String type = parseType();
        Token token = current;
        eat(TokenType.COLON);

        List<String> ids = parseIdentList();
        eat(TokenType.SEMICOLON);

        for (String varName : ids) {
            if (!symbolTable.add(varName, new Symbol(varName, type))) {
                semanticError("Na linha "+token.line+": Redeclaração da variável '" + varName + "'");
            }
        }
    }

    // ident-list ::= IDENTIFIER ident-list'
    private List<String> parseIdentList() {
        List<String> ids = new ArrayList<>();

        ids.add(current.lexeme);
        eat(TokenType.IDENTIFIER);

        while (current.type == TokenType.COMMA) {
            advance();
            ids.add(current.lexeme);
            eat(TokenType.IDENTIFIER);
        }

        return ids;
    }

    // type ::= INT | FLOAT | CHAR
    private String parseType() {
        if (isType(current.type)) {
            String type;
            switch (current.type) {
                case INT:    type = "int";    break;
                case FLOAT:  type = "float";  break;
                case CHAR:   type = "char";   break;
                default:     type = null;     // não deve ocorrer
            }
            advance();
            return type;
        } else {
            error("Na linha " + current.line + ": Tipo esperado (int, float ou char)");
            return null;
        }
    }

    private boolean isType(TokenType type) {
        return type == TokenType.INT || type == TokenType.FLOAT || type == TokenType.CHAR;
    }

    // stmt-list ::= stmt stmt-list'
    private void parseStmtList() {
        parseStmt();
        parseStmtListPrime();
    }

    // stmt-list' ::= ";" stmt stmt-list' | ε
    private void parseStmtListPrime() {
        while (current.type == TokenType.SEMICOLON) {
            advance();
            parseStmt();
        }
    }

    // stmt ::= assign-stmt | if-stmt | while-stmt | repeat-stmt | read-stmt | write-stmt
    private void parseStmt() {
        switch (current.type) {
            case IDENTIFIER: parseAssignStmt(); break;
            case IF:         parseIfStmt();     break;
            case WHILE:      parseWhileStmt();  break;
            case REPEAT:     parseRepeatStmt(); break;
            case IN:         parseReadStmt();   break;
            case OUT:        parseWriteStmt();  break;
            default:
                error("Na linha " + current.line + ": Comando inválido: " + current.type);
        }
    }

    // assign-stmt ::= IDENTIFIER "=" simple-expr
    private void parseAssignStmt() {
        String varName = current.lexeme;

        Symbol symbol = this.symbolTable.lookup(varName);
        if (symbol == null) {
            semanticError("Na linha " + current.line + ": Variável '" + varName + "' não declarada.");
        }

        eat(TokenType.IDENTIFIER);
        eat(TokenType.ASSIGN);

        parseSimpleExpr();
    }

    // if-stmt ::= IF condition THEN opt-decl-list stmt-list if-stmt'
    private void parseIfStmt() {
        eat(TokenType.IF);
        parseCondition(); // já verifica se é booleano
        eat(TokenType.THEN);

        symbolTable.enterScope();
        parseOptDeclList();
        parseStmtList();
        symbolTable.exitScope();

        parseIfStmtPrime();
    }

    // if-stmt' ::= END | ELSE decl stmt-list END
    private void parseIfStmtPrime() {
        if (current.type == TokenType.END) {
            advance();
        } else if (current.type == TokenType.ELSE) {
            advance();

            symbolTable.enterScope();
            parseDecl();
            parseStmtList();
            symbolTable.exitScope();

            eat(TokenType.END);
        } else {
            error("Na linha " + current.line + ": Esperado END ou ELSE");
        }
    }

    // repeat-stmt ::= REPEAT opt-decl-list stmt-list stmt-suffix
    private void parseRepeatStmt() {
        eat(TokenType.REPEAT);

        symbolTable.enterScope();

        parseOptDeclList();
        parseStmtList();
        parseStmtSuffix();

        symbolTable.exitScope();
    }

    // stmt-suffix ::= UNTIL condition
    private void parseStmtSuffix() {
        eat(TokenType.UNTIL);
        parseCondition();
    }

    // while-stmt ::= stmt-prefix opt-decl-list stmt-list END
    private void parseWhileStmt() {
        parseStmtPrefix();

        symbolTable.enterScope();
        parseOptDeclList();
        parseStmtList();
        eat(TokenType.END);
        symbolTable.exitScope();
    }

    // stmt-prefix ::= WHILE condition DO
    private void parseStmtPrefix() {
        eat(TokenType.WHILE);
        parseCondition();
        eat(TokenType.DO);
    }

    // read-stmt ::= IN "(" IDENTIFIER ")"
    private void parseReadStmt() {
        eat(TokenType.IN);
        eat(TokenType.LPAREN);

        String varName = current.lexeme;
        eat(TokenType.IDENTIFIER);

        Symbol symbol = symbolTable.lookup(varName);
        if (symbol == null) {
            semanticError("Na linha " + current.line + ": Identificador '" + varName + "' não declarado.");
        }

        eat(TokenType.RPAREN);
    }

    // write-stmt ::= OUT "(" writable ")"
    private void parseWriteStmt() {
        eat(TokenType.OUT);
        eat(TokenType.LPAREN);

        String type = parseWritable();
        if (!isWritableType(type)) {
            semanticError("Na linha " + current.line + ": Tipo '" + type + "' não pode ser usado em write.");
        }

        eat(TokenType.RPAREN);
    }

    // writable ::= simple-expr | literal
    private String parseWritable() {
        if (isLiteral(current.type)) {
            return parseLiteral();
        } else {
            return parseSimpleExpr();
        }
    }

    // condition ::= expression
    private void parseCondition() {
        String type = parseExpression();
        if (!type.equals("boolean")) {
            semanticError("Na linha " + current.line + ": Condição deve ser do tipo boolean, mas foi " + type);
        }
    }

    // expression ::= simple-expr expression'
    private String parseExpression() {
        String leftType = parseSimpleExpr();
        return parseExpressionPrime(leftType);
    }

    // expression' ::= relop simple-expr | ε
    private String parseExpressionPrime(String inheritedType) {
        if (isRelOp(current.type)) {
            TokenType relop = current.type;
            advance();
            String rightType = parseSimpleExpr();

            if (!isRelationalCompatible(inheritedType, rightType, relop)) {
                semanticError("Na linha " + current.line + ": Tipos incompatíveis em expressão relacional: " 
                    + inheritedType + " e " + rightType);
            }

            return "boolean";
        }
        return inheritedType;
    }

    // simple-expr ::= term simple-expr'
    private String parseSimpleExpr() {
        String leftType = parseTerm();
        return parseSimpleExprPrime(leftType);
    }

    // simple-expr' ::= addop term simple-expr' | ε
    private String parseSimpleExprPrime(String inheritedType) {
        String resultType = inheritedType;

        while (isAddOp(current.type)) {
            TokenType op = current.type;
            advance();
            String rightType = parseTerm();

            if (op == TokenType.OR) {  // operador lógico ||
                if (!isLogicalCompatible(inheritedType, rightType)) {
                    semanticError("Na linha " + current.line + ": Tipos incompatíveis para operador ||: "
                        + resultType + " e " + rightType);
                }
                resultType = "boolean";
            } else {  // operadores aritméticos + e -
                if (!isArithmeticCompatible(resultType, rightType)) {
                    semanticError("Na linha " + current.line + ": Tipos incompatíveis em expressão aritmética: "
                        + resultType + " e " + rightType);
                }
                resultType = getArithmeticResultType(resultType, rightType);
            }

        }

        return resultType;
    }

    // term ::= factor-a term'
    private String parseTerm() {
        String leftType = parseFactorA();
        return parseTermPrime(leftType);
    }

    // term' ::= mulop factor-a term' | ε
    private String parseTermPrime(String inheritedType) {
        while (isMulOp(current.type)) {
            TokenType op = current.type;
            advance();
            String rightType = parseFactorA();

            if (op == TokenType.AND) {
                // Operador lógico &&
                if (!isLogicalCompatible(inheritedType, rightType)) {
                    semanticError("Na linha " + current.line + ": Tipos incompatíveis para operador &&: "
                        + inheritedType + " e " + rightType);
                }
                inheritedType = "boolean";
            } else {
                // Operadores aritméticos * ou /
                if (!isArithmeticCompatible(inheritedType, rightType)) {
                    semanticError("Na linha " + current.line + ": Tipos incompatíveis para operador " + op + ": " 
                        + inheritedType + " e " + rightType);
                }
                inheritedType = getArithmeticResultType(inheritedType, rightType);
            }
        }
        return inheritedType;
    }

    // factor-a ::= factor | "!" factor | "-" factor
    private String parseFactorA() {
        String type;

        if (current.type == TokenType.NOT) {
            advance();
            type = parseFactor();
            if (!type.equals("boolean")) {
                semanticError("Na linha " + current.line + ": Operador '!' exige tipo boolean.");
            }
            return "boolean";
        } else if (current.type == TokenType.MINUS) {
            advance();
            type = parseFactor();
            if (!isNumeric(type)) {
                semanticError("Na linha " + current.line + ": Operador unário '-' exige tipo numérico.");
            }
            return type;
        } else {
            return parseFactor();
        }
    }

    // factor ::= IDENTIFIER | constant | "(" expression ")"
    private String parseFactor() {
        String type;

        switch (current.type) {
            case IDENTIFIER:
                String varName = current.lexeme;
                Token token = current;
                eat(TokenType.IDENTIFIER);

                Symbol symbol = symbolTable.lookup(varName);
                if (symbol == null) {
                    semanticError("Na linha " + token.line + ": Variável '" + varName + "' não declarada.");
                    return "erro";
                }
                return symbol.getType();

            case INTEGER_CONST:
            case FLOAT_CONST:
            case CHAR_CONST:
                return parseConstant();

            case LPAREN:
                eat(TokenType.LPAREN);
                type = parseExpression();
                eat(TokenType.RPAREN);
                return type;

            default:
                semanticError("Na linha " + current.line + ": Fator inválido: " + current.type);
                return "erro";
        }
    }

    // constant ::= INTEGER_CONST | FLOAT_CONST | CHAR_CONST
    private String parseConstant() {
        switch (current.type) {
            case INTEGER_CONST:
                advance();
                return "int";

            case FLOAT_CONST:
                advance();
                return "float";

            case CHAR_CONST:
                advance();
                return "char";

            default:
                error("Na linha " + current.line + ": Constante esperada.");
                return "erro";
        }
    }

    // literal ::= STRING_LITERAL | CHAR_CONST
    private String parseLiteral() {
        if (current.type == TokenType.STRING) {
            advance();
            return "string";
        } else if (current.type == TokenType.CHAR_CONST) {
            advance();
            return "char";
        } else {
            error("Na linha " + current.line + ": Literal inválido");
            return "erro";
        }
    }


    private boolean isRelOp(TokenType type) {
        return type == TokenType.EQ || type == TokenType.NEQ ||
               type == TokenType.GT || type == TokenType.GE ||
               type == TokenType.LT || type == TokenType.LE;
    }

    private boolean isAddOp(TokenType type) {
        return type == TokenType.PLUS || type == TokenType.MINUS || type == TokenType.OR;
    }

    private boolean isMulOp(TokenType type) {
        return type == TokenType.STAR || type == TokenType.SLASH || type == TokenType.AND;
    }

    private boolean isLiteral(TokenType type) {
        return type == TokenType.STRING || type == TokenType.CHAR_CONST;
    }

    private boolean isNumeric(String type){
        if(type.equals("int") || type.equals("float") || type.equals("char")){
            return true;
        }
        return false;
    }

    private boolean isArithmeticCompatible(String tipo1, String tipo2) {
        // Mesma categoria
        if (tipo1.equals("float") && tipo2.equals("float")) return true;
        if (tipo1.equals("int") && tipo2.equals("int")) return true;

        // Compatibilidades mistas permitidas
        if ((tipo1.equals("float") && tipo2.equals("int")) ||
            (tipo1.equals("int") && tipo2.equals("float"))) return true;

        if ((tipo1.equals("int") && tipo2.equals("char")) ||
            (tipo1.equals("char") && tipo2.equals("int"))) return true;

        // Qualquer outro caso: incompatível
        return false;
    }

    private boolean isLogicalCompatible(String tipo1, String tipo2) {
        return tipo1.equals("boolean") && tipo2.equals("boolean");
    }

    private String getArithmeticResultType(String tipo1, String tipo2) {
        if (tipo1.equals("float") || tipo2.equals("float")) {
            return "float";
        }
        if (tipo1.equals("int") || tipo2.equals("int")) {
            return "int";
        }
        if (tipo1.equals("char") && tipo2.equals("char")) {
            return "int"; // ou "char", conforme política da linguagem
        }
        return "erro";
    }

    private boolean isRelationalCompatible(String t1, String t2, TokenType op) {
        // Comparação de igualdade aceita mais tipos
        if (op == TokenType.EQ || op == TokenType.NEQ) {
            // Permite comparação entre booleanos, strings, numéricos e char
            if (t1.equals(t2)) return true;
            if ((t1.equals("int") || t1.equals("float") || t1.equals("char")) &&
                (t2.equals("int") || t2.equals("float") || t2.equals("char"))) {
                return true;
            }
        } else {
            // Para <, >, <=, >=: só numéricos e char
            if ((t1.equals("int") || t1.equals("float") || t1.equals("char")) &&
                (t2.equals("int") || t2.equals("float") || t2.equals("char"))) {
                return true;
            }
        }
        return false;
    }

    private boolean isWritableType(String type) {
        return type.equals("int") || type.equals("float") ||
            type.equals("char") || type.equals("string") || type.equals("boolean");
    }


}
