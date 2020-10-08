import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class App extends Application {
    public enum Estados {
        ABERTURA, NOMES, JOGO
    }

    private static final int NROLINHAS = 5;
    private ColorPinLine senhaSecreta;
    private List<LinhaJogo> linhasDeJogo;
    private TextField tfNome;

    private Scene sceneJogada;
    private Scene sceneAbertura;
    private Scene sceneNomes;
    private Stage primaryStage;

    private Estados estado;
    private int jogadaAndamento;

    private Scene montaCenaJogada(){
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // Define uma senha para a senha secreta
        senhaSecreta = new ColorPinLine();
        List<Cor> senha = Arrays.asList(Cor.AMARELO, Cor.ROSA, Cor.VERDE, Cor.CINZA, Cor.VERMELHO, Cor.LARANJA);
        for (int i = 0; i < ColorPinLine.QTDADE; i++) {
            senhaSecreta.setCor(senha.get(i), i);
        }
        senhaSecreta.block();

        linhasDeJogo = new ArrayList<>();
        for(int i=0;i<NROLINHAS;i++){
            LinhaJogo l = LinhaJogo.criaLinhaJogo();
            linhasDeJogo.add(l);
        }

        Button but = new Button("Fecha jogada");
        but.setOnAction(e -> verificaSenha(e));
        Button btVolta = new Button("Volta tela inicial");
        btVolta.setOnAction(e -> trocaTela());

        grid.add(senhaSecreta, 0, 1);
        int nl = 2;
        for(LinhaJogo lin:linhasDeJogo){
            grid.add(lin,0,nl);
            nl++;
        }
        grid.add(but, 0, nl);
        grid.add(btVolta, 1, nl);

        jogadaAndamento = 0;
        linhasDeJogo.get(jogadaAndamento).getTentativa().unblock();
        return new Scene(grid);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        estado = Estados.ABERTURA;

        // Monta a cena da jogada
        sceneJogada = montaCenaJogada();

        // Monta a cena da abertura
        GridPane gridA = new GridPane();
        gridA.setAlignment(Pos.CENTER);
        gridA.setHgap(20);
        gridA.setVgap(10);
        gridA.setPadding(new Insets(25, 25, 25, 25));
        gridA.add(new Label("Tela de abertura"), 0, 1);
        Button b = new Button("Ok");
        b.setOnAction(e -> trocaTela());
        gridA.add(b, 0, 2);
        sceneAbertura = new Scene(gridA,500,200);

       // Monta a cena de entrada de nomes
       GridPane gridN = new GridPane();
       gridN.setAlignment(Pos.CENTER);
       gridN.setHgap(20);
       gridN.setVgap(10);
       gridN.setPadding(new Insets(25, 25, 25, 25));
       gridN.add(new Label("Nome: "), 0, 1);
       tfNome = new TextField();
       gridN.add(tfNome,1,1);
       Button bt = new Button("Ok");
       bt.setOnAction(e -> trocaTela());
       gridN.add(bt, 2, 1);
       sceneNomes = new Scene(gridN);

        // Exibe a cena da abertura no palco
        primaryStage.setTitle("Inicio do jogo");
        primaryStage.setScene(sceneAbertura);
        primaryStage.show();
    }

    void coletaNome(){
        System.out.println("Nome do jogador: "+tfNome.getText());
    }

    public void trocaTela() {
        Scene s = null;
        String titulo = "";
        switch (estado) {
            case ABERTURA:
                s = sceneNomes;
                titulo = "Tela dos Nomes";
                estado = Estados.NOMES;
                break;
            case JOGO:
                s = sceneAbertura;
                titulo = "Tela de abertura";
                estado = Estados.ABERTURA;
                break;
            case NOMES: 
                s = sceneJogada;
                titulo = "Tela de Jogo";
                estado = Estados.JOGO;
                coletaNome();
                break;
            default:
                break;
        }
        primaryStage.setTitle(titulo);
        primaryStage.setScene(s);
        primaryStage.show();
    }

    public void verificaSenha(ActionEvent event) {
        ColorPinLine tentativa = linhasDeJogo.get(jogadaAndamento).getTentativa();
        BWPinLine pistas = linhasDeJogo.get(jogadaAndamento).getPista();
        int corretos = 0;
        int foraPos = 0;
        // Determina quantos pinos corretos e quantos fora de posição
        for (int i = 0; i < senhaSecreta.getChildren().size(); i++) {
            Pino pOrig = (Pino) senhaSecreta.getChildren().get(i);
            Pino pTent = (Pino) tentativa.getChildren().get(i);
            if (pOrig.getCor().equals(pTent.getCor())) {
                corretos++;
            } else {
                if (tentativa.getChildren().stream().map(p -> ((Pino) p).getCor()).filter(c -> c.equals(pOrig.getCor()))
                        .count() > 0) {
                    foraPos++;
                }
            }
        }
        // Acerta o registrador de pistas conforme o numero de corretos e fora de
        // posição
        //System.out.println(corretos + ", " + foraPos);
        int pos = 0;
        // Liga um pino preto para cada pino correto
        while (corretos > 0) {
            pistas.setPinPreto(pos);
            //System.out.println("Preto");
            pos++;
            corretos--;
        }
        // Liga um pino branco para cada pino fora de posição
        while (foraPos > 0) {
            pistas.setPinBranco(pos);
            //System.out.println("Branco");
            pos++;
            foraPos--;
        }
        // Desabilita os pinos restantes
        while (pos < pistas.getChildren().size()) {
            pistas.setEmpty(pos);
            pos++;
        }
        // Se ganhou exibe dialogo correspondete

        // Senao atualiza o numero da tentativa e bloqueia a anterior
        tentativa.block();
        jogadaAndamento++;
        linhasDeJogo.get(jogadaAndamento).getTentativa().unblock();
        // Verifica se não acabou o jogo ...
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}