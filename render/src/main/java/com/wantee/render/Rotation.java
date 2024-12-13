package com.wantee.render;

public enum Rotation {
    Rotation0(0), Rotation90(90), Rotation180(180), Rotation270(270);

    private int degree = 0;

    public int getDegree() { return degree; }

    public static Rotation getRotation(int degree) {
        Rotation[] rotations = Rotation.values();
        for(Rotation r: rotations) {
            if (r.degree == degree) {
                return r;
            }
        }
        return Rotation0;
    }

    Rotation(int degree) {
        this.degree = degree;
    }
}
