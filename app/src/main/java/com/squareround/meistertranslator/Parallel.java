package com.squareround.meistertranslator;

import su.levenetc.android.textsurface.animations.AnimationsSet;
import su.levenetc.android.textsurface.contants.TYPE;
import su.levenetc.android.textsurface.interfaces.ISurfaceAnimation;

public class Parallel extends AnimationsSet {
    public Parallel(ISurfaceAnimation... animations) {
        super(TYPE.PARALLEL, animations);
    }
}