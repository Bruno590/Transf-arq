package com.uel;

public class Package {

  private Integer numPacote;
  private byte[] pacote;

  public Package(Integer numPacote, byte[] pacote) {
    this.numPacote = numPacote;
    this.pacote = pacote;
  }

  public Integer getNumPacote() {
    return numPacote;
  }

  public void setNumPacote(Integer numPacote) {
    this.numPacote = numPacote;
  }

  public byte[] getPacote() {
    return pacote;
  }

  public void setPacote(byte[] pacote) {
    this.pacote = pacote;
  }
}