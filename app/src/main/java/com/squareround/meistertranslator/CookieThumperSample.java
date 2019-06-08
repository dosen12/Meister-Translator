package com.squareround.meistertranslator;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import su.levenetc.android.textsurface.contants.Align;
import su.levenetc.android.textsurface.Text;
import su.levenetc.android.textsurface.TextBuilder;
import su.levenetc.android.textsurface.TextSurface;
import su.levenetc.android.textsurface.animations.Alpha;
import su.levenetc.android.textsurface.animations.ChangeColor;
import su.levenetc.android.textsurface.animations.Circle;
import su.levenetc.android.textsurface.animations.Delay;
import su.levenetc.android.textsurface.animations.Parallel;
import su.levenetc.android.textsurface.animations.Rotate3D;
import su.levenetc.android.textsurface.animations.Sequential;
import su.levenetc.android.textsurface.animations.ShapeReveal;
import su.levenetc.android.textsurface.animations.SideCut;
import su.levenetc.android.textsurface.animations.Slide;
import su.levenetc.android.textsurface.animations.TransSurface;
import su.levenetc.android.textsurface.contants.Direction;
import su.levenetc.android.textsurface.contants.Pivot;
import su.levenetc.android.textsurface.contants.Side;

public class CookieThumperSample {

    public static void play(TextSurface textSurface, AssetManager assetManager) {

        final Typeface robotoBlack = Typeface.createFromAsset(assetManager, "fonts/Roboto-Black.ttf");
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTypeface(robotoBlack);

        Text textDaai = TextBuilder
                .create("마이스터 번역기")
                .setPaint(paint)
                .setSize(60)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.SURFACE_CENTER).build();

        Text textBraAnies = TextBuilder
                .create("간편한")
                .setPaint(paint)
                .setSize(44)
                .setAlpha(0)
                .setColor(Color.RED)
                .setPosition(Align.BOTTOM_OF, textDaai).build();

        Text textFokkenGamBra = TextBuilder
                .create(" 조작법")
                .setPaint(paint)
                .setSize(44)
                .setAlpha(0)
                .setColor(Color.RED)
                .setPosition(Align.RIGHT_OF, textBraAnies).build();

        Text textHaai = TextBuilder
                .create(" 버튼만")
                .setPaint(paint)
                .setSize(44)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.BOTTOM_OF, textFokkenGamBra).build();

        Text textDaaiAnies = TextBuilder
                .create(" 누르면")
                .setPaint(paint)
                .setSize(44)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, textHaai).build();

        Text texThyLamInnie = TextBuilder
                .create(" 번역 결과가")
                .setPaint(paint)
                .setSize(44)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.RIGHT_OF, textDaaiAnies).build();

        Text textThrowDamn = TextBuilder
                .create(" 나옵니다")
                .setPaint(paint)
                .setSize(44)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, texThyLamInnie).build();

        Text textDevilishGang = TextBuilder
                .create("지금 바로")
                .setPaint(paint)
                .setSize(64)
                .setAlpha(0)
                .setColor(Color.RED)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, textThrowDamn).build();

        Text textSignsInTheAir = TextBuilder
                .create("체험해보세요")
                .setPaint(paint)
                .setSize(64)
                .setAlpha(0)
                .setColor(Color.RED)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, textDevilishGang).build();

        Text LastWord = TextBuilder
                .create(" 마이스터 번역기 ")
                .setPaint(paint)
                .setSize(60)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, textDevilishGang).build();

        textSurface.play(
                new Sequential(
                        ShapeReveal.create(textDaai, 750, SideCut.show(Side.LEFT), false),
                        new Parallel(ShapeReveal.create(textDaai, 600, SideCut.hide(Side.LEFT), false), new Sequential(Delay.duration(300), ShapeReveal.create(textDaai, 600, SideCut.show(Side.LEFT), false))),
                        new Parallel(new TransSurface(500, textBraAnies, Pivot.CENTER), ShapeReveal.create(textBraAnies, 1300, SideCut.show(Side.LEFT), false)),
                        Delay.duration(500),
                        new Parallel(new TransSurface(750, textFokkenGamBra, Pivot.CENTER), Slide.showFrom(Side.LEFT, textFokkenGamBra, 750), ChangeColor.to(textFokkenGamBra, 750, Color.RED)),
                        Delay.duration(500),
                        new Parallel(TransSurface.toCenter(textHaai, 500), Rotate3D.showFromSide(textHaai, 750, Pivot.TOP)),
                        new Parallel(TransSurface.toCenter(textDaaiAnies, 500), Slide.showFrom(Side.TOP, textDaaiAnies, 500)),
                        new Parallel(TransSurface.toCenter(texThyLamInnie, 750), Slide.showFrom(Side.LEFT, texThyLamInnie, 500)),
                        Delay.duration(500),
                        new Parallel(
                                new TransSurface(3000, textSignsInTheAir, Pivot.CENTER),
                                new Sequential(
                                        new Sequential(ShapeReveal.create(textThrowDamn, 800, Circle.show(Side.CENTER, Direction.OUT), false)),
                                        new Sequential(ShapeReveal.create(textDevilishGang, 800, Circle.show(Side.CENTER, Direction.OUT), false)),
                                        new Sequential(ShapeReveal.create(textSignsInTheAir, 800, Circle.show(Side.LEFT, Direction.OUT), false))
                                )
                        ),
                        Delay.duration(200),
                        new Parallel(
                                ShapeReveal.create(textThrowDamn, 1500, SideCut.hide(Side.LEFT), true),
                                new Sequential(Delay.duration(500), ShapeReveal.create(textDevilishGang, 1500, SideCut.hide(Side.LEFT), true)),
                                new Sequential(Delay.duration(500), ShapeReveal.create(textSignsInTheAir, 1500, SideCut.hide(Side.LEFT), true)),
                                Alpha.hide(texThyLamInnie, 1500),
                                Alpha.hide(textDaaiAnies, 1500),
                                Alpha.hide(textHaai, 1500),
                                Alpha.hide(textDaai, 1500),
                                Alpha.hide(textFokkenGamBra, 1500),
                                new Sequential(new Sequential(Delay.duration(500),ShapeReveal.create(LastWord, 1500, SideCut.show(Side.LEFT), false))
                        )
                        )
                )
        );
    }
}