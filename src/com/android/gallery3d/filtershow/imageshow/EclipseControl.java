/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.filtershow.imageshow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import com.android.gallery3d.R;

public class EclipseControl {
    private float mCenterX = Float.NaN;
    private float mCenterY = 0;
    private float mRadiusX = 200;
    private float mRadiusY = 300;
    private static int MIN_TOUCH_DIST = 80;// should be a resource & in dips

    private float[] handlex = new float[9];
    private float[] handley = new float[9];
    private int mSliderColor;
    private int mCenterDotSize = 40;
    private float mDownX;
    private float mDownY;
    private float mDownCenterX;
    private float mDownCenterY;
    private float mDownRadiusX;
    private float mDownRadiusY;
    private Matrix mScrToImg;

    private final static int HAN_CENTER = 0;
    private final static int HAN_NORTH = 7;
    private final static int HAN_NE = 8;
    private final static int HAN_EAST = 1;
    private final static int HAN_SE = 2;
    private final static int HAN_SOUTH = 3;
    private final static int HAN_SW = 4;
    private final static int HAN_WEST = 5;
    private final static int HAN_NW = 6;

    public EclipseControl(Context context) {
        mSliderColor = context.getResources().getColor(R.color.slider_line_color);
    }

    public void setRadius(float x, float y) {
        mRadiusX = x;
        mRadiusY = y;
    }

    public void setCenter(float x, float y) {
        mCenterX = x;
        mCenterY = y;
    }

    public int getCloseHandle(float x, float y) {
        float min = Float.MAX_VALUE;
        int handle = -1;
        for (int i = 0; i < handlex.length; i++) {
            float dx = handlex[i] - x;
            float dy = handley[i] - y;
            float dist = dx * dx + dy * dy;
            if (dist < min) {
                min = dist;
                handle = i;
            }
        }

        if (min < MIN_TOUCH_DIST * MIN_TOUCH_DIST) {
            return handle;
        }
        for (int i = 0; i < handlex.length; i++) {
            float dx = handlex[i] - x;
            float dy = handley[i] - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
        }

        return -1;
    }

    public void setScrToImageMatrix(Matrix scrToImg) {
        mScrToImg = scrToImg;
    }

    public void actionDown(float x, float y, Oval oval) {
        float[] point = new float[] {
                x, y };
        mScrToImg.mapPoints(point);
        mDownX = point[0];
        mDownY = point[1];
        mDownCenterX = oval.getCenterX();
        mDownCenterY = oval.getCenterY();
        mDownRadiusX = oval.getRadiusX();
        mDownRadiusY = oval.getRadiusY();
    }

    public void actionMove(int handle, float x, float y, Oval oval) {
        float[] point = new float[] {
                x, y };
        mScrToImg.mapPoints(point);
        x = point[0];
        y = point[1];
        int sign = 1;
        switch (handle) {
            case HAN_CENTER:
                float ctrdx = mDownX - mDownCenterX;
                float ctrdy = mDownY - mDownCenterY;
                oval.setCenter(x - ctrdx, y - ctrdy);
                // setRepresentation(mVignetteRep);
                break;
            case HAN_NORTH:
                sign = -1;
            case HAN_SOUTH:
                float raddy = mDownRadiusY - Math.abs(mDownY - mDownCenterY);
                oval.setRadiusY(Math.abs(y - oval.getCenterY() + sign * raddy));
                break;
            case HAN_EAST:
                sign = -1;
            case HAN_WEST:
                float raddx = mDownRadiusX - Math.abs(mDownX - mDownCenterX);
                oval.setRadiusX(Math.abs(x - oval.getCenterX() - sign * raddx));
                break;
            case HAN_SE:
            case HAN_NE:
            case HAN_SW:
            case HAN_NW:
                float sin45 = (float) Math.sin(45);
                float dr = (mDownRadiusX + mDownRadiusY) * sin45;
                float ctr_dx = mDownX - mDownCenterX;
                float ctr_dy = mDownY - mDownCenterY;
                float downRad = Math.abs(ctr_dx) + Math.abs(ctr_dy) - dr;
                float rx = oval.getRadiusX();
                float ry = oval.getRadiusY();
                float r = (Math.abs(rx) + Math.abs(ry)) * sin45;
                float dx = x - oval.getCenterX();
                float dy = y - oval.getCenterY();
                float nr = Math.abs(Math.abs(dx) + Math.abs(dy) - downRad);
                oval.setRadius(rx * nr / r, ry * nr / r);

                break;
        }
    }

    void paintPoint(Canvas canvas, float x, float y) {
        if (x == Float.NaN) {
            return;
        }

        Paint paint = new Paint();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLUE);
        int[] colors3 = new int[] {
                mSliderColor, mSliderColor, 0x66000000, 0 };
        RadialGradient g = new RadialGradient(x, y, mCenterDotSize, colors3, new float[] {
                0, .3f, .31f, 1 }, Shader.TileMode.CLAMP);
        paint.setShader(g);
        canvas.drawCircle(x, y, mCenterDotSize, paint);
    }

    void paintRadius(Canvas canvas, float cx, float cy, float rx, float ry) {
        if (cx == Float.NaN) {
            return;
        }
        int mSliderColor = 0xFF33B5E5;
        Paint paint = new Paint();
        RectF rect = new RectF(cx - rx, cy - ry, cx + rx, cy + ry);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);
        paint.setColor(Color.BLACK);
        paintOvallines(canvas, rect, paint, cx, cy, rx, ry);

        paint.setStrokeWidth(3);
        paint.setColor(Color.WHITE);
        paintOvallines(canvas, rect, paint, cx, cy, rx, ry);
    }

    public void paintOvallines(
            Canvas canvas, RectF rect, Paint paint, float cx, float cy, float rx, float ry) {
        canvas.drawOval(rect, paint);
        float da = 4;
        float arclen = da + da;
        for (int i = 0; i < 361; i += 90) {
            float dx = rx + 10;
            float dy = ry + 10;
            rect.left = cx - dx;
            rect.top = cy - dy;
            rect.right = cx + dx;
            rect.bottom = cy + dy;
            canvas.drawArc(rect, i - da, arclen, false, paint);
            dx = rx - 10;
            dy = ry - 10;
            rect.left = cx - dx;
            rect.top = cy - dy;
            rect.right = cx + dx;
            rect.bottom = cy + dy;
            canvas.drawArc(rect, i - da, arclen, false, paint);
        }
        da *= 2;
        for (int i = 45; i < 361; i += 90) {
            double angle = Math.PI * i / 180.;
            float x = cx + (float) (rx * Math.cos(angle));
            float y = cy + (float) (ry * Math.sin(angle));
            canvas.drawRect(x - da, y - da, x + da, y + da, paint);
        }
        rect.left = cx - rx;
        rect.top = cy - ry;
        rect.right = cx + rx;
        rect.bottom = cy + ry;
    }

    public void fillHandles(Canvas canvas, float cx, float cy, float rx, float ry) {
        handlex[0] = cx;
        handley[0] = cy;
        int k = 1;

        for (int i = 0; i < 360; i += 45) {
            double angle = Math.PI * i / 180.;

            float x = cx + (float) (rx * Math.cos(angle));
            float y = cy + (float) (ry * Math.sin(angle));
            handlex[k] = x;
            handley[k] = y;

            k++;
        }
    }

    public void draw(Canvas canvas) {
        paintRadius(canvas, mCenterX, mCenterY, mRadiusX, mRadiusY);
        fillHandles(canvas, mCenterX, mCenterY, mRadiusX, mRadiusY);
        paintPoint(canvas, mCenterX, mCenterY);
    }
}
