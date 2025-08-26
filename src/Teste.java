import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import lexical.Lexer;
import lexical.Token;
import sintatic.*;

public class Teste {
    public static void main(String[] args) {
        // Diretórios conforme nova estrutura
        Path pastaEntrada = Path.of("entradas");

        for (int i = 1; i <= 6; i++) {
            try {
                // Caminho do arquivo de entrada
                Path caminhoEntrada = pastaEntrada.resolve("exemplo" + i + ".txt");

                // Lê o conteúdo do arquivo
                String codigoFonte = Files.readString(caminhoEntrada);

                System.out.println("Execução do exemplo " + i + ":");

                // Executa o analisador léxico
                Lexer lexer = new Lexer(codigoFonte);
                List<Token> tokens = lexer.getTokens();

                Parser parser = new Parser(tokens);
                parser.parseProgram();

                System.out.println("\nExecução com sucesso do exemplo "+i+"\n");

            } catch (Exception e) {
                System.err.println("Erro ao processar exemplo" + i + ".txt: " + e.getMessage() + "\n");
            }
        }
    }
    
}