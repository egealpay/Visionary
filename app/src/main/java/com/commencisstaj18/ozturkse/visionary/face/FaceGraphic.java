// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.commencisstaj18.ozturkse.visionary.face;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.commencisstaj18.ozturkse.visionary.GraphicOverlay;
import com.commencisstaj18.ozturkse.visionary.ui.main.MainActivity;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float ID_TEXT_SIZE = 64.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = 50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private int facing;

    private final Paint facePositionPaint;
    private final Paint idPaint;
    private final Paint boxPaint;

    private volatile FirebaseVisionFace firebaseVisionFace;

    public FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        final int selectedColor = Color.GREEN;

        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        idPaint = new Paint();
        idPaint.setColor(selectedColor);
        idPaint.setTextSize(ID_TEXT_SIZE);

        boxPaint = new Paint();
        boxPaint.setColor(selectedColor);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    /**
     * Updates the face instance from the detection of the most recent frame. Invalidates the relevant
     * portions of the overlay to trigger a redraw.
     */
    public void updateFace(FirebaseVisionFace face, int facing) {
        firebaseVisionFace = face;
        this.facing = facing;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        FirebaseVisionFace face = firebaseVisionFace;
        if (face == null) {
            return;
        }

        MainActivity.Companion.recognizeFace();

        float x = translateX(face.getBoundingBox().centerX());
        float y = translateY(face.getBoundingBox().centerY());

        canvas.drawText(
                MainActivity.Companion.getResponseName(),
                x,
                y - ID_Y_OFFSET * 5,
                idPaint
        );

        int smilingProbability = Math.round(face.getSmilingProbability() * 100);

        if (smilingProbability > 0) {
            canvas.drawText(
                    "happy: %" + smilingProbability,
                    x,
                    y + ID_Y_OFFSET * 5,
                    idPaint
            );
        }


        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getBoundingBox().width() / 2.0f);
        float yOffset = scaleY(face.getBoundingBox().height() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, boxPaint);
    }

}
