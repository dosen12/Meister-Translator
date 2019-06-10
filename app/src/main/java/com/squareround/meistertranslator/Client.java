package com.squareround.meistertranslator;

import java.util.ArrayList;

public interface Client< Type > {

    Type getResult();

    boolean getSuccess();

    ArrayList< Boolean > getProgress();

    void clientExecute();

}
