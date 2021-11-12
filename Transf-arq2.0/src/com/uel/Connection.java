package com.uel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Connection implements Runnable {

  private final Socket socket;
  private static final String arquivoEnviar = "C:\\Users\\Usuário\\Desktop\\teste.txt";
  public static final String arquivoReceber = "C:\\Users\\Usuário\\Desktop\\teste-recebido.txt";

  private static final Scanner input = new Scanner(System.in);

  public Connection(Socket socket) {
    this.socket = socket;
  }

  public static void main(String[] args) throws IOException {
    int op;
    do {
      System.out.println(
          "O que você deseja fazer? \n1 - Enviar um arquivo\n2 - Receber um arquivo");
      op = input.nextInt();
    } while (op != 1 && op != 2);

    Socket socket = null;
    System.out.println("Qual a porta da aplicação? ");
    int porta = input.nextInt();
    ServerSocket serverSocket = new ServerSocket(porta);

    if (op == 1) {
      System.out.println("Aguardando conexão com outro usuário...");

      while (true) { // enviar arquivo para várias conexões
        socket = serverSocket.accept(); // aguarda a conexão com outro usuário

        Connection c = new Connection(socket);
        Thread t = new Thread(c);
        t.start();
      }
      //      serverSocket.close();

    } else {
      System.out.println("Informe o ip do usuário que enviará o arquivo");
      String ip = input.next();
      Socket s = new Socket(ip, porta);

      Connection.receberArquivo(s);
    }
  }

  private static int receberPacote(List<Package> pacotes, int qtdBytesPorPacote, InputStream is,
      BufferedOutputStream bos)
      throws IOException {

    byte[] bytearray = new byte[qtdBytesPorPacote];
    int bytesParaEscrita;
    int numPacote = is.read();
    int bytesRead = is.read(bytearray, 0, qtdBytesPorPacote);
    pacotes.add(new Package(numPacote, bytearray));

    if (bytesRead == -1) {
      return -1;
    }
    if (bytesRead < qtdBytesPorPacote) {
      bytesParaEscrita = qtdBytesPorPacote - bytesRead;

    } else {
      bytesParaEscrita = bytesRead;
    }
    bos.write(bytearray, 0, bytesParaEscrita);
    bos.flush();
    System.out.println(
        "Pacote " + numPacote + " do arquivo " + arquivoReceber + " baixada (" + bytesParaEscrita
            + " bytes lidos)");

    return 1;
  }

  private static void receberArquivo(Socket socket) throws IOException {
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;

    try {
      System.out.println("Conectando...");

      InputStream is = socket.getInputStream();
      fos = new FileOutputStream(arquivoReceber);
      bos = new BufferedOutputStream(fos);
      int qtdBytesPorPacote = 5;
      int tamArqEnviado = is.read();

      List<Package> pacotes = new ArrayList<>(); //pacotes recebidos
      long tempoInicial = new java.util.Date().getTime();

      while (true) {
        if (receberPacote(pacotes, qtdBytesPorPacote, is, bos) == -1) {
          break;
        }
      }

      long tempoExecucao = (new java.util.Date().getTime() - tempoInicial)/1000; //te devolverá o tempo em segundos
      double velocidade = tamArqEnviado/tempoExecucao;
//
//      System.out.println("Arquivo enviado (" + tamArqEnviado + "bytes).");
//      System.out.println("Pacotes enviados:" + nPacotes + "pacotes");
      System.out.println("Velocidade de envio: " + velocidade + "bit/s");

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (fos != null) {
        fos.close();
      }
      if (bos != null) {
        bos.close();
      }
    }
  }

  private static int enviarPacote(int numPacote,
      BufferedInputStream bis, int qtdBytesPorPacote, int tamArqEnviado, Socket socket)
      throws IOException {
    byte[] mybytearray = new byte[qtdBytesPorPacote];

    bis.read(mybytearray, 0, mybytearray.length);
    OutputStream os = socket.getOutputStream();
    System.out.println(
        "Enviando pacote " + numPacote + "do arquivo " + arquivoEnviar + "(" + mybytearray.length
            + " bytes)");
    os.write(numPacote);
    os.write(mybytearray, 0, mybytearray.length);
    os.flush();

    tamArqEnviado += qtdBytesPorPacote;
    return tamArqEnviado;
  }

  @Override
  public void run() {
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    System.out.println("Conexão com: " + socket);
    
    int op, i=0, pacotes=0;

    System.out.println("Informe o temanho do arquivo:/n1-500 bytes/n2-1000 bytes/n3-1500bytes");
    op = input.nextInt();
    
    switch (op)
    {
    case 1:
    	pacotes=100;
    	break;
    case 2:
    	pacotes=200;
    	break;
    case 3:
    	pacotes=300;
    	break;
    }
    
    int tamArqEnviado = 0; // em bytes
    int qtdBytesPorPacote = 5;
    File myFile = new File(arquivoEnviar);
    int tamTotalArq = (int) myFile.length();
    int numPacote = 1;
    
    
    
   

    try {
      fis = new FileInputStream(myFile);
      bis = new BufferedInputStream(fis);
      
      OutputStream os = socket.getOutputStream();
      
      os.write(tamTotalArq);

      for (i=0;i<pacotes;i++) {
        tamArqEnviado = enviarPacote(numPacote, bis, qtdBytesPorPacote, tamArqEnviado, socket);
        numPacote++;
      }

      System.out.println("Arquivo enviado (" + tamArqEnviado + "bytes).");

    } catch (IOException e) {
      e.printStackTrace();

    } finally {
      try {
        if (fis != null) {
          fis.close();
        }
        if (bis != null) {
          bis.close();
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}