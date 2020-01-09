package com.adam.pong.game;

import javafx.scene.canvas.Canvas;

public class ResizableCanvas extends Canvas {

    @Override
    public boolean isResizable() {
        return true;
    }

}

// https://stackoverflow.com/questions/24533556/how-to-make-canvas-resizable-in-javafx