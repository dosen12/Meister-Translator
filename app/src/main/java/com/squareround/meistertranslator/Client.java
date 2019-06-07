package com.squareround.meistertranslator;

public interface Client< Type > {

    Type getResult();

    boolean getSuccess();

    void clientExecute();

}
