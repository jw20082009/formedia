package com.wantee.render.blender;

import java.util.LinkedList;

public class Room {
    LinkedList<Seat> mSeatList = new LinkedList<>();
    Seat mBackgroundSeat;

    public void setBackground(Seat background) {
        mBackgroundSeat = background;
    }

    void draw(){}
}
