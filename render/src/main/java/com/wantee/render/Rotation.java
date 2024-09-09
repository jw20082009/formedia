package com.wantee.render;

public enum Rotation {
    Rotation0(0), Rotation90(90), Rotation180(180), Rotation270(270);

    private int degree = 0;

    public int getDegree() { return degree; }

    Rotation(int degree) {
        this.degree = degree;
    }
}
