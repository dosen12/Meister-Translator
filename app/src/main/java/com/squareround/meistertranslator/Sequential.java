package com.squareround.meistertranslator;

import su.levenetc.android.textsurface.animations.AnimationsSet;
import su.levenetc.android.textsurface.contants.TYPE;
import su.levenetc.android.textsurface.interfaces.ISurfaceAnimation;

public class Sequential extends AnimationsSet {
    public Sequential(ISurfaceAnimation... animations) {
        super(TYPE.SEQUENTIAL, animations);
    }
}
