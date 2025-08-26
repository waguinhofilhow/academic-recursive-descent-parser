package lexical;

public enum TokenType {
    // Palavras reservadas
    PROGRAM, BEGIN, END, INT, FLOAT, CHAR, IF, THEN,
    ELSE, REPEAT, UNTIL, WHILE, DO, IN, OUT,

    // Símbolos
    COLON,         // :
    SEMICOLON,     // ;
    COMMA,         // ,
    ASSIGN,        // =
    LPAREN,        // (
    RPAREN,        // )
    NOT,           // !
    MINUS,         // -
    PLUS,          // +
    STAR,          // *
    SLASH,         // /
    OR,            // ||
    AND,           // &&

    // Operadores relacionais
    EQ,            // ==
    GT,            // >
    GE,            // >=
    LT,            // <
    LE,            // <=
    NEQ,           // !=

    // Identificadores e literais
    IDENTIFIER,
    INTEGER_CONST,
    FLOAT_CONST,
    CHAR_CONST,
    STRING,

    // Fim de arquivo
    EOF
    }