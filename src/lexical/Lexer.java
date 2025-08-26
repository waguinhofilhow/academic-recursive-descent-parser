package lexical;

import java.util.*;

public class Lexer {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Lexer(String source) {
        this.source = source;
        scanTokens();
    }

    private void scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", line));
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '{':
                while (!isAtEnd() && peek() != '}') {
                    if (peek() == '\n') line++;
                    advance();
                }
                if (!isAtEnd()) advance(); // consume '}'
                else System.err.println("Fim de arquivo inesperado.");
                break;

            case '%':
                while (!isAtEnd() && peek() != '\n') advance();
                if (!isAtEnd()) {
                    advance();
                    line++;
                }
                break;

            case ':': addToken(TokenType.COLON); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case ',': addToken(TokenType.COMMA); break;

            case '=':
                if (peek() == '=') {
                    advance();
                    addToken(TokenType.EQ);
                } else {
                    addToken(TokenType.ASSIGN);
                }
                break;

            case '(':
                addToken(TokenType.LPAREN);
                break;

            case ')':
                addToken(TokenType.RPAREN);
                break;

            case '!':
                if (peek() == '=') {
                    advance();
                    addToken(TokenType.NEQ);
                } else {
                    addToken(TokenType.NOT);
                }
                break;

            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case '*': addToken(TokenType.STAR); break;
            case '/': addToken(TokenType.SLASH); break;

            case '|':
                if (peek() == '|') {
                    advance();
                    addToken(TokenType.OR);
                } else {
                    System.err.println("Caractere inesperado na linha " + line + ": " + peek());
                }
                break;

            case '&':
                if (peek() == '&') {
                    advance();
                    addToken(TokenType.AND);
                } else {
                    System.err.println("Caractere inesperado na linha " + line + ": " + peek());
                }
                break;

            case '>':
                if (peek() == '=') {
                    advance();
                    addToken(TokenType.GE);
                } else {
                    addToken(TokenType.GT);
                }
                break;

            case '<':
                if (peek() == '=') {
                    advance();
                    addToken(TokenType.LE);
                } else {
                    addToken(TokenType.LT);
                }
                break;

            case '\'':
                if (isAscii(peek())) {
                    char value = advance(); // char literal
                    if (peek() == '\'') {
                        advance(); // consume closing quote
                        addToken(TokenType.CHAR_CONST, String.valueOf(value));
                    } else {
                        System.err.println("Esperado fechamento de caractere na linha " + line);
                    }
                }
                break;

            case '\"':
                StringBuilder str = new StringBuilder();
                while (!isAtEnd() && peek() != '"' && peek() != '\n') {
                    str.append(advance());
                }
                if (peek() == '\"') {
                    advance(); // consume closing "
                    addToken(TokenType.STRING, str.toString());
                } else {
                    System.err.println("String não terminada na linha " + line);
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                // Ignora espaços
                break;

            case '\n':
                line++;
                break;

            default:
                if (isDigit(c)) {
                    StringBuilder number = new StringBuilder();
                    number.append(c);

                    while (isDigit(peek())) number.append(advance());

                    if (peek() == '.' && isDigit(peekNext())) {
                        number.append(advance()); // consume '.'
                        while (isDigit(peek())) number.append(advance());
                        addToken(TokenType.FLOAT_CONST, number.toString());
                    } else {
                        addToken(TokenType.INTEGER_CONST, number.toString());
                    }

                } else if (Character.isLetter(c) || c == '_') {
                    StringBuilder ident = new StringBuilder();
                    ident.append(c);
                    while (Character.isLetter(peek()) || isDigit(peek()) || peek() == '_') {
                        ident.append(advance());
                    }

                    String text = ident.toString();
                    TokenType type = switch (text.toLowerCase()) {
                        case "program" -> TokenType.PROGRAM;
                        case "begin" -> TokenType.BEGIN;
                        case "end" -> TokenType.END;
                        case "int" -> TokenType.INT;
                        case "float" -> TokenType.FLOAT;
                        case "char" -> TokenType.CHAR;
                        case "if" -> TokenType.IF;
                        case "then" -> TokenType.THEN;
                        case "else" -> TokenType.ELSE;
                        case "repeat" -> TokenType.REPEAT;
                        case "until" -> TokenType.UNTIL;
                        case "while" -> TokenType.WHILE;
                        case "do" -> TokenType.DO;
                        case "in" -> TokenType.IN;
                        case "out" -> TokenType.OUT;
                        default -> TokenType.IDENTIFIER;
                    };
                    addToken(type, text);
                } else {
                    System.err.println("Caractere inesperado na linha " + line + ": " + c);
                }
        }
    }

    private char advance() {
        if (isAtEnd()) return '\0';
        return source.charAt(current++);
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAscii(char c) {
        return c >= 0 && c <= 127;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void addToken(TokenType type) {
        addToken(type, source.substring(start, current));
    }

    private void addToken(TokenType type, String text) {
        tokens.add(new Token(type, text, line));
    }

    public List<Token> getTokens() {
        return tokens;
    }
}