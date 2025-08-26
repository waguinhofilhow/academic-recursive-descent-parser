import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import lexical.Lexer;
import lexical.Token;
import sintatic.*;

public class Teste {
    public static void main(String[] args) {
        Path pastaEntrada = Path.of("input");

        try {
            // Caminho do arquivo de entrada
            Path caminhoEntrada = pastaEntrada.resolve("input.txt");

            // Lê o conteúdo do arquivo
            String codigoFonte = Files.readString(caminhoEntrada);


            // Executa o analisador léxico
            Lexer lexer = new Lexer(codigoFonte);
            List<Token> tokens = lexer.getTokens();

            Parser parser = new Parser(tokens);
            parser.parseProgram();

        } catch (Exception e) {
            System.err.println("Erro ao processar input:" + e.getMessage() + "\n");
        }
    }
    
}